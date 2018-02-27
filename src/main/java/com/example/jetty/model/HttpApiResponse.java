package com.example.jetty.model;

/**
 * 
 * @author tamnb
 */
public class HttpApiResponse {
	public Object data; // object data return to client
	public String message; // return message if error when processing fail
	public int code; // server return code, ex : success is 200 , other is error

	public HttpApiResponse(int code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
}
