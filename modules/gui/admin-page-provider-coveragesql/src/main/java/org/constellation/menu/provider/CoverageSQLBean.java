/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.menu.provider;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.geotoolkit.internal.sql.CoverageDatabaseInstaller;
import org.opengis.util.FactoryException;

/**
 * Coverage-SQL configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLBean extends AbstractProviderConfigBean{

    public static final String SERVICE_NAME = "coverage-sql";
    
    public CoverageSQLBean(){
        super(SERVICE_NAME,
              "/provider/coveragesql.xhtml",
              "/provider/coveragesqlConfig.xhtml",
              "/provider/coveragesqlLayerConfig.xhtml");
        addBundle("provider.coveragesql");
    }
    
    @Override
    public void create() {
        try {
            final DataSource dataSource = null;
            final File postgisInstall = null;
                    
            if (dataSource == null) {
                throw new IllegalArgumentException("The DataSource is null");
            }
            
            final Connection con  = dataSource.getConnection();
            
            final CoverageDatabaseInstaller pgInstaller = new CoverageDatabaseInstaller(con);
            pgInstaller.run("CREATE TRUSTED PROCEDURAL LANGUAGE 'plpgsql' HANDLER plpgsql_call_handler VALIDATOR plpgsql_validator;");
            pgInstaller.run("CREATE SCHEMA postgis;");
            pgInstaller.run(postgisInstall);
            pgInstaller.install();
            LOGGER.info("Coverage database created");
            con.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public boolean getHasCreationMethod() {
        return true;
    }
}
