package com.serverless;

import java.util.Map;

public class Response {

	private final String message;
	private final String input;

	public Response(String message, String input) {
		this.message = message;
		this.input = input;
	}

	public String getMessage() {
		return this.message;
	}

	public String getInput() {
		return this.input;
	}
}
