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
        <title>JSP Page</title>
        <link rel="stylesheet" type="text/css" href="resources/css/style.css"/>
    </head>
    <body>
        <f:view>
        
            <div id="content" class="content">
                
                <h:form id="form_2">
                    <%-- formular panel --%>
                    <h:outputText  id="ServiceIdentificationLabel" value="Service Identification Section : " style="font-size:18px;"/>
                    <h:panelGrid id="ServiceIdentificationGrid" columns="2" cellpadding="2" cellspacing="0" width="100%" style="margin-top:20px;">
                        
                        <h:outputText  id="SI_TitleLabel"    value="Title : "/>
                        <h:inputText   id="SI_TitleInput"    value="#{servicesBean.title}"/>
                        
                        <h:outputText  id="SI_AbstractLabel" value="Abstract : "/>                
                        <h:inputText   id="SI_AbstractInput" value="#{servicesBean.abstract}"/>
                        
                        <h:outputText  id="SI_KeywordsLabel" value="Keywords : "/>                
                        <h:selectManyListbox id="SI_KeywordsInput">
                            <f:selectItems value="#{servicesBean.keywords}"/>  
                        </h:selectManyListbox>
                        
                        <h:outputText  id="SI_ServiceTypeLabel" value="Service Type : "/>                
                        <h:inputText   id="SI_ServiceTypeInput" value="#{servicesBean.serviceType}"/>
                        
                        <!-- note: service version is not editable because it depend on the implementation -->
                        <h:outputText  id="SI_ServiceVersionLabel" value="Service Versions : "/>                
                        <h:selectManyListbox id="SI_ServiceVersionInput" >
                            <f:selectItems value="#{servicesBean.versions}"/>  
                        </h:selectManyListbox>
                        
                        <h:outputText  id="SI_FeesLabel" value="Fees : "/>                
                        <h:inputText   id="SI_FeesInput" value="#{servicesBean.fees}"/>
                        
                        <h:outputText  id="SI_AccessConstraintsLabel" value="Acces Constraints : "/>                
                        <h:inputText   id="SI_AccessConstraintsInput" value="#{servicesBean.accessConstraints}"/>
                        
                        
                        <h:outputText  id="emptyLabel_11"/>
                        <h:outputText  id="emptyLabel_12"/>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        
                        <!-- a panel for Service Provider Section --> 
                        <h:outputText  id="ServiceProviderLabel" value="Service Provider Section : " style="font-size:18px;"/>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        
                        
                        <h:outputText  id="SP_NameLabel" value="Provider name : "/>
                        <h:inputText   id="SP_NameInput" value="#{servicesBean.providerName}"/>
                        <h:outputText  id="SP_SiteLabel" value="Provider Site : "/>                
                        <h:inputText   id="SI_SiteInput" value="#{servicesBean.providerSite}"/>
                        
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        
                        <!-- a panel for service Contact Section -->
                        <h:outputText  id="ServiceContactLabel" value="Service Contact Section : " style="font-size:18px;"/>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        
                        
                        <h:outputText  id="SPC_IndividualNameLabel" value="individual name : "/>                
                        <h:inputText   id="SPC_IndividualNameInput" value="#{servicesBean.individualName}"/>
                        <h:outputText  id="SPC_PositionNameLabel"   value="Position name : "/>                
                        <h:inputText   id="SPC_PositionNameInput"   value="#{servicesBean.positionName}"/>
                        
                        <h:outputText  id="SPC_phoneVoiceLabel" value="Phone number (voice) : "/>                
                        <h:inputText   id="SPC_phoneVoiceInput" value="#{servicesBean.phoneVoice}"/>
                        <h:outputText  id="SPC_phonefaxLabel"   value="Phone number (Facsimile) : "/>                
                        <h:inputText   id="SPC_phonefaxInput"   value="#{servicesBean.phoneFacsimile}"/>
                        
                        
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <h:outputText  id="ContactInfoLabel" value="Contact Info Section : " style="font-size:18px;"/>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        
                        <h:outputText  id="CI_DeliveryPointLabel" value="Delivery point : "/>                
                        <h:inputText   id="CI_DeliveryPointInput" value="#{servicesBean.deliveryPoint}"/>
                        <h:outputText  id="CI_CityLabel" value="City : "/>                
                        <h:inputText   id="CI_cityInput" value="#{servicesBean.city}"/>
                        <h:outputText  id="CI_AdministrativeAreaLabel" value="Administrative Area : "/>                
                        <h:inputText   id="CI_AdministrativeAreaInput" value="#{servicesBean.administrativeArea}"/>
                        <h:outputText  id="CI_PostalCodeLabel" value="Postal Code : "/>                
                        <h:inputText   id="CI_PostalCodeInput" value="#{servicesBean.postalCode}"/>
                        <h:outputText  id="CI_CountryLabel" value="Country : "/>                
                        <h:inputText   id="CI_CountryInput" value="#{servicesBean.country}"/>
                        <h:outputText  id="CI_ElectronicAddressLabel" value="Electronic address : "/>                
                        <h:inputText   id="CI_ElectronicAddressInput" value="#{servicesBean.electronicAddress}"/>
                        
                        <h:outputText  id="SPC_RoleLabel" value="Role : "/>                
                        <h:inputText   id="SPC_RoleInput" value="#{servicesBean.role}"/>
                        
                        <!-- cette section ne devrait apparaitre que pour le WMS -->
                        
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <f:verbatim>
                            <br/>
                        </f:verbatim>
                        <h:outputText  id="WMSectionLabel" value="WMS extras Section : " style="font-size:18px;"/>
                        <h:outputText  id="emptyLabel_5"/>
                        <h:outputText  id="WMS_LayerLimitLabel" value="Layer limit : "/>                
                        <h:inputText   id="WMS_LayerLimitInput" value="#{servicesBean.layerLimit}"/>
                        <h:outputText  id="WMS_MaxHeightLabel"  value="Max Height : "/>                
                        <h:inputText   id="WMS_MaxHeightInput"  value="#{servicesBean.maxHeight}"/>
                        <h:outputText  id="WMS_MaxWidthLabel"   value="Max Wdth : "/>                
                        <h:inputText   id="WMS_MaxWidthInput"   value="#{servicesBean.maxWidth}"/>
                        
                        <t:commandLink styleClass="button" id="StorForm" onclick="this.blur();" action="#{servicesBean.storeForm}" style="margin-top:50px;">
                            <h:outputText id="label1" value="Save the form"/>
                        </t:commandLink>
                        
                        
                        <t:commandLink styleClass="button" id="goBack" onclick="this.blur();" action="#{servicesBean.goBack}" style="margin-top:50px;">
                            <h:outputText id="label2" value="Back"/>
                        </t:commandLink>
                    </h:panelGrid>
                </h:form>
            </div>
        </f:view>
    </body>
</html>

