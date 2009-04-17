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
package org.constellation.console;

import java.util.Date;
import java.util.TimeZone;
import java.awt.geom.Point2D;
import java.sql.SQLException;
import java.io.IOException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.text.Format;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.opengis.coverage.Coverage;
import org.geotoolkit.io.LineFormat;
import org.geotools.resources.Arguments;
import org.geotoolkit.util.Utilities;
import org.geotoolkit.coverage.SpatioTemporalCoverage3D;
import org.geotoolkit.coverage.OrdinateOutsideCoverageException;

import org.constellation.catalog.Database;
import org.constellation.catalog.CatalogException;
import org.constellation.catalog.NoSuchRecordException;
import org.constellation.coverage.catalog.Layer;
import org.constellation.coverage.catalog.LayerTable;
import org.constellation.coverage.model.Descriptor;
import org.constellation.coverage.model.DescriptorTable;


/**
 * Utilitaire de lignes de commandes pour extraire des valeurs de la base de données d'images.
 * Cet utilitaire attend les entrées suivantes:
 * <p>
 *   <li><p>Les arguments optionnels suivants:
 *     <ul>
 *       <li>{@code -locale} spécifie les conventions à utiliser pour la lecture et le formattage des
 *           nombres et des dates. Par défaut, le {@linkplain java.util.Locale#getDefault format local}
 *           est utilisé. Pour forcer l'usage du point comme séparateur décimal, utilisez par exemple
 *           {@code -locale=en_CA} (le code de pays {@code CA} utilise des dates au format
 *           {@code "dd/MM/yy"}).</li>
 *       <li>{@code -date-pattern} spécifie le format des dates. Les caractères autorisés sont
 *           {@linkplain SimpleDateFormat décrit ici}. Si cet argument n'est pas spécifié, alors
 *           le format est déterminé à partir de {@code -locale}.
 *           <b>Exemple:</b> {@code -date-pattern="dd/MM/yyyy HH:mm"}.</li>
 *       <li>{@code -timezone} spécifie le fuseau horaire des dates. Si cet argument n'est pas spécifié,
 *           alors le fuseau horaire local est utilisé. <b>Exemple:</b> {@code -timezone=UTC}.</li>
 *       <li>{@code -precision} spécifie le nombre de chiffres après la virgule à conserver pour
 *           la sortie. Si cet argument n'est pas spécifié, alors la précision dépend de la valeur
 *           de {@code -locale}.</li>
 *       <li>{@code -fromModel} indique que la valeur doit être calculée à partir du modèle plutôt
 *           que de tenter de lire les images pré-calculées. Ce paramètre n'a aucun effet sur les
 *           descripteurs qui ne possèdent pas de modèle.</li>
 *     </ul>
 *   </p></li>
 *   <li><p>Un nombre arbitraire d'arguments qui donnent les noms des {@linkplain Descriptor descripteurs}.
 *       Exemples: {@code SST}, {@code CHL}, {@code SLA}.</p></li>
 *   <li><p>Une suite de coordonnées (<var>date</var>, <var>x</var>,<var>y</var>) du
 *       {@linkplain System#in périphérique d'entrée standard}. Les lignes vierges et
 *       celles commençant par le caractère {@code #} sont ignorées.</p></li>
 * </ul>
 * <p>
 * Les valeurs de chacun des descripteurs sont renvoyées sur le
 * {@linkplain System#out périphérique de sortie standard}. En cas d'erreur, la trace
 * de l'exception est envoyée sur le {@linkplain System#err périphérique d'erreur}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Extractor extends Arguments {
    /**
     * Le nom du fichier en cours de lecture, ou {@code null} pour le périphérique d'entrée standard.
     */
    private final String inputFile;

    /**
     * Le périphérique d'entrée, ou {@code null} s'il n'a pas encore été construit.
     */
    private LineNumberReader in;

    /**
     * Le fuseau horaire, ou {@code null} pour le fuseau par défaut.
     */
    private final String timezone;

    /**
     * Le patron pour les dates, ou {@code null} pour la valeur par défaut.
     */
    private final String datePattern;

    /**
     * Le nombre de chiffre après la virgule à utiliser pour la sortie, ou -1 pour la valeur
     * par défaut.
     */
    private final int precision;

    /**
     * {@code true} pour afficher des informations pendant les traitements.
     */
    private final boolean verbose;

    /**
     * {@code true} pour calculer à partir du modèle plutôt que de tenter de lire les
     * images pré-calculées. Ce paramètre n'a aucun effet sur les descripteurs qui ne
     * possèdent pas de modèle.
     */
    private final boolean fromModel;

    /**
     * Construit un extracteurs à partir des arguments spécifiés.
     */
    private Extractor(final String[] arguments) {
        super(arguments);
        final Integer precision;
        verbose     = getFlag           ("-verbose");
        datePattern = getOptionalString ("-date-pattern");
        timezone    = getOptionalString ("-timezone");
        precision   = getOptionalInteger("-precision");
        inputFile   = getOptionalString ("-file");
        fromModel   = getFlag           ("-fromModel");
        this.precision = (precision != null) ? precision.intValue() : -1;
    }

    /**
     * Signal qu'une erreur est survenue lors du traitement des données. Cette méthode est utilisée
     * pour les exceptions prévisibles seulement (par exemple celles qui peuvent être dûes par un
     * nombre mal formatté par l'utilisateur).
     */
    private void reportUserError(final Exception exception) {
        out.flush();
        if (in != null) {
            err.print("Erreur à la ligne ");
            err.println(in.getLineNumber());
        }
        err.println(exception.getLocalizedMessage());
    }

    /**
     * Procède à l'extraction des valeurs.
     */
    private void process(final String[] descriptors) throws CatalogException, SQLException, IOException {
        /*
         * Etablit les connexions à la base de données.
         */
        final Database        database        = new Database();
        final LayerTable      layerTable      = database.getTable(LayerTable.class);
        final DescriptorTable descriptorTable = database.getTable(DescriptorTable.class);
        final SpatioTemporalCoverage3D[] coverages = new SpatioTemporalCoverage3D[descriptors.length];
        for (int i=0; i<descriptors.length; i++) {
            final String name = descriptors[i];
            Layer      layer      = null;
            Descriptor descriptor = null;
            Coverage   coverage   = null;
            try {
                layer = layerTable.getEntry(name);
            } catch (NoSuchRecordException ignore) {
                try {
                    descriptor = descriptorTable.getEntryLenient(name);
                } catch (NoSuchRecordException exception) {
                    reportUserError(exception);
                    return;
                }
                layer = descriptor.getLayer();
            }
            if (fromModel) {
                coverage = layer.getModel().asCoverage();
            }
            if (coverage == null) {
                if (descriptor != null) {
                    coverage = descriptor.getCoverage();
                } else {
                    coverage = layer.getCoverage();
                }
            }
            coverages[i] = new SpatioTemporalCoverage3D(name, coverage);
        }
        /*
         * Prépare une fois pour toute les objets qui seront nécessaires, et configure les formatteurs.
         */
        final double[]       samples    = new double[2];
        final Point2D.Double position   = new Point2D.Double();
        final NumberFormat   format     = NumberFormat.getNumberInstance(locale);
        final DateFormat     dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        final LineFormat     parser     = new LineFormat(new Format[]{dateFormat, format, format});
        if (precision >= 0) {
            format.setMinimumFractionDigits(precision);
            format.setMaximumFractionDigits(precision);
        }
        if (datePattern != null) {
            if (dateFormat instanceof SimpleDateFormat) {
                ((SimpleDateFormat) dateFormat).applyLocalizedPattern(datePattern);
            }
        }
        if (timezone != null) {
            dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        }
        if (verbose) {
            if (dateFormat instanceof SimpleDateFormat) {
                err.print("Format date:   ");
                err.println(((SimpleDateFormat) dateFormat).toLocalizedPattern());
            }
            if (format instanceof DecimalFormat) {
                err.print("Format nombre: ");
                err.println(((DecimalFormat) format).toLocalizedPattern());
            }
        }
        final int precision = format.getMaximumFractionDigits();
        /*
         * Procède à la lecture des lignes du périphérique d'entrée standard.
         */
        if (inputFile != null) {
            in = new LineNumberReader(new FileReader(inputFile));
        } else {
            in = new LineNumberReader(new InputStreamReader(System.in));
        }
        String line; while ((line=in.readLine()) != null) {
            if ((line=line.trim()).length()==0 || line.charAt(1)=='#') {
                continue;
            }
            try {
                parser.setLine(line);
            } catch (ParseException exception) {
                reportUserError(exception);
                return;
            }
            final Date time =  (Date)   parser.getValue(0);
            position.x      = ((Number) parser.getValue(1)).doubleValue();
            position.y      = ((Number) parser.getValue(2)).doubleValue();
            /*
             * Procède à l'extraction des valeurs et écrit vers le périphérique de sortie standard.
             */
            for (int i=0; i<coverages.length; i++) {
                final double[] values;
                try {
                    values = coverages[i].evaluate(position, time, samples);
                } catch (OrdinateOutsideCoverageException exception) {
                    reportUserError(exception);
                    return;
                }
                final String n = format.format(values[0]);
                out.print(Utilities.spaces(precision + 6 - n.length()));
                out.print(n);
            }
            out.println();
        }
        in.close();
        in = null;
        database.close();
    }

    /**
     * Affiche les valeurs pour tous les descripteurs énumérés.
     */
    public static void main(String[] descriptors) {
        final Extractor extractor = new Extractor(descriptors);
        descriptors = extractor.getRemainingArguments(Integer.MAX_VALUE);
        if (descriptors.length == 0) {
            extractor.err.println("Des descripteurs doivent être spécifiés en argument.");
        } else try {
            extractor.process(descriptors);
        } catch (Exception exception) {
            extractor.out.flush();
            exception.printStackTrace(extractor.err);
            return;
        }
        extractor.out.flush();
    }
}
