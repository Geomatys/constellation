/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.constellation.gui.util;

import org.apache.commons.lang3.ObjectUtils;
import org.constellation.configuration.StyleBrief;

import java.util.Comparator;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class StyleBeanComparator implements Comparator<StyleBrief> {

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
    public int compare(final StyleBrief l1, final StyleBrief l2) {
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

    private int compareName(final StyleBrief s1, final StyleBrief s2) {
        return ObjectUtils.compare(s1.getName(), s2.getName());
    }

    private int compareDate(final StyleBrief s1, final StyleBrief s2) {
        return -ObjectUtils.compare(s1.getDate(), s2.getDate(), true);
    }

    private int compareOwner(final StyleBrief s1, final StyleBrief s2) {
        return s1.getOwner().compareTo(s2.getOwner());
    }

    private int compareDataType(final StyleBrief s1, final StyleBrief s2) {
        return s1.getType().compareTo(s2.getType());
    }
}
