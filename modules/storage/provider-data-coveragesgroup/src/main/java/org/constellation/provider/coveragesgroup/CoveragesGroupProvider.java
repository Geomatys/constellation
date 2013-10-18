/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2012, Geomatys
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
package org.constellation.provider.coveragesgroup;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import org.geotoolkit.feature.DefaultName;
import org.geotoolkit.map.MapContext;
import org.geotoolkit.util.FileUtilities;

import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.constellation.provider.coveragesgroup.util.MapContextIO;
import org.constellation.admin.dao.DataRecord.DataType;

import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.*;

/**
 *
 * @author Cédric Briançon (Geomatys)
 * @author Quentin Boileau (Geomatys)
 */
public class CoveragesGroupProvider extends AbstractLayerProvider {

    public static final String KEY_PATH = "path";

    private Map<Name,File> index = null;
    private boolean visited;
    private File path;

    public CoveragesGroupProvider(final ProviderService service, final ParameterValueGroup param) {
        super(service,param);
        this.visited = false;
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
    public LayerDetails get(final Name key) {
        return get(key, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayerDetails get(final Name key, Date version) {
        return get(key, null, null);
    }

    /**
     * hacked method to pass the login/pass to WebMapServer
     */
    public LayerDetails get(final Name key, final String login, final String password) {
        if (index == null) {
            visit();
        }
        final File mapContextFile = index.get(key);
        if (mapContextFile != null) {
            return new CoveragesGroupLayerDetails(key, mapContextFile, login, password);
        }
        return null;
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
            index.clear();
            visit();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void dispose() {
        synchronized(this){
            index.clear();
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
                final MapContext mapContext = MapContextIO.readMapContextFile(candidate, "", "");
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
