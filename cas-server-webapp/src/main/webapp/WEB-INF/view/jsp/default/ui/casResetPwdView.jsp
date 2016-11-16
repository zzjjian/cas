<jsp:directive.include file="includes/top.jsp" />

<div class="box" id="login">
    <form:form method="post" id="fmr" action="${pageContext.request.contextPath}/resetPwd/save" commandName="credential" htmlEscape="true">

		<img src="${pageContext.request.contextPath}/images/Slogo.png" />

        <section class="row">
            <label for="username"><spring:message code="screen.welcome.label.netid" /></label>
            <spring:message code="screen.welcome.label.netid.accesskey" var="userNameAccessKey" />
            <form:input cssClass="required" cssErrorClass="error" id="username" size="25" tabindex="1" accesskey="${userNameAccessKey}" path="username" autocomplete="off" htmlEscape="true" />
        </section>
        <section class="row btn-row">
            <input class="btn-submit" name="resetPwd" accesskey="r" value="RESETPWD" tabindex="2" type="submit" />
        </section>
        <form:errors path="*" id="msg" cssClass="errors" element="div" htmlEscape="false" />
        <c:if test="${not empty error}">
		  	<div id="msgError" class="errors">
        		<p class="message">${error}</p>
  	 	 	</div>
  	 	  </c:if>
    </form:form>
</div>

<div id="sidebar">
    <div class="sidebar-content">
			

		<img src="${pageContext.request.contextPath}/images/ebaotech_Client_background.png" />

		
    </div>
</div>


<!--

<div id="sidebar">
    <div class="sidebar-content">
        <p><spring:message code="screen.welcome.security" /></p>

        <div id="list-languages">
        	


            <%final String queryString = request.getQueryString() == null ? "" : request.getQueryString().replaceAll("&locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]|^locale=([A-Za-z][A-Za-z]_)?[A-Za-z][A-Za-z]", "");%>
            <c:set var='query' value='<%=queryString%>' />
            <c:set var="xquery" value="${fn:escapeXml(query)}" />

            <h3>Languages:</h3>

            <c:choose>
                <c:when test="${not empty requestScope['isMobile'] and not empty mobileCss}">
                    <form method="get" action="login?${xquery}">
                        <select name="locale">
                            <option value="en">English</option>
                            <option value="es">Spanish</option>
                            <option value="fr">French</option>
                            <option value="ru">Russian</option>
                            <option value="nl">Nederlands</option>
                            <option value="sv">Svenska</option>
                            <option value="it">Italiano</option>
                            <option value="ur">Urdu</option>
                            <option value="zh_CN">Chinese (Simplified)</option>
                            <option value="zh_TW">Chinese (Traditional)</option>
                            <option value="de">Deutsch</option>
                            <option value="ja">Japanese</option>
                            <option value="hr">Croatian</option>
                            <option value="uk">Ukranian</option>
                            <option value="cs">Czech</option>
                            <option value="sl">Slovenian</option>
                            <option value="pl">Polish</option>
                            <option value="ca">Catalan</option>
                            <option value="mk">Macedonian</option>
                            <option value="fa">Farsi</option>
                            <option value="ar">Arabic</option>
                            <option value="pt_PT">Portuguese</option>
                            <option value="pt_BR">Portuguese (Brazil)</option>
                        </select>
                        <input type="submit" value="Switch">
                    </form>
                </c:when>
                <c:otherwise>
                    <c:set var="loginUrl" value="login?${xquery}${not empty xquery ? '&' : ''}locale=" />
                    <ul>
                        <li class="first"><a href="${loginUrl}en">English</a></li>
                        <li><a href="${loginUrl}es">Spanish</a></li>
                        <li><a href="${loginUrl}fr">French</a></li>
                        <li><a href="${loginUrl}ru">Russian</a></li>
                        <li><a href="${loginUrl}nl">Nederlands</a></li>
                        <li><a href="${loginUrl}sv">Svenska</a></li>
                        <li><a href="${loginUrl}it">Italiano</a></li>
                        <li><a href="${loginUrl}ur">Urdu</a></li>
                        <li><a href="${loginUrl}zh_CN">Chinese (Simplified)</a></li>
                        <li><a href="${loginUrl}zh_TW">Chinese (Traditional)</a></li>
                        <li><a href="${loginUrl}de">Deutsch</a></li>
                        <li><a href="${loginUrl}ja">Japanese</a></li>
                        <li><a href="${loginUrl}hr">Croatian</a></li>
                        <li><a href="${loginUrl}uk">Ukranian</a></li>
                        <li><a href="${loginUrl}cs">Czech</a></li>
                        <li><a href="${loginUrl}sl">Slovenian</a></li>
                        <li><a href="${loginUrl}ca">Catalan</a></li>
                        <li><a href="${loginUrl}mk">Macedonian</a></li>
                        <li><a href="${loginUrl}fa">Farsi</a></li>
                        <li><a href="${loginUrl}ar">Arabic</a></li>
                        <li><a href="${loginUrl}pt_PT">Portuguese</a></li>
                        <li><a href="${loginUrl}pt_BR">Portuguese (Brazil)</a></li>
                        <li class="last"><a href="${loginUrl}pl">Polish</a></li>
                    </ul>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<jsp:directive.include file="includes/bottom.jsp" />
-->