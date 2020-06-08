package org.seaboxdata.systemmng.util.data;

import java.io.Serializable;

public class JsonResp implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5468336530515560024L;
	private static final String DEFAULT_SUCCESS_MSG = "操作成功！";
	private static final String DEFAULT_ERROR_MSG = "操作失败！";
	private boolean status; // 状态
	private String code; // 响应代码，模块简称 + 编号，例如：SYS001
	private String message; // 消息内容
	private Object result; // 结果集，可以嵌套
	private Object extension; // 扩展信息，可以嵌套，但不建议，本节点是可以省略的。

	public JsonResp() {
		super();
	}

	public JsonResp(boolean status) {
		this.setStatus(status);
		if (status) {			
			this.setMessage(DEFAULT_SUCCESS_MSG);
		} else {
			this.setMessage(DEFAULT_ERROR_MSG);
		}
	}

	public JsonResp(String code, String message) {
		super();
		this.setStatus(false);
		this.code = code;
		this.message = message;
	}

	public JsonResp(Object result) {
		super();
		this.setStatus(true);
		this.result = result;
	}

	public JsonResp(Object result, Object extension) {
		super();
		this.setStatus(true);
		this.result = result;
		this.extension = extension;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object getExtension() {
		return extension;
	}

	public void setExtension(Object extension) {
		this.extension = extension;
	}

	public String toString() {
		return "{status:" + this.status + ", code:"+ this.code +", result:"+ this.result +", message:"+ this.message +"}";
	}
}
