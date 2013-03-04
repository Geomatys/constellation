/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2011, Geomatys
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

package org.constellation.provider.configuration;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Writer;
import javax.xml.transform.Result;
import java.io.OutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collections;
import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import javanet.staxutils.IndentingXMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import org.geotoolkit.parameter.DefaultParameterDescriptor;
import org.geotoolkit.parameter.DefaultParameterDescriptorGroup;
import org.apache.sis.util.ArgumentChecks;
import org.geotoolkit.xml.parameter.ParameterValueReader;
import org.geotoolkit.xml.parameter.ParameterValueWriter;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterValueGroup;

import static org.geotoolkit.parameter.Parameters.*;

/**
 * General parameters for provider configuration files.
 *
 * @author Johann Sorel (Geomatys)
 */
public final class ProviderParameters {

    public static final String CONFIG_DESCRIPTOR_NAME = "config";

    ////////////////////////////////////////////////////////////////////////////
    // Source parameters ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final String SOURCE_DESCRIPTOR_NAME = "source";
    public static final ParameterDescriptor<String> SOURCE_ID_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("id","source id",String.class,null,true);
    public static final ParameterDescriptor<Boolean> SOURCE_LOADALL_DESCRIPTOR =
             new DefaultParameterDescriptor<Boolean>("load_all","source load all datas",Boolean.class,true,true);


    ////////////////////////////////////////////////////////////////////////////
    // Source layer parameters /////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    public static final ParameterDescriptor<String> LAYER_NAME_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("name","layer name",String.class,null,true);
    public static final ParameterDescriptor<String> LAYER_STYLE_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("style","layer styles",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_DATE_START_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("periode_start","layer date start field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_DATE_END_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("periode_end","layer date end field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_START_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("elevation_start","layer elevation start field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_END_FIELD_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("elevation_end","layer elevation end field",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_ELEVATION_MODEL_DESCRIPTOR =
             new DefaultParameterDescriptor<String>("elevation_model","layer elevation model",String.class,null,false);
    public static final ParameterDescriptor<Boolean> LAYER_IS_ELEVATION_MODEL_DESCRIPTOR =
             new DefaultParameterDescriptor<Boolean>("is_elevation_model","layer is elevation model",Boolean.class,false,false);
    public static final ParameterDescriptor<String> LAYER_QUERY_LANGUAGE =
             new DefaultParameterDescriptor<String>("language","layer query language",String.class,null,false);
    public static final ParameterDescriptor<String> LAYER_QUERY_STATEMENT =
             new DefaultParameterDescriptor<String>("statement","layer query statement",String.class,null,false);
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
     * Config
     *  -> Source
     *    -> sourceConfig
     *    -> layers
     */
    public static ParameterDescriptorGroup createDescriptor(final GeneralParameterDescriptor sourceConfigDescriptor){
        final ParameterDescriptorGroup sourceDescriptor = new DefaultParameterDescriptorGroup(
            Collections.singletonMap("name", SOURCE_DESCRIPTOR_NAME),
            0, Integer.MAX_VALUE,SOURCE_ID_DESCRIPTOR,SOURCE_LOADALL_DESCRIPTOR,
            sourceConfigDescriptor,LAYER_DESCRIPTOR);
        final ParameterDescriptorGroup configDescriptor =
            new DefaultParameterDescriptorGroup(CONFIG_DESCRIPTOR_NAME,sourceDescriptor);

        return configDescriptor;
    }

    public static ParameterValueGroup read(final Object input,
            final ParameterDescriptorGroup desc) throws IOException, XMLStreamException{
        final ParameterValueReader reader = new ParameterValueReader(desc);
        reader.setInput(input);
        return (ParameterValueGroup) reader.read();
    }

    public static void write(final Object output,
            final ParameterValueGroup parameters) throws IOException, XMLStreamException{
        final ParameterValueWriter writer = new ParameterValueWriter();
        writer.setOutput(toWriter(output));
        writer.write(parameters);
    }

    public static String getSourceId(final ParameterValueGroup source){
        return stringValue(SOURCE_ID_DESCRIPTOR,source);
    }

    public static boolean isLoadAll(final ParameterValueGroup source){
        final Boolean val = (Boolean)value(SOURCE_LOADALL_DESCRIPTOR,source);
        return val == null || val;
    }

    public static List<ParameterValueGroup> getLayers(final ParameterValueGroup source){
        final List<ParameterValueGroup> allLayers = source.groups(LAYER_DESCRIPTOR.getName().getCode());
        final List<ParameterValueGroup> queryLayers = getQueryLayers(source);

        final List<ParameterValueGroup> normalLayers = new ArrayList<ParameterValueGroup>(allLayers);
        normalLayers.removeAll(queryLayers);

        return normalLayers;
    }

    public static List<ParameterValueGroup> getQueryLayers(final ParameterValueGroup source){
        final List<ParameterValueGroup> allLayers = source.groups(LAYER_DESCRIPTOR.getName().getCode());
        final List<ParameterValueGroup> queryLayers = new ArrayList<ParameterValueGroup>();

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

    /**
     * Create writer with indentation.
     */
    private static XMLStreamWriter toWriter(final Object output)
            throws XMLStreamException{
        final XMLOutputFactory XMLfactory = XMLOutputFactory.newInstance();
        XMLfactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        XMLStreamWriter writer;
        if(output instanceof File){
            try {
                writer = XMLfactory.createXMLStreamWriter(new FileOutputStream((File) output));
            } catch (FileNotFoundException ex) {
                throw new XMLStreamException(ex.getLocalizedMessage(), ex);
            }
        }else if(output instanceof OutputStream){
            writer = XMLfactory.createXMLStreamWriter((OutputStream)output);
        }else if(output instanceof Result){
            writer = XMLfactory.createXMLStreamWriter((Result)output);
        }else if(output instanceof Writer){
            writer = XMLfactory.createXMLStreamWriter((Writer)output);
        }else{
            throw new XMLStreamException("Output type is not supported : "+ output);
        }

        return new IndentingXMLStreamWriter(writer);
    }

}
