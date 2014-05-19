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
import java.util.logging.Logger;

// OpenOffice dependencies
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XSingleServiceFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.comp.loader.FactoryHelper;
import com.sun.star.registry.XRegistryKey;

// Geotools dependencies
import org.geotools.openoffice.Formulas;
import org.geotools.openoffice.MethodInfo;

// Constellation dependencies
import org.constellation.numeric.table.Table;
import org.constellation.numeric.table.TableFactory;
import org.constellation.numeric.table.Interpolation;


/**
 * Implémentation de l'interface {@link XNumeric} qui sera exportée vers
 * <A HREF="http://www.openoffice.org">OpenOffice</A>.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Numeric extends Formulas implements XNumeric {
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
    private static final String __serviceName = "org.constellation.openoffice.Numeric";

    /**
     * Le service que l'on étend.
     */
    private static final String ADDIN_SERVICE = "com.sun.star.sheet.AddIn";

    /**
     * Construit une nouvelle instance du service.
     */
    @SuppressWarnings("unchecked")
    public Numeric() {
        methods.put("getLocatedIndex", new MethodInfo("Numeric", "LOCATE.INDEX",
            "Retourne les index autour de la valeur xi spécifiée.",
            new String[] {
                "xOptions",      "Fournit par OpenOffice.",
                "data",          "Les données. Doit contenir au minimum une colonne, celle des x. " +
                                 "Toutes les colonnes suivantes (optionnelles) sont des y. Si une " +
                                 "valeur #N/A est trouvée dans l'une de ces colonnes, les index "   +
                                 "retournés éviteront de pointer vers la ligne correspondante.",
                "xi",            "La valeur xi pour laquelle on veut les index."
        }));
        methods.put("getInterpolated", new MethodInfo("Numeric", "INTERPOLATE",
            "Interpole les valeurs de y pour les valeurs xi spécifiées.",
            new String[] {
                "xOptions",      "Fournit par OpenOffice.",
                "data",          "Les données. Doit contenir au minimum deux colonnes. La première "   +
                                 "colonne est celle des x. Toutes les colonnes suivantes sont celles " +
                                 "des y.",
                "xi",            "Les valeurs xi pour lesquelles on veut interpoler.",
                "interpolation", "Le type d'interpolation: \"nearest\" ou \"linear\".",
                "skipMissingY",  "VRAI pour ignorer les valeurs manquantes dans les vecteurs des y," +
                                 "ou FAUX pour retourner #N/A si de telles valeurs sont rencontrées."
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
        if (implementation.equals(Numeric.class.getName())) {
            return FactoryHelper.getServiceFactory(Numeric.class, __serviceName, factories, registry);
        }
        return null;
    }

    /**
     * Writes the service information into the given registry key.
     * This method is called by the {@code com.sun.star.comp.loader.JavaLoader}.
     *
     * @param  registry     The registry key.
     * @return {@code true} if the operation succeeded.
     */
    public static boolean __writeRegistryServiceInfo(final XRegistryKey registry) {
        final String classname = Numeric.class.getName();
        return FactoryHelper.writeRegistryServiceInfo(classname, __serviceName, registry)
            && FactoryHelper.writeRegistryServiceInfo(classname, ADDIN_SERVICE, registry);
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
     * Construit une table pour les données spécifiées.
     */
    private static Table createTable(final double[][] data, final Object interpolation) {
        final Interpolation type;
        if (interpolation == null) {
            type = Interpolation.NEAREST;
        } else if (interpolation instanceof Interpolation) {
            type = (Interpolation) interpolation;
        } else {
            type = Interpolation.valueOf(String.valueOf(interpolation).toUpperCase().trim());
        }
        final double[]   x = new double[data.length];
        final double[][] y = new double[data.length==0 ? 0 : Math.max(data[0].length - 1, 0)][x.length];
        for (int j=0; j<x.length; j++) {
            final double[] row = data[j];
            x[j] = row[0];
            for (int i=0; i<y.length;) {
                y[i][j] = row[++i];
            }
        }
        return TableFactory.getDefault().create(x, y, type);
    }

    /**
     * {inheritDoc}
     */
    public double[][] getLocatedIndex(final XPropertySet xOptions,
                                      final double[][]   data,
                                      final double       xi)
    {
        try {
            final int[] index = new int[3]; // TODO
            final Table table = createTable(data, Interpolation.NEAREST);
            table.locate(xi, index);
            final double[][] r = new double[index.length][];
            for (int i=0; i<index.length; i++) {
                r[i] = new double[] {index[i]};
            }
            return r;
        } catch (Throwable exception) {
            // Attrape aussi NoClassDefFoundError, qui se produit souvent.
            reportException("getLocatedIndex", exception);
            return getFailure(1, 1);
        }
    }

    /**
     * {inheritDoc}
     */
    public double[][] getInterpolated(final XPropertySet xOptions,
                                      final double[][]   data,
                                      final double[][]   xi,
                                      final Object       interpolation,
                                      final Object       skipMissingY)
    {
        try {
            final Table  table = createTable(data, interpolation);
            final int     numY = Math.max(table.getNumCol()-1, 0);
            final double[][] r = new double[xi.length][];
            double[] transfert = null;
            for (int j=0; j<r.length; j++) {
                final double[] source = xi[j];
                final double[] target = new double[source.length * numY];
                for (int i=0; i<source.length; i++) {
                    transfert = table.interpolateAll(source[i], transfert);
                    System.arraycopy(transfert, 0, target, i*numY, numY);
                }
                r[j] = target;
            }
            return r;
        } catch (Throwable exception) {
            // Attrape aussi NoClassDefFoundError, qui se produit souvent.
            reportException("getInterpolated", exception);
            return getFailure(xi.length, 1);
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
