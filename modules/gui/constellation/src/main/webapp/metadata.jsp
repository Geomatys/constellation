
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://myfaces.apache.org/sandbox" prefix="s"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <title>Constellation Metadata Editor</title>
        <link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
    </head>
    <body>
        <f:view>
            <center><h:graphicImage url="resources/img/constellation400.png"/></center>

            <h:form id="form_1" enctype="multipart/form-data">
                <div id="contentUpload" class="content">
                    <h3>Load your preference file</h3>
                    <br/>
		    <br/>
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
                        
                        <br/>
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
                    <h3>Web Services Metadata editor for Constellation</h3>
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
                    <h3>Web Services configuration and maintenance for Constellation</h3>
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

            <h:form id="form_4" enctype="multipart/form-data">
                <div class="content">
                    <h3>Data ans Style Providers</h3>
                    <br/>
                    <h:panelGrid id="providers_panel" columns="2" width="100%" columnClasses="colclasse">

                    <a4j:commandButton value="Reload data providers" action="#{servicesBean.reloadLayerProviders}" reRender="layerPane">
                    </a4j:commandButton>

                    <a4j:commandButton value="Reload style providers" action="#{servicesBean.reloadStyleProviders}" reRender="stylePane">
                    </a4j:commandButton>

                    <h:panelGroup id="layerPane">
                        <a4j:repeat value="#{servicesBean.layerProviders}" var="x">
                            <h:outputText value="#{x}"/><br/>
                        </a4j:repeat>
                    </h:panelGroup>

                    <h:panelGroup id="stylePane">
                        <a4j:repeat value="#{servicesBean.styleProviders}" var="x">
                            <h:outputText value="#{x}"/><br/>
                        </a4j:repeat>
                    </h:panelGroup>

                    </h:panelGrid>

                </div>
            </h:form>


            <iframe style="display: none;" src="" name="target_xml" id="target_xml"/>
        </f:view>
    </body>
</html>
