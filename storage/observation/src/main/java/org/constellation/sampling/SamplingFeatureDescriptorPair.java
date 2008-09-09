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
package org.constellation.sampling;

// J2SE dependencies
import java.util.Date;
import java.lang.reflect.UndeclaredThrowableException;

// Sicade dependencies
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.model.Descriptor;
import org.opengis.observation.sampling.SamplingFeature;



/**
 * Une paire {@linkplain SamplingFeature station} - {@linkplain Descriptor descripteur}.
 * Utilisée par {@link MeasurementTableFiller} pour déterminer un ordre optimal
 * dans lequel ces éléments devraient être évalués.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class SamplingFeatureDescriptorPair implements Comparable<SamplingFeatureDescriptorPair> {
    /**
     * La station.
     */
    final SamplingFeature samplingFeature;

    /**
     * Le descripteur du paysage océanique.
     */
    final Descriptor descriptor;

    /**
     * La valeur. Sera calculée par {@link MeasurementTableFiller}.
     */
    float value = Float.NaN;

    /**
     * Construit une nouvelle paire pour la station et le descripteur spécifié.
     */
    public SamplingFeatureDescriptorPair(final SamplingFeature samplingFeature, final Descriptor descriptor) {
        this.samplingFeature    = samplingFeature;
        this.descriptor = descriptor;
    }

    /**
     * Retourne la date à laquelle le descripteur sera évalué.
     */
    private long getTime() throws CatalogException {
        final Date time = null; //samplingFeature.getTime();
        if (time == null) {
            /*
             * Place les stations dont la date est indéterminée à la fin. C'est cohérent
             * avec le classement des valeurs NaN de type 'float' par exemple.
             */
            return Long.MAX_VALUE;
        }
        return time.getTime() + Math.round((24*60*60*1000) * descriptor.getRegionOfInterest().getDayOffset());
    }

    /**
     * Compare cette paire avec la paire spécifiée.
     */
    public int compareTo(final SamplingFeatureDescriptorPair that) {
        final long t1, t2;
        try {
            t1 = this.getTime();
            t2 = that.getTime();
        } catch (CatalogException exception) {
            // Sera traité de manière particulière par MeasurementTableFiller
            throw new UndeclaredThrowableException(exception);
        }
        if (t1 < t2) return -1;
        if (t1 > t2) return +1;
        return 0;
    }

    /**
     * Retourne une représentation textuelle de cette paire, à des fins de déboguage.
     */
    @Override
    public String toString() {
        return ""; //'(' + samplingFeature.getName() + ", " + descriptor.getName() + ')';
    }
}
