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
package org.constellation.wps.converters;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.geotoolkit.coverage.io.CoverageIO;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;



/**
 * Implementation of ObjectConverter to convert a reference into a GridCoverageReader.
 * Reference is define by a <code>Map<String,String></code> with entries keys :
 * <ul>
 * <li>href : Url to the data</li>
 * <li>mime : mime type of the data like text/xml, ...</li>
 * <li>schema : is the data requires a schema</li>
 * <li>encoding : the data encoding like UTF8, ...</li>
 * <li>method : GET or POST</li>
 * </ul>
 * @author Quentin Boileau
 */
public final class ReferenceToGridCoverageReaderConverter extends SimpleConverter<Map<String,String>, GridCoverageReader> {

    private static ReferenceToGridCoverageReaderConverter INSTANCE;

    private ReferenceToGridCoverageReaderConverter(){
    }

    public static synchronized ReferenceToGridCoverageReaderConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToGridCoverageReaderConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends GridCoverageReader> getTargetClass() {
        return GridCoverageReader.class ;
    }
 
    @Override
    public GridCoverageReader convert(Map<String,String> source) throws NonconvertibleObjectException {
                    
        try {
            final URL url = new URL(source.get("href"));
            return CoverageIO.createSimpleReader(url);
        } catch (MalformedURLException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : Malformed url",ex);
        } catch (CoverageStoreException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : Can't read coverage",ex);
        } catch (IOException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : IO",ex);
        }
    }
}