<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page language="java" %>
<%@ page session="true" %>
<%@ page buffer="100kb" %>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
	<link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
    </head>
    <body>
        <f:view>
            <h:form id="form_1">
            	<div id="content" class="content">
			
			<h:outputText value="Web Services Metadata editor for Seagis" style="font-size:17px;font-weight:bold;"/>
			<br/>
		        <h:panelGrid id="ServiceIdentificationGrid" columns="2" cellpadding="2" cellspacing="30" width="100%">
		            <h:outputLabel for="selone_lb" value="Select Web services metadata : " />
		            <h:selectOneListbox id="selone_lb" size="4" value="#{servicesBean.webServiceMode}"
		                                validator="#{servicesBean.validateWebService}" styleClass="selectOneListbox" required="true" >
		                <f:selectItems id="selone_lb_cars" value="#{servicesBean.webServices}" />
		            </h:selectOneListbox>
                             
                             <t:commandLink styleClass="button" id="open-id" onclick="" action="#{servicesBean.switchMode}">
                                            <h:outputText id="label" value="Open metadata form"/>
                             </t:commandLink>
                             
                             <t:commandLink styleClass="button" id="export-id" onclick="this.blur();document.getElementById('target_xml').src=(document.getElementById('form_1:urlId').value);" action="#{servicesBean.storeData}">
                                            <h:outputText id="label2" value="Export preference"/>
                             </t:commandLink>
		            
		        </h:panelGrid>
                         <h:outputText value="File : #{servicesBean.urlPreference} has been saved at the root of war !! " style="color:green;" rendered="false"/>
                         <h:outputLink value="#{servicesBean.urlPreference}">After you have exported your preference file, download it </h:outputLink>
                         <a id="link" href="preference.sml">here</a>
		</div>
                 <h:inputText id="urlId" value="#{servicesBean.urlPreference}" style="display:none;" onchange="document.getElementById('target_xml').src=(document.getElementById('form_1:urlId').value);"/>
            </h:form>
            <iframe style="display: none;" src="" name="target_xml" id="target_xml"/>
        </f:view>
    </body>
</html>