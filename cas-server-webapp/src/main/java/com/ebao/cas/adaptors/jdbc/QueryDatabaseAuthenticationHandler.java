/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.ebao.cas.adaptors.jdbc;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.Assert;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that if provided a query that returns a password (parameter of query
 * must be username) will compare that password to a translated version of the
 * password provided by the user. If they match, then authentication succeeds.
 * Default password translator is plaintext translator.
 *
 * @author Scott Battaglia
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 *
 * @since 3.0.0
 */
public class QueryDatabaseAuthenticationHandler extends AbstractJdbcUsernamePasswordAuthenticationHandler {

    @NotNull
    private String sql;

    /**
     * {@inheritDoc}
     */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
//      final String encryptedPassword = this.getPasswordEncoder().encode(credential.getPassword());
        final String encryptedPassword = encrypt(credential.getUsername(), "UNICORN", credential.getPassword(), "SHA-512");
        try {
 //         final String dbPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            final String dbPassword= (String)values.get("PASSWORD");
            final String uid=String.valueOf(values.get("USER_ID"));
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("uid", uid);
            attributes.put("client_id", "key");
            
            if (!dbPassword.equals(encryptedPassword)) {
                throw new FailedLoginException("Password does not match value on record.");
            }
            
    		return createHandlerResult(credential, this.principalFactory.createPrincipal(username, attributes),
    				null);
    		
        } catch (final IncorrectResultSizeDataAccessException e) {
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
                 
        } catch (final DataAccessException e) {
            throw new PreventedException("SQL exception while executing query for " + username, e);
        }
        
    }

    /**
     * @param sql The sql to set.
     */
    public void setSql(final String sql) {
        this.sql = sql;
    }
    
    
    public static String encrypt(String salt1,String salt2,String message, String algorithm) {
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

    
}
