package com.ebao.cas.controller;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ebao.cas.credential.JdbcUsernamePasswordCredential;
import com.ebao.cas.credential.UserPasswordForm;
import com.ebao.cas.encrypt.PasswordEncrypt;
import com.ebao.cas.util.JsonUtil;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Controller("changePwdController")
@RequestMapping("/changePwd")
public class ChangePwdController {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PasswordEncrypt passwordEncrypt; 
	private String checkPwdsql = "SELECT COUNT(1) NUM FROM T_PUB_USER WHERE USER_NAME=? AND PASSWORD=? ";
	private String UpdatePwdsql = "UPDATE  T_PUB_USER SET PASSWORD=? ,PASSWORD_CHANGE=? ,NEED_CHANGE_PASS=? WHERE USER_NAME=?";
	private String getUserIdsql = "SELECT T.USER_ID,(SELECT T.USER_ID FROM  T_PUB_USER_PASSWORD T1 WHERE T1.USER_ID = T.USER_ID)PUSER_ID FROM T_PUB_USER T WHERE T.USER_NAME = ?";
	private String updateCheckPwdSql ="UPDATE T_PUB_USER_PASSWORD SET PASS_WORD=?,UPDATE_BY=?,UPDATE_TIME=? WHERE USER_ID=?";
	private String insertUPSql="INSERT INTO T_PUB_USER_PASSWORD (UP_ID,PASS_WORD,USER_ID,INSERT_BY,INSERT_TIME,UPDATE_BY,UPDATE_TIME) VALUES (?,?,?,?,?,?,?)";
	private String getSeqId = "SELECT T.NEXT_VAL FROM T_PUB_SEQUENCE T WHERE T.SEQUENCE_NAME = 'S_UID'";
	private String updateSeqID="UPDATE T_PUB_SEQUENCE T SET T.NEXT_VAL = T.NEXT_VAL +1 WHERE T.SEQUENCE_NAME ='S_UID'";
	private String getPwdForChekcsql = "SELECT T1.PASS_WORD FROM T_PUB_USER T,T_PUB_USER_PASSWORD T1 WHERE T.USER_NAME=? AND T1.USER_ID = T.USER_ID";
	
	@RequestMapping(value = "save", method = RequestMethod.POST)
	protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response,
			JdbcUsernamePasswordCredential credential) throws Exception {
		ModelAndView mav = new ModelAndView("default/ui/casExpiredPassView");
		
		final String encryptedOldPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getOldpassword());
		credential.setOldpassword(encryptedOldPassword);
		mav.addObject("credential", credential);
		if (credential.getNewpassword() != null
				&& !credential.getNewpassword().equals(credential.getConfirmpassword())) {
			credential.setNewpassword("");
			credential.setOldpassword("");
			credential.setConfirmpassword("");
			mav.addObject("error", "the confirm password is not equal the new password ");
			return mav;
		}
		if (!checkPassword(credential)) {
			credential.setNewpassword("");
			credential.setOldpassword("");
			credential.setConfirmpassword("");
			mav.addObject("error", "the old password is invalid");
			return mav;
		}
		
		final String encryptedNewPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getNewpassword());
		credential.setNewpassword(encryptedNewPassword);
		saveUserMessage(credential);
		comparePwdSave(credential);
		System.out.println("handleRequestInternal**************"+"redirect:"+credential.getLoginUrl());
		mav = new ModelAndView("redirect:"+credential.getLoginUrl());

		return mav;
	}

	@RequestMapping(value = "check", method = { RequestMethod.GET, RequestMethod.POST })
	protected @ResponseBody Map<String, String> checkPasswordForBefore (@RequestBody  JdbcUsernamePasswordCredential credential) throws Exception {
		Map<String, String> modelMap = new HashMap<String, String>();
		modelMap.put("reFlag", "error");
		final String encryptedOldPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getOldpassword());
		credential.setOldpassword(encryptedOldPassword);
		final String encryptedNewPassword = passwordEncrypt.encrypt(credential.getUsername(), credential.getNewpassword());
		credential.setNewpassword(encryptedNewPassword);
		boolean re = comparePwdCheck(credential);
		if(re){
			modelMap.put("reFlag", "success");
		}
		return modelMap;
	}

	private boolean checkPassword(JdbcUsernamePasswordCredential credential) {
		
		int num  = jdbcTemplate.queryForObject(checkPwdsql,Integer.class, new Object[] {credential.getUsername(),credential.getOldpassword()} );
		if (num == 1)
			return true;
		return false;
	}

	private void saveUserMessage(JdbcUsernamePasswordCredential credential) {
		
		jdbcTemplate.update(UpdatePwdsql, new Object[] { credential.getNewpassword(), new Date(), 0, credential.getUsername() });
	}

	private boolean comparePwdCheck(JdbcUsernamePasswordCredential credential) throws JsonParseException, JsonMappingException, IOException {
		if(credential.getNewpassword() == null){
			return false;
		}
		UserPasswordForm upf = getComparePwdForm(credential);
		Set<String> set = new HashSet<String>();
		set.add(upf.getPwd1());
		set.add(upf.getPwd2());
		set.add(upf.getPwd3());
		set.add(upf.getPwd4());
		set.add(upf.getPwd5());
		if (set.contains(credential.getNewpassword())) {
			return false;
		}
		return true;
	}

	public void comparePwdSave(JdbcUsernamePasswordCredential credential) throws IOException {
		List<Map<String, Object>> dbList = jdbcTemplate.queryForList(getUserIdsql, credential.getUsername());
		Map<String, Object> map = new HashMap<String, Object>();
		if(dbList.size()>0){
			map = dbList.get(0);
		}
		UserPasswordForm upf = getComparePwdForm(credential);
		upf.setPwd1(upf.getPwd2());
		upf.setPwd2(upf.getPwd3());
		upf.setPwd3(upf.getPwd4());
		upf.setPwd4(upf.getPwd5());
		upf.setPwd5(credential.getNewpassword());
		String jsonPwd = JsonUtil.toJSON(upf);
		if(map==null || map.get("PUSER_ID")==null || "".equals(map.get("PUSER_ID"))){
			Integer id = jdbcTemplate.queryForObject(getSeqId, Integer.class);
			jdbcTemplate.update(insertUPSql,new Object[]{id,jsonPwd,map.get("USER_ID"),map.get("USER_ID"),new Date(),map.get("USER_ID"),new Date()});
			jdbcTemplate.update(updateSeqID);
		}else{
			jdbcTemplate.update(updateCheckPwdSql,new Object[]{jsonPwd,map.get("PUSER_ID"),new Date(),map.get("USER_ID")});
		}
	}
	
	private UserPasswordForm getComparePwdForm(JdbcUsernamePasswordCredential credential) throws JsonParseException, JsonMappingException, IOException{
		
		List<Map<String, Object>> dbList = jdbcTemplate.queryForList(getPwdForChekcsql,credential.getUsername());
		Map<String, Object> pwdMap = new HashMap<String, Object>();
		if(dbList.size()>0){
			pwdMap = dbList.get(0);
		}
		String pwd = pwdMap!= null ?(String)pwdMap.get("PASS_WORD"):null;
		UserPasswordForm upf = pwd!= null?JsonUtil.fromJSON(pwd, UserPasswordForm.class):new UserPasswordForm();
		return upf;
	}
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public PasswordEncrypt getPasswordEncrypt() {
		return passwordEncrypt;
	}

	public void setPasswordEncrypt(PasswordEncrypt passwordEncrypt) {
		this.passwordEncrypt = passwordEncrypt;
	}
	
	

}
