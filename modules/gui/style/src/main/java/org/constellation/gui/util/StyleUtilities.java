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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.sis.util.Static;
import org.apache.sis.util.logging.Logging;
import org.constellation.gui.binding.Style;
import org.constellation.gui.binding.StyleElement;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.style.MutableStyle;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.constellation.gui.util.StyleFactories.FF;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public final class StyleUtilities extends Static {

    private static final Logger LOGGER = Logging.getLogger(StyleUtilities.class);

    public static Expression expression(final String label) {
        if (label.startsWith("{") && label.endsWith("}")) {
            return FF.property(label.substring(1, label.length() - 1));
        }
        return FF.literal(label);
    }

    public static Expression opacity(final double opacity) {
        return (opacity >= 0 && opacity <= 1.0) ? FF.literal(opacity) : Expression.NIL;
    }

    public static Expression literal(final Object value) {
        return value != null ? FF.literal(value) : Expression.NIL;
    }

    public static <T> T type(final StyleElement<T> elt) {
        return elt != null ? elt.toType() : null;
    }

    public static <T> List<T> singletonType(final StyleElement<T> elt) {
        return elt != null ? Collections.singletonList(elt.toType()) : new ArrayList<T>(0);
    }

    public static <T> List<T> listType(final List<? extends StyleElement<T>> elts) {
        final List<T> list = new ArrayList<T>();
        if (elts == null) {
            return list;
        }
        for (final StyleElement<T> elt : elts) {
            list.add(elt.toType());
        }
        return list;
    }

    public static Filter filter(final String filter) {
        if (filter == null) {
            return null;
        }
        try {
            return CQL.parseFilter(filter);
        } catch (CQLException ex) {
            LOGGER.log(Level.WARNING, "An error occurred during filter parsing.", ex);
        }
        return null;
    }

    public static String writeJson(final Object object) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }

    public static <T> T readJson(final String json, final Class<T> clazz) throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, clazz);
    }

    public static String toHex(final Color color) {
        return String.format("#%06X", (0xFFFFFF & color.getRGB()));
    }
}
