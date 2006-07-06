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
package net.sicade.util;

// J2SE dependencies
import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.LinkedHashSet;

// Geotools dependencies
import org.geotools.resources.Arguments;


/**
 * Un ensemble immutable form� par toutes les combinaisons possibles des �l�ments d'un autre
 * ensemble.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class CombinaisonSet extends AbstractSet<String> {
    /**
     * Degr�s de l'ensemble. 0 pour un ensemble vide, 1 pour contenir les
     * �l�ments tel quels, 2 pour contenir toutes les paires possibles, 3
     * pour contenir tous les trios possibles, etc.
     */
    private final int degree;

    /**
     * La liste des �l�ments � combiner entre eux.
     */
    private String[] elements;

    /**
     * Caract�res � utiliser entre les �l�ments � l'int�rieur d'une combinaison.
     * Par d�faut, il s'agit du symbole de multiplication repr�sent� par un point.
     */
    private String separator = "\u22C5";

    /**
     * Construit un ensemble repr�sentant les paires de tous les �l�ments de l'ensemble
     * sp�cifi�.
     *
     * @param elements Les �l�ments dont on veut fabriquer des paires.
     */
    public CombinaisonSet(final Set<String> elements) {
        this(elements, 2);
    }

    /**
     * Construit un ensemble repr�sentant les combinaisons de tous les �l�ments de l'ensemble
     * sp�cifi�.
     *
     * @param elements Les �l�ments dont on veut fabriquer des combinaisons.
     * @param degree Degr�s de l'ensemble: 0 pour un ensemble vide, 1 pour contenir les
     *               �l�ments tel quels, 2 pour contenir toutes les paires possibles, 3
     *               pour contenir tous les trios possibles, etc.
     */
    public CombinaisonSet(final Set<String> elements, final int degree) {
        this.degree = degree;
        if (degree < 0) {
            throw new IllegalArgumentException(String.valueOf(degree));
        }
        this.elements = elements.toArray(new String[elements.size()]);
    }

    /**
     * Retourne le nombre de combinaisons dans cet ensemble.
     */
    public int size() {
        int c = elements.length;
        switch (degree) {
            case 0: return 0;
            case 1: return c;
            case 2: return c*(c+1)/2;
            case 3: return c*(c*(c+3)+2)/6;
            case 4: return c*(c*(c*(c+6)+11)+6)/24;
            default: throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    /**
     * Retourne un it�rateur qui baleira toutes les combinaisons pr�sentes dans cet ensemble.
     */
    public Iterator<String> iterator() {
        return new Iter();
    }

    /**
     * Impl�mentation de l'it�rateur. Les cha�nes de caract�res repr�sentant les �l�ments
     * sont fabriqu�s � la vol�e.
     */
    private final class Iter implements Iterator<String> {
        /**
         * Position courante de l'it�rateur. Le premier �l�ment de ce tableau variera
         * le plus lentement, tandis que le dernier �l�ment variera le plus vite.
         */
        private final int[] index;

        /**
         * La longueur de {@link #buffer} apr�s l'ajout de chaque �l�ment.
         */
        private final int[] bufferBreakPoints;

        /**
         * Buffer � utiliser pour construire les cha�nes de caract�res.
         */
        private final StringBuffer buffer = new StringBuffer();

        /**
         * Le premier index dans le tableau {@link #index} qui a chang�. Sera utilis� pour
         * d�terminer � partir de quel �l�ment le {@link #buffer} aura besoin d'�tre reconstruit.
         */
        private int changedIndex;

        /**
         * Construit un it�rateur.
         */
        public Iter() {
            index             = new int[degree];
            bufferBreakPoints = new int[index.length];
        }

        /**
         * Indique si l'it�rateur a un autre �l�ment � retourner.
         */
        public boolean hasNext() {
            return index.length!=0 && index[0]<elements.length;
        }

        /**
         * Fabrique et retourne le prochain �l�ment.
         */
        public String next() {
            int i = changedIndex;
            while (true) {
                buffer.append(elements[index[i]]);
                if (++i >= index.length) {
                    break;
                }
                buffer.append(separator);
                bufferBreakPoints[i] = buffer.length();
            }
            final String value = buffer.toString();
            while (++index[--i] >= elements.length) {
                if (i == 0) {
                    break;
                }
            }
            Arrays.fill(index, i+1, index.length, index[i]);
            buffer.setLength(bufferBreakPoints[i]);
            changedIndex = i;
            return value;
        }

        /**
         * Op�ration non-support�e, puisque l'ensemble est immutable.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Retourne le s�parateur � utiliser entre deux �l�ments d'une combinaison. Le s�parateur
     * par d�faut est "&times;". Ainsi, si cet ensemble comprend les �l�ments "E1" et "E2",
     * alors les combinaisons seront formatt�es comme "E1&times;E1", "E1&times;E2" et
     * "E2&times;E2".
     *
     * @return Le s�parateur � utiliser entre deux �l�ments d'une combinaison.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Sp�cifie le s�parateur � utiliser entre deux �l�ments d'une combinaison.
     *
     * @param separator Le s�parateur � utiliser.
     * @throws IllegalArgumentException si {@code separator} est nul.
     */
    public void setSeparator(final String separator) throws IllegalArgumentException {
        if (separator == null) {
            throw new IllegalArgumentException();
        }
        this.separator = separator;
    }
    
    /**
     * Affiche toutes les combinaisons des �l�ments sp�cifi�s. Les arguments suivants peuvent
     * �tre sp�cifi�s sur la ligne de commande:
     *
     * <ul>
     *   <li>{@code -separator} Le caract�re � utiliser comme s�parateur ("&times;" par d�faut);</li>
     * </ul>
     *
     * Les autres arguments sont interpr�t�s comme autant d'�l�ments � combiner. Le r�sultat des
     * combinaisons est envoy� sur le p�riph�rique de sortie standard.
     */
    public static void main(final String[] args) {
        final Arguments  arguments = new Arguments(args);
        final String     separator = arguments.getOptionalString("-separator");
        final Set<String> elements = new LinkedHashSet<String>();
        for (final String element : arguments.getRemainingArguments(Integer.MAX_VALUE)) {
            elements.add(element);
        }
        final Set<String> set = new CombinaisonSet(elements);
        if (separator != null) {
            ((CombinaisonSet) set).setSeparator(separator);
        }
        for (final String element : set) {
            arguments.out.println(element);
        }
    }
}
