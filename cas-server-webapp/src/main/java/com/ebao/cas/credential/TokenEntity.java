package com.ebao.cas.credential;

public class TokenEntity {

	private String access_token;

	public TokenEntity() {

	}

	public TokenEntity(String token) {
		this.access_token = token;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}
}
