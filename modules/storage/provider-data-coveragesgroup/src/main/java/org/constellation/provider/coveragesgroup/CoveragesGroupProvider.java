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
package org.constellation.provider.coveragesgroup;

import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import org.apache.sis.storage.DataStore;
import org.constellation.admin.SpringHelper;
import org.constellation.admin.StyleBusiness;
import org.constellation.api.DataType;
import org.constellation.provider.AbstractDataProvider;
import org.constellation.provider.Data;
import org.constellation.provider.ProviderFactory;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.SOURCE_CONFIG_DESCRIPTOR;
import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.URL;
import org.constellation.provider.coveragesgroup.util.MapContextIO;
import org.geotoolkit.feature.type.DefaultName;
import org.geotoolkit.feature.type.Name;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.util.FileUtilities;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupProvider extends AbstractDataProvider {

    public static final String KEY_PATH = "path";

    private Map<Name,File> index = null;
    private boolean visited;
    private File path;

    @Inject
    private StyleBusiness styleBusiness;
    
    public CoveragesGroupProvider(String providerId, final ProviderFactory service, final ParameterValueGroup param) {
        super(providerId, service,param);
        this.visited = false;
        SpringHelper.injectDependencies(this);
    }

    @Override
    public DataStore getMainStore() {
        //throw new UnsupportedOperationException("Not supported yet.");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Name> getKeys() {
        if (index == null) {
            visit();
        }
        return index.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Data get(final Name key, Date version) {
        return get(key, null, null);
    }

    /**
     * hacked method to pass the login/pass to WebMapServer
     */
    public Data get(final Name key, final String login, final String password) {
        if (index == null) {
            visit();
        }
        final File mapContextFile = index.get(key);
        if (mapContextFile != null) {
            return new CoveragesGroupLayerDetails(key, mapContextFile, login, password, styleBusiness);
        }
        return null;
    }

    /**
     *
     * @param key layer name
     * @param login used if one or more layer of MapContext come from secured map service
     * @param password used if one or more layer of MapContext come from secured map service
     * @return MapContext or null if layer name doesn't exist.
     * @throws JAXBException
     */
    public MapContext getMapContext(final Name key, final String login, final String password) throws JAXBException {
        if (index == null) {
            visit();
        }

        final File mapContextFile = index.get(key);
        return MapContextIO.readMapContextFile(mapContextFile, login, password, styleBusiness);
    }

    private static ParameterValueGroup getSourceConfiguration(final ParameterValueGroup params){
        final List<ParameterValueGroup> groups = params.groups(SOURCE_CONFIG_DESCRIPTOR.getName().getCode());
        if(!groups.isEmpty()){
            return groups.get(0);
        }
        return null;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void reload() {
        synchronized(this){
            if (index != null) {
                index.clear();
            }
            visit();
        }
        fireUpdateEvent();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            if (index != null) {
                index.clear();
            }
        }
    }

    /**
     * Visit all files to keep only XML files.
     */
    @Override
    protected void visit() {
        final ParameterValue<URL> paramUrl = (ParameterValue<URL>) getSourceConfiguration(getSource()).parameter(URL.getName().getCode());

        if(paramUrl == null || paramUrl.getValue() == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final URL urlPath = paramUrl.getValue();

        try {
            path = new File(urlPath.toURI());
        } catch (URISyntaxException e) {
            getLogger().log(Level.INFO,"Fails to convert path url to file.");
            path = new File(urlPath.getPath());
        }

        List<File> candidates = new ArrayList<File>() ;

        if (path.isDirectory()) {
            candidates.addAll(FileUtilities.scanDirectory(path, new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    final String fullName = pathname.getName();
                    final int idx = fullName.lastIndexOf('.');
                    final String extension = fullName.substring(idx + 1);
                    return extension.equalsIgnoreCase("xml");
                }
            }));
        } else {
            candidates.add(path);
        }

        index = new HashMap<Name, File>();
        for (final File candidate : candidates) {
            try {
                final MapContext mapContext = MapContextIO.readMapContextFile(candidate, "", "", styleBusiness);
                if (mapContext != null) {
                    final DefaultName name = new DefaultName(mapContext.getName());
                    index.put(name, candidate);
                }
            } catch (JAXBException e) {
                getLogger().log(Level.WARNING, "Candidate MapContext file can't be read : "+candidate.getAbsolutePath(), e);
            }
        }

        super.visit();
    }

    @Override
    public DataType getDataType() {
        return DataType.COVERAGE;
    }

}
