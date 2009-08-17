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
        <title>Constellation Authentification</title>
        <link rel="stylesheet" type="text/css" href="resources/css/login.css"/>
    </head>
    <body>

        <div style="top: 15%; position: absolute; width: 100%;">

        <f:view>
            <center><h:graphicImage url="resources/img/constellation400.png"/></center>

            <div class="sidebox">
                <h:form>

                    <div class="boxhead">
                        <h2></h2>
                    </div>

                    <div class="boxbody">

                        <div style="text-align:right; padding:15px; width:300px">
                            <h:outputText id="login_label" value="Login : " />
                            <h:inputText id="loginId" value="#{authentificationBean.login}"/>
                            <br/>
                            <h:outputText id="pass_label" value="Password : " />
                            <h:inputSecret id="passId" value="#{authentificationBean.password}"/>
                        </div>

                        <center>
                            <t:commandLink styleClass="button" id="authentificationButton"  action="#{authentificationBean.authentify}">
                                <h:outputText id="labelAuthentification" value="Connection"/>
                            </t:commandLink>
                        </center>

                    </div>
                </h:form>
            </div>

            <center>2007-2009 <b>Geomatys</b></center>

        </f:view>

        </div>
    </body>
</html>
