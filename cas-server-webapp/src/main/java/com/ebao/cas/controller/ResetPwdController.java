package com.ebao.cas.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.ProducerCallback;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.ebao.cas.credential.JdbcUsernamePasswordCredential;
import com.ebao.cas.credential.UserPasswordForm;
import com.ebao.cas.encrypt.PasswordEncrypt;
import com.ebao.cas.util.JsonUtil;
import com.ebao.cas.util.MapWrap;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Controller("resetPwdController")
@RequestMapping("/resetPwd")
public class ResetPwdController {

	private static final Logger logger = LoggerFactory.getLogger(ResetPwdController.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PasswordEncrypt passwordEncrypt;
	@Autowired
	private Destination dest;
	@Autowired
	private ConnectionFactory connectionFactory;
	

	public static final String STATUS_ENABLED = "Y";

	private char[] str = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
			's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	private String getUserSql = "SELECT * FROM T_PUB_USER T WHERE T.USER_NAME = ?";
	private String UpdatePwdsql = "UPDATE  T_PUB_USER SET PASSWORD=?, STATUS=?, INVALID_LOGIN=?, PASSWORD_CHANGE=?, NEED_CHANGE_PASS=? WHERE USER_NAME=?";
	private String insertUPSql = "INSERT INTO T_PUB_USER_PASSWORD (UP_ID,PASS_WORD,USER_ID,INSERT_BY,INSERT_TIME,UPDATE_BY,UPDATE_TIME) VALUES (?,?,?,?,?,?,?)";
	private String getUserIdsql = "SELECT T.USER_ID,(SELECT T.USER_ID FROM  T_PUB_USER_PASSWORD T1 WHERE T1.USER_ID = T.USER_ID)PUSER_ID FROM T_PUB_USER T WHERE T.USER_NAME = ?";
	private String getSeqId = "SELECT T.NEXT_VAL FROM T_PUB_SEQUENCE T WHERE T.SEQUENCE_NAME = 'S_UID'";
	private String updateSeqID = "UPDATE T_PUB_SEQUENCE T SET T.NEXT_VAL = T.NEXT_VAL +1 WHERE T.SEQUENCE_NAME ='S_UID'";
	private String updateCheckPwdSql = "UPDATE T_PUB_USER_PASSWORD SET PASS_WORD=?,UPDATE_BY=?,UPDATE_TIME=? WHERE USER_ID=?";
	private String getPwdForChekcsql = "SELECT T1.PASS_WORD FROM T_PUB_USER T,T_PUB_USER_PASSWORD T1 WHERE T.USER_NAME=? AND T1.USER_ID = T.USER_ID";

	@RequestMapping(value = "", method = RequestMethod.GET)
	protected ModelAndView getResetPwdView(final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {

		JdbcUsernamePasswordCredential credential = new JdbcUsernamePasswordCredential();
		credential.setUsername("");
		logger.info("**************start controller*****************");
		ModelAndView mav = new ModelAndView("default/ui/casResetPwdView");
		mav.addObject("credential", credential);
		return mav;
	}

	@RequestMapping(value = "save", method = RequestMethod.POST)
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response,
			JdbcUsernamePasswordCredential credential) throws Exception {

		ModelAndView mav = new ModelAndView("default/ui/resetPwdSuccessView");
		try {
			logger.info("**************start controller*****************");
			logger.info("**************username:::" + credential.getUsername());

			// get uesr information
			String userName = ((JdbcUsernamePasswordCredential) credential).getUsername();
			if (userName == null || "".equals(userName)) {
				JdbcUsernamePasswordCredential credentialForError = new JdbcUsernamePasswordCredential();
				credential.setUsername("");
				logger.info("**************user is null*****************");
				mav = new ModelAndView("default/ui/casResetPwdView");
				mav.addObject("credential", credentialForError);
				mav.addObject("error", "Username is a required field.");
				return mav;
			}

			/*
			 * if ("Jan000".equals(userName)) { throw new RuntimeException(); }
			 */

			List<Map<String, Object>> dbList = jdbcTemplate.queryForList(getUserSql, userName);
			Map<String, Object> map = new HashMap<String, Object>();
			if (dbList.size() > 0) {
				map = dbList.get(0);
			}

			logger.info("userId:" + map.get("USER_ID"));
			logger.info("userPwd:" + map.get("PASSWORD"));

			// get new password
			String newPwd = genRandomNum(8);
			//logger.info("new passwrod:" + newPwd);

			// encrypt new password
			String encryptPwd = passwordEncrypt.encrypt(userName, newPwd);
			//logger.info("encrypted new password:" + encryptPwd);

			// change password
			jdbcTemplate.update(UpdatePwdsql, new Object[] { encryptPwd, STATUS_ENABLED, 0, new Date(), 0, userName });
			credential.setNewpassword(encryptPwd);

			// compare password save
			comparePwdSave((JdbcUsernamePasswordCredential) credential);

			// send mail
			sendMail(map, newPwd);
		} catch (Exception e) {
			mav = new ModelAndView("default/ui/resetPwdErrorView");
			mav.addObject("error", "There has some error,please contact administrator.");
		}

		return mav;
	}

	protected String genRandomNum(int length) {
		final int maxNum = 36;
		int i;
		int count = 0;

		StringBuffer pwd = new StringBuffer("");
		Random r = new Random();
		while (count < length) {
			i = Math.abs(r.nextInt(maxNum));
			if (i >= 0 && i < str.length) {
				pwd.append(str[i]);
				count++;
			}
		}
		return pwd.toString();
	}

	public void comparePwdSave(JdbcUsernamePasswordCredential credential) throws IOException {
		List<Map<String, Object>> dbList = jdbcTemplate.queryForList(getUserIdsql, credential.getUsername());
		Map<String, Object> map = new HashMap<String, Object>();
		if (dbList.size() > 0) {
			map = dbList.get(0);
		}
		UserPasswordForm upf = getComparePwdForm(credential);
		upf.setPwd1(upf.getPwd2());
		upf.setPwd2(upf.getPwd3());
		upf.setPwd3(upf.getPwd4());
		upf.setPwd4(upf.getPwd5());
		upf.setPwd5(credential.getNewpassword());
		String jsonPwd = JsonUtil.toJSON(upf);
		if (map == null || map.get("PUSER_ID") == null || "".equals(map.get("PUSER_ID"))) {
			Integer id = jdbcTemplate.queryForObject(getSeqId, Integer.class);
			jdbcTemplate.update(insertUPSql, new Object[] { id, jsonPwd, map.get("USER_ID"), map.get("USER_ID"),
					new Date(), map.get("USER_ID"), new Date() });
			jdbcTemplate.update(updateSeqID);
		} else {
			//logger.info("***************pwd:" + jsonPwd);
			jdbcTemplate.update(updateCheckPwdSql,
					new Object[] { jsonPwd, map.get("PUSER_ID"), new Date(), map.get("USER_ID") });
		}
	}

	private UserPasswordForm getComparePwdForm(JdbcUsernamePasswordCredential credential)
			throws JsonParseException, JsonMappingException, IOException {

		List<Map<String, Object>> dbList = jdbcTemplate.queryForList(getPwdForChekcsql, credential.getUsername());
		Map<String, Object> pwdMap = new HashMap<String, Object>();
		if (dbList.size() > 0) {
			pwdMap = dbList.get(0);
		}
		String pwd = pwdMap != null ? (String) pwdMap.get("PASS_WORD") : null;
		UserPasswordForm upf = pwd != null ? JsonUtil.fromJSON(pwd, UserPasswordForm.class) : new UserPasswordForm();
		return upf;
	}

	private void sendMail(Map<String, Object> map, String newPwd) {
		MapWrap contentWrap = MapWrap.newInstance().put("username", map.get("USER_NAME")).put("password", newPwd);
		final MapWrap wrap = MapWrap.newInstance().put("emailTo", map.get("EMAIL")).put("emailContent", contentWrap.toMap());
		
		//queue name, wrap, uuid
		final String integrationPoint = "resetPasswordNotify";
		final String correlationId = UUID.randomUUID().toString();
		
		final String userName = (String)map.get("USER_NAME");
		final Long userId = Long.valueOf(String.valueOf(map.get("USER_ID")));
		
		//get jmsTemplate
		JmsTemplate jmsTemplate = null;
		jmsTemplate = new JmsTemplate(connectionFactory);
		jmsTemplate.setSessionTransacted(false);
		jmsTemplate.setPubSubDomain(false);
		
		try {
			jmsTemplate.execute(dest, new ProducerCallback<Object>() {
				@Override
				public Object doInJms(Session session, MessageProducer producer) throws JMSException {

					ObjectMessage message = session.createObjectMessage();
					
					//add mock data for send mail
					message.setLongProperty("userId", userId);
					message.setStringProperty("userName", userName);
					/*Map<String, Object> ciMap = new HashMap<String, Object>();
					ciMap.put("SourceSystemId", new Integer(1));
					ciMap.put("CommandUUID", UUID.randomUUID().toString());
					ciMap.put("GlobalTransactionId",3216544L);
					ciMap.put("TransactionType", 1L);
					ciMap.put("NeedResponse", "N");
					ciMap.put("SourceSystemBaseURL", "fff");
					message.setObjectProperty("ci", ciMap);*/
					
					message.setJMSCorrelationID(correlationId);
					message.setObject(wrap.toHashMap());
					
					/*try {
						fireSendEvent(message);
					} catch (ClientException e) {
						throw new JMSException(e.getMessage());
					}*/
					//producer.setTimeToLive(timeToLive);
					producer.send(message);
					//addSynchronizationIfNecessary(integrationPoint, content);
					return null;
				}
			});
		} catch (JmsException e) {
			throw e;
			//throw new ClientException(e, ClientExceptionCode.CLIENT_ERR_JMS, "send error", e.getMessage());
		}
	}
}
