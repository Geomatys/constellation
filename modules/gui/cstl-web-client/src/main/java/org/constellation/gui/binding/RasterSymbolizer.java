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

package org.constellation.gui.binding;

import static org.apache.sis.util.ArgumentChecks.ensureNonNull;

/**
 * @author Fabien Bernard (Geomatys).
 * @version 0.9
 * @since 0.9
 */
public class RasterSymbolizer implements Symbolizer {

    public RasterSymbolizer() {
    }

    public RasterSymbolizer(final org.opengis.style.RasterSymbolizer symbolizer) {
        ensureNonNull("symbolizer", symbolizer);
    }

    @Override
    public org.opengis.style.Symbolizer toType() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
