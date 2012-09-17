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

import java.util.List;
import org.constellation.admin.service.ConstellationServer;
import java.util.Objects;

/**
 *
 * @author Johann Sorel (Geomatys)
 */
public interface MenuItem {

    /**
     *
     * @param server : Server to test
     * @return true if this menu item is available for the given server.
     */
    public boolean isAvailable(final ConstellationServer server);

    /**
     * The identifier of the MenuItem
     *
     * @return an identifier never null
     */
    public String getId();

    /**
     * The localized name of the MenuItem
     *
     * @return a name never null
     */
    public String getTitle();

    /**
     * List of string path to xhtml pages that need to be copied in the web application.
     *
     * @return list never null
     */
    public List<String> getPages();

    /**
     *
     * @return path to the resource bundle used for pages and path translation.
     */
    public String getResourceBundlePath();

    /**
     * A Path which will be added in the administration menu.
     * @return never null.
     */
    public Path getPath();

    /**
     * A Path describe a menu item displayed in the administration menu.
     */
    public static final class Path {

        public final Path parent;
        public final String i18nKey;
        public final String linkedPage;
        public final String icon;
        public final int priority;

        public Path(final Path parent, final String i18nKey, final String linkedPage,
                final String icon, final int priority) {
            this.parent = parent;
            this.i18nKey = i18nKey;
            this.linkedPage = linkedPage;
            this.icon = icon;
            this.priority = priority;
        }

        public boolean isChildOf(final Path path) {
            if (this.equals(path)) return true;
            if (this.parent != null) return this.parent.isChildOf(path);
            else return false;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Path) {
                final Path that = (Path) obj;
                return Objects.equals(this.i18nKey, that.i18nKey) &&
                       Objects.equals(this.icon, that.icon) &&
                       Objects.equals(this.linkedPage, that.linkedPage) &&
                       Objects.equals(this.priority, that.priority) &&
                       Objects.equals(this.parent, that.parent);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + (this.parent != null ? this.parent.hashCode() : 0);
            hash = 53 * hash + (this.i18nKey != null ? this.i18nKey.hashCode() : 0);
            hash = 53 * hash + (this.linkedPage != null ? this.linkedPage.hashCode() : 0);
            hash = 53 * hash + (this.icon != null ? this.icon.hashCode() : 0);
            hash = 53 * hash + this.priority;
            return hash;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[Path]\n");
            if (parent != null) {
                sb.append("parent:").append(parent.i18nKey).append('\n');;
            } else {
                sb.append("no parent\n");
            }
            sb.append("i18nKey:").append(i18nKey).append('\n');
            sb.append("linkedPage:").append(linkedPage).append('\n');
            sb.append("icon:").append(icon).append('\n');
            sb.append("priority:").append(priority).append('\n');
            return  sb.toString();
        }

    }

}
