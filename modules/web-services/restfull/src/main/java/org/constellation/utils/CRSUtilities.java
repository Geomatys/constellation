package org.constellation.utils;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.geotoolkit.factory.AuthorityFactoryFinder;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.FactoryException;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author bgarcia
 */
public class CRSUtilities {

    private static final Logger LOGGER = Logger.getLogger(CRSUtilities.class.getName());

    private static SortedMap<String, String> ePSGCodes;

    public static void main(String[] args) throws FactoryException {
        Map<String, String> allCodes = pagingAndFilterCode(0, 10, "Lambert");
        LOGGER.log(Level.INFO, allCodes.size() + " elements");
        for (String key : allCodes.keySet()) {
            LOGGER.log(Level.INFO, key + " => " + allCodes.get(key));
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

    public static Map<String, String> pagingAndFilterCode(final int start, final int nbByPage, final String filter) {
        SortedMap<String, String> selectedEPSGCode = new TreeMap<>();
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
                    return s.contains(filter) || s.equalsIgnoreCase(filter);
                }
            };

            selectedEPSGCode = Maps.filterKeys(ePSGCodes, myStringPredicate);
        }

        //selectedEPSGCode is empty because they don't have a filter applied
        if (selectedEPSGCode.isEmpty()) {
            int epsgCode = ePSGCodes.size();
            if (nbByPage > epsgCode) {
                return ePSGCodes;
            } else {
                selectedEPSGCode = getSubCRSMap(start, nbByPage, ePSGCodes);
            }
        } else {
            selectedEPSGCode = getSubCRSMap(start, nbByPage, selectedEPSGCode);
        }
        return selectedEPSGCode;
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

}
