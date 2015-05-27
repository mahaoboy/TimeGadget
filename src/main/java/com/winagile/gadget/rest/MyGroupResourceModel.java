package com.winagile.gadget.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "issuetype")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyGroupResourceModel {
	@XmlElement(name = "value")
	private String issuetype;

	@XmlElement(name = "label")
	private String label;

	public MyGroupResourceModel() {
	}

	public MyGroupResourceModel(String issuetype, String label) {
		this.issuetype = issuetype;
		this.label = label;
	}

	public String getIssuetype() {
		return issuetype;
	}

	public void setIssuetype(String issuetype) {
		this.issuetype = issuetype;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
