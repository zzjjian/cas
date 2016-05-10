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

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.AccountDisabledException;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.util.Assert;

import com.ebao.cas.encrypt.PasswordEncrypt;

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
    private String disableSql="update  T_PUB_USER set STATUS='N' where user_id=?";
    private String addSql="update  T_PUB_USER set INVALID_LOGIN = INVALID_LOGIN+1 where user_id=?";
    private String resetSql="update  T_PUB_USER set INVALID_LOGIN = 0 where user_id=?";
    private String configDay="select para_value from T_PUB_PARA_DEF where para_id = 1178811";
    private String pwdSql="update  T_PUB_USER set NEED_CHANGE_PASS = ? where user_id=?";
    private PasswordEncrypt passwordEncrypt;
    /**
     * {@inheritDoc}
     */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
//      final String encryptedPassword = this.getPasswordEncoder().encode(credential.getPassword());
        final String encryptedPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getPassword());
        try {
 //         final String dbPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            final String dbPassword= (String)values.get("PASSWORD");
            final String uid=String.valueOf(values.get("USER_ID"));
            final String status=String.valueOf(values.get("STATUS"));
            final String invalid=String.valueOf(values.get("INVALID_LOGIN"));
            final String needFlag=String.valueOf(values.get("NEED_CHANGE_PASS"));
            final Date pwdDate = (Date) values.get("PASSWORD_CHANGE");
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("uid", uid);
            attributes.put("client_id", "key");
            if ("N".equals(status) ) {
                throw new AccountDisabledException("This account has been disabled");
            }
            if(3<= Long.parseLong(invalid)){
            	disableUser(Long.valueOf(uid));
            	throw new AccountDisabledException("This account has been disabled");
            }
            if (!dbPassword.equals(encryptedPassword)) {
            	addInvalidLogin(Long.valueOf(uid));
                throw new FailedLoginException("Password does not match value on record.");
            }
            resetInvalidLogin(Long.valueOf(uid));
            if(!needchangepwd(pwdDate,needFlag,Long.valueOf(uid))){
            	 throw new CredentialExpiredException("this account has been expired");
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
    
    
    
    private void disableUser(Long userId){
    	 getJdbcTemplate().update(disableSql,new Long[]{userId});
    }
    private void addInvalidLogin(Long userId){
    	 getJdbcTemplate().update(addSql,new Long[]{userId});
    }
    
    private void resetInvalidLogin(Long userId){
    	 getJdbcTemplate().update(resetSql,new Long[]{userId});
    }
    
    public boolean needchangepwd(Date startDate,String neddFlage,Long userId) {
		if ("0".equals(neddFlage)) {
			return false;
		}
		Long dbcongig =getJdbcTemplate().queryForObject(configDay,Long.class);
		if (startDate != null) {
			double bd = getBetweenDays(startDate, new Date());
			if (bd > dbcongig) {
				 getJdbcTemplate().update(pwdSql,new Long[]{0l,userId});
				return false;
			}
		}
		return true;
	}

	private double getBetweenDays(Date startDate, Date endDate) {
		double result = 0;
		result = (endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000);
		return result;
	}

	public String getDisableSql() {
		return disableSql;
	}

	public void setDisableSql(String disableSql) {
		this.disableSql = disableSql;
	}

	public String getAddSql() {
		return addSql;
	}

	public void setAddSql(String addSql) {
		this.addSql = addSql;
	}

	public String getResetSql() {
		return resetSql;
	}

	public void setResetSql(String resetSql) {
		this.resetSql = resetSql;
	}

	public String getConfigDay() {
		return configDay;
	}

	public void setConfigDay(String configDay) {
		this.configDay = configDay;
	}

	public String getPwdSql() {
		return pwdSql;
	}

	public void setPwdSql(String pwdSql) {
		this.pwdSql = pwdSql;
	}

	public PasswordEncrypt getPasswordEncrypt() {
		return passwordEncrypt;
	}

	public void setPasswordEncrypt(PasswordEncrypt passwordEncrypt) {
		this.passwordEncrypt = passwordEncrypt;
	}

	public String getSql() {
		return sql;
	}

    
}
