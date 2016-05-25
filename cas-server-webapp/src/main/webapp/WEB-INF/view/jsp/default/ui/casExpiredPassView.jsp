<%--

    Licensed to Apereo under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Apereo licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License.  You may obtain a
    copy of the License at the following location:

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

--%>
<jsp:directive.include file="includes/top.jsp" />

<script type="text/javascript">
function comparePassword(){
		if($("#newpassword").val()==null || $("#newpassword").val()==""){
			return
		}
		var user ={
	      'username' :$("#username").val(),
	      'oldpassword' :$("#oldpassword").val(),
	      'newpassword' :$("#newpassword").val()
	     }
	    var filter =  /^(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[a-zA-Z0-9]{8,20}$/;
	    var patt1=new RegExp(filter);
		$.ajax( {   
	    type : "POST",   
	    url : "${pageContext.request.contextPath}/changePwd/check", 
	    contentType: "application/json; charset=utf-8",  
	    data : JSON.stringify(user),  
	    dataType: "json",   
	    success : function(data) {   	    	
	       if(!("success"== data.reFlag)){
	       		$("#msg").show();
	       		$("#msg").html("the new password is same as five times before");
	       		$("#submit_user").addClass('disabled',true);
	       		$("#submit_user").addClass("btn-reset").removeClass("btn-submit");
	       		return;
	       }
	       if(!patt1.test($("#newpassword").val())){
	      		$("#msg").show();
	       		$("#msg").html("Passwords must have at least 8 characters and contain at least two of the following: uppercase letters, lowercase letters, numbers, and symbols.");
	       		$("#submit_user").attr('disabled',true);
	       		$("#submit_user").addClass("btn-reset").removeClass("btn-submit");
	       		return;
	       }
	       $("#submit_user").attr('disabled',false);
	       $("#submit_user").addClass("btn-submit").removeClass("btn-reset");
	       $("#msg").html("")
	       $("#msg").hide()
	       return;
	    },   
	    error :function(data){   
	    	alert("there has some error,please contact administrator");   
	    }   
	});   
	
	return false;  
}  
function onSubmitCheck(){
	if($("#oldpassword").val()==null || $("#oldpassword").val()=="" ){
 			$("#msg").show();
			$("#msg").html("New Password is a required field.");
			return true ;
		}
 	if($("#newpassword").val()==null || $("#newpassword").val()=="" ){
 			$("#msg").show();
			$("#msg").html("New Password is a required field.");
			return true ;
		}
		
    if($("#confirmpassword").val()==null || $("#confirmpassword").val()=="" ){
    		$("#msg").show();
			$("#msg").html("Confirm Password is a required field.");
			return true ;
		}
	if($("#confirmpassword").val() != $("#newpassword").val()){
			$("#msg").show();
			$("#msg").html("the confirm password not same as new password");
			return true ;
		}
}
function onSubmitUser(){
	if(onSubmitCheck()){
		return
	}
	$("#msg").html("");
	$("#msg").hide();
	$("#passwordForm").action="${pageContext.request.contextPath}/changePwd/save"; 
	$("#passwordForm").submit();
	
}
</script>
<div class="box" id="login">

    <form:form method="post" id="passwordForm" action="${pageContext.request.contextPath}/changePwd/save" commandName="credential" htmlEscape="true">
		<h2><spring:message code="screen.expiredpass.heading" /></h2>
		  <c:if test="${not empty error}">
		  	<div id="msgError" class="errors">
        		<p class="message">${error}</p>
  	 	 	</div>
  	 	  </c:if>
  	 	<div id="msg" style="display:none;" class="errors">
  	 	</div>
        <section class="row">
            <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
            <form:input cssClass="required" cssErrorClass="error" id="username" name="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
        </section>
            <label for="password"><spring:message code="screen.welcome.label.old.password" /></label>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="oldpassword" name="oldpassword" size="25" tabindex="2" path="oldpassword"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
        </section>
		<section class="row">
            <label for="password"><spring:message code="screen.welcome.label.new.password" /></label>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="newpassword" name="newpassword" size="25" tabindex="2" path="newpassword"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" onblur="comparePassword()"/>
        </section>
        <section class="row">
            <label for="password"><spring:message code="screen.welcome.label.confrim.password" /></label>
            <spring:message code="screen.welcome.label.password.accesskey" var="passwordAccessKey" />
            <form:password cssClass="required" cssErrorClass="error" id="confirmpassword" name="confirmpassword" size="25" tabindex="2" path="confirmpassword"  accesskey="${passwordAccessKey}" htmlEscape="true" autocomplete="off" />
        </section>
        <form:input  type="hidden" cssClass="required" cssErrorClass="error" id="loginUrl" size="25" tabindex="1" accesskey="${loginUrlAccessKey}" path="loginUrl" autocomplete="off" htmlEscape="true" />
        
        <section class="row btn-row">
            <input class="btn-submit" id="submit_user" name="submit_user" accesskey="l" onclick="onSubmitUser()" value="<spring:message code="screen.welcome.button.submit" />" tabindex="6" type="button" />
            <input class="btn-reset" name="reset" accesskey="c" value="<spring:message code="screen.welcome.button.clear" />" tabindex="7" type="reset" />
        </section>
    </form:form>
</div>

<jsp:directive.include file="includes/bottom.jsp" />

<script type="text/javascript">
if(document.getElementById('loginUrl').value == null || document.getElementById('loginUrl').value=="")
	document.getElementById('loginUrl').value=window.location.href
</script>