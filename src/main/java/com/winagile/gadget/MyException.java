package com.winagile.gadget;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class MyException extends Throwable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger log = LogManager.getLogger(MyException.class);
	private String exType;

	public MyException() {
	}

	public MyException(String type, String paramString) {
		super(paramString);
		this.exType = type;
	}

	public String getExType() {
		return exType;
	}

	public MyException(String paramString) {
		super(paramString);
	}

	public MyException(String paramString, Throwable paramThrowable) {
		super(paramString, paramThrowable);
	}

	public MyException(Throwable paramThrowable) {
		super(paramThrowable);

		log.error(StaticParams.getPrintStack(paramThrowable));
	}

	protected MyException(String paramString, Throwable paramThrowable,
			boolean paramBoolean1, boolean paramBoolean2) {
		super(paramString, paramThrowable, paramBoolean1, paramBoolean2);
	}
}
