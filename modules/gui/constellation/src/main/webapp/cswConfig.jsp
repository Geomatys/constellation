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
        <title>Constellation Metadata Editor</title>
        <link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
    </head>
    <body>
        <f:view>
            <h:form id="form_1">
                <div id="configuration" class="content">
                    
                    <h:outputText value="Configuration and maintenance for Constellation CSW" style="font-size:17px;font-weight:bold;"/>
                    <br/>
                    <h:panelGrid id="maintenance_grid" columns="1" cellpadding="2" cellspacing="30" width="100%">
                        <t:commandLink styleClass="button" id="restart-id" onclick="" action="#{configurationBean.restartServices}">
                            <h:outputText id="restart_label" value="Restart the services"/>
                        </t:commandLink>

                        <t:commandLink styleClass="button" id="generate_index-id" action="#{configurationBean.generateIndex}">
                            <h:outputText id="generate_label" value="Generate Index"/>
                        </t:commandLink>

			<h:panelGrid id="generate_parameters_grid" columns="3" cellpadding="2" cellspacing="30" width="100%">    	
			    <h:outputLabel value="service ID:"/>
			    <h:inputText id="service_id" value="#{configurationBean.serviceIdentifier}" />			    
			    <h:selectOneRadio value="#{configurationBean.currentSynchroneMode}">
				<f:selectItems value="#{configurationBean.synchroneMode}" />
		            </h:selectOneRadio>
			</h:panelGrid>

			<h:panelGrid id="add_parameters_grid" columns="3" cellpadding="2" cellspacing="30" width="100%">    			
			    <t:commandLink styleClass="button" id="addToIndex-id" action="#{configurationBean.addToIndex}">
	                        <h:outputText id="add_to_label" value="Add to Index"/>
	                    </t:commandLink>
                            <h:outputLabel value="records identifiers:"/>
			    <h:inputText id="records_ids" value="#{configurationBean.recordIdentifiers}" />
			</h:panelGrid>
			
			<t:commandLink styleClass="button" id="refresh_contact-id" action="#{configurationBean.resfreshContact}">
                            <h:outputText id="refresh_contact_label" value="Refresh Contacts"/>
                        </t:commandLink>
	
			<t:commandLink styleClass="button" id="refresh_vocabulary-id" action="#{configurationBean.resfreshVocabulary}">
                            <h:outputText id="refresh_vocabulary_label" value="Refresh vocabulary"/>
                        </t:commandLink>
			
			<t:commandLink styleClass="button" id="config-back" action="authentified">
                            <h:outputText id="config-back-lable" value="Back"/>
                        </t:commandLink>
                        
                    </h:panelGrid>
                    
                   
                </div>
            </h:form>

        </f:view>
    </body>
</html>
