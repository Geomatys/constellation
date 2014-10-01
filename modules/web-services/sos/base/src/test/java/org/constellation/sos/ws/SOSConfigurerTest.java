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
package org.constellation.sos.ws;

import java.util.ArrayList;
import java.util.Arrays;
import org.constellation.sos.configuration.SOSConfigurer;
import org.junit.Assert;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles("standard")
public abstract class SOSConfigurerTest  implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    protected static SOSConfigurer configurer = new SOSConfigurer();
    
    public void getObservationsCsvTest() throws Exception {
        
        String result = configurer.getDecimatedObservationsCsv("default", "urn:ogc:object:sensor:GEOM:3", Arrays.asList("urn:ogc:def:phenomenon:GEOM:depth"), null, null, 10);
        String expResult = "Time,depth\n" +
                                 "2007-05-01T02:59:00,6.56\n" +
                                 "2007-05-01T04:53:00,6.56\n" +
                                 "2007-05-01T04:59:00,6.56\n" +
                                 "2007-05-01T06:53:00,6.56\n" +
                                 "2007-05-01T06:59:00,6.56\n" +
                                 "2007-05-01T08:53:00,6.56\n" +
                                 "2007-05-01T08:59:00,6.56\n" +
                                 "2007-05-01T10:53:00,6.56\n" +
                                 "2007-05-01T10:59:00,6.56\n" +
                                 "2007-05-01T12:53:00,6.56\n" +
                                 "2007-05-01T17:59:00,6.55\n" +
                                 "2007-05-01T19:53:00,6.55\n" +
                                 "2007-05-01T19:59:00,6.55\n" +
                                 "2007-05-01T21:53:00,6.55\n";
        Assert.assertEquals(expResult, result);
        
        result = configurer.getDecimatedObservationsCsv("default", "urn:ogc:object:sensor:GEOM:8", Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"), null, null, 10);
        expResult = "Time,depth,temperature\n" +
                    "2007-05-01T12:59:00,6.56,12.0\n" +
                    "2007-05-01T13:23:00,6.56,13.0\n" +
                    "2007-05-01T13:59:00,6.56,14.0\n" +
                    "2007-05-01T14:23:00,6.56,14.0\n" +
                    "2007-05-01T14:59:00,6.56,15.0\n" +
                    "2007-05-01T15:23:00,6.56,15.0\n" +
                    "2007-05-01T15:59:00,6.56,16.0\n" +
                    "2007-05-01T16:23:00,6.56,16.0\n";
        
        Assert.assertEquals(expResult, result);
    }
    
    public void getObservationsCsvProfileTest() throws Exception {
        String result = configurer.getDecimatedObservationsCsv("default", "urn:ogc:object:sensor:GEOM:2", Arrays.asList("urn:ogc:def:phenomenon:GEOM:aggregatePhenomenon"), null, null, 10);
        String expResult = "depth,temperature\n" +
                           "12,18.5\n" +
                           "87,23.9\n" +
                           "96,26.2\n" +
                           "171,26.2\n" +
                           "192,31.4\n" +
                           "267,31.4\n" +
                           "384,35.1\n" +
                           "459,35.1\n";
        Assert.assertEquals(expResult, result);
    }
    
}
