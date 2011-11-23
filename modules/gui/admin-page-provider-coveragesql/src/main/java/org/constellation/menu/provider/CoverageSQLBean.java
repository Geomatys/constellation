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
import javax.sql.DataSource;
import org.geotoolkit.internal.sql.CoverageDatabaseInstaller;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.parameter.Parameters;
import org.geotoolkit.referencing.IdentifiedObjects;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.util.FactoryException;

/**
 * Coverage-SQL configuration bean.
 *
 * @author Johann Sorel (Geomatys)
 */
public class CoverageSQLBean extends AbstractProviderConfigBean{

    public static final String SERVICE_NAME = "coverage-sql";
    
    private String postgisInstall;
            
    public CoverageSQLBean(){
        super(SERVICE_NAME,
              "/provider/coveragesql.xhtml",
              "/provider/coveragesqlConfig.xhtml",
              "/provider/coveragesqlLayerConfig.xhtml");
        addBundle("provider.coveragesql");
    }
    
    public void createCoverageDatabase() {
        try {
            String url      = null;
            String user     = null;
            String password = null;
            for (GeneralParameterValue groups : configuredParams.values()) {
                if (IdentifiedObjects.nameMatches(groups.getDescriptor(), configuredParams.getDescriptor().descriptor("CoverageDatabase"))) {
                    if (groups instanceof ParameterValueGroup) {
                        for (GeneralParameterValue value : ((ParameterValueGroup)groups).values()) {
                            ParameterValue realValue = (ParameterValue) value;
                            if (value.getDescriptor().getName().getCode().equals("URL")) {
                                url = realValue.stringValue();
                            } else if (value.getDescriptor().getName().getCode().equals("user")) {
                                user = realValue.stringValue();
                            } else if (value.getDescriptor().getName().getCode().equals("password")) {
                                password = realValue.stringValue();
                            }
                        }
                    }
                }
            }
            if (url != null) {
                final DataSource dataSource = new DefaultDataSource(url);
                final File postgisInstallDir = new File(postgisInstall);

                if (dataSource == null) {
                    throw new IllegalArgumentException("The DataSource is null");
                }

                final Connection con  = dataSource.getConnection(user, password);

                final CoverageDatabaseInstaller pgInstaller = new CoverageDatabaseInstaller(con);
                pgInstaller.postgisDir = postgisInstallDir;
                pgInstaller.user = user;
                pgInstaller.admin = user;
                pgInstaller.install();
                LOGGER.info("Coverage database created");
                con.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
    }

    /**
     * @return the postgisInstall
     */
    public String getPostgisInstall() {
        return postgisInstall;
    }

    /**
     * @param postgisInstall the postgisInstall to set
     */
    public void setPostgisInstall(String postgisInstall) {
        this.postgisInstall = postgisInstall;
    }
}
