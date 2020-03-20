package my.demo.service;

import java.io.Serializable;

public class ServiceResult<T extends Serializable> implements Serializable {
	private static final long serialVersionUID = -8163829609385722989L;
	private T result = null;
	private boolean success = true;
	private String message = "";
	
	public ServiceResult() {
		
	}
	public ServiceResult(T result) {
		this.result = result;
		this.success = true;
	}
	public ServiceResult<T> fail(String message) {
		this.success = false;
		this.message = message;
		return this;
	}
	public ServiceResult<T> success(T result) {
		this.success = true;
		this.result = result;
		return this;
	}
	
	public T getResult() {
		return result;
	}
	public void setResult(T result) {
		this.result = result;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}