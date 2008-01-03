<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<%@ page language="java" %>
<%@ page session="true" %>
<%@ page buffer="100kb" %>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<%@ taglib uri="https://ajax4jsf.dev.java.net/ajax" prefix="a4j"%>
<html>
    <head>
        <link href="styles.css" rel="stylesheet" type="text/css"/>
    </head>
    <body>        
        <f:view>
                <f:loadBundle basename="net.seagis.bean.messages" var="msgs"/>
                <h:form id="form_1" >
                   <t:dataTable id="data" 
                                 value="#{resultData.layers}" 
                                 var="layers"               
                                 styleClass="layers" 
                                 headerClass="layersHeader" 
                                 columnClasses="name,period,description"
                                 rowClasses="row_1,row_2"
                                 sortable="true"                  
                                 rows="10"
                                 sortColumn="#{resultData.sortColumn}" 
                                 sortAscending="#{resultData.sortAscending}"
                                 preserveDataModel="true"
                                 preserveSort="true"
                                 rowOnMouseOver="this.style.backgroundColor='#A5CBFF'"
                                 rowOnMouseOut="this.style.backgroundColor=''"
                                 rowOnClick="this.style.backgroundColor='#FFE0E0'"
                                 rowOnDblClick="this.style.backgroundColor='#E0E0E0'">
                        <t:column id="column_1" rendered="#{resultData.editable}">
                            <f:facet  name="header">
                                <h:outputText id="text_1" value="#{msgs.checkboxHeader}"/>
                            </f:facet>
                            <h:selectBooleanCheckbox id="checkbox_1" value="#{layers.markedForDeletion}" onchange="submit()">
                            </h:selectBooleanCheckbox>
                        </t:column>
                        <t:column id="column_2" defaultSorted="true">
                            <f:facet  name="header">
                                <h:outputText id="text_2" value="#{msgs.nameHeader}"/>
                            </f:facet>
                            <h:outputText id="text_22" value="#{layers.name},"/>
                        </t:column>
                        <t:column id="column_3">
                            <f:facet  name="header">
                                <h:outputText id="text_3" value="#{msgs.periodHeader}"/>
                            </f:facet>
                            <h:outputText id="text_33" value="#{layers.period},"/>
                        </t:column>
                        <t:column id="column_4">
                            <f:facet  name="header">
                                <h:outputText id="text_4" value="#{msgs.descriptionHeader}"/>
                            </f:facet>
                            <h:outputText id="text_44" value="#{layers.description}"/>
                        </t:column>
                    </t:dataTable>
                    <h:panelGrid id="panelGrid_1" columns="1"  styleClass="scrollerTable2" columnClasses="standardTable_ColumnCentered" >
                        <t:dataScroller id="scroll_1"
                                        for="data"
                                        fastStep="10"
                                        pageCountVar="pageCount"
                                        pageIndexVar="pageIndex"
                                        styleClass="scroller"
                                        paginator="true"
                                        paginatorMaxPages="50"
                                        paginatorTableClass="paginator"
                                        paginatorActiveColumnStyle="font-weight:bold;">
                            <f:actionListener  type="net.seagis.bean.DataScrollerActionListener"/>
                            <f:facet name="first" >
                                <t:graphicImage id="graphicImage_1" url="images/arrow-first.gif" border="1" />
                            </f:facet>
                            <f:facet name="last">
                                <t:graphicImage id="graphicImage_2"  url="images/arrow-last.gif" border="1" />
                            </f:facet>
                            <f:facet name="previous">
                                <t:graphicImage id="graphicImage_3" url="images/arrow-previous.gif" border="1" />
                            </f:facet>
                            <f:facet name="next">
                                <t:graphicImage id="graphicImage_4" url="images/arrow-next.gif" border="1" />
                            </f:facet>
                            <f:facet name="fastforward">
                                <t:graphicImage id="graphicImage_5" url="images/arrow-ff.gif" border="1" />
                            </f:facet>
                            <f:facet name="fastrewind">
                                <t:graphicImage id="graphicImage_6" url="images/arrow-fr.gif" border="1" />
                            </f:facet>
                        </t:dataScroller>
                    </h:panelGrid>
                    <h:outputText value="#{msgs.editPrompt}"/>
                    <h:selectBooleanCheckbox onchange="submit()" value="#{resultData.editable}"/>                    
                        <h:commandButton id="deleteButtonText_1"
                                     value="#{msgs.deleteButtonText}" 
                                     rendered="#{resultData.editable}" 
                                     action="#{resultData.deleteLayers}" 
                                     >                          
                        </h:commandButton>
                     
                     
               </h:form>
               <h:form id="form_2">
                         <h:panelGrid id="panelGrid_2" columns="2" cellpadding="2" cellspacing="0" width="400px">
                                <h:outputText  id="text_10" value="Folder Path : "/>
                                <h:inputText  id="text_11" value="#{resultData.serverPath}"/>
                                <h:outputText  id="text_12"  value="Layer Name : "/>                
                                <h:inputText  id="text_13" value="#{resultData.layerName}"/>
                                <h:commandButton id="addButtonText_1"
                                     value="Lancer le moissonage " 
                                     action="#{resultData.addLayers}"/> 
                </h:panelGrid>   
                </h:form>
        </f:view>
    </body>
</html>

