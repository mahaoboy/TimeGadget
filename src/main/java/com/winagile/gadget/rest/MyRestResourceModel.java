package com.winagile.gadget.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyRestResourceModel {

    @XmlElement(name = "value")
    private String message;
    
    @XmlElement(name = "label")
    private String label;

    public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public MyRestResourceModel() {
    }

    public MyRestResourceModel(String message, String label) {
        this.message = message;
        this.label = label;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}