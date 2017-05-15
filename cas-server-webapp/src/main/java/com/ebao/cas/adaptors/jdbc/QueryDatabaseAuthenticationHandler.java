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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.adaptors.jdbc.AbstractJdbcUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.ebao.cas.adaptors.rest.QueryRestAuthenticationHandler;
import com.ebao.cas.encrypt.PasswordEncrypt;
import com.ebao.cas.util.Hex;
import com.ebao.cas.util.Utf8;

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
    private String disableSql="UPDATE  T_PUB_USER SET STATUS='N' WHERE USER_ID=?";
    private String addSql="UPDATE  T_PUB_USER SET INVALID_LOGIN = INVALID_LOGIN+1 WHERE USER_ID=?";
    private String configDay="SELECT PARA_VALUE FROM T_PUB_PARA_DEF WHERE PARA_ID = ?";
    private String pwdSql="UPDATE  T_PUB_USER SET NEED_CHANGE_PASS = ? WHERE USER_ID=?";
    
    private String getpwdSql="SELECT PASSWORD FROM T_MAF_ACCOUNT_SECURE WHERE ACCOUNT_ID=?";
    private String failTimesSql="SELECT FAIL_RETRY_TIMES FROM T_MAF_ACCOUNT_SYSTEM WHERE ACCOUNT_ID=?";
    private String resetSql="UPDATE  T_MAF_ACCOUNT_SYSTEM SET FAIL_RETRY_TIMES = 0 WHERE ACCOUNT_ID=?";
    private PasswordEncrypt passwordEncrypt;
    private final static Long PASSWORD_CHECK_DAY_DEFUAL = 90L;
    private final static Long INVALID_LOGIN_TIMES_DEFUAL = 5L;
    private final static Long PASSWORD_CHECK_DAY_ID = 1178811L;
    private final static Long INVALID_LOGIN_TIMES_ID = 1178812L;
    private static final Logger logger = LoggerFactory.getLogger(QueryRestAuthenticationHandler.class);
    /**
     * {@inheritDoc}
     */
    @Override
    protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
            throws GeneralSecurityException, PreventedException {

        final String username = credential.getUsername();
//      final String encryptedPassword = this.getPasswordEncoder().encode(credential.getPassword());
        //final String encryptedPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getPassword());
        try {
 //         final String dbPassword = getJdbcTemplate().queryForObject(this.sql, String.class, username);
            final Map<String, Object> values = getJdbcTemplate().queryForMap(this.sql, username);
            //final String dbPassword= (String)values.get("PASSWORD");
            final String uid=String.valueOf(values.get("ACCOUNT_ID"));
            final Boolean status=((Integer.valueOf(String.valueOf(values.get("ENABLED"))))==1);
            //final String invalid=String.valueOf(values.get("INVALID_LOGIN"));
            //final String needFlag=String.valueOf(values.get("NEED_CHANGE_PASS"));
            //final Date pwdDate = (Date) values.get("PASSWORD_CHANGE");
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("uid", uid);
            attributes.put("client_id", "key");
            if (status == false) {
            	logger.error("This account has been disabled");
                throw new AccountLockedException("This account has been locked");
            }
            /*Long dbconfig = getConfigParaById(INVALID_LOGIN_TIMES_ID)==null?INVALID_LOGIN_TIMES_DEFUAL:getConfigParaById(INVALID_LOGIN_TIMES_ID);
            if(dbconfig<= Long.parseLong(invalid)){
            	logger.error("This account has been disabled");
            	disableUser(Long.valueOf(uid));
            	throw new AccountLockedException("This account has been locked");
            }*/
            //****************password check********************
            String saltedPass = credential.getPassword() + "{" + credential.getUsername() + "}";
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

    		byte[] digest = messageDigest.digest(Utf8.encode(saltedPass));
    		String encodePwd = new String(Hex.encode(digest));
    		System.out.println(encodePwd);
    		
            final Map<String, Object> pwdValues = getJdbcTemplate().queryForMap(this.getpwdSql, uid);
            final String dbPassword= (String)pwdValues.get("PASSWORD");
            if (!dbPassword.equals(encodePwd)) {
            	//addInvalidLogin(Long.valueOf(uid));
            	logger.error("Password does not match value on record.");
                throw new FailedLoginException("Password does not match value on record.");
            }
            
            //****************fail entry times check***************
            /*final Map<String, Object> failTimesValues = getJdbcTemplate().queryForMap(this.failTimesSql, uid);
            Long failTimes = Long.valueOf(String.valueOf(failTimesValues.get("FAIL_RETRY_TIMES")));
            if(this.INVALID_LOGIN_TIMES_DEFUAL <= failTimes){
            	logger.error("This account has been disabled");
            	disableUser(Long.valueOf(uid));
            	throw new AccountLockedException("This account has been locked");
            }*/
            
            //resetInvalidLogin(Long.valueOf(uid));
            /*if(!needchangepwd(pwdDate,needFlag,Long.valueOf(uid))){
            	 logger.error("this account password has been expired");
            	 throw new CredentialExpiredException("this account has been expired");
            }*/
    		return createHandlerResult(credential, this.principalFactory.createPrincipal(username, attributes),
    				null);
    		
        } catch (final IncorrectResultSizeDataAccessException e) {
        	logger.error("there has some error",e);
            if (e.getActualSize() == 0) {
                throw new AccountNotFoundException(username + " not found with SQL query");
            } else {
                throw new FailedLoginException("Multiple records found for " + username);
            }
                 
        } catch (final DataAccessException e) {
        	logger.error("there has some error",e);
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
		if ("1".equals(neddFlage)) {
			return false;
		}
		Long dbconfig = getConfigParaById(PASSWORD_CHECK_DAY_DEFUAL)==null?PASSWORD_CHECK_DAY_ID:getConfigParaById(PASSWORD_CHECK_DAY_DEFUAL);
//		Long dbcongig =getJdbcTemplate().queryForObject(configDay,Long.class);
		if (startDate != null) {
			double bd = getBetweenDays(startDate, new Date());
			if (bd > dbconfig) {
				 getJdbcTemplate().update(pwdSql,new Long[]{1l,userId});
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
	private Long getConfigParaById(Long id){
		List<Map<String, Object>> dbList = getJdbcTemplate().queryForList(configDay, id);
		Map<String, Object> map = new HashMap<String, Object>();
		Long dbconfig = null;
		if(dbList.size()>0){
			map = dbList.get(0);
			dbconfig = new Long((String)map.get("para_value"));
		}
		return dbconfig;
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
