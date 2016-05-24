package cn.qdsc.msp.ui.contacts;

import java.io.Serializable;

public class SortModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private String contactName;
	private String telephone;
	private String cellphone_1;
	private String cellphone_2;
	private String email_1;
	private String email_2;
	private String contactCompany;
	private String contactAddress;
	private String sortLetters;
	
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getTelephone() {
		return telephone;
	}
	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}
	public String getCellphone_1() {
		return cellphone_1;
	}
	public String getCellphone_2() {
		return cellphone_2;
	}
	public void setCellphone_1(String contactNumber) {
		this.cellphone_1 = contactNumber;
	}
	public void setCellphone_2(String contactNumber) {
		this.cellphone_2 = contactNumber;
	}
	public String getEmail_1() {
		return email_1;
	}
	public String getEmail_2() {
		return email_2;
	}
	public void setEmail_1(String contactEmail) {
		this.email_1 = contactEmail;
	}
	public void setEmail_2(String contactEmail) {
		this.email_2 = contactEmail;
	}
	public String getContactCompany() {
		return contactCompany;
	}
	public void setContactCompany(String contactCompany) {
		this.contactCompany = contactCompany;
	}
	
	public String getContactAddress() {
		return contactAddress;
	}
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}

	@Override
	public String toString() {
		return "SortModel{" +
				"contactName='" + contactName + '\'' +
				", telephone='" + telephone + '\'' +
				", cellphone_1='" + cellphone_1 + '\'' +
				", cellphone_2='" + cellphone_2 + '\'' +
				", email_1='" + email_1 + '\'' +
				", email_2='" + email_2 + '\'' +
				", contactCompany='" + contactCompany + '\'' +
				", contactAddress='" + contactAddress + '\'' +
				", sortLetters='" + sortLetters + '\'' +
				'}';
	}
}
