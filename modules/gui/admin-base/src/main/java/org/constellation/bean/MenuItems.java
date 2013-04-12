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

package org.constellation.bean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.ServiceRegistry;
import org.apache.sis.internal.util.UnmodifiableArrayList;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public final class MenuItems {

    private static final List<MenuItem> PAGES;

    static {
        final Iterator<MenuItem> ite = ServiceRegistry.lookupProviders(MenuItem.class);
        final List<MenuItem> lst = new ArrayList<MenuItem>();
        while(ite.hasNext()){
            final MenuItem page = ite.next();
            lst.add(page);
        }

        PAGES = UnmodifiableArrayList.wrap(lst.toArray(new MenuItem[lst.size()]));
    }

    private MenuItems() {}

    public static List<MenuItem> getPages() {
        return PAGES;
    }
}
