/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation.coverage;

// Utilitaire
import java.util.Date;
import java.util.TimeZone;
import java.awt.geom.Point2D;
import java.sql.SQLException;

// Entr�s / sorties
import java.io.IOException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;

// Formattage
import java.text.Format;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.geotools.io.LineFormat;

// OpenGIS
import org.opengis.coverage.Coverage;

// Geotools
import org.geotools.resources.Arguments;
import org.geotools.resources.Utilities;
import org.geotools.coverage.SpatioTemporalCoverage3D;
import org.geotools.coverage.OrdinateOutsideCoverageException;

// Sicade
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;
import net.sicade.observation.NoSuchRecordException;


/**
 * Utilitaire de lignes de commandes pour extraire des valeurs de la base de donn�es d'images.
 * Cet utilitaire attend les entr�es suivantes:
 * <p>
 *   <li><p>Les arguments optionnels suivants:
 *     <ul>
 *       <li>{@code -locale} sp�cifie les conventions � utiliser pour la lecture et le formattage des
 *           nombres et des dates. Par d�faut, le {@linkplain java.util.Locale#getDefault format local}
 *           est utilis�. Pour forcer l'usage du point comme s�parateur d�cimal, utilisez par exemple
 *           {@code -locale=en_CA} (le code de pays {@code CA} utilise des dates au format
 *           {@code "dd/MM/yy"}).</li>
 *       <li>{@code -date-pattern} sp�cifie le format des dates. Les caract�res autoris�s sont
 *           {@linkplain SimpleDateFormat d�crit ici}. Si cet argument n'est pas sp�cifi�, alors
 *           le format est d�termin� � partir de {@code -locale}.
 *           <b>Exemple:</b> {@code -date-pattern="dd/MM/yyyy HH:mm"}.</li>
 *       <li>{@code -timezone} sp�cifie le fuseau horaire des dates. Si cet argument n'est pas sp�cifi�,
 *           alors le fuseau horaire local est utilis�. <b>Exemple:</b> {@code -timezone=UTC}.</li>
 *       <li>{@code -precision} sp�cifie le nombre de chiffres apr�s la virgule � conserver pour
 *           la sortie. Si cet argument n'est pas sp�cifi�, alors la pr�cision d�pend de la valeur
 *           de {@code -locale}.</li>
 *       <li>{@code -fromModel} indique que la valeur doit �tre calcul�e � partir du mod�le plut�t
 *           que de tenter de lire les images pr�-calcul�es. Ce param�tre n'a aucun effet sur les
 *           descripteurs qui ne poss�dent pas de mod�le.</li>
 *     </ul>
 *   </p></li>
 *   <li><p>Un nombre arbitraire d'arguments qui donnent les noms des {@linkplain Descriptor descripteurs}.
 *       Exemples: {@code SST}, {@code CHL}, {@code SLA}.</p></li>
 *   <li><p>Une suite de coordonn�es (<var>date</var>, <var>x</var>,<var>y</var>) du
 *       {@linkplain System#in p�riph�rique d'entr�e standard}. Les lignes vierges et
 *       celles commen�ant par le caract�re {@code #} sont ignor�es.</p></li>
 * </ul>
 * <p>
 * Les valeurs de chacun des descripteurs sont renvoy�es sur le
 * {@linkplain System#out p�riph�rique de sortie standard}. En cas d'erreur, la trace
 * de l'exception est envoy�e sur le {@linkplain System#err p�riph�rique d'erreur}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Extractor extends Arguments {
    /**
     * Le nom du fichier en cours de lecture, ou {@code null} pour le p�riph�rique d'entr�e standard.
     */
    private final String inputFile;

    /**
     * Le p�riph�rique d'entr�e, ou {@code null} s'il n'a pas encore �t� construit.
     */
    private LineNumberReader in;

    /**
     * Le fuseau horaire, ou {@code null} pour le fuseau par d�faut.
     */
    private final String timezone;

    /**
     * Le patron pour les dates, ou {@code null} pour la valeur par d�faut.
     */
    private final String datePattern;

    /**
     * Le nombre de chiffre apr�s la virgule � utiliser pour la sortie, ou -1 pour la valeur
     * par d�faut.
     */
    private final int precision;

    /**
     * {@code true} pour afficher des informations pendant les traitements.
     */
    private final boolean verbose;

    /**
     * {@code true} pour calculer � partir du mod�le plut�t que de tenter de lire les
     * images pr�-calcul�es. Ce param�tre n'a aucun effet sur les descripteurs qui ne
     * poss�dent pas de mod�le.
     */
    private final boolean fromModel;

    /**
     * Construit un extracteurs � partir des arguments sp�cifi�s.
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
        this.precision = (precision!=null) ? precision.intValue() : -1;
    }

    /**
     * Signal qu'une erreur est survenue lors du traitement des donn�es. Cette m�thode est utilis�e
     * pour les exceptions pr�visibles seulement (par exemple celles qui peuvent �tre d�es par un
     * nombre mal formatt� par l'utilisateur).
     */
    private void reportUserError(final Exception exception) {
        out.flush();
        if (in != null) {
            err.print("Erreur � la ligne ");
            err.println(in.getLineNumber());
        }
        err.println(exception.getLocalizedMessage());
    }

    /**
     * Proc�de � l'extraction des valeurs.
     */
    private void process(final String[] descriptors) throws CatalogException, SQLException, IOException {
        /*
         * Etablit les connexions � la base de donn�es.
         */
        final SpatioTemporalCoverage3D[] coverages = new SpatioTemporalCoverage3D[descriptors.length];
        final Observations observations = Observations.getDefault();
        for (int i=0; i<descriptors.length; i++) {
            final String name = descriptors[i];
            Coverage coverage = null;
            if (fromModel) {
                coverage = observations.getModelCoverage(name);
            }
            if (coverage == null) try {
                coverage = observations.getDescriptorCoverage(name);
            } catch (NoSuchRecordException exception) {
                reportUserError(exception);
                return;
            }
            coverages[i] = new SpatioTemporalCoverage3D(name, coverage);
        }
        /*
         * Pr�pare une fois pour toute les objets qui seront n�cessaires, et configure les formatteurs.
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
         * Proc�de � la lecture des lignes du p�riph�rique d'entr�e standard.
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
             * Proc�de � l'extraction des valeurs et �crit vers le p�riph�rique de sortie standard.
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
                out.print(Utilities.spaces(precision+6 - n.length()));
                out.print(n);
            }
            out.println();
        }
        in.close();
        in = null;
    }

    /**
     * Affiche les valeurs pour tous les descripteurs �num�r�s.
     */
    public static void main(String[] descriptors) {
        final Extractor extractor = new Extractor(descriptors);
        descriptors = extractor.getRemainingArguments(Integer.MAX_VALUE);
        if (descriptors.length == 0) {
            extractor.err.println("Des descripteurs doivent �tre sp�cifi�s en argument.");
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
