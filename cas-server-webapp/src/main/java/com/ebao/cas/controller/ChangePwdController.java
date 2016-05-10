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
	private String checkPwdsql = "select count(*) num from T_PUB_USER where User_Name=? and PassWord=? ";
	private String UpdatePwdsql = "update  T_PUB_USER set PassWord=? ,PASSWORD_CHANGE=? ,NEED_CHANGE_PASS=? where User_Name=?";
	private String getUserIdsql = "select t.user_id,(select t.User_Id from  T_PUB_USER_PASSWORD t1 where t1.user_id = t.user_id)PUSER_ID from T_PUB_USER t where t.user_name = ?";
	private String updateCheckPwdSql ="update T_PUB_USER_PASSWORD set PASS_WORD=?,UPDATE_BY=?,UPDATE_TIME=? where USER_ID=?";
	private String insertUPSql="insert into T_PUB_USER_PASSWORD (UP_ID,PASS_WORD,USER_ID,INSERT_BY,INSERT_TIME,UPDATE_BY,UPDATE_TIME) values (?,?,?,?,?,?,?)";
	private String getSeqId = "select t.next_val from T_PUB_SEQUENCE t where t.sequence_name = 'S_UID'";
	private String updateSeqID="update T_PUB_SEQUENCE t set t.next_val = t.next_val +1 where t.sequence_name ='S_UID'";
	private String getPwdForChekcsql = "select t1.PASS_WORD from T_PUB_USER t,T_PUB_USER_PASSWORD t1 where t.User_Name=? and t1.USER_ID = t.User_ID";
	
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
		mav.setViewName("cas/login");
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
		
		jdbcTemplate.update(UpdatePwdsql, new Object[] { credential.getNewpassword(), new Date(), 1, credential.getUsername() });
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
