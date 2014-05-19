/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2014, Geomatys
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
package org.constellation.utils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.constellation.dto.CRSCoverageList;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * @author bgarcia
 */
public class CRSUtilities {

    private static final Logger LOGGER = Logger.getLogger(CRSUtilities.class.getName());

    private static SortedMap<String, String> ePSGCodes;

    public static void main(String[] args) throws FactoryException {
        CRSCoverageList allCodes = pagingAndFilterCode(0, 10, "Lambert");
        LOGGER.log(Level.INFO, allCodes.getLength() + " total elements");
        LOGGER.log(Level.INFO, allCodes.getSelectedEPSGCode().size() + " elements");

        for (String key : allCodes.getSelectedEPSGCode().keySet()) {
            LOGGER.log(Level.INFO, key + " => " + allCodes.getSelectedEPSGCode().get(key));
        }
    }


    /**
     * @throws FactoryException
     */
    public static Map<String, String> setWKTMap() throws FactoryException {
        if (ePSGCodes == null) {
            ePSGCodes = new TreeMap<>();
            final CRSAuthorityFactory factory = AuthorityFactoryFinder.getCRSAuthorityFactory("EPSG", null);
            final Collection<String> codes = factory.getAuthorityCodes(CoordinateReferenceSystem.class);

            for (final String code : codes) {
                try {
                    final IdentifiedObject obj = factory.createObject(code);
                    final String wkt = obj.getName().toString();
                    ePSGCodes.put(wkt + " - EPSG:" + code, code);
                } catch (Exception ex) {
                    //some objects can not be expressed in WKT, we skip them
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "not available in WKT : " + code);
                    }
                }
            }
        }
        return ePSGCodes;
    }

    public static CRSCoverageList pagingAndFilterCode(final int start, final int nbByPage, final String filter) {
        SortedMap<String, String> selectedEPSGCode = new TreeMap<>();
        final CRSCoverageList coverageList = new CRSCoverageList();
        try {
            setWKTMap();
        } catch (FactoryException e) {
            LOGGER.log(Level.WARNING, "Error on wkt factory", e);
        }

        if (!filter.equalsIgnoreCase("none")) {
            //filter epsg codes
            Predicate<String> myStringPredicate = new Predicate<String>() {
                @Override
                public boolean apply(final String s) {
                    String s1 = s.toLowerCase();
                    String filter1 = filter.toLowerCase();
                    return s1.contains(filter1) || s1.equalsIgnoreCase(filter1);
                }
            };

            selectedEPSGCode = Maps.filterKeys(ePSGCodes, myStringPredicate);
            coverageList.setLength(selectedEPSGCode.size());
        }else{
            coverageList.setLength(ePSGCodes.size());
        }


        //selectedEPSGCode is empty because they don't have a filter applied
        if (selectedEPSGCode.isEmpty()) {
            int epsgCode = ePSGCodes.size();
            if (nbByPage > epsgCode) {
                coverageList.setSelectedEPSGCode(ePSGCodes);
                return coverageList;
            } else {
                selectedEPSGCode = getSubCRSMap(start, nbByPage, ePSGCodes);
            }
        } else {
            selectedEPSGCode = getSubCRSMap(start, nbByPage, selectedEPSGCode);
        }
        coverageList.setSelectedEPSGCode(selectedEPSGCode);
        return coverageList;
    }


    private static SortedMap<String, String> getSubCRSMap(final int start, final int nbByPage, SortedMap<String, String> sortedMap) {
        final Set<String> keys = sortedMap.keySet();
        String[] key = new String[keys.size()];
        key = keys.toArray(key);
        final String startKey = key[start];
        if((start + nbByPage) > key.length){
            sortedMap = sortedMap.tailMap(startKey);
        }else{
            final String endPageKey = key[start + nbByPage];
            sortedMap = sortedMap.subMap(startKey, endPageKey);
        }
        return sortedMap;
    }

    public static int getEPSGCodesLength(){
        if(ePSGCodes==null){
            try {
                setWKTMap();
            } catch (FactoryException e) {
                LOGGER.log(Level.WARNING, "error on epsg map building", e);
            }
        }
        return ePSGCodes.size();
    }

}
