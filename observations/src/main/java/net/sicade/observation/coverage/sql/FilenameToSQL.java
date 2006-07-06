/*
 * Sicade - Systèmes intégrés de connaissances
 *          pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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

package net.sicade.observation.coverage.sql;

// J2SE dependencies
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Convertit des noms de fichiers en requêtes SQL pour insertion dans la
 * base de données d'images. La liste des fichiers est attendue sur le
 * périphérique d'entrée standard, et les instructions SQL sont écrites
 * vers le périphérique de sortie standard. Les noms de fichiers doivent
 * avoir la syntaxe suivante:
 * <ul>
 *   <li>Ils peuvent commencer par n'importe quelle suite de caractères
 *       alphabétiques.</li>
 *   <li>A la première séquence de chiffres trouvées, trois cas de figures : 
 *      <li> les 4 premiers représentent l'année et les 3 suivants le jour julien.</li>
 *      <li> les 4 premiers représentent l'année, les 2 suivants le mois et les 2 derniers le jour.</li>
 *      <li> les 4 premiers représentent l'année et les 3 suivants le jour julien (de début), les 
 *           4 suivants représentent l'année et les 3 derniers le jour julien (de fin).</li>
 *   </li>
 *   <li>Le premier caractère suivant la séquence de chiffres précédement mentionnés
 *       doit être un caractère non-numérique, sinon une erreur sera levée.
 *       Tous les caractères suivants (notamment l'extension du fichier)
 *       seront ignorés.</li>
 * </ul>
 *
 * @author Martin Desruisseaux
 * @author Marc Despinoy
 * @author Antoine Hnawia
 */
public class FilenameToSQL {
    
    public static void main(final String[] args) throws IOException {
        if (args.length != 4) {
            System.out.println("Arguments attendus:");
            System.out.println("  - Identifiant de la sous-série");
            System.out.println("  - Identifiant de la couverture géographique");
            System.out.println("  - Décalage (en jours) entre la date du fichier celle de début");
            System.out.println("  - Durée (en jours) de la synthèse");
            System.exit(1);
            return;
        }
        final long offset, duration;
        try {
            offset   = Integer.parseInt(args[2]) * (24*60*60*1000L);
            duration = Integer.parseInt(args[3]) * (24*60*60*1000L);
        } catch (NumberFormatException e) {
            System.err.print("Argument invalide: ");
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
            return;
        }
        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        final Calendar calendar = format.getCalendar();
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        /*
         * Parcourt chaque ligne reçue du périphérique d'entré standard,
         * en ignorant les lignes vierges. Les espaces au début et à la
         * seront ignorés.
         */
        String filename;
        while ((filename=in.readLine()) != null) {
            filename = filename.trim();
            final int length = filename.length();
            if (length == 0) {
                continue;
            }
            /*
             * Ignore tous les lettres au début du nom de fichier, puis repère
             * tous les chiffres jusqu'au premier symbole non-numérique. La plage
             * d'index (dans la chaîne de caractère) qui contient des chiffres ira
             * de 'lower' inclusivement jusqu'à 'upper' exclusivement.
             */
            int lower = 0;
            while (Character.isLetter(filename.charAt(lower))) {
                if (++lower == length) {
                    System.err.print("Nom de fichier invalide ");
                    System.err.print("(Absence de date): ");
                    System.err.println(filename);
                    System.exit(1);
                    return;
                }
            }
            int upper = lower;
            while (++upper<length && Character.isDigit(filename.charAt(upper)));
            if (upper-lower < 5) {
                System.err.print("Nom de fichier invalide ");
                System.err.print("(Un minimum de 5 chiffres était attendu): ");
                System.err.println(filename);
                System.exit(1);
                return;
            }
            int stop = filename.indexOf('.', upper);
            if (stop < 0) {
                stop = length;
            }
            
            /* 
             * Convertit l'année et le jour julien (ou l'année, le mois et le jour) en date, puis 
             * écrit cette date au format attendu par le langage SQL.
             * Si upper - lower = 7 alors le format de la date est aaaajjj avec aaaa = année et jjj le jour julien.
             * Si upper - lower = 8 alors le format de la date est aaaammjj avec aaaa = année mm = mois et jj = jour.
             * Si upper - lower = 14 alors le format de la date est aaaajjjaaaajjj avec 
             * aaaa = année et jjj = jour julien (répété car une fois date de début puis date de fin).
             */
            final int d = upper - lower;
            int an;
            int mois;
            int jour;
            if (d == 7) {
                an      = Integer.parseInt(filename.substring(lower, lower+4));
                mois    = 0;
                jour    = Integer.parseInt(filename.substring(lower+4, upper));
            } else if (d == 8) {
                final int date  = Integer.parseInt(filename.substring(lower, upper));
                an              = date/10000;
                jour            = date - (an*10000);
                mois            = jour/100;
                jour           -= mois*100;
            } else if (d == 14) {
                upper   -= 7;
                an      = Integer.parseInt(filename.substring(lower, lower+4));
                mois    = 0;
                jour    = Integer.parseInt(filename.substring(lower+4, upper));
            } else {
                System.err.print("Nom de fichier invalide : " + filename  + ". ");
                System.err.println("Erreur lors du calcul des dates...");
                System.exit(1);
                return;
            }
            
            calendar.clear();
            mois = (mois != 0) ? (mois - 1) : mois;
            calendar.set(an, mois, jour);
            
            final Date time = calendar.getTime();
            time.setTime(time.getTime() - offset);
            System.out.print("INSERT INTO coverages.\"GridCoverages\" " +
                                "(subseries, filename, \"startTime\", " +
                                "\"endTime\", extent) VALUES ('");
            System.out.print(args[0]);
            System.out.print("', '");
            System.out.print(filename.substring(0, stop));
            System.out.print("', '");
            System.out.print(format.format(time));
            System.out.print("', '");
            time.setTime(time.getTime() + duration);
            System.out.print(format.format(time));
            System.out.print("', '");
            System.out.print(args[1]);
            System.out.println("');");
        }
    }
    
}
