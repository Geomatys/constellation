/*
 * Sicade - Syst�mes int�gr�s de connaissances
 *          pour l'aide � la d�cision en environnement
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
 * Convertit des noms de fichiers en requ�tes SQL pour insertion dans la
 * base de donn�es d'images. La liste des fichiers est attendue sur le
 * p�riph�rique d'entr�e standard, et les instructions SQL sont �crites
 * vers le p�riph�rique de sortie standard. Les noms de fichiers doivent
 * avoir la syntaxe suivante:
 * <ul>
 *   <li>Ils peuvent commencer par n'importe quelle suite de caract�res
 *       alphab�tiques.</li>
 *   <li>A la premi�re s�quence de chiffres trouv�es, trois cas de figures : 
 *      <li> les 4 premiers repr�sentent l'ann�e et les 3 suivants le jour julien.</li>
 *      <li> les 4 premiers repr�sentent l'ann�e, les 2 suivants le mois et les 2 derniers le jour.</li>
 *      <li> les 4 premiers repr�sentent l'ann�e et les 3 suivants le jour julien (de d�but), les 
 *           4 suivants repr�sentent l'ann�e et les 3 derniers le jour julien (de fin).</li>
 *   </li>
 *   <li>Le premier caract�re suivant la s�quence de chiffres pr�c�dement mentionn�s
 *       doit �tre un caract�re non-num�rique, sinon une erreur sera lev�e.
 *       Tous les caract�res suivants (notamment l'extension du fichier)
 *       seront ignor�s.</li>
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
            System.out.println("  - Identifiant de la sous-s�rie");
            System.out.println("  - Identifiant de la couverture g�ographique");
            System.out.println("  - D�calage (en jours) entre la date du fichier celle de d�but");
            System.out.println("  - Dur�e (en jours) de la synth�se");
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
         * Parcourt chaque ligne re�ue du p�riph�rique d'entr� standard,
         * en ignorant les lignes vierges. Les espaces au d�but et � la
         * seront ignor�s.
         */
        String filename;
        while ((filename=in.readLine()) != null) {
            filename = filename.trim();
            final int length = filename.length();
            if (length == 0) {
                continue;
            }
            /*
             * Ignore tous les lettres au d�but du nom de fichier, puis rep�re
             * tous les chiffres jusqu'au premier symbole non-num�rique. La plage
             * d'index (dans la cha�ne de caract�re) qui contient des chiffres ira
             * de 'lower' inclusivement jusqu'� 'upper' exclusivement.
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
                System.err.print("(Un minimum de 5 chiffres �tait attendu): ");
                System.err.println(filename);
                System.exit(1);
                return;
            }
            int stop = filename.indexOf('.', upper);
            if (stop < 0) {
                stop = length;
            }
            
            /* 
             * Convertit l'ann�e et le jour julien (ou l'ann�e, le mois et le jour) en date, puis 
             * �crit cette date au format attendu par le langage SQL.
             * Si upper - lower = 7 alors le format de la date est aaaajjj avec aaaa = ann�e et jjj le jour julien.
             * Si upper - lower = 8 alors le format de la date est aaaammjj avec aaaa = ann�e mm = mois et jj = jour.
             * Si upper - lower = 14 alors le format de la date est aaaajjjaaaajjj avec 
             * aaaa = ann�e et jjj = jour julien (r�p�t� car une fois date de d�but puis date de fin).
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
