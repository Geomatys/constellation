/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.constellation.bean;

import java.util.List;

/**
 *
 * @author jsorel
 */
public interface MenuItem {

    public String getResourceBundlePath();

    public List<Path> getPaths();

    public static final class Path{

        public final Path parent;
        public final String i18nKey;
        public final String linkedPage;
        public final String icon;

        public Path(final Path parent, final String i18nKey, final String linkedPage, final String icon) {
            this.parent = parent;
            this.i18nKey = i18nKey;
            this.linkedPage = linkedPage;
            this.icon = icon;
        }

    }

}
