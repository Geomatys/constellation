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

import org.apache.sis.parameter.DefaultParameterDescriptorGroup;
import org.apache.sis.util.ArgumentChecks;
import org.constellation.provider.DataProvider;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.geotoolkit.parameter.Parameters.value;

/**
 * General parameters for provider configuration files.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ProviderParameters {

    ////////////////////////////////////////////////////////////////////////////
    // Source parameters ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final String SOURCE_DESCRIPTOR_NAME = "source";
    //TODO remove this parameter, duplicates argument in factory storeProvider method.
    /** /!\ NO ! Keep ALL the arguments, because we need them for old configuration import.
     */
    public static final ParameterDescriptor<String> SOURCE_ID_DESCRIPTOR =
             new DefaultParameterDescriptor<>("id","source id",String.class,null,true);
    public static final ParameterDescriptor<Boolean> SOURCE_LOADALL_DESCRIPTOR =
             new DefaultParameterDescriptor<>("load_all","source load all datas",Boolean.class,true,true);
    public static final ParameterDescriptor<Date> SOURCE_DATE_DESCRIPTOR =
             new DefaultParameterDescriptor<>("date","source creation date",Date.class,null,false);
    public static final ParameterDescriptor<String> SOURCE_TYPE_DESCRIPTOR =
            new DefaultParameterDescriptor<>("providerType","provider type",String.class,null,false);


    ////////////////////////////////////////////////////////////////////////////
    // Source layer parameters /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final ParameterDescriptor<String> LAYER_NAME_DESCRIPTOR =
             new DefaultParameterDescriptor<>("name","layer name",String.class,null,true);
    public static final ParameterDescriptor<String> LAYER_STYLE_DESCRIPTOR =
             new DefaultParameterDescriptor<>("style","layer styles",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_DATE_START_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<>("periode_start","layer date start field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_DATE_END_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<>("periode_end","layer date end field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_START_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<>("elevation_start","layer elevation start field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_END_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<>("elevation_end","layer elevation end field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_MODEL_DESCRIPTOR =
             new DefaultParameterDescriptor<>("elevation_model","layer elevation model",String.class,null,false);
    public static final ParameterDescriptor<Boolean> LAYER_IS_ELEVATION_MODEL_DESCRIPTOR =
             new DefaultParameterDescriptor<>("is_elevation_model","layer is elevation model",Boolean.class,false,false);
    public static final ParameterDescriptor<String> LAYER_QUERY_LANGUAGE =
             new DefaultParameterDescriptor<>("language","layer query language",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_QUERY_STATEMENT =
             new DefaultParameterDescriptor<>("statement","layer query statement",String.class,null,false);
    public static final ParameterDescriptorGroup LAYER_DESCRIPTOR =
            new DefaultParameterDescriptorGroup(
                Collections.singletonMap("name", "Layer"),
                0, Integer.MAX_VALUE,
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
        final ParameterDescriptorGroup sourceDescriptor = new org.apache.sis.parameter.DefaultParameterDescriptorGroup(
                Collections.singletonMap("name", SOURCE_DESCRIPTOR_NAME),1, Integer.MAX_VALUE,
                SOURCE_ID_DESCRIPTOR,SOURCE_LOADALL_DESCRIPTOR,SOURCE_DATE_DESCRIPTOR,SOURCE_TYPE_DESCRIPTOR,
                 sourceConfigDescriptor,LAYER_DESCRIPTOR);
        return sourceDescriptor;
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
