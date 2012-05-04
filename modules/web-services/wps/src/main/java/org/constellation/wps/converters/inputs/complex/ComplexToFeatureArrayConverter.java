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
package org.constellation.wps.converters.inputs.complex;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.opengis.feature.Feature;

/**
 * Implementation of ObjectConverter to convert a complex input into a Feature array.
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ComplexToFeatureArrayConverter extends AbstractInputConverter {

    private static ComplexToFeatureArrayConverter INSTANCE;

    private ComplexToFeatureArrayConverter() {
    }

    public static synchronized ComplexToFeatureArrayConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComplexToFeatureArrayConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? extends Object> getTargetClass() {
        return Feature[].class;
    }
    
    /**
     * {@inheritDoc}
     * @return Feature array.
     */
    @Override
    public Feature[] convert(final Map<String, Object> source) throws NonconvertibleObjectException {

        final List<Object> data = (List<Object>) source.get(IN_DATA);


        XmlFeatureReader fcollReader = null;
        try {
            fcollReader = getFeatureReader(source);
            if (!data.isEmpty()) {

                final List<Feature> features = new ArrayList<Feature>();
                for (int i = 0; i < data.size(); i++) {
                    final Feature f = (Feature) fcollReader.read(data.get(i));
                    features.add((Feature) WPSUtils.fixFeature(f));
                }
                return features.toArray(new Feature[features.size()]);
            } else {
                throw new NonconvertibleObjectException("Invalid data input : Empty Feature list.");
            }

        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Unable to reach the schema url.", ex);
        } catch (IllegalArgumentException ex) {
            throw new NonconvertibleObjectException("Unable to read the feature with the specified schema.", ex);
        } catch (JAXBException ex) {
            throw new NonconvertibleObjectException("Unable to read the feature schema.", ex);
        } catch (CstlServiceException ex) {
            throw new NonconvertibleObjectException("Unable to spread the CRS in feature.", ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        } catch (XMLStreamException ex) {
            throw new NonconvertibleObjectException("Unable to read feature from nodes.", ex);
        } finally {
            if (fcollReader != null) {
                fcollReader.dispose();
            }
        }
    }
}
