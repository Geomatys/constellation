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

import org.apache.commons.lang.ObjectUtils;
import org.constellation.configuration.Layer;
import org.geotoolkit.temporal.object.TemporalUtilities;

import java.util.Comparator;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class LayerComparator implements Comparator<Layer> {

    private final Criteria criteria;
    private final Mode mode;

    public static enum Criteria {
        TITLE,
        DATE,
        OWNER,
        DATA_TYPE;

        public static Criteria fromName(final String name) {
            if ("title".equalsIgnoreCase(name)) {
                return TITLE;
            }
            if ("date".equalsIgnoreCase(name)) {
                return DATE;
            }
            if ("owner".equalsIgnoreCase(name)) {
                return OWNER;
            }
            if ("dataType".equalsIgnoreCase(name)) {
                return DATA_TYPE;
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

    public LayerComparator(final String criteria, final String mode) {
        this(Criteria.fromName(criteria), Mode.fromName(mode));
    }

    public LayerComparator(final Criteria criteria, final Mode mode) {
        ensureNonNull("criteria", criteria);
        ensureNonNull("mode",     mode);
        this.criteria = criteria;
        this.mode     = mode;
    }

    @Override
    public int compare(final Layer l1, final Layer l2) {
        final int result;
        switch (criteria) {
            case TITLE:
                result = compareTitle(l1, l2);
                break;
            case DATE:
                result = compareDate(l1, l2);
                break;
            case OWNER:
                result = compareOwner(l1, l2);
                break;
            case DATA_TYPE:
                result = compareDataType(l1, l2);
                break;
            default: return 0;
        }
        return Mode.ASCENDING.equals(mode) ? -result : result;
    }

    private int compareTitle(final Layer l1, final Layer l2) {
        return ObjectUtils.compare(l1.getTitle(), l2.getTitle());
    }

    private int compareDate(final Layer l1, final Layer l2) {
        return ObjectUtils.compare(
                TemporalUtilities.parseDateSafe(l1.getDate(), false),
                TemporalUtilities.parseDateSafe(l2.getDate(), false),
                true);
    }

    private int compareOwner(final Layer l1, final Layer l2) {
        // TODO: implement owner sort
        return 0;
    }

    private int compareDataType(final Layer l1, final Layer l2) {
        return ObjectUtils.compare(l1.getProviderType(), l2.getProviderType());
    }
}
