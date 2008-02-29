/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.ncml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.NcMLReader;


/**
 * Utilities for parsing NcML files.
 *
 * @source $URL$
 * @author Cédric Briançon
 */
public class NcmlReading {
    /**
     * The {@code namespace} for <netcdf> tags.
     */
    public static final Namespace NETCDFNS = Namespace.getNamespace(
            "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");

    /**
     * Returns a list of {@linkplain ucar.nc2.ncml.Aggregation Aggregation}, which should be a
     * list of tags containing one or several <netcdf> tags. If these tags were not found, or
     * an error during the parsing of the NcML file has occured, it will returns {@code null}.
     */
    public static List<Aggregation> getNestedAggregations(final File ncml) throws IOException {
        try {
            final List<Aggregation> aggregations = new ArrayList<Aggregation>();
            final String  ncmlPath     = ncml.getAbsolutePath();
            final Element globalNetcdf = getGlobalNetcdfElement(ncml);
            final Element globalAggr   = globalNetcdf.getChild("aggregation", NETCDFNS);
            if (globalAggr.getChild("netcdf", NETCDFNS).getAttribute("location") == null) {
                @SuppressWarnings("unchecked")
                final Collection<Element> children = globalAggr.getChildren("netcdf", NETCDFNS);
                for (final Element element : children) {
                    final NetcdfDataset netcdfData = NcMLReader.readNcML(ncmlPath, element, null);
                    aggregations.add(netcdfData.getAggregation());
                }
            } else {
                final NetcdfDataset netcdfData = NcMLReader.readNcML(ncmlPath, globalNetcdf, null);
                aggregations.add(netcdfData.getAggregation());
            }
            return aggregations;
        }  catch (JDOMException ex) {
            throw new IOException();
        }
    }

    /**
     * Returns a list of {@code JDOM} elements, matching with the whole children <netcdf>
     * of the main aggregation.
     *
     * @param ncml The NcML file to read.
     * @throws IOException when an error of reading NcML file has occured.
     */
    public static List<Element> getNestedNetcdfElement(final File ncml) throws IOException {
        try {
            final List<Element> elements = new ArrayList<Element>();
            final Element globalNetcdf = getGlobalNetcdfElement(ncml);
            final Element globalAggr = globalNetcdf.getChild("aggregation", NETCDFNS);
            if (globalAggr.getChild("netcdf", NETCDFNS).getAttribute("location") == null) {
                @SuppressWarnings("unchecked")
                final Collection<Element> children = globalAggr.getChildren("netcdf", NETCDFNS);
                for (final Element mainNetcdf : children) {
                    @SuppressWarnings("unchecked")
                    final Collection<Element> c2 = mainNetcdf.getChild("aggregation", NETCDFNS).getChildren();
                    for (final Element element : c2) {
                        elements.add(element);
                    }
                }
            }
            return elements;
        } catch (JDOMException ex) {
            throw new IOException("An error to create the JDOM Document has occured.");
        }
    }

    /**
     * Returns the XML tags {@code <netcdf>} with its content for the NcML file specified.
     * This tags has to contain the whole data of the NcML file dedicated to specified
     * meta data for NetCDF files.
     * An NcML file can contains several <netcdf> tags, this method will return the first
     * occurrence.
     *
     * @param  ncml The NcML file to read.
     * @return The XML node {@code <netcdf>}.
     * @throws JDOMException if the getting of this tags has failed.
     * @throws IOException if the creation of this document has failed.
     */
    private static Element getGlobalNetcdfElement(final File ncml) throws IOException, JDOMException {
        final SAXBuilder builder = new SAXBuilder();
        final Document doc = builder.build(ncml);
        final Element root = doc.getRootElement();
        if (root.getName().equals("netcdf")) {
            return root;
        } else {
            Element elemDataset = root.getChild("dataset", root.getNamespace());
            return elemDataset.getChild("netcdf", NETCDFNS);
        }
    }

    /**
     *
     * @param variable
     * @param netcdfTags
     * @return
     */
    public static Element getVariableElement(final String variable, final Element netcdfTags) {
        final Collection<Element> children = netcdfTags.getChildren("variable", NETCDFNS);
        for (final Element varNcml : children) {
            if (variable.startsWith(varNcml.getAttributeValue("name").toLowerCase())) {
                return varNcml;
            }
        }
        return null;
    }
}
