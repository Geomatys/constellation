/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.admin.listener;

import org.apache.sis.util.logging.Logging;
import org.constellation.admin.SpringHelper;
import org.constellation.api.PropertyConstants;
import org.constellation.business.IConfigurationBusiness;
import org.constellation.business.IDataBusiness;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ServletContextListener that run {@link org.constellation.business.IDataBusiness#computeEmptyDataStatistics()}
 * at constellation startup.
 * This listener MUST be added after all Spring initialization in web.xml file.
 *
 * @author Quentin Boileau (Geomatys)
 */
public class AnalyseListener implements ServletContextListener {

    private static final Logger LOGGER = Logging.getLogger(AnalyseListener.class);

    @Inject
    private IDataBusiness dataBusiness;

    @Inject
    private IConfigurationBusiness configurationBusiness;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        SpringHelper.injectDependencies(this);

        if (configurationBusiness != null) {
            //check if data analysis is required
            String propertyValue = configurationBusiness.getProperty(PropertyConstants.DATA_ANALYSE_KEY);
            boolean doAnalysis = propertyValue == null ? false : Boolean.valueOf(propertyValue);

            if (doAnalysis && dataBusiness != null) {
                LOGGER.log(Level.FINE, "Start data analysis");
                dataBusiness.computeEmptyDataStatistics(true);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //nothing to do
    }
}
