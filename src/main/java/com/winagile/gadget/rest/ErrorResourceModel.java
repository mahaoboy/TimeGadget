package com.winagile.gadget.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "errormodel")
@XmlAccessorType(XmlAccessType.FIELD)
public class ErrorResourceModel {
	@XmlElement(name = "errorMessages")
	private ErrorMessagesList errorMessages;

	public ErrorResourceModel() {
	}

	public ErrorResourceModel(String errorxs) {
		this.errorMessages = new ErrorMessagesList(errorxs);
	}

	public ErrorMessagesList getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(ErrorMessagesList errorMessages) {
		this.errorMessages = errorMessages;
	}

}

@XmlRootElement
class ErrorMessagesList {
	@XmlElement
	private String error;

	public ErrorMessagesList() {
	}

	public ErrorMessagesList(String error) {
		this.error = error;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
}