<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.constellation.admin.util.SQLExecuter"%>
<%@page import="org.constellation.admin.EmbeddedDatabase"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.ResultSetMetaData"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.Statement"%>
<html>
<head>
    <script src="scripts/jquery.js"></script>
    <link rel="stylesheet" href="styles/bootstrap.css">
</head>
<body>
<div class="container">
    <h1>SQL CONNECTION</h1>
    <div class="row">
        <div class="col-md-12">
<form action="sql.jsp" method="post" id="sqlForm">
<br />
query : <textarea rows="5" cols="80" name="query" id="query"><%= request.getParameter("query")==null?"":request.getParameter("query")%></textarea><br />

    <input type="submit" title="submit" class="btn"/>
</form>
            </div>
        </div>
    <div class="row">
        <div class="col-md-12">
            <button class="btn btn-primary" id="provider">provider</button>
            <button class="btn btn-primary" id="data">data</button>
            <button class="btn btn-primary" id="style">style</button>
            <button class="btn btn-primary" id="styled_data">styled_data</button>
            <button class="btn btn-primary" id="sensor">sensor</button>
            <button class="btn btn-primary" id="sensored_data">sensored_data</button>
            <button class="btn btn-primary" id="service">service</button>
            <button class="btn btn-primary" id="service_details">service_details</button>
            <button class="btn btn-primary" id="service_i18n">service_i18n</button>
            <button class="btn btn-primary" id="layer">layer</button>
            <button class="btn btn-primary" id="task">task</button>
            <button class="btn btn-primary" id="user">user</button>
            <button class="btn btn-primary" id="mapcontext">mapcontext</button>
            <button class="btn btn-primary" id="mapcontext_styled_layer">mapcontext_styled_layer</button>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
result :
<%if (request.getParameter("query")!=null && request.getParameter("query").length() > 0) {

	String query=request.getParameter("query");

	SQLExecuter sqlExecuter = null;
        try {
            sqlExecuter = EmbeddedDatabase.createSQLExecuter();

            Statement st= sqlExecuter.createStatement();
            ResultSet rs= null;
            Integer nb = null;
            if (query.toLowerCase().trim().startsWith("select")){
                rs= st.executeQuery(query);
            } else {
                nb = st.executeUpdate(query);
            }

            if (rs!=null){
%>
        </div>
        </div>
    </div>
<table class="table table-striped">
<%              ResultSetMetaData rsmd = rs.getMetaData();
                for (int i = 1; i<=rsmd.getColumnCount();i++){%>
                    <th><%=rsmd.getColumnLabel(i) %></th>
<%              }
                while(rs.next()){%>
	<tr>
<%                  for(int i = 1; i<=rsmd.getColumnCount();i++){
                        String s = rs.getString(i);
                        if (s != null) {%>
                        <td><%= StringEscapeUtils.escapeHtml(s)%></td>
                      <%} else {%>
                            <td>null</td>
                     <%}
                    } %>
	</tr>
<%              }
            } else { %>
    nb row affected :  <%=nb %>
<%          }
        } catch (SQLException ex) {%>
                An SQL error occurs<%=ex.getMessage()%>
<%      } finally {
            if (sqlExecuter != null) sqlExecuter.close();
        }%>

</table>
<%} else {%>

<h1>fill the form</h1>
<%} %>
</body>
</html>
<script>
    $(function(){
        $("#task").on("click", function(){
            $("#query").val('select * from admin.task');
            $("#sqlForm").submit();
        });
        $("#provider").on("click", function(){
            $("#query").val('select * from admin.provider');
            $("#sqlForm").submit();
        });
        $("#data").on("click", function(){
            $("#query").val('select * from admin.data');
            $("#sqlForm").submit();
        });
        $("#service").on("click", function(){
            $("#query").val('select * from admin.service');
            $("#sqlForm").submit();
        });
        $("#layer").on("click", function(){
            $("#query").val('select * from admin.layer');
            $("#sqlForm").submit();
        });
        $("#service_details").on("click", function(){
            $("#query").val('select * from admin.service_details');
            $("#sqlForm").submit();
        });
        $("#service_i18n").on("click", function(){
            $("#query").val('select * from admin.service_i18n');
            $("#sqlForm").submit();
        });
        $("#style").on("click", function(){
            $("#query").val('select * from admin.style');
            $("#sqlForm").submit();
        });
        $("#user").on("click", function(){
            $("#query").val('select * from admin."USER"');
            $("#sqlForm").submit();
        });
        $("#styled_data").on("click", function(){
            $("#query").val('select * from admin.styled_data');
            $("#sqlForm").submit();
        });
        $("#sensor").on("click", function(){
            $("#query").val('select * from admin.sensor');
            $("#sqlForm").submit();
        });
        $("#sensored_data").on("click", function(){
            $("#query").val('select * from admin.sensored_data');
            $("#sqlForm").submit();
        });
        $("#mapcontext").on("click", function(){
            $("#query").val('select * from admin.mapcontext');
            $("#sqlForm").submit();
        });
        $("#mapcontext_styled_layer").on("click", function(){
            $("#query").val('select * from admin.mapcontext_styled_layer');
            $("#sqlForm").submit();
        });
    })
</script>
