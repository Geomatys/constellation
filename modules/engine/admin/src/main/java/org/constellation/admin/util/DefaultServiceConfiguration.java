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

package org.constellation.admin.util;

import org.constellation.configuration.DataSourceType;
import org.constellation.configuration.GetFeatureInfoCfg;
import org.constellation.configuration.LayerContext;
import org.constellation.configuration.ProcessContext;
import org.constellation.configuration.Processes;
import org.constellation.configuration.SOSConfiguration;
import org.constellation.configuration.WebdavContext;
import org.constellation.generic.database.Automatic;
import org.constellation.generic.database.BDD;
import org.constellation.ws.MimeType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
public class DefaultServiceConfiguration {
    
    public static Object getDefaultConfiguration(final String serviceType) {
        switch(serviceType.toLowerCase()) {
            case "csw": return new Automatic("filesystem", new BDD());
            case "wps": return new ProcessContext(new Processes(true));
            case "sos": final SOSConfiguration baseConfig = new SOSConfiguration(new Automatic(null, new BDD()), new Automatic(null, new BDD()));
                        baseConfig.setObservationReaderType(DataSourceType.FILESYSTEM);
                        baseConfig.setObservationFilterType(DataSourceType.LUCENE);
                        baseConfig.setObservationWriterType(DataSourceType.FILESYSTEM);
                        baseConfig.setSMLType(DataSourceType.FILESYSTEM);
                        return baseConfig;
            case "webdav": return new WebdavContext();
            // other case assume WXS
            default: final LayerContext configuration = new LayerContext();
                    ((LayerContext)configuration).setGetFeatureInfoCfgs(createGenericConfiguration());
                    return configuration;
        }
    }
    
    /** Create the default {@link GetFeatureInfoCfg} list to configure a LayerContext.
     * This list is build from generic {@link FeatureInfoFormat} and there supported mimetype.
     * HTMLFeatureInfoFormat, CSVFeatureInfoFormat, GMLFeatureInfoFormat
     *
     * @return a list of {@link GetFeatureInfoCfg}
     */
    private static List<GetFeatureInfoCfg> createGenericConfiguration () {
        //Default featureInfo configuration
        final List<GetFeatureInfoCfg> featureInfos = new ArrayList<>();

        //HTML
        featureInfos.add(new GetFeatureInfoCfg(MimeType.TEXT_HTML, "org.constellation.map.featureinfo.HTMLFeatureInfoFormat"));

        //CSV
        featureInfos.add(new GetFeatureInfoCfg(MimeType.TEXT_PLAIN, "org.constellation.map.featureinfo.CSVFeatureInfoFormat"));

        //GML
        featureInfos.add(new GetFeatureInfoCfg(MimeType.APP_GML, "org.constellation.map.featureinfo.GMLFeatureInfoFormat"));//will return map server GML
        featureInfos.add(new GetFeatureInfoCfg(MimeType.APP_GML_XML, "org.constellation.map.featureinfo.GMLFeatureInfoFormat"));//will return GML 3

        //XML
        featureInfos.add(new GetFeatureInfoCfg(MimeType.APP_XML, "org.constellation.map.featureinfo.XMLFeatureInfoFormat"));
        featureInfos.add(new GetFeatureInfoCfg(MimeType.TEXT_XML, "org.constellation.map.featureinfo.XMLFeatureInfoFormat"));
        
        return featureInfos;
    }
}
