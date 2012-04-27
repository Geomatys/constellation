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
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.constellation.wps.converters.inputs.AbstractInputConverter;
import org.constellation.wps.utils.WPSUtils;
import org.constellation.ws.CstlServiceException;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.xml.XmlFeatureReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;

/**
 * Implementation of ObjectConverter to convert a complex input into a FeatureCollection. 
 *
 * @author Quentin Boileau (Geomatys).
 */
public final class ComplexToFeatureCollectionConverter extends AbstractInputConverter {

    private static ComplexToFeatureCollectionConverter INSTANCE;

    private ComplexToFeatureCollectionConverter() {
    }

    public static synchronized ComplexToFeatureCollectionConverter getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ComplexToFeatureCollectionConverter();
        }
        return INSTANCE;
    }

    @Override
    public FeatureCollection convert(Map<String, Object> source) throws NonconvertibleObjectException {

        final List<Object> data = (List<Object>) source.get(IN_DATA);

        if (data.size() > 1) {
            throw new NonconvertibleObjectException("Invalid data input : Only one Feature/FeatureCollection expected.");
        }
        FeatureCollection extractData = null;

        //Read featureCollection
        XmlFeatureReader fcollReader = null;
        try {
            
            fcollReader = getFeatureReader(source);
            extractData = (FeatureCollection) fcollReader.read(data.get(0));
            return (FeatureCollection) WPSUtils.fixFeature( extractData);

        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Unable to reach the schema url.", ex);
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