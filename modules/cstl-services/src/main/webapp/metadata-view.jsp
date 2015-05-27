<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ page import="java.net.URL" %>
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.io.BufferedReader" %>
<%@ page import="java.io.InputStreamReader" %>
<%@ page import="com.fasterxml.jackson.databind.ObjectMapper" %>
<%@ page import="org.constellation.json.metadata.binding.RootObj" %>
<%@ page import="org.constellation.json.metadata.binding.SuperBlockObj" %>
<%@ page import="org.constellation.json.metadata.binding.BlockObj" %>
<%@ page import="org.constellation.json.metadata.binding.FieldObj" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.constellation.json.metadata.binding.ComponentObj" %>
<%@ page import="org.apache.sis.util.logging.Logging" %>
<%@ page import="java.util.logging.Level" %>
<%@ page import="java.util.logging.Logger" %>

<%@ page contentType="text/html; charset=UTF-8" %>


<%!
    private static final Logger LOGGER = Logging.getLogger("metadata-view.jsp");

    //method to resolve parameters passed to this page
    public String resolveParams(HttpServletRequest request,String param){
        return request.getParameter(param);
    }

    //method to connect to rest api
    public String sendRequest(String targetUrl) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(targetUrl);
            URLConnection connection = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();
        }catch(Exception ex){
            LOGGER.log(Level.WARNING,"Cannot send request to get metadata json : "+ ex.getLocalizedMessage(),ex);
            return null;
        }
        return sb.toString();
    }

    public String getVignette(RootObj rootObj) {
        for(SuperBlockObj sb : rootObj.getRoot().getChildren()){
            for(BlockObj b : sb.getSuperblock().getChildren()){
                for(ComponentObj comp : b.getBlock().getChildren()){
                    if(comp instanceof FieldObj){
                        FieldObj f = (FieldObj) comp;
                        if(f.getField() == null) continue;
                        if(f.getField().getPath().contains("graphicOverview") &&
                                f.getField().getPath().contains("fileName")){
                            return f.getField().value;
                        }
                    }
                }
            }
        }
        return "images/default_quicklook.png";
    }
    public String getTitle(RootObj rootObj, String defaultValue) {
        for(SuperBlockObj sb : rootObj.getRoot().getChildren()){
            for(BlockObj b : sb.getSuperblock().getChildren()){
                for(ComponentObj comp : b.getBlock().getChildren()){
                    if(comp instanceof FieldObj){
                        FieldObj f = (FieldObj) comp;
                        if(f.getField() == null) continue;
                        if("title".equalsIgnoreCase(f.getField().getTag())){
                            return f.getField().value;
                        }
                    }
                }
            }
        }
        return defaultValue;
    }
    public String getDate(RootObj rootObj) {
        for(SuperBlockObj sb : rootObj.getRoot().getChildren()){
            for(BlockObj b : sb.getSuperblock().getChildren()){
                for(ComponentObj comp : b.getBlock().getChildren()){
                    if(comp instanceof FieldObj){
                        FieldObj f = (FieldObj) comp;if(f.getField() == null) continue;
                        if(f.getField().getPath().contains("temporalElement") &&
                                f.getField().getPath().contains("beginPosition")){
                            return f.getField().value;
                        }
                    }
                }
            }
        }
        return "";
    }
    public boolean isParagraph(FieldObj fieldObj) {
        return "textarea".equalsIgnoreCase(fieldObj.getField().getRender());
    }
    public boolean isWebLink(FieldObj fieldObj) {
        return "web".equalsIgnoreCase(fieldObj.getField().getRender()) ||
                fieldObj.getField().value.startsWith("http://");
    }
    public boolean isCodeList(FieldObj fieldObj) {
        return fieldObj.getField().getRender().toLowerCase().contains("codelist");
    }
    public String translate(String key,ResourceBundle rscBundle) {
        try{
            return rscBundle.getString(key);
        }catch(Exception ex){
            return key;
        }
    }
%>

<%
    //1) get parameters passed to this page metadata id and lang
    String lang = resolveParams(request,"lang");
    String metadataId = resolveParams(request, "id");
    Locale locale = "fr".equalsIgnoreCase(lang) ? Locale.FRENCH : Locale.ENGLISH;
    ResourceBundle bundle = ResourceBundle.getBundle("bundle.metadata",locale);

    //2) get cstl url from liferay portal-ext.properties
    String cstlUrl = request.getRequestURL().toString();
    cstlUrl = cstlUrl.substring(0,cstlUrl.indexOf("/metadata-view.jsp"));

    RootObj pojo = null;
    String responseJson = null;

    //3) send to cstl rest api the request to get the rootObj as json text
    if(metadataId != null) {
        responseJson = sendRequest(cstlUrl+"/api/1/metadata/metadataJson/resolve/"+metadataId);
    }

    //4) unmarshall the json to get the java representation of the model

    if(responseJson != null && !responseJson.isEmpty()){
        try {
            final ObjectMapper jsonMapper = new ObjectMapper();
            pojo = jsonMapper.readValue(responseJson, RootObj.class);
        }catch(Exception ex) {
            LOGGER.log(Level.WARNING,"Cannot obtain object from json : "+ ex.getLocalizedMessage()+" \nresponseJson="+responseJson,ex);
        }
    }


    //5) fetch the model and place the html markup in this page

    //6) the pdf button will call a request to cstl api rest that will call this page with params and
    //   generate by flying saucer the attachment file as the final pdf result.
