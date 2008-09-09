/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2008, Geomatys
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
package org.constellation.console;

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
final class NcmlReading {
    /**
     * The {@code namespace} for &lt;netcdf> tag.
     */
    public static final Namespace NETCDFNS = Namespace.getNamespace(
            "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");

    /**
     * Gets the {@code start, increment and npts} parameters found in the NcML file for
     * the {@code netcdf} tag.
     *
     * @param timeElement
     * @return
     */
    public static NcmlTimeValues createNcmlTimeValues(final Element timeElement) {
        final Element timeValues = timeElement.getChild("values", NETCDFNS);
        final long startTime =
                Math.round(Double.valueOf(timeValues.getAttributeValue("start")));
        final long increment =
                Math.round(Double.valueOf(timeValues.getAttributeValue("increment")));
        final int npts = Integer.valueOf(timeValues.getAttributeValue("npts"));
        return new NcmlTimeValues(startTime, increment, npts);
    }

    /**
     * Returns a list of {@linkplain ucar.nc2.ncml.Aggregation Aggregation}, which should be a
     * list of tags containing one or several &lt;netcdf> tags. If these tags were not found, or
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
     * Returns a list of {@code JDOM} elements, matching with the whole children &lt;netcdf>
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
            } else {
                List<Element> children = globalAggr.getChildren("netcdf", NETCDFNS);
                for (final Element elem : children) {
                    elements.add(elem);
                }
            }
            return elements;
        } catch (JDOMException ex) {
            throw new IOException("An error to create the JDOM Document has occured.");
        }
    }

    /**
     * Returns the XML tag {@code &lt;netcdf>} with its content for the NcML file specified.
     * This tag has to contain the whole data of the NcML file dedicated to specified
     * meta data for NetCDF files.
     * An NcML file can contains several &lt;netcdf> tag, this method will return the first
     * occurrence.
     *
     * @param  ncml The NcML file to read.
     * @return The XML node {@code &lt;netcdf>}.
     * @throws JDOMException if the getting of this tag has failed.
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
     * Returns the {@code &lt;variable>} tag, which is a child of a {@code &lt;netcdf>} tag in
     * parameter, and has a variable name specified or its only variable is {@code time}, or
     * {@code null} if no tag matches.
     *
     * @param variable The variable to check in the {@code &lt;netcdf>} tag.
     * @param netcdfTag The parent {@code &lt;netcdf>} tag.
     * @return {@code null} if no tag matches. Otherwise the {@code &lt;variable> tag} matching
     *         with the variable's name specified.
     */
    public static Element getVariableElement(final String variable, final Element netcdfTag) {
        final Collection<Element> variables = netcdfTag.getChildren("variable", NETCDFNS);
        for (final Element varNcml : variables) {
            // If the only variable for this netcdf tag is {@code time}, then we select this tag
            // as valid.
            if (varNcml.getAttributeValue("name").equalsIgnoreCase("time") && 
                    variables.size() == 1) 
            {
                return varNcml;
            }
            // If there are several variables and the variable specified by the user is found among
            // them, then we can return this tag.
            if (variable.startsWith(varNcml.getAttributeValue("name").toLowerCase())) {
                return varNcml;
            }
        }
        return null;
    }
}
