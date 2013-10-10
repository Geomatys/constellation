/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

package org.constellation.gui.util;

import org.apache.commons.lang3.ObjectUtils;
import org.constellation.dto.StyleBean;

import java.util.Comparator;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class StyleBeanComparator implements Comparator<StyleBean> {

    private final Criteria criteria;
    private final Mode mode;

    public static enum Criteria {
        NAME,
        DATE,
        OWNER,
        TYPE;

        public static Criteria fromName(final String name) {
            if ("name".equalsIgnoreCase(name)) {
                return NAME;
            }
            if ("date".equalsIgnoreCase(name)) {
                return DATE;
            }
            if ("owner".equalsIgnoreCase(name)) {
                return OWNER;
            }
            if ("type".equalsIgnoreCase(name)) {
                return TYPE;
            }
            throw new IllegalArgumentException("Unknown sort criteria: \"" + name + "\".");
        }
    }

    public static enum Mode {
        ASCENDING,
        DESCENDING;

        public static Mode fromName(final String name) {
            if ("ascending".equalsIgnoreCase(name)) {
                return ASCENDING;
            }
            if ("descending".equalsIgnoreCase(name)) {
                return DESCENDING;
            }
            throw new IllegalArgumentException("Unknown sort mode: \"" + name + "\".");
        }
    }

    public StyleBeanComparator(final String criteria, final String mode) {
        this(Criteria.fromName(criteria), Mode.fromName(mode));
    }

    public StyleBeanComparator(final Criteria criteria, final Mode mode) {
        ensureNonNull("criteria", criteria);
        ensureNonNull("mode",     mode);
        this.criteria = criteria;
        this.mode     = mode;
    }

    @Override
    public int compare(final StyleBean l1, final StyleBean l2) {
        final int result;
        switch (criteria) {
            case NAME:
                result = compareName(l1, l2);
                break;
            case DATE:
                result = compareDate(l1, l2);
                break;
            case OWNER:
                result = compareOwner(l1, l2);
                break;
            case TYPE:
                result = compareDataType(l1, l2);
                break;
            default: return 0;
        }
        return Mode.ASCENDING.equals(mode) ? -result : result;
    }

    private int compareName(final StyleBean s1, final StyleBean s2) {
        return ObjectUtils.compare(s1.getName(), s2.getName());
    }

    private int compareDate(final StyleBean s1, final StyleBean s2) {
        return -ObjectUtils.compare(s1.getDate(), s2.getDate(), true);
    }

    private int compareOwner(final StyleBean s1, final StyleBean s2) {
        // TODO: implement owner sort
        return 0;
    }

    private int compareDataType(final StyleBean s1, final StyleBean s2) {
        // TODO: implement owner sort
        return 0;
    }
}
