<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" %>
<%@ page session="true" %>
<%@ page buffer="100kb" %>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>


<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <f:view>
            <h:form id="form_1">
                
                <!-- TODO enable seulement ceux present (en fonction des fichier XML present --> 
                <h:panelGrid id="ServiceIdentificationGrid" columns="2" cellpadding="2" cellspacing="0" width="600px">
                     <h:commandButton id="WMS"
                                      value="change WMS metadata" 
                                      action="#{servicesBean.setWMSMode}"/>
                     <h:commandButton id="CSW"
                                      value="change CSW metadata" 
                                      action="#{servicesBean.setCSWMode}"/>
                     <h:commandButton id="WCS"
                                      value="change WCS metadata" 
                                      action="#{servicesBean.setWCSMode}"/>
                     <h:commandButton id="SOS"
                                      value="change SOS metadata" 
                                      action="#{servicesBean.setSOSMode}"/>                 
                                     
                </h:panelGrid>   
            </h:form>
        </f:view>
    </body>
</html>
