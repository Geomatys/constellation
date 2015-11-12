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
/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2010, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.constellation.data.om2;

import com.vividsolutions.jts.geom.Geometry;
import org.apache.sis.geometry.GeneralEnvelope;
import org.geotoolkit.data.AbstractReadingTests;
import org.geotoolkit.data.FeatureStore;
import org.geotoolkit.data.FeatureStoreFinder;
import org.geotoolkit.feature.FeatureTypeBuilder;
import org.geotoolkit.internal.sql.DefaultDataSource;
import org.geotoolkit.internal.sql.ScriptRunner;
import org.geotoolkit.referencing.CRS;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geotoolkit.util.NamesExt;
import org.opengis.util.GenericName;


/**
 *
 * @author Guilhem Legal (Geomatys)
 * @module pending
 */
public class OM2DataStoreTest extends AbstractReadingTests{

    private static DefaultDataSource ds;
    private static FeatureStore store;
    private static Set<GenericName> names = new HashSet<>();
    private static List<ExpectedResult> expecteds = new ArrayList<>();
    static{
        try{
            final String url = "jdbc:derby:memory:TestOM2;create=true";
            ds = new DefaultDataSource(url);

            Connection con = ds.getConnection();

            final ScriptRunner exec = new ScriptRunner(con);
            exec.run(getResourceAsStream("org/constellation/om2/structure_observations.sql"));
            exec.run(getResourceAsStream("org/constellation/sql/sos-data-om2.sql"));

            final Map params = new HashMap<>();
            params.put("dbtype", "OM2");
            params.put(OM2FeatureStoreFactory.SGBDTYPE.getName().toString(), "derby");
            params.put(OM2FeatureStoreFactory.DERBYURL.getName().toString(), url);

            store = FeatureStoreFinder.open(params);

            final String nsCstl = "http://constellation.org/om2";
            final String nsGML = "http://www.opengis.net/gml";
            final GenericName name = NamesExt.create(nsCstl, "Sensor");
            names.add(name);

            final FeatureTypeBuilder featureTypeBuilder = new FeatureTypeBuilder();
            featureTypeBuilder.setName(name);
            featureTypeBuilder.add(NamesExt.create(nsCstl, "id"),String.class,1,1,false,null);
            featureTypeBuilder.add(NamesExt.create(nsCstl, "position"),Geometry.class,1,1,false,null);
            featureTypeBuilder.setDefaultGeometry(NamesExt.create(nsCstl, "position"));

            int size = 10;
            GeneralEnvelope env = new GeneralEnvelope(CRS.decode("EPSG:27582"));
            env.setRange(0, 65400, 65400);
            env.setRange(1, 1731368, 1731368);

            final ExpectedResult res = new ExpectedResult(name,
                    featureTypeBuilder.buildFeatureType(), size, env);
            expecteds.add(res);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }


    @Override
    protected FeatureStore getDataStore() {
        return store;
    }

    @Override
    protected Set<GenericName> getExpectedNames() {
        return names;
    }

    @Override
    protected List<ExpectedResult> getReaderTests() {
        return expecteds;
    }

    public static InputStream getResourceAsStream(final String url) {
        final ClassLoader cl = getContextClassLoader();
        return cl.getResourceAsStream(url);
    }
    
    public static ClassLoader getContextClassLoader() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }
}
