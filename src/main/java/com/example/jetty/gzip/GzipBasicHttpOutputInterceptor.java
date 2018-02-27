package com.example.jetty.gzip;

import static org.eclipse.jetty.http.CompressedContentFormat.GZIP;

import java.nio.ByteBuffer;
import java.nio.channels.WritePendingException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.gzip.GzipFactory;
import org.eclipse.jetty.server.handler.gzip.GzipHttpOutputInterceptor;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.IteratingNestedCallback;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class GzipBasicHttpOutputInterceptor implements HttpOutput.Interceptor {
	public static Logger LOG = Log.getLogger(GzipHttpOutputInterceptor.class);
	private final static byte[] GZIP_HEADER = new byte[] { (byte) 0x1f, (byte) 0x8b, Deflater.DEFLATED, 0, 0, 0, 0, 0,
			0, 0 };

	public final static HttpField VARY_ACCEPT_ENCODING_USER_AGENT = new PreEncodedHttpField(HttpHeader.VARY,
			HttpHeader.ACCEPT_ENCODING + ", " + HttpHeader.USER_AGENT);
	public final static HttpField VARY_ACCEPT_ENCODING = new PreEncodedHttpField(HttpHeader.VARY,
			HttpHeader.ACCEPT_ENCODING.asString());

	private enum GZState {
		MIGHT_COMPRESS, NOT_COMPRESSING, COMMITTING, COMPRESSING, FINISHED
	};

	private final AtomicReference<GZState> _state = new AtomicReference<>(GZState.MIGHT_COMPRESS);
	private final CRC32 _crc = new CRC32();

	private GzipFactory _factory;
	private HttpOutput.Interceptor _interceptor;
	private HttpChannel _channel;
	private HttpField _vary;
	private int _bufferSize;
	private boolean _syncFlush;

	private Deflater _deflater;
	private ByteBuffer _buffer;

	public GzipBasicHttpOutputInterceptor(GzipFactory factory, HttpChannel channel, HttpOutput.Interceptor next,
			boolean syncFlush) {
		this(factory, VARY_ACCEPT_ENCODING_USER_AGENT, channel.getHttpConfiguration().getOutputBufferSize(), channel,
				next, syncFlush);
	}

	public GzipBasicHttpOutputInterceptor(GzipFactory factory, HttpField vary, HttpChannel channel,
			HttpOutput.Interceptor next, boolean syncFlush) {
		this(factory, vary, channel.getHttpConfiguration().getOutputBufferSize(), channel, next, syncFlush);
	}

	public GzipBasicHttpOutputInterceptor(GzipFactory factory, HttpField vary, int bufferSize, HttpChannel channel,
			HttpOutput.Interceptor next, boolean syncFlush) {
		_factory = factory;
		_channel = channel;
		_interceptor = next;
		_vary = vary;
		_bufferSize = bufferSize;
		_syncFlush = syncFlush;
	}

	public HttpOutput.Interceptor getNextInterceptor() {
		return _interceptor;
	}

	@Override
	public boolean isOptimizedForDirectBuffers() {
		return false; // No point as deflator is in user space.
	}

	@Override
	public void write(ByteBuffer content, boolean complete, Callback callback) {
		switch (_state.get()) {
		case MIGHT_COMPRESS:
			commit(content, complete, callback);
			break;

		case NOT_COMPRESSING:
			_interceptor.write(content, complete, callback);
			return;

		case COMMITTING:
			callback.failed(new WritePendingException());
			break;

		case COMPRESSING:
			gzip(content, complete, callback);
			break;

		default:
			callback.failed(new IllegalStateException("state=" + _state.get()));
			break;
		}
	}

	private void addTrailer() {
		int i = _buffer.limit();
		_buffer.limit(i + 8);

		int v = (int) _crc.getValue();
		_buffer.put(i++, (byte) (v & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 8) & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 16) & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 24) & 0xFF));

		v = _deflater.getTotalIn();
		_buffer.put(i++, (byte) (v & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 8) & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 16) & 0xFF));
		_buffer.put(i++, (byte) ((v >>> 24) & 0xFF));
	}

	private void gzip(ByteBuffer content, boolean complete, final Callback callback) {
		if (content.hasRemaining() || complete)
			new GzipBufferCB(content, complete, callback).iterate();
		else
			callback.succeeded();
	}

	protected void commit(ByteBuffer content, boolean complete, Callback callback) {
		// Are we excluding because of status?
		Response response = _channel.getResponse();
		int sc = response.getStatus();
		if (sc > 0 && (sc < 200 || sc == 204 || sc == 205 || sc >= 300)) {
			LOG.debug("{} exclude by status {}", this, sc);
			noCompression();
			_interceptor.write(content, complete, callback);
			return;
		}



		// Has the Content-Encoding header already been set?
		HttpFields fields = response.getHttpFields();

		// Are we the thread that commits?
		if (_state.compareAndSet(GZState.MIGHT_COMPRESS, GZState.COMMITTING)) {
			// We are varying the response due to accept encoding header.
			if (_vary != null) {
				if (fields.contains(HttpHeader.VARY))
					fields.addCSV(HttpHeader.VARY, _vary.getValues());
				else
					fields.add(_vary);
			}

			long content_length = response.getContentLength();
			if (content_length < 0 && complete)
				content_length = content.remaining();

			_deflater = _factory.getDeflater(_channel.getRequest(), content_length);

			if (_deflater == null) {
//				LOG.debug("{} exclude no deflater", this);
				_state.set(GZState.NOT_COMPRESSING);
				_interceptor.write(content, complete, callback);
				return;
			}

			fields.put(GZIP._contentEncoding);
			_crc.reset();
			_buffer = _channel.getByteBufferPool().acquire(_bufferSize, false);
			BufferUtil.fill(_buffer, GZIP_HEADER, 0, GZIP_HEADER.length);

			// Adjust headers
			response.setContentLength(-1);
			String etag = fields.get(HttpHeader.ETAG);
			if (etag != null)
				fields.put(HttpHeader.ETAG, etagGzip(etag));

//			LOG.debug("{} compressing {}", this, _deflater);
			_state.set(GZState.COMPRESSING);

			gzip(content, complete, callback);
		} else
			callback.failed(new WritePendingException());
	}

	private String etagGzip(String etag) {
		int end = etag.length() - 1;
		return (etag.charAt(end) == '"') ? etag.substring(0, end) + GZIP._etag + '"' : etag + GZIP._etag;
	}

	public void noCompression() {
		while (true) {
			switch (_state.get()) {
			case NOT_COMPRESSING:
				return;

			case MIGHT_COMPRESS:
				if (_state.compareAndSet(GZState.MIGHT_COMPRESS, GZState.NOT_COMPRESSING))
					return;
				break;

			default:
				throw new IllegalStateException(_state.get().toString());
			}
		}
	}

	public void noCompressionIfPossible() {
		while (true) {
			switch (_state.get()) {
			case COMPRESSING:
			case NOT_COMPRESSING:
				return;

			case MIGHT_COMPRESS:
				if (_state.compareAndSet(GZState.MIGHT_COMPRESS, GZState.NOT_COMPRESSING))
					return;
				break;

			default:
				throw new IllegalStateException(_state.get().toString());
			}
		}
	}

	public boolean mightCompress() {
		return _state.get() == GZState.MIGHT_COMPRESS;
	}

	private class GzipBufferCB extends IteratingNestedCallback {
		private ByteBuffer _copy;
		private final ByteBuffer _content;
		private final boolean _last;

		public GzipBufferCB(ByteBuffer content, boolean complete, Callback callback) {
			super(callback);
			_content = content;
			_last = complete;
		}

		@Override
		protected Action process() throws Exception {
			if (_deflater == null)
				return Action.SUCCEEDED;

			if (_deflater.needsInput()) {
				if (BufferUtil.isEmpty(_content)) {
					if (_deflater.finished()) {
						_factory.recycle(_deflater);
						_deflater = null;
						_channel.getByteBufferPool().release(_buffer);
						_buffer = null;
						if (_copy != null) {
							_channel.getByteBufferPool().release(_copy);
							_copy = null;
						}
						return Action.SUCCEEDED;
					}

					if (!_last) {
						return Action.SUCCEEDED;
					}

					_deflater.finish();
				} else if (_content.hasArray()) {
					byte[] array = _content.array();
					int off = _content.arrayOffset() + _content.position();
					int len = _content.remaining();
					BufferUtil.clear(_content);

					_crc.update(array, off, len);
					_deflater.setInput(array, off, len);
					if (_last)
						_deflater.finish();
				} else {
					if (_copy == null)
						_copy = _channel.getByteBufferPool().acquire(_bufferSize, false);
					BufferUtil.clearToFill(_copy);
					int took = BufferUtil.put(_content, _copy);
					BufferUtil.flipToFlush(_copy, 0);
					if (took == 0)
						throw new IllegalStateException();

					byte[] array = _copy.array();
					int off = _copy.arrayOffset() + _copy.position();
					int len = _copy.remaining();

					_crc.update(array, off, len);
					_deflater.setInput(array, off, len);
					if (_last && BufferUtil.isEmpty(_content))
						_deflater.finish();
				}
			}

			BufferUtil.compact(_buffer);
			int off = _buffer.arrayOffset() + _buffer.limit();
			int len = _buffer.capacity() - _buffer.limit() - (_last ? 8 : 0);
			if (len > 0) {
				int produced = _deflater.deflate(_buffer.array(), off, len,
						_syncFlush ? Deflater.SYNC_FLUSH : Deflater.NO_FLUSH);
				_buffer.limit(_buffer.limit() + produced);
			}
			boolean finished = _deflater.finished();

			if (finished)
				addTrailer();

			_interceptor.write(_buffer, finished, this);
			return Action.SCHEDULED;
		}
	}
}

/*
 * Location:
 * /home/tamnb/Desktop/xkeam-service-jetty-ctv-2.0.war!/WEB-INF/classes/com/ctv/
 * x_keam/jetty/JettyServiceSubscriber.class Java compiler version: 7 (51.0)
 * JD-Core Version: 0.7.1
 */