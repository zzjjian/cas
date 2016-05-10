package com.ebao.cas.credential;

import org.jasig.cas.authentication.UsernamePasswordCredential;

public class JdbcUsernamePasswordCredential extends UsernamePasswordCredential{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5781809020936925818L;
	private String oldpassword;
	private String newpassword;
	private String confirmpassword;
	public JdbcUsernamePasswordCredential(){
		
	}
	public String getOldpassword() {
		return oldpassword;
	}
	public void setOldpassword(String oldpassword) {
		this.oldpassword = oldpassword;
	}
	public String getNewpassword() {
		return newpassword;
	}
	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}
	public String getConfirmpassword() {
		return confirmpassword;
	}
	public void setConfirmpassword(String confirmpassword) {
		this.confirmpassword = confirmpassword;
	}
	
	
}
