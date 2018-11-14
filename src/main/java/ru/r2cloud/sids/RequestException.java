package ru.r2cloud.sids;

public class RequestException extends Exception {

	private static final long serialVersionUID = 7729000779918884325L;

	private final int responseCode;
	private final String responseBody;

	public RequestException(String message, int responseCode, String responseBody) {
		super(message);
		this.responseBody = responseBody;
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public String getResponseBody() {
		return responseBody;
	}

}
