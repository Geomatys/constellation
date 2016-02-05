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

package org.constellation.provider.configuration;

import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.util.ArgumentChecks;
import org.constellation.provider.DataProvider;
import org.opengis.parameter.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.geotoolkit.parameter.Parameters.value;

/**
 * General parameters for provider configuration files.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ProviderParameters {

    private static final ParameterBuilder BUILDER = new ParameterBuilder();

    ////////////////////////////////////////////////////////////////////////////
    // Source parameters ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final String SOURCE_DESCRIPTOR_NAME = "source";
    //TODO remove this parameter, duplicates argument in factory storeProvider method.
    /** /!\ NO ! Keep ALL the arguments, because we need them for old configuration import.
     */
    public static final ParameterDescriptor<String> SOURCE_ID_DESCRIPTOR =
            BUILDER.addName("id").setRemarks("source id").setRequired(true).create(String.class, null);
    public static final ParameterDescriptor<Boolean> SOURCE_LOADALL_DESCRIPTOR =
            BUILDER.addName("load_all").setRemarks("source load all datas").setRequired(true).create(Boolean.class, Boolean.TRUE);
    public static final ParameterDescriptor<Date> SOURCE_DATE_DESCRIPTOR =
            BUILDER.addName("date").setRemarks("source creation date").setRequired(false).create(Date.class, null);
    public static final ParameterDescriptor<String> SOURCE_TYPE_DESCRIPTOR =
            BUILDER.addName("providerType").setRemarks("provider type").setRequired(false).create(String.class, null);

    public static final ParameterDescriptor<Boolean> SOURCE_CREATEDATASET_DESCRIPTOR =
            BUILDER.addName("create_dataset").setRemarks("optional internal parameters").setRequired(false).create(Boolean.class, null);

    ////////////////////////////////////////////////////////////////////////////
    // Source layer parameters /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final ParameterDescriptor<String> LAYER_NAME_DESCRIPTOR =
            BUILDER.addName("name").setRemarks("layer name").setRequired(true).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_STYLE_DESCRIPTOR =
            BUILDER.addName("style").setRemarks("layer styles").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_DATE_START_FIELD_DESCRIPTOR =
            BUILDER.addName("periode_start").setRemarks("layer date start field").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_DATE_END_FIELD_DESCRIPTOR =
            BUILDER.addName("periode_end").setRemarks("layer date end field").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_START_FIELD_DESCRIPTOR =
            BUILDER.addName("elevation_start").setRemarks("layer elevation start field").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_END_FIELD_DESCRIPTOR =
            BUILDER.addName("elevation_end").setRemarks("layer elevation end field").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_MODEL_DESCRIPTOR =
            BUILDER.addName("elevation_model").setRemarks("layer elevation model").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<Boolean> LAYER_IS_ELEVATION_MODEL_DESCRIPTOR =
            BUILDER.addName("is_elevation_model").setRemarks("layer is elevation model").setRequired(false).create(Boolean.class, Boolean.FALSE);
    public static final ParameterDescriptor<String> LAYER_QUERY_LANGUAGE =
            BUILDER.addName("language").setRemarks("layer query language").setRequired(false).create(String.class, null);
    public static final ParameterDescriptor<String> LAYER_QUERY_STATEMENT =
            BUILDER.addName("statement").setRemarks("layer query statement").setRequired(false).create(String.class, null);
    public static final ParameterDescriptorGroup LAYER_DESCRIPTOR =  BUILDER.addName("Layer")
                    .createGroup(0, Integer.MAX_VALUE,
                            LAYER_NAME_DESCRIPTOR,
                            LAYER_STYLE_DESCRIPTOR,
                            LAYER_DATE_START_FIELD_DESCRIPTOR,
                            LAYER_DATE_END_FIELD_DESCRIPTOR,
                            LAYER_ELEVATION_START_FIELD_DESCRIPTOR,
                            LAYER_ELEVATION_END_FIELD_DESCRIPTOR,
                            LAYER_ELEVATION_MODEL_DESCRIPTOR,
                            LAYER_IS_ELEVATION_MODEL_DESCRIPTOR,
                            LAYER_QUERY_LANGUAGE,
                            LAYER_QUERY_STATEMENT);


    private ProviderParameters(){}

    /**
     * Create a descriptor composed of the given source configuration.
     * Source
     *  -> config
     *  -> layers
     */
    public static ParameterDescriptorGroup createDescriptor(final GeneralParameterDescriptor sourceConfigDescriptor){
        return BUILDER.addName(SOURCE_DESCRIPTOR_NAME).setRequired(true).createGroup(1, Integer.MAX_VALUE,
                SOURCE_ID_DESCRIPTOR,SOURCE_LOADALL_DESCRIPTOR,SOURCE_DATE_DESCRIPTOR,SOURCE_TYPE_DESCRIPTOR,
                SOURCE_CREATEDATASET_DESCRIPTOR, sourceConfigDescriptor,LAYER_DESCRIPTOR);
    }

    public static boolean isLoadAll(final ParameterValueGroup source){
        final Boolean val = (Boolean)value(SOURCE_LOADALL_DESCRIPTOR,source);
        return val == null || val;
    }

    public static List<ParameterValueGroup> getLayers(final ParameterValueGroup source){
        final List<ParameterValueGroup> allLayers = source.groups(LAYER_DESCRIPTOR.getName().getCode());
        final List<ParameterValueGroup> queryLayers = getQueryLayers(source);

        final List<ParameterValueGroup> normalLayers = new ArrayList<>(allLayers);
        normalLayers.removeAll(queryLayers);

        return normalLayers;
    }

    public static List<ParameterValueGroup> getQueryLayers(final ParameterValueGroup source){
        final List<ParameterValueGroup> allLayers = source.groups(LAYER_DESCRIPTOR.getName().getCode());
        final List<ParameterValueGroup> queryLayers = new ArrayList<>();

        for(final ParameterValueGroup candidate : allLayers){
            final String language = value(LAYER_QUERY_LANGUAGE, candidate);
            final String statement = value(LAYER_QUERY_STATEMENT, candidate);

            if(language != null && statement != null){
                queryLayers.add(candidate);
            }
        }

        return queryLayers;
    }

    public static ParameterValueGroup getLayer(final ParameterValueGroup source, final String name){
        final List<ParameterValueGroup> allLayers = source.groups(LAYER_DESCRIPTOR.getName().getCode());

        for(final ParameterValueGroup candidate : allLayers){
            final String layerName = value(LAYER_NAME_DESCRIPTOR, candidate);

            if(name.equals(layerName)){
                return candidate;
            }
        }

        return null;
    }

    public static boolean containLayer(final ParameterValueGroup source, final String name){
        return getLayer(source, name) != null;
    }

    public static ParameterValueGroup getSourceConfiguration(
            final ParameterValueGroup group,final ParameterDescriptorGroup desc){
        final List<ParameterValueGroup> groups = group.groups(desc.getName().getCode());
        if(!groups.isEmpty()){
            return groups.get(0);
        }else{
            return null;
        }
    }

    public static String getNamespace(final DataProvider provider) {
        ParameterValueGroup group = provider.getSource();

        // Get choice if exists.
        try {
            group = group.groups("choice").get(0);
        } catch (ParameterNotFoundException ignore) {
        }

        // Get provider type configuration.
        final List<GeneralParameterValue> values = group.values();
        for (final GeneralParameterValue value : values) {
            if (value instanceof ParameterValueGroup) {
                group = (ParameterValueGroup) value;
            }
        }

        // Get namespace.
        try {
            final String namespace = group.parameter("namespace").stringValue();
            return "no namespace".equals(namespace) ? null : namespace;
        } catch (ParameterNotFoundException | IllegalStateException ignore) {
        }

        // Return default.
        return "http://geotoolkit.org"; // return default
    }

    public static List<ParameterValueGroup> getSources(final ParameterValueGroup config){
        return config.groups(SOURCE_DESCRIPTOR_NAME);
    }

    public static ParameterValueGroup getOrCreate(final ParameterDescriptorGroup desc,
            final ParameterValueGroup parent){

        ArgumentChecks.ensureBetween("descriptor occurences", 0, 1, desc.getMaximumOccurs());

        final String code = desc.getName().getCode();
        final List<ParameterValueGroup> candidates = parent.groups(desc.getName().getCode());

        if(candidates.isEmpty()){
            return parent.addGroup(code);
        }else{
            return candidates.get(0);
        }

    }
}
