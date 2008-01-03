/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package net.seagis.bean;

//~--- non-JDK imports --------------------------------------------------------

import net.seagis.coverage.catalog.Collector;

import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.NamingException;

import net.seagis.catalog.UpdatePolicy;

/**
 *
 * @author olivier
 */
public class Writer {
   
    private   Collector collector2;
    // public class CustomerBean {
    private Connection conn;

    public void open() throws SQLException, NamingException, CatalogException {
        if (conn != null) {
            return;
        }

         
        collector2 = new Collector();

        Database db = collector2.getDatabase();

        conn = db.getConnection();
        System.out.println("openWriter()");
        
        
    }

    public void setAll(Layers[] layers) throws SQLException, NamingException, CatalogException {
        try {
            open();

            // System.out.println("apres open()");
            conn.setReadOnly(false);

            Statement stmt = conn.createStatement();

            // System.out.println("avan executeQuery()");
            for (int i = 0; i < layers.length; i++) {
                String sql = "DELETE FROM \"Layers\" WHERE name='" + layers[i].getName() + "'";
                String sql2 = "DELETE FROM \"Series\" WHERE layer='" + layers[i].getName() + "'";
                /*String sql3 = "DELETE FROM \"GridCoverages\" WHERE (\"filename\" ='" + layers[i].getName()+ "') AND (\"series\" = 'Caraibes')";
                String sql4 = "DELETE FROM \"Series\" WHERE layer='" + layers[i].getName() + "'";*/
                
                //System.out.println(sql);
                System.out.println(sql);
                stmt.executeUpdate(sql);
                System.out.println(sql2);
                stmt.executeUpdate(sql2);
                /*System.out.println(sql3);
                stmt.executeUpdate(sql3);
                System.out.println(sql4);
                stmt.executeUpdate(sql4);*/
                System.out.println("sql1 et 2 executer");
            }
        } finally {
            close();
        }
    }
    public void setLayersAndSeries(String ServerPath, String layerName) throws SQLException, NamingException, CatalogException {
        try{
                                    Collector collector = new Collector();
                                    Database database = collector.getDatabase();
                                    Connection connection = database.getConnection();     
             
                                     // System.out.println("apres open()");
                                    connection.setReadOnly(false);
                                    
                                    final String insert = "INSERT INTO \"Layers\" VALUES (?, ?, ?, NULL,NULL, ?)";
                                    PreparedStatement st = connection.prepareStatement(insert);
                                    st.setString(1, layerName);
                                    st.setString(2, "Anomalie de la hauteur de l'eau");
                                    st.setString(3, "Modèle numérique");
                                    st.setString(4, "Données au format Caraibes");
                                    
                                    //INSERT INTO
                                    //System.out.println("avant update layers");
                                    st.executeUpdate();                                    
                                    //System.out.println("avant update Series");
                                    final String insert2 = "INSERT INTO \"Series\" VALUES (?, ?, ?, 'nc', 'Caraïbes (depth)',TRUE,NULL)";                                   
                                    PreparedStatement st2 = connection.prepareStatement(insert2);
                                    st2.setString(1, layerName);
                                    st2.setString(2, layerName);
                                    st2.setString(3, ServerPath);
                                    st2.executeUpdate();            
                                    //connection.close();
                                    //System.out.println("Debut  Collector");
                                    //true pour simuler
                                    collector.setPretend(false);
                                    collector.setPolicy(UpdatePolicy.SKIP_EXISTING);
                                    collector.process(layerName);
                                    collector.close();
                                    connection.close();
            
            
            /* open();

                                    // System.out.println("apres open()");
                                    conn.setReadOnly(false);
                                    final String insert = "INSERT INTO \"Layers\" VALUES (?, ?, ?, NULL,NULL, ?)";
                                    PreparedStatement st = conn.prepareStatement(insert);
                                    st.setString(1, layerName);
                                    st.setString(2, "Anomalie de la hauteur de l'eau");
                                    st.setString(3, "Modèle numérique");
                                    st.setString(4, "Données au format Caraibes");
                                    
                                    //INSERT INTO
                                    System.out.println("avant update layers");
                                    st.executeUpdate();                                    
                                    System.out.println("avant update Series");
                                    final String insert2 = "INSERT INTO \"Series\" VALUES (?, ?, ?, 'nc', 'Caraïbes (depth)',TRUE,NULL)";                                   PreparedStatement st2 = conn.prepareStatement(insert2);
                                    st2.setString(1, layerName);
                                    st2.setString(2, layerName);
                                    st2.setString(3, ServerPath);
                                    st2.executeUpdate();
                                    conn.close();
                                    System.out.println("Debut  Collector");
                                    collector.setPretend(true);
                                    
                                    System.out.println("setPretend");
                                    collector.setPolicy(UpdatePolicy.REPLACE_EXISTING);
                                    
                                    System.out.println("process");
                                    collector.process("Caraibes");
                                    System.out.println("close");
                                    collector.close();
                                    close();*/

        } finally {
            close();
        }
    }
    public void close() throws SQLException, CatalogException {
        if (conn == null) {
            return;
        }

        System.out.println("closeWriter()");
        conn.close();
        collector2.close();
        conn = null;
    }
}


//~ Formatted by Jindent --- http://www.jindent.com
