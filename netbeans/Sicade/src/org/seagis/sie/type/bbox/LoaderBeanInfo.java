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
package net.seagis.sie.type.bbox;

// J2SE dependencies
import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;

// OpenIDE dependencies
import org.openide.loaders.UniFileLoader;
import org.openide.util.Utilities;


/**
 * Propriétés de {@link Loader}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class LoaderBeanInfo extends SimpleBeanInfo {
    /**
     * Construit un ensemble de propriétés par défaut.
     */
    public LoaderBeanInfo() {
    }

    /**
     * Retourne des informations supplémentaires concernant la classe parente.
     */
    @Override
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {
                Introspector.getBeanInfo(UniFileLoader.class)
            };
        } catch (IntrospectionException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Retourne l'icône représentant les propriétés de {@link Loader}.
     */
    @Override
    public Image getIcon(final int type) {
        switch (type) {
            case BeanInfo.ICON_MONO_16x16: // Fall through
            case BeanInfo.ICON_COLOR_16x16: {
                return Utilities.loadImage(RootNode.ICON_PATH);
            }
            case BeanInfo.ICON_MONO_32x32: // Fall through
            case BeanInfo.ICON_COLOR_32x32: {
                return Utilities.loadImage(RootNode.ICON_32_PATH);
            }
            default: {
                return super.getIcon(type);
            }
        }
    }
}
