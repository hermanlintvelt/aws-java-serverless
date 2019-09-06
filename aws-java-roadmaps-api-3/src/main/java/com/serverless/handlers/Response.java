package com.serverless.handlers;

public class Response {

	private final String message;
	private final String error;

	public Response(String message, String input) {
		this.message = message;
		this.error = input;
	}

	public String getMessage() {
		return this.message;
	}

	public String getError() {
		return this.error;
	}
}
