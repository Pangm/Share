package com.michelle.share;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Bitmap;

public class Contact implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 415056597730896540L;
	private String name = "";
	private ArrayList<String> phones = null;
	private ArrayList<String> mails = null;
	private Bitmap contactPhotoBitmap = null;
	
	public Contact() {
	}

	public Contact(String name, ArrayList<String> phones,
			ArrayList<String> mails, Bitmap contactPhotoBitmap) {
		super();
		this.name = name;
		this.phones = phones;
		this.mails = mails;
		this.contactPhotoBitmap = contactPhotoBitmap;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the phones
	 */
	public ArrayList<String> getPhones() {
		return phones;
	}

	/**
	 * @param phones the phones to set
	 */
	public void setPhones(ArrayList<String> phones) {
		this.phones = phones;
	}

	/**
	 * @return the mails
	 */
	public ArrayList<String> getMails() {
		return mails;
	}

	/**
	 * @param mails the mails to set
	 */
	public void setMails(ArrayList<String> mails) {
		this.mails = mails;
	}

	/**
	 * @return the contactPhotoBitmap
	 */
	public Bitmap getContactPhotoBitmap() {
		return contactPhotoBitmap;
	}

	/**
	 * @param contactPhotoBitmap the contactPhotoBitmap to set
	 */
	public void setContactPhotoBitmap(Bitmap contactPhotoBitmap) {
		this.contactPhotoBitmap = contactPhotoBitmap;
	}
	
	
}
