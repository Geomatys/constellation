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

package org.constellation.wfs;

import org.constellation.business.IDataBusiness;
import org.constellation.business.ILayerBusiness;
import org.constellation.business.IProviderBusiness;
import org.constellation.business.IServiceBusiness;
import org.constellation.configuration.ConfigDirectory;
import org.constellation.admin.SpringHelper;
import org.constellation.api.ProviderType;
import org.constellation.configuration.LayerContext;
import org.constellation.provider.DataProviders;
import org.constellation.provider.ProviderFactory;
import org.constellation.test.utils.BasicMultiValueMap;
import org.constellation.test.utils.BasicUriInfo;
import org.constellation.test.utils.SpringTestRunner;
import org.constellation.util.Util;
import org.constellation.wfs.ws.rs.FeatureCollectionWrapper;
import org.constellation.wfs.ws.rs.WFSService;
import org.constellation.ws.rs.WebService;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.util.FileUtilities;
import org.geotoolkit.util.sql.DerbySqlScriptRunner;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.parameter.ParameterValueGroup;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotoolkit.parameter.ParametersExt.createGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateGroup;
import static org.geotoolkit.parameter.ParametersExt.getOrCreateValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.test.context.ActiveProfiles;

// JUnit dependencies

/**
 *
 * @author Guilhem Legal (Geomatys)
 */
@RunWith(SpringTestRunner.class)
@ContextConfiguration("classpath:/cstl/spring/test-derby.xml")
@ActiveProfiles({"standard","derby"})
public class WFSServiceTest implements ApplicationContextAware {

    protected ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Inject
    private IServiceBusiness serviceBusiness;
    
    @Inject
    protected ILayerBusiness layerBusiness;
    
    @Inject
    protected IProviderBusiness providerBusiness;
    
    @Inject
    protected IDataBusiness dataBusiness;
    
    private static WFSService service;

    private static final BasicUriInfo info = new BasicUriInfo(null, null);

    private static final MultivaluedMap<String,String> queryParameters = new BasicMultiValueMap<>();
    private static final MultivaluedMap<String,String> pathParameters = new BasicMultiValueMap<>();

    private static boolean initialized = false;
    
    @PostConstruct
    public void setUpClass() {
        SpringHelper.setApplicationContext(applicationContext);
        if (!initialized) {
            try {
                ConfigDirectory.setupTestEnvironement("WFSServiceTest");

                layerBusiness.removeAll();
                serviceBusiness.deleteAll();
                dataBusiness.deleteAll();
                providerBusiness.removeAll();

                final ProviderFactory featfactory = DataProviders.getInstance().getFactory("feature-store");

                final String url = "jdbc:derby:memory:TestWFSServiceOM";
                final DefaultDataSource ds = new DefaultDataSource(url + ";create=true");
                Connection con = ds.getConnection();
                DerbySqlScriptRunner sr = new DerbySqlScriptRunner(con);
                sr.run(Util.getResourceAsStream("org/constellation/om2/structure_observations.sql"));
                sr.run(Util.getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));
                con.close();
                ds.shutdown();

                final ParameterValueGroup sourceOM = featfactory.getProviderDescriptor().createValue();
                getOrCreateValue(sourceOM, "id").setValue("omSrc");
                getOrCreateValue(sourceOM, "load_all").setValue(true);    

                final ParameterValueGroup choiceOM = getOrCreateGroup(sourceOM, "choice");
                final ParameterValueGroup omconfig = createGroup(choiceOM, " SOSDBParameters");
                getOrCreateValue(omconfig, "sgbdtype").setValue("derby");
                getOrCreateValue(omconfig, "derbyurl").setValue(url);
                
                providerBusiness.storeProvider("omSrc", null, ProviderType.LAYER, "feature-store", sourceOM);
                dataBusiness.create(new QName("http://www.opengis.net/sampling/1.0", "SamplingPoint"), "omSrc", "VECTOR", false, true, null, null);
                
                final LayerContext config = new LayerContext();
                config.getCustomParameters().put("shiroAccessible", "false");
                config.getCustomParameters().put("transactionSecurized", "false");
                config.getCustomParameters().put("transactionnal", "true");

                serviceBusiness.create("wfs", "default", config, null);
                layerBusiness.add("SamplingPoint",       "http://www.opengis.net/sampling/1.0",  "omSrc",      null, "default", "wfs", null);
                DataProviders.getInstance().reload();
                service = new WFSService();

                Field privateStringField = WebService.class.getDeclaredField("uriContext");
                privateStringField.setAccessible(true);
                privateStringField.set(service, info);

                pathParameters.add("serviceId", "default");
                queryParameters.add("serviceId", "default");
                info.setPathParameters(pathParameters);
                info.setQueryParameters(queryParameters);
                initialized = true;
            } catch (Exception ex) {
                Logger.getLogger(WFSServiceTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        ConfigDirectory.shutdownTestEnvironement("WFSServiceTest");
        
        if (service != null) {
            service.destroy();
        }
        File derbyLog = new File("derby.log");
        if (derbyLog.exists()) {
            derbyLog.delete();
        }
    }

    @Test
    public void transactionInsertTest() throws Exception {

        /*
         * we verify that the number of features before insert
         */
        InputStream is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        Response result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        FeatureCollection collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

        /*
         * we insert the feature
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

        /*
         * we delete the features
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.DeleteFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been deleted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(6, collection.size());

        /*
         * we insert the feature with another request
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.InsertFeature2.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        /*
         * we verify that the features has been inserted
         */
        is = new FileInputStream(FileUtilities.getFileFromResource("org.constellation.wfs.request.xml.GetFeature.xml"));
        result = service.doPOSTXml(is);

        assertEquals(Response.Status.OK.getStatusCode(), result.getStatus());

        assertTrue(result.getEntity() instanceof FeatureCollectionWrapper);
        collection = ((FeatureCollectionWrapper) result.getEntity()).getFeatureCollection();
        assertEquals(8, collection.size());

    }
}
