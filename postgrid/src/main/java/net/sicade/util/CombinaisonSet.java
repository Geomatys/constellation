/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
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
 */
package net.sicade.util;

import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;
import java.util.AbstractSet;
import java.util.LinkedHashSet;

import org.geotools.resources.Arguments;


/**
 * Un ensemble immutable formé par toutes les combinaisons possibles des éléments d'un autre
 * ensemble.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
public class CombinaisonSet extends AbstractSet<String> {
    /**
     * Degrés de l'ensemble. 0 pour un ensemble vide, 1 pour contenir les
     * éléments tel quels, 2 pour contenir toutes les paires possibles, 3
     * pour contenir tous les trios possibles, etc.
     */
    private final int degree;

    /**
     * La liste des éléments à combiner entre eux.
     */
    private String[] elements;

    /**
     * Caractères à utiliser entre les éléments à l'intérieur d'une combinaison.
     * Par défaut, il s'agit du symbole de multiplication représenté par un point.
     */
    private String separator = "\u22C5";

    /**
     * Construit un ensemble représentant les paires de tous les éléments de l'ensemble
     * spécifié.
     *
     * @param elements Les éléments dont on veut fabriquer des paires.
     */
    public CombinaisonSet(final Set<String> elements) {
        this(elements, 2);
    }

    /**
     * Construit un ensemble représentant les combinaisons de tous les éléments de l'ensemble
     * spécifié.
     *
     * @param elements Les éléments dont on veut fabriquer des combinaisons.
     * @param degree Degrés de l'ensemble: 0 pour un ensemble vide, 1 pour contenir les
     *               éléments tel quels, 2 pour contenir toutes les paires possibles, 3
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
     * Retourne un itérateur qui baleira toutes les combinaisons présentes dans cet ensemble.
     */
    public Iterator<String> iterator() {
        return new Iter();
    }

    /**
     * Implémentation de l'itérateur. Les chaînes de caractères représentant les éléments
     * sont fabriqués à la volée.
     */
    private final class Iter implements Iterator<String> {
        /**
         * Position courante de l'itérateur. Le premier élément de ce tableau variera
         * le plus lentement, tandis que le dernier élément variera le plus vite.
         */
        private final int[] index;

        /**
         * La longueur de {@link #buffer} après l'ajout de chaque élément.
         */
        private final int[] bufferBreakPoints;

        /**
         * Buffer à utiliser pour construire les chaînes de caractères.
         */
        private final StringBuffer buffer = new StringBuffer();

        /**
         * Le premier index dans le tableau {@link #index} qui a changé. Sera utilisé pour
         * déterminer à partir de quel élément le {@link #buffer} aura besoin d'être reconstruit.
         */
        private int changedIndex;

        /**
         * Construit un itérateur.
         */
        public Iter() {
            index             = new int[degree];
            bufferBreakPoints = new int[index.length];
        }

        /**
         * Indique si l'itérateur a un autre élément à retourner.
         */
        public boolean hasNext() {
            return index.length!=0 && index[0]<elements.length;
        }

        /**
         * Fabrique et retourne le prochain élément.
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
         * Opération non-supportée, puisque l'ensemble est immutable.
         */
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Retourne le séparateur à utiliser entre deux éléments d'une combinaison. Le séparateur
     * par défaut est "&times;". Ainsi, si cet ensemble comprend les éléments "E1" et "E2",
     * alors les combinaisons seront formattées comme "E1&times;E1", "E1&times;E2" et
     * "E2&times;E2".
     *
     * @return Le séparateur à utiliser entre deux éléments d'une combinaison.
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * Spécifie le séparateur à utiliser entre deux éléments d'une combinaison.
     *
     * @param separator Le séparateur à utiliser.
     * @throws IllegalArgumentException si {@code separator} est nul.
     */
    public void setSeparator(final String separator) throws IllegalArgumentException {
        if (separator == null) {
            throw new IllegalArgumentException();
        }
        this.separator = separator;
    }

    /**
     * Affiche toutes les combinaisons des éléments spécifiés. Les arguments suivants peuvent
     * être spécifiés sur la ligne de commande:
     *
     * <ul>
     *   <li>{@code -separator} Le caractère à utiliser comme séparateur ("&times;" par défaut);</li>
     * </ul>
     *
     * Les autres arguments sont interprétés comme autant d'éléments à combiner. Le résultat des
     * combinaisons est envoyé sur le périphérique de sortie standard.
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
