/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
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

package org.constellation.metadata;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mehdi Sidhoum
 */
public class Utils {
    
    /**
     * Returns true if the list contains a string in one of the list elements.
     * @param list
     * @param str
     * @return
     */
    public static boolean matchesStringfromList(List<String> list, String str) {
        boolean str_available = false;
        for (String s : list) {
            Pattern pattern = Pattern.compile(str,Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
            Matcher matcher = pattern.matcher(s);
            if (matcher.find()) {
                str_available = true;
            }
        }
        return str_available;
    }

}