%>

<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"></meta>
    <link rel="stylesheet" type="text/css" href="<%= request.getContextPath()%>/styles/pdfmetadata.css"/>
    <style type="text/css">
        body {
            font-family: Arial Unicode MS, Lucida Sans Unicode, Arial, verdana, arial, helvetica, sans-serif !important;
            margin:0px;
            width:793.92px;
            text-align:left;
        }
        @page{
            size: 8.27in 11.69in;
            @bottom-right { content: "Page " counter(page) " / " counter(pages); }
        }
    </style>
</head>
<body>
<% if(pojo != null) {%>
<form name="metadataform" id="metadataform">
    <div class="metadataContainer" style="min-width:750px;">
        <table class="headerMetadata">
            <tbody>
            <tr>
                <td class="headerMdCol1">
                    <img style="border:none;width:80px;" alt="" src="<%=getVignette(pojo)%>"/>
                </td>
                <td class=" headerMdCol2"><%=getTitle(pojo,metadataId)%></td>
                <td class=" headerMdCol3"><%=getDate(pojo)%></td>
            </tr>
            </tbody>
        </table>
        <br/>
        <div id="advancedViewMetadata">
            <% for(SuperBlockObj superBlock : pojo.getRoot().getChildren()) {%>
            <div class="block-row" style="Page-Break: Avoid;">
                <div>
                    <div class="row-fluid">
                        <div class="span12 small-block">
                            <h3 class="heading-block"><%= translate(superBlock.getSuperblock().getName(),bundle)%></h3>
                            <p><%= translate(superBlock.getSuperblock().getHelp(),bundle)%></p>
                        </div>
                        <div class="span12">
                            <div class="collapse-block">
                                <div>
                                    <div>
                                        <%
                                            if(superBlock.getSuperblock().getChildren() != null) {
                                                for(BlockObj blockObj : superBlock.getSuperblock().getChildren()) {
                                        %>
                                        <div class="collapse-row-wrapper open" style="Page-Break: Avoid;">
                                            <div class="collapse-row-heading">
                                                <span class="text"><%= translate(blockObj.getBlock().getName(),bundle) %></span>
                                            </div>
                                            <div class="collapse-row-inner">
                                                <table class="table-mimic">
                                                    <tr class="table-row">
                                                        <td>
                                                            <div class="table-cell">
                                                                <div class="fieldset">
                                                                    <%
                                                                        if(blockObj.getBlock().getChildren() != null) {
                                                                            for(ComponentObj comp : blockObj.getBlock().getChildren()) {
                                                                                if(!(comp instanceof FieldObj)) continue;
                                                                                FieldObj fieldObj = (FieldObj)comp;
                                                                                if(fieldObj.getField() == null)continue;
                                                                    %>
                                                                    <ul class="metadata-list" style="margin-top:2px;">
                                                                        <li>
                                                                            <span class="label-data"><%= translate(fieldObj.getField().getName(),bundle) %></span>
                                                                            <% if(isParagraph(fieldObj)){%>
                                                                            <pre class="metadataPre" style="margin:0px;">
                                                                                <span escape="false" class="data"><%= fieldObj.getField().value %></span>
                                                                            </pre>
                                                                            <%} else if(isWebLink(fieldObj)){%>
                                                                            &nbsp;<a href="<%= fieldObj.getField().value.replaceAll("&","&amp;")%>" target="_blank">
                                                                            <span class="datalink"><%= fieldObj.getField().value.length() > 120 ? fieldObj.getField().value.substring(0,120).replaceAll("&","&amp;")+"..." :fieldObj.getField().value.replaceAll("&","&amp;")%></span>
                                                                        </a>
                                                                            <%} else if(isCodeList(fieldObj)){%>
                                                                            <span class="data"><%= translate(fieldObj.getField().value,bundle)%></span>
                                                                            <% }else {%>
                                                                            <span class="data" ><%= fieldObj.getField().value %></span>
                                                                            <%}%>
                                                                        </li>
                                                                    </ul>
                                                                    <%
                                                                            }
                                                                        }
                                                                    %>
                                                                </div>
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </div>
                                        <%
                                                }
                                            }
                                        %>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <%}%>
        </div>
    </div>
</form>
<%}else{%>
<div style="padding: 10px;">
        <span style="font-size:14px;font-weight:bold;color:#ff7777;">
            <%=translate("msg.error.metadata.loading",bundle)%>
        </span>
</div>
<%}%>
</body>

</html>