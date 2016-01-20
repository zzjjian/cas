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
package com.ebao.cas.adaptors.rest;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.handler.support.AbstractUsernamePasswordAuthenticationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import java.io.InputStream;
import java.net.URI;
import java.security.GeneralSecurityException;
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
public class QueryRestAuthenticationHandler extends AbstractUsernamePasswordAuthenticationHandler {

	private static final Logger logger = LoggerFactory.getLogger(QueryRestAuthenticationHandler.class);

	@NotNull
	private String requestUrl;

	private final String userUrl = "/restlet/v1/public/system/login";

	private Map<String, Object> maps;

	protected final HandlerResult authenticateUsernamePasswordInternal(final UsernamePasswordCredential credential)
			throws GeneralSecurityException, PreventedException {

		final String username = credential.getUsername();
		final String password = credential.getPassword();

		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();

			URI uri = new URIBuilder(requestUrl + userUrl).setParameter("name", username)
					.setParameter("password", password).setParameter("type", "1").build();

			HttpPost httpget = new HttpPost(uri.toString());

			CloseableHttpResponse response = httpclient.execute(httpget);

			try {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						byte[] b = new byte[1024];
						int length = 0;
						StringBuffer sb = new StringBuffer();
						while ((length = instream.read(b)) != -1) {
							sb.append(new String(b, 0, length, "UTF-8"));
						}

						ObjectMapper objectMapper = new ObjectMapper();
						maps = objectMapper.readValue(sb.toString(), Map.class);
					} finally {
						instream.close();
					}
				}
			} finally {
				response.close();
			}

		} catch (Exception e) {
			logger.info("login with runtime exception: {}", e.getMessage());
			throw new FailedLoginException("Login Failed");
		}

		logger.info("restful api return type: {}", maps.get("Type"));

		if (maps != null) {
			int type = (Integer) maps.get("Type");
			if (type == 0) {
				final String uid = String.valueOf((Integer) maps.get("Uid"));
				Map<String, Object> attributes = new HashMap<String, Object>();
				attributes.put("uid", uid);
				attributes.put("client_id", "key");
				logger.info("restful api return uid: {}", uid);
				return createHandlerResult(credential, this.principalFactory.createPrincipal(username, attributes),
						null);
			}
		}
		throw new FailedLoginException((String) maps.get("ErrKey"));
	}

	public String getRequestUrl() {
		return requestUrl;
	}

	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}
}
