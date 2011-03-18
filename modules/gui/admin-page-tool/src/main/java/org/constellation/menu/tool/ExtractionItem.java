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

package org.constellation.menu.tool;

import org.constellation.bean.AbstractMenuItem;


/**
 * Add a automated extraction tool page.
 *
 * @author Johann Sorel (Geomatys)
 */
public class ExtractionItem extends AbstractMenuItem{


    public ExtractionItem() {
        super(
            new String[]{"/org/constellation/menu/tool/extraction.xhtml"},
            null,
            new Path(TOOLS_PATH,"extraction", "/org/constellation/menu/tool/extraction.xhtml", "org.constellation.icons.antenna.png.mfRes",100)
            );
    }

}
