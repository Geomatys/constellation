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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.constellation.provider.AbstractLayerProvider;
import org.constellation.provider.LayerDetails;
import org.constellation.provider.ProviderService;
import org.opengis.feature.type.Name;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

import static org.constellation.provider.coveragesgroup.CoveragesGroupProviderService.*;
import org.geotoolkit.feature.DefaultName;

/**
 *
 * @author Cédric Briançon
 */
public class CoveragesGroupProvider extends AbstractLayerProvider {

    public static final String KEY_FOLDER_PATH = "path";

    private final Map<Name,File> index = new HashMap<Name,File>();

    private File folder;

    public CoveragesGroupProvider(final ProviderService service, final ParameterValueGroup param) {
        super(service,param);
        visit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Name> getKeys() {
        return index.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayerDetails get(final Name key) {
        final File mapContextFile = index.get(key);
        return new CoveragesGroupLayerDetails(key, mapContextFile);
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
        final ParameterValue<String> param = (ParameterValue<String>) getSourceConfiguration(getSource()).parameter(FOLDER_DESCRIPTOR.getName().getCode());

        if(param == null){
            getLogger().log(Level.WARNING,"Provided File path is not defined.");
            return;
        }

        final String path = param.getValue();

        if (path == null) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

        folder = new File(path);

        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            getLogger().log(Level.WARNING,"Provided File does not exits or is not a folder.");
            return;
        }

        visit(folder);
        super.visit();
    }

    /**
     * Visit all files and directories contained in the directory specified, and add
     * all XML files in {@link #index}.
     *
     * @param file The starting file or folder.
     */
    private void visit(final File file) {

        if (file.isDirectory()) {
            final File[] list = file.listFiles();
            if (list != null) {
                for (int i = 0; i < list.length; i++) {
                    visit(list[i]);
                }
            }
        } else {
            test(file);
        }
    }

    /**
     * Keep only XML files.
     *
     * @param candidate Candidate to be a map context file.
     */
    private void test(final File candidate) {
        final String fullName = candidate.getName();
        final int idx = fullName.lastIndexOf('.');
        final String extension = fullName.substring(idx + 1);
        if (extension.equalsIgnoreCase("xml")) {
            final String name = fullName.substring(0, idx);
            index.put(new DefaultName(name), candidate);
        }
    }
}
