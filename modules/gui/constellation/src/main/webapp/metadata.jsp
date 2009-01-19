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
            <h:form id="form_1" enctype="multipart/form-data">
                <div id="contentUpload" class="content">
                    <h:outputText value="Load your preference file : " style="font-size:17px;font-weight:bold;"/>
                    <br/>
		    <br/>
                    <%-- Upload panel --%>
                    <h:panelGrid id="upload_panel" columns="2">
                        <h:outputText id="upload_label" value="Upload XML file : " />
                        <t:inputFileUpload id="fileupload"
                                           accept="temp/*"
                                           storage="memory"
                                           maxlength="200000"
                                           value="#{servicesBean.uploadedFile}"
                                           immediate="true"
                                           required="true">
                            <f:valueChangeListener type="org.constellation.bean.UploadListener" />
                        </t:inputFileUpload>
                        
                        <t:commandLink styleClass="button" id="uploadButton" onclick="this.blur();" style="margin:20px;" action="#{servicesBean.doUpload}">
                            <h:outputText id="labelUpload" value="Upload"/>
                        </t:commandLink>
                        <f:verbatim><br></f:verbatim>
                        
                        <s:pprPanelGroup id="statepanel" partialTriggerPattern="form_1:.*">
                            <h:panelGrid id="fileinfos_panel" columns="2" border="0" cellspacing="5">
                                
                                <h:outputText value="FileName:"/>
                                <h:outputText value="#{servicesBean.uploadedFile.name}"/>
                                
                                <h:outputText value="FileSize:"/>
                                <h:outputText value="#{servicesBean.uploadedFile.size}"/>
                                
                            </h:panelGrid>
                        </s:pprPanelGroup>
                        
                    </h:panelGrid>
                </div>
            </h:form>
            
            <h:form id="form_2">
                <div id="content" class="content">
                    
                    <h:outputText value="Web Services Metadata editor for Constellation" style="font-size:17px;font-weight:bold;"/>
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
                        
                        <t:commandLink styleClass="button" id="export-id" onclick="this.blur();document.getElementById('target_xml').src=(document.getElementById('form_2:urlId').value);" action="#{servicesBean.storeData}">
                            <h:outputText id="label2" value="Export preference"/>
                        </t:commandLink>
                        
                    </h:panelGrid>
                    
                    <h:panelGrid id="downloadPanel" columns="1" cellpadding="2" cellspacing="30">
                        <h:outputLink value="#{servicesBean.urlPreference}" rendered="#{servicesBean.existPrefrence}">
                            <h:outputText value="Click to download your preference file."/>
                        </h:outputLink>
                    </h:panelGrid>
                </div>
                <h:inputText id="urlId" value="#{servicesBean.urlPreference}" style="display:none;" onchange="document.getElementById('target_xml').src=(document.getElementById('form_2:urlId').value);"/>
            </h:form>

             <h:form id="form_3">
                <div id="configuration" class="content">
                    
                    <h:outputText value="Web Services configuration and maintenance for Constellation" style="font-size:17px;font-weight:bold;"/>
                    <br/>
                    <h:panelGrid id="maintenanceGrid" columns="2" cellpadding="2" cellspacing="30" width="100%">
                        <t:commandLink styleClass="button" id="restart-id" onclick="" action="#{configurationBean.restartServices}">
                            <h:outputText id="restart_label" value="Restart the services"/>
                        </t:commandLink>
                        
                        <t:commandLink styleClass="button" id="csw-configure-id" action="configureCSW">
                            <h:outputText id="csw_label" value="Configure CSW"/>
                        </t:commandLink>
                        
                    </h:panelGrid>
                    
                   
                </div>
            </h:form>

            <iframe style="display: none;" src="" name="target_xml" id="target_xml"/>
        </f:view>
    </body>
</html>
