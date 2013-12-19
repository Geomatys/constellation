<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="org.geotoolkit.util.StringUtilities"%>
<%@page import="org.constellation.admin.util.SQLExecuter"%>
<%@page import="org.constellation.admin.EmbeddedDatabase"%>
<%@page import="java.sql.SQLException"%>
<%@page import="java.sql.ResultSetMetaData"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.sql.DriverManager"%>
<%@page import="java.sql.Statement"%>
<%@page import="org.constellation.admin.dao.Session"%>
<html>
<body><h1>SQL CONNECTION</h1>
<form action="sql.jsp" method="post">
<br />
query : <textarea rows="5" cols="80" name="query"><%= request.getParameter("query")==null?"":request.getParameter("query")%></textarea><br />
<input type="submit" title="submit">
</form>
<br />
result : <br />
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
<table border="1">
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