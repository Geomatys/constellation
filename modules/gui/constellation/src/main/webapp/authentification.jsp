<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" %>
<%@ page session="true" %>
<%@ page buffer="100kb" %>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Seagis Authentification</title>
        <link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
    </head>
    <body>
        <f:view>
            <h:form id="form_1" enctype="multipart/form-data">
                <div id="contentUpload" class="content">
                    <h:outputText value="Enter your login and password : " style="font-size:17px;font-weight:bold;"/>
                    <br/>
		    <br/>
                    <%-- Authentification panel --%>
                    <h:panelGrid id="authentification_panel" columns="2">
                        <h:outputText id="login_label" value="login : " />
                        <h:inputText id="loginId" value="#{authentificationBean.login}"/>

			<h:outputText id="pass_label" value="Pass : " />
                        <h:inputSecret id="passId" value="#{authentificationBean.password}"/>
                        
                        <t:commandLink styleClass="button" id="authentificationButton" style="margin:20px;" action="#{authentificationBean.authentify}">
                            <h:outputText id="labelAuthentification" value="Connection"/>
                        </t:commandLink>
                        <f:verbatim><br></f:verbatim>
                        
                    </h:panelGrid>
                </div>
            </h:form>

        </f:view>
    </body>
</html>
