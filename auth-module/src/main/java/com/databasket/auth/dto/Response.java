package com.databasket.auth.dto;


public class Response<T>{
	
	private int status;
    private String message;
    private String error;
    private T body;
 
	public Response(int value, String message, T body) {
		this.body=body;
		this.status = value;
		this.message=message;
	}

	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public T getBody() {
		return body;
	}
	public void setBody(T body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "Response [status=" + status + ", " + (message != null ? "message=" + message + ", " : "")
				+ (error != null ? "error=" + error + ", " : "") + (body != null ? "body=" + body : "") + "]";
	}
	
}
