package com.ebao.cas.credential;
import java.io.Serializable;

public class UserPasswordForm implements Serializable {
	private static final long serialVersionUID = 2733158585147930907L;
	private String pwd1;
	private String pwd2;
	private String pwd3;
	private String pwd4;
	private String pwd5;
	public String getPwd1() {
		return pwd1;
	}
	public void setPwd1(String pwd1) {
		this.pwd1 = pwd1;
	}
	public String getPwd2() {
		return pwd2;
	}
	public void setPwd2(String pwd2) {
		this.pwd2 = pwd2;
	}
	public String getPwd3() {
		return pwd3;
	}
	public void setPwd3(String pwd3) {
		this.pwd3 = pwd3;
	}
	public String getPwd4() {
		return pwd4;
	}
	public void setPwd4(String pwd4) {
		this.pwd4 = pwd4;
	}
	public String getPwd5() {
		return pwd5;
	}
	public void setPwd5(String pwd5) {
		this.pwd5 = pwd5;
	}
	
	
	
}

