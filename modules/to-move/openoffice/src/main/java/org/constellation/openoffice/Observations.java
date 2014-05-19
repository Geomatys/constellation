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
package org.constellation.openoffice;

// J2SE dependencies
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.logging.Logger;
import java.awt.geom.Point2D;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

// OpenOffice dependencies
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.registry.XRegistryKey;

// OpenGIS dependencies
import org.opengis.geometry.DirectPosition;

// Geotools dependencies
import org.geotools.openoffice.Formulas;
import org.geotools.openoffice.MethodInfo;
import org.geotoolkit.coverage.SpatioTemporalCoverage3D;

// Constellation dependencies
import org.constellation.observation.CatalogException;
import org.constellation.observation.coverage.DynamicCoverage;


/**
 * Implémentation de l'interface {@link XObservations} qui sera exportée vers
 * <A HREF="http://www.openoffice.org">OpenOffice</A>.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Observations extends Formulas implements XObservations {
    /**
     * The logger to use for all message to log in this package.
     */
    private static final Logger LOGGER = Logger.getLogger("org.constellation.openoffice");

    /**
     * Le nom sous lequel sera enregistré cette composante.
     *
     * <strong>Note:</strong> Bien que ce nom de correspondent pas aux conventions
     * du Java, on ne peut le changer car il correspond au nom attendu par OpenOffice.
     */
    private static final String __serviceName = "org.constellation.openoffice.Observations";

    /**
     * Le service que l'on étend.
     */
    private static final String ADDIN_SERVICE = "com.sun.star.sheet.AddIn";

    /**
     * Ensemble des couvertures construites pour un descripteur donné.
     */
    private final Map<String, Reference<SpatioTemporalCoverage3D>> descriptors =
          new HashMap<String, Reference<SpatioTemporalCoverage3D>>();

    /**
     * Tableau pré-alloué pour la méthode {@link #getDescriptorValue}.
     */
    private transient double[] values;

    /**
     * Construit une nouvelle instance du service.
     */
    @SuppressWarnings("unchecked")
    public Observations() {
        methods.put("getDescriptorValue", new MethodInfo("Observations", "EVALUATE",
            "Évalue la valeur d'un descripteur du paysage océanique " +
            "à une position spatio-temporelle donnée.",
            new String[] {
                "xOptions",    "Fournit par OpenOffice.",
                "Descripteur", "Nom du descripteur du paysage océanique.",
                "Date",        "Date et heure à laquelle évaluer le descripteur.",
                "Longitude",   "Longitude (en degrés) à laquelle évaluer le descripteur.",
                "Latitude",    "Latitude (en degrés) à laquelle évaluer le descripteur."
        }));
        methods.put("getVoxelCenter", new MethodInfo("Observations", "VOXEL.CENTER",
            "Retourne la coordonnée spatio-temporelle au centre du voxel le plus proche. " +
            "L'appel de la fonction EVALUATE à cette coordonnée ne devrait pas impliquer d'interpolations.",
            new String[] {
                "xOptions",    "Fournit par OpenOffice.",
                "Descripteur", "Nom du descripteur du paysage océanique.",
                "Date",        "Date et heure.",
                "Longitude",   "Longitude (en degrés).",
                "Latitude",    "Latitude (en degrés)."
        }));
    }

    /**
     * Returns a factory for creating the service.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}.
     *
     * @param   implementation The name of the implementation for which a service is desired.
     * @param   factories      The service manager to be used if needed.
     * @param   registry       The registryKey
     * @return  A factory for creating the component.
     */
    public static XSingleServiceFactory __getServiceFactory(
                                        final String               implementation,
                                        final XMultiServiceFactory factories,
                                        final XRegistryKey         registry)
    {
        if (implementation.equals(Observations.class.getName())) {
            return FactoryHelper.getServiceFactory(Observations.class, __serviceName, factories, registry);
        }
        return Numeric.__getServiceFactory(implementation, factories, registry);
    }

    /**
     * Writes the service information into the given registry key.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}.
     *
     * @param  registry     The registry key.
     * @return {@code true} if the operation succeeded.
     */
    public static boolean __writeRegistryServiceInfo(final XRegistryKey registry) {
        final String classname = Observations.class.getName();
        return FactoryHelper.writeRegistryServiceInfo(classname, __serviceName, registry)
             && FactoryHelper.writeRegistryServiceInfo(classname, ADDIN_SERVICE, registry)
             && Numeric.__writeRegistryServiceInfo(registry);
    }

    /**
     * The service name that can be used to create such an object by a factory.
     */
    public String getServiceName() {
        return __serviceName;
    }

    /**
     * Provides the supported service names of the implementation, including also
     * indirect service names.
     *
     * @return Sequence of service names that are supported.
     */
    public String[] getSupportedServiceNames() {
        return new String[] {ADDIN_SERVICE, __serviceName};
    }

    /**
     * Tests whether the specified service is supported, i.e. implemented by the implementation.
     *
     * @param  name Name of service to be tested.
     * @return {@code true} if the service is supported, {@code false} otherwise.
     */
    public boolean supportsService(final String name) {
        return name.equals(ADDIN_SERVICE) || name.equals(__serviceName);
    }

    /**
     * Retourne les données pour un descripteur du nom spécifié.
     *
     * @param  name Le nom du {@linkplain Descriptor descripteur}.
     * @return La converture des données pour le descripteur spécifié.
     * @throws NoSuchRecordException si aucun descripteur n'a été trouvée pour le nom spécifié.
     * @throws CatalogException si une erreur est survenue lors de l'interrogation du catalogue.
     */
    private SpatioTemporalCoverage3D getDescriptorCoverage(final String descriptor)
            throws CatalogException
    {
        final org.constellation.observation.Observations obs = org.constellation.observation.Observations.getDefault();
        // Pour la synchronisation, utilise le même verrou que 'Observations.getDescriptorCoverage'.
        synchronized (obs) {
            Reference<SpatioTemporalCoverage3D> ref = descriptors.get(descriptor);
            if (ref != null) {
                final SpatioTemporalCoverage3D coverage = ref.get();
                if (coverage != null) {
                    return coverage;
                }
            }
            final SpatioTemporalCoverage3D coverage = new SpatioTemporalCoverage3D(descriptor,
                                                         obs.getDescriptorCoverage(descriptor));
            ref = new SoftReference<SpatioTemporalCoverage3D>(coverage);
            descriptors.put(descriptor, ref);
            return coverage;
        }
    }

    /**
     * {inheritDoc}
     *
     * The XPropertySet contains a PropertyValue named "NullDate",
     * which is the mentioned base for all date calculations in Calc.
     */
    public Object getDescriptorValue(final XPropertySet xOptions,
                                     final String       descriptor,
                                     final double       t,
                                     final double       x,
                                     final double       y)
    {
        try {
            final Date                     time     = toDate(xOptions, t);
            final Point2D.Double           position = new Point2D.Double(x,y);
            final SpatioTemporalCoverage3D coverage = getDescriptorCoverage(descriptor);
            synchronized (coverage) {
                values = coverage.evaluate(position, time, values);
            }
            return new Double(values[0]);
        } catch (Throwable exception) {
            // Attrape aussi NoClassDefFoundError, qui se produit souvent.
            return getLocalizedMessage(exception);
        }
    }

    /**
     * {inheritDoc}
     */
    public double[][] getVoxelCenter(final XPropertySet xOptions,
                                     final String       descriptor,
                                     final double       t,
                                     final double       x,
                                     final double       y)
    {
        try {
            Point2D position = new Point2D.Double(x,y);
            Date        date = getEpoch(xOptions);
            if (date == null) {
                // Un message a déjà été enregistré dans le journal par getEpoch.
                return null;
            }
            final long epoch = date.getTime();
            date.setTime(date.getTime() + Math.round(t * DAY_TO_MILLIS));
            final SpatioTemporalCoverage3D coverage = getDescriptorCoverage(descriptor);
            final DirectPosition coord = ((DynamicCoverage) coverage.getWrappedCoverage())
                                         .snap(coverage.toDirectPosition(position, date));
            position = coverage.toPoint2D(coord);
            date     = coverage.toDate   (coord);
            return new double[][] {{(date.getTime() - epoch) / (double)DAY_TO_MILLIS,
                                    position.getX(), position.getY()}};
        } catch (Throwable exception) {
            // Attrape aussi NoClassDefFoundError, qui se produit souvent.
            reportException("getVoxelCenter", exception);
            return null;
        }
    }

    /**
     * Retourne le journal dans lequel écrire d'éventuels avertissements.
     */
    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
