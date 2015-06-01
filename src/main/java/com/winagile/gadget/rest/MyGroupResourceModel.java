package com.winagile.gadget.rest;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MyGroupResourceModel {
	@XmlElement(name = "group")
	private MyGroupMiddleResourceModel group;

	public MyGroupResourceModel() {
	}

	public MyGroupResourceModel(MyGroupMiddleResourceModel group) {
		this.group = group;
	}

}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class MyGroupMiddleResourceModel {
	@XmlElement(name = "options")
	private List<MyRestResourceModel> options;

	@XmlElement(name = "label")
	private String label;

	public MyGroupMiddleResourceModel() {
	}

	public MyGroupMiddleResourceModel(List<MyRestResourceModel> options,
			String label) {
		this.options = options;
		this.label = label;
	}

}
