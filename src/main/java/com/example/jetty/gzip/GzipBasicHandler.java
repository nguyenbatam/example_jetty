package com.example.jetty.gzip;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHttpInputInterceptor;

public class GzipBasicHandler extends GzipHandler {
	private int _inflateBufferSize = -1;

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// Handle request inflation
		if (_inflateBufferSize > 0) {
			HttpField ce = baseRequest.getHttpFields().getField(HttpHeader.CONTENT_ENCODING);
			if (ce != null && "gzip".equalsIgnoreCase(ce.getValue())) {
				// TODO should check ce.contains and then remove just the gzip
				// encoding
				baseRequest.getHttpFields().remove(HttpHeader.CONTENT_ENCODING);
				baseRequest.getHttpFields().add(new HttpField("X-Content-Encoding", ce.getValue()));
				baseRequest.getHttpInput().addInterceptor(new GzipHttpInputInterceptor(
						baseRequest.getHttpChannel().getByteBufferPool(), _inflateBufferSize));

			}
		}

		HttpOutput out = baseRequest.getResponse().getHttpOutput();

		HttpOutput.Interceptor orig_interceptor = out.getInterceptor();
		try {
			// install interceptor and handle
			out.setInterceptor(new GzipBasicHttpOutputInterceptor(this, getVaryField(), baseRequest.getHttpChannel(),
					orig_interceptor, isSyncFlush()));
			if (_handler != null)
				_handler.handle(target, baseRequest, request, response);
		} finally {
			// reset interceptor if request not handled
			if (!baseRequest.isHandled() && !baseRequest.isAsyncStarted())
				out.setInterceptor(orig_interceptor);
		}
	}

}

/*
 * Location:
 * /home/tamnb/Desktop/xkeam-service-jetty-ctv-2.0.war!/WEB-INF/classes/com/ctv/
 * x_keam/jetty/JettyServiceSubscriber.class Java compiler version: 7 (51.0)
 * JD-Core Version: 0.7.1
 */