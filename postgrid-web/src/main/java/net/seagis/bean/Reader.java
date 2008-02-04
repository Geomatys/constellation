/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package net.seagis.bean;

//~--- non-JDK imports --------------------------------------------------------

import net.seagis.console.Collector;

import net.seagis.catalog.CatalogException;
import net.seagis.catalog.Database;

//~--- JDK imports ------------------------------------------------------------

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.naming.NamingException;

import javax.servlet.jsp.jstl.sql.Result;
import javax.servlet.jsp.jstl.sql.ResultSupport;

/**
 *
 * @author olivier
 */
public class Reader {
    private Collector collector;

    // public class CustomerBean {
    private Connection conn;

    public void open() throws SQLException, NamingException, CatalogException {
        if (conn != null) {
            return;
        }

        
        if (collector != null) {
            return;
        }

        collector = new Collector();

        Database db = collector.getDatabase();

        conn = db.getConnection();
        System.out.println("openReader()");
    }

    public Layers[] getAll() throws SQLException, NamingException, CatalogException {
        try {
            open();

            // System.out.println("apres open()");
            Statement stmt = conn.createStatement();

            // System.out.println("avan executeQuery()");
            ResultSet result = stmt.executeQuery("SELECT * FROM \"Layers\" ORDER BY name");

            // System.out.println("apres executeQuery()");
            //Layers[] layers = new Layers[28];
            int  cpt    = 0;
               List<Layers> layersList = new ArrayList();
            while (result.next()) {

                // System.out.println("dans result "+cpt+" = "+result.getString("name")+" "+result.getDouble("period")+" "+result.getString("description"));
                /*layers[cpt] = new Layers(result.getString("name"), result.getDouble("period"),
                                         result.getString("description"));
*/
                layersList.add(new Layers(result.getString("name"), result.getDouble("period"), result.getString("description")));
                // System.out.println("apres creation layers"+layers[cpt].toString());
                

                // System.out.println("apres cpt++"+cpt+" = "+result.getString("name")+" "+result.getDouble("period")+" "+result.getString("description"));
            }
               
               
               
            Layers[] layers = new Layers[layersList.size()];   
            Iterator it = layersList.iterator();
           while (cpt<layersList.size()) {
               //System.out.println("dans result "+cpt+" = "+result.getString("name")+" "+result.getDouble("period")+" "+result.getString("description"));
               System.out.println("dans result "+(layersList.size()+1)+" "+cpt+" "+ layersList.get(cpt).getName());
               
               layers[cpt] = layersList.get(cpt);
                /*layers[cpt] = new Layers(result.getString("name"), result.getDouble("period"),
                                         result.getString("description"));*/
                cpt++; 
           }
            //layersList.removeAll(layersList);
            System.out.println("dans result "+ layers.length);
               
           return layers;
        } finally {
            close();
        }
    }

    public void close() throws SQLException {
        if (conn == null) {
            return;
        }

        System.out.println("closeReader()");
        conn.close();
        conn = null;
    }
}
