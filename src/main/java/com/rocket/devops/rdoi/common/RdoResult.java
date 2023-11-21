package com.rocket.devops.rdoi.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response format adapt to RDO
 */
public class RdoResult<T> {
	private Integer status;

	/**
	 * if it is successful response, then the returncode should be 0.
	 */
	private String returncode = "0";

	private String message = "";

	private Integer pageIndex;
	private Integer pageSize;
	private Long recordsTotal;

	@JsonInclude(JsonInclude.Include.ALWAYS)
	private T payload;

	public RdoResult() {
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getReturncode() {
		return returncode;
	}

	public void setReturncode(String returncode) {
		this.returncode = returncode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getPayload() {
		return payload;
	}

	public void setPayload(T payload) {
		this.payload = payload;
	}

	public Integer getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Long getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(Long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
}
