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


import java.util.Map;
import org.geotoolkit.coverage.grid.GridCoverage2D;
import org.geotoolkit.coverage.io.CoverageStoreException;
import org.geotoolkit.coverage.io.GridCoverageReader;
import org.geotoolkit.util.converter.NonconvertibleObjectException;
import org.geotoolkit.util.converter.SimpleConverter;



/**
 * Implementation of ObjectConverter to convert a reference into a GridCoverage2D.
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
public final class ReferenceToGridCoverage2DConverter extends SimpleConverter<Map<String,String>, GridCoverage2D> {

    private static ReferenceToGridCoverage2DConverter INSTANCE;

    private ReferenceToGridCoverage2DConverter(){
    }

    public static synchronized ReferenceToGridCoverage2DConverter getInstance(){
        if(INSTANCE == null){
            INSTANCE = new ReferenceToGridCoverage2DConverter();
        }
        return INSTANCE;
    }

    @Override
    public Class<? super Map> getSourceClass() {
        return Map.class;
    }

    @Override
    public Class<? extends GridCoverage2D> getTargetClass() {
        return GridCoverage2D.class ;
    }
 
    @Override
    public GridCoverage2D convert(Map<String,String> source) throws NonconvertibleObjectException {
                    
        try{
            final GridCoverageReader reader = ReferenceToGridCoverageReaderConverter.getInstance().convert(source);
            return (GridCoverage2D)reader.read(0, null);
        } catch (CoverageStoreException ex) {
            throw new NonconvertibleObjectException("Reference grid coverage invalid input : Can't read coverage",ex);
        }
    }
}