package com.ebao.cas.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

public class PasswordEncrypt {
	private String salt2;
	private String algorithm;
	public String encrypt(String salt1,String message) {
		Assert.notNull(message, "encrypt message can't be null!");
		if (!"SHA-224".equals(algorithm) && !"SHA-256".equals(algorithm)
				&& !"SHA-384".equals(algorithm) && !"SHA-512".equals(algorithm)) {
			throw new RuntimeException(
					"algorithm must be SHA-256/224,SHA-512/384.");
		}
		if(StringUtils.isNotEmpty(salt2)){
			message=salt2+"-"+message;
		}
		if(StringUtils.isNotEmpty(salt1)){
			message=salt1+"-"+message;
		}
		byte[] buffer = message.getBytes();

		// The SHA algorithm results in a 20-byte digest
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		/**
		 * Ensure the digest's buffer is empty. This isn't necessary the first
		 * time used. However, it is good practice to always empty the buffer
		 * out in case you later reuse it.
		 */
		md.reset();

		// Fill the digest's buffer with data to compute a message digest from.
		md.update(buffer);

		// Generate the digest. This does any necessary padding required by the
		// algorithm.
		byte[] digest = md.digest();

		// Save or print digest bytes. Integer.toHexString() doesn't print
		// leading zeros.
		StringBuffer hexString = new StringBuffer();
		String sHexBit = null;
		for (int i = 0; i < digest.length; i++) {
			sHexBit = Integer.toHexString(0xFF & digest[i]);
			if (sHexBit.length() == 1) {
				sHexBit = "0" + sHexBit;
			}
			hexString.append(sHexBit);
		}
		return hexString.toString();
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getSalt2() {
		return salt2;
	}

	public void setSalt2(String salt2) {
		this.salt2 = salt2;
	}
	
	
	
}
