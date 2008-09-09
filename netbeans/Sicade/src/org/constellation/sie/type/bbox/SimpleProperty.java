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
package org.constellation.sie.type.bbox;

// J2SE dependencies
import java.util.Date;
import java.awt.geom.Dimension2D;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;

// OpenIDE dependencies
import org.openide.nodes.Sheet;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

// Constellation dependencies
import org.constellation.util.DateRange;


/**
 * Une propriété simple en lecture seule, pour affichage dans la feuille des propriétés.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SimpleProperty extends PropertySupport.ReadOnly {
    /**
     * La valeur de la propriété.
     */
    private final Object value;

    /**
     * Procède à la construction de la propriété. Tous les constructeurs publiques
     * délègueront leur travail à ce constructeur.
     */
    private SimpleProperty(final String name, final Class type, final Object value) {
        super(name, type, NbBundle.getMessage(SimpleProperty.class, name), null);
        this.value = value;
    }

    /**
     * Construit une nouvelle instance pour la valeur spécifiée.
     */
    public SimpleProperty(final String name, final double value) {
        this(name, Double.TYPE, value);
    }

    /**
     * Construit une nouvelle instance pour la date spécifiée.
     */
    public SimpleProperty(final String name, final Date value) {
        this(name, Date.class, value);
    }

    /**
     * Retourne la valeur de cette propriété.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Méthode de commodité fabriquant une feuille de propriétés à partir des informations
     * fournies. Chacun des arguments peut être nul.
     */
    public static Sheet createSheet(final GeographicBoundingBox area,
                                    final DateRange             time,
                                    final Dimension2D     resolution)
    {
        final Sheet sheet = Sheet.createDefault();
        final Sheet.Set s = sheet.get(Sheet.PROPERTIES);
        if (area != null) {
            s.put(new SimpleProperty("northBoundLatitude", area.getNorthBoundLatitude()));
            s.put(new SimpleProperty("southBoundLatitude", area.getSouthBoundLatitude()));
            s.put(new SimpleProperty("westBoundLongitude", area.getWestBoundLongitude()));
            s.put(new SimpleProperty("eastBoundLongitude", area.getEastBoundLongitude()));
        }
        if (time != null) {
            s.put(new SimpleProperty("startTime", time.getMinValue()));
            s.put(new SimpleProperty(  "endTime", time.getMaxValue()));
        }
        if (resolution != null) {
            s.put(new SimpleProperty("xResolution", resolution.getWidth()));
            s.put(new SimpleProperty("yResolution", resolution.getHeight()));
        }
        return sheet;
    }
}
