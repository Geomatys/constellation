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
package org.constellation.coverage.model;

import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.Locale;
import java.io.Writer;
import java.io.IOException;
import java.text.NumberFormat;

import org.geotoolkit.io.TableWriter;
import org.geotoolkit.util.Utilities;
import org.constellation.coverage.catalog.Layer;
import org.geotoolkit.util.Strings;


/**
 * Implémentation d'une entrée représentant une {@linkplain LinearModel modèle linéaire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class LinearModelEntry extends ModelEntry implements LinearModel {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 3859434295505042982L;

    /**
     * Les termes d'un modèle linéaire calculant le paramètre.
     */
    private final List<Term> terms;

    /**
     * Une vue non-modifiable de la liste des termes.
     */
    private final List<Term> unmodifiable;

    /**
     * Les descripteurs. La liste ne sera construite que la première fois où elle sera nécessaire.
     */
    private transient List<Descriptor> descriptors;

    /**
     * Pour chaque terme du modèle linéaire, les index des descripteurs à utiliser dans la
     * liste {@link #descriptors}. Ne sera construit que la première fois où ces index seront
     * nécessaires.
     */
    private transient int[][] index;

    /**
     * Les coefficients extrait de la liste {@link #terms} une fois pour toute.
     */
    private transient double[] coefficients;

    /**
     * Construit un modèle linéaire.
     *
     * @param target  La couche dans laquelle seront stockées les valeurs de la variable dépendante.
     * @param terms   Les termes d'un modèle linéaire calculant le paramètre.
     */
    public LinearModelEntry(final Layer      target,
                            final List<Term> terms)
    {
        super(target);
        this.terms = new ArrayList<Term>(terms);
        unmodifiable = Collections.unmodifiableList(this.terms);
    }

    /**
     * Vérifie que les deux listes spécifiées contiennent les même éléments, mais sans tenir compte
     * de l'ordre. Cette opération n'est valide que si l'opération appliquée entre chaque item est
     * commucatif (par exemple la multiplication). Cette comparaison est différente de celle effectuée
     * par {@link Set#equals} du fait que les listes contiennent parfois des doublons.
     */
    private static boolean equalsIgnoreOrder(final List<Descriptor> d1, final List<Descriptor> d2) {
        if (d1 == d2) {
            return true;
        }
        if (d1.size() != d2.size()) {
            return false;
        }
        final List<Descriptor> stack = new LinkedList<Descriptor>(d2);
        for (final Descriptor d : d1) {
            if (!stack.remove(d)) {
                return false;
            }
        }
        return stack.isEmpty();
    }

    /**
     * Ajoute le terme spécifié à ce modèle linéaire. S'il existe un terme <var>t</var> dont le
     * {@linkplain Term#getDescriptors produit de descripteurs} est le même que celui de {@code
     * term}, alors le {@linkplain Term#getCoefficient coefficient} de {@code term} sera ajouté
     * au coefficient de ce terme <var>t</var>. Sinon, {@code term} sera simplement ajouté à la
     * liste des termes de ce modèle.
     * <p>
     * Cette méthode ne devrait être appelée que pendant la phase de construction du modèle
     * linéaire. Une fois cette construction terminée, le modèle ne devrait plus être modifié.
     */
    protected void add(final Term term) {
        final List<Descriptor> searchFor = term.getDescriptors();
        for (int i=terms.size(); --i>=0;) {
            final Term candidate = terms.get(i);
            final List<Descriptor> termDescriptors = candidate.getDescriptors();
            if (equalsIgnoreOrder(searchFor, termDescriptors)) {
                final double sum = term.getCoefficient() + candidate.getCoefficient();
                final Term t = new LinearModelTerm(sum, termDescriptors);
                terms.set(i, t);
                termsChanged();
                return;
            }
        }
        terms.add(term);
        termsChanged();
    }

    /**
     * Remplace toutes les occurences du descripteur spécifié par la somme des termes spécifié.
     *
     * @param  descriptor Descripteur à rechercher.
     * @param  expansion  Termes remplaçant le descripteur spécifié.
     * @return Nombre d'occurences du descripteurs.
     * @throws IllegalArgumentException si {@code expansion} contient le descripteur à remplacer.
     */
    protected int substitute(final Descriptor descriptor, final Term[] expansion)
            throws IllegalArgumentException
    {
        /*
         * Vérification des arguments.
         */
        if (!descriptor.getDistribution().isIdentity()) {
            throw new IllegalArgumentException("Distribution non-supportée.");
        }
        for (final Term term : expansion) {
            final List<Descriptor> termDescriptors = term.getDescriptors();
            if (termDescriptors.contains(descriptor)) {
                throw new IllegalArgumentException("Recursivité détectée.");
            }
            for (final Descriptor check : termDescriptors) {
                if (!check.getDistribution().isIdentity()) {
                    throw new IllegalArgumentException("Distribution non-supportée.");
                }
            }
        }
        /*
         * Procède à la substitution.
         */
        int count=0, previous;
        do {
            previous = count;
            for (int i=terms.size(); --i>=0;) {
                final Term term = terms.get(i);
                final List<Descriptor> termDescriptors = term.getDescriptors();
                for (int toReplace=termDescriptors.size(); --toReplace>=0;) {
                    if (descriptor.equals(termDescriptors.get(toReplace))) {
                        /*
                         * Nous avons trouvé un terme qui contient le descripteur recherché.  Supprime
                         * le terme trouvé (puisqu'il sera remplacé par une somme de termes) et ajoute
                         * tous les termes spécifiés en argument multipliés par l'ancien terme (sans
                         * le descripteur que l'on remplace).
                         */
                        if (terms.remove(i) != term) {
                            throw new AssertionError(); // Paranoiac check
                        }
                        for (final Term substitution : expansion) {
                            // Ajoute term*substitution, mais dans lequel on a retiré le descripteur
                            // de 'term' qui se trouve à l'index 'toReplace'.
                            final Term product = new LinearModelTerm(term, substitution, toReplace);
                            add(product);
                        }
                        count++;
                        break;
                    }
                }
            }
        } while (previous != count);
        return count;
    }

    /**
     * Prévient cette implémentation que la liste des termes a changée.
     */
    private void termsChanged() {
        descriptors  = null;
        index        = null;
        coefficients = null;
    }

    /**
     * {inheritDoc}
     */
    public List<Term> getTerms() {
        return unmodifiable;
    }

    /**
     * {inheritDoc}
     */
    public synchronized List<Descriptor> getDescriptors() {
        if (descriptors == null) {
            final Set<Descriptor> d = new LinkedHashSet<Descriptor>();
            for (final Term t : getTerms()) {
                d.addAll(t.getDescriptors());
            }
            final Descriptor[] da = d.toArray(new Descriptor[d.size()]);
            Arrays.sort(da, Descriptor.TIME_ORDER);
            descriptors = Collections.unmodifiableList(Arrays.asList(da));
        }
        return descriptors;
    }

    /**
     * {inheritDoc}
     */
    public double evaluate(final double[] values) {
        int[][] index = this.index;
        if (index == null) {
            /*
             * Construit les index. Note: il n'est pas nécessaire de synchroniser;
             * ce n'est pas grave si les index sont construit deux fois, puisque le
             * résultat devrait être identique à chaque exécution.
             */
            int termIndice = 0;
            final Map<Descriptor,Integer> descriptors = new HashMap<Descriptor,Integer>();
            for (final Descriptor d : getDescriptors()) {
                if (descriptors.put(d, termIndice++) != null) {
                    throw new AssertionError(d); // Should never happen.
                }
            }
            assert termIndice == descriptors.size();
            termIndice = 0;
            final List<Term> terms = getTerms();
            final double[] coeff = new double[terms.size()];
            index = new int[coeff.length][];
            for (final Term term : terms) {
                final List<Descriptor> termDescriptors = term.getDescriptors();
                final int[] indices = new int[termDescriptors.size()];
                int i = 0;
                for (final Descriptor d : termDescriptors) {
                    indices[i++] = descriptors.get(d);
                }
                index[termIndice] = indices;
                coeff[termIndice++] = term.getCoefficient();
            }
            assert termIndice == index.length;
            this.index = index;
            this.coefficients = coeff;
        }
        /*
         * Calcule maintenant le modèle linéaire en utilisant les index créés précédements.
         * La plupart du temps, l'exécution de cette méthode commencera directement ici sans
         * exécuter tout le bloc précédent.
         */
        double sum = 0;
        for (int i=0; i<index.length; i++) {
            double product = coefficients[i];
            final int[] indices = index[i];
            assert indices.length == getTerms().get(i).getDescriptors().size();
            for (int j=0; j<indices.length; j++) {
                product *= values[indices[j]];
            }
            sum += product;
        }
        return sum;
    }

    /**
     * {@inheritDoc}
     */
    public void print(final Writer out, final Locale locale) throws IOException {
        /*
         * Obtient le nombre maximal de descripteurs par terme.
         */
        int count = 0;
        final List<Term> terms = getTerms();
        for (final Term term : terms) {
            final int n = term.getDescriptors().size();
            if (n > count) {
                count = n;
            }
        }
        /*
         * Obtient le nombre maximal de symboles (par exemple le symbole de l'opérateur
         * nabla) devant les noms de descripteurs de chaque colonnes.
         */
        final int[] leadingSymbols = new int[count];
        for (final Term term : terms) {
            int i=0;
            for (final Descriptor descriptor : term.getDescriptors()) {
                final int n = leadingSymbols(descriptor.getName());
                if (n > leadingSymbols[i]) {
                    leadingSymbols[i] = n;
                }
                i++;
            }
        }
        /*
         * Procède au formattage.
         */
        final NumberFormat format = (locale != null) ? NumberFormat.getNumberInstance(locale)
                                                     : NumberFormat.getNumberInstance();
        format.setMinimumFractionDigits(9);
        format.setMaximumFractionDigits(9);
        final TableWriter table = new TableWriter(out, 1);
        int remainingColumns = -1;
        for (final Term term : terms) {
            if (remainingColumns >= 0) {
                while (--remainingColumns >= 0) {
                    table.nextColumn();
                }
                table.write('+');
                table.nextLine();
            }
            table.setAlignment(TableWriter.ALIGN_RIGHT);
            table.write(format.format(term.getCoefficient()));
            table.nextColumn();
            table.setAlignment(TableWriter.ALIGN_LEFT);
            final List<Descriptor> descriptors = term.getDescriptors();
            int i=0;
            for (final Descriptor descriptor : descriptors) {
                final String name = descriptor.getName();
                table.write('\u00D7'); // Multiplication sign
                table.write(Strings.spaces(leadingSymbols[i] - leadingSymbols(name) + 1));
                table.write(name);
                table.nextColumn();
                i++;
            }
            remainingColumns = count - descriptors.size();
        }
        table.nextLine();
        table.flush();
    }

    /**
     * Retourne le nombre de caractères autre qu'une lettre au début du symbole spécifié.
     */
    private static int leadingSymbols(final String name) {
        final int length = name.length();
        int i; for (i=0; i<length; i++) {
            if (Character.isLetter(name.charAt(i))) {
                break;
            }
        }
        return i;
    }

    /**
     * Vérifie si cet objet est égal à l'objet spécifié.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final LinearModelEntry that = (LinearModelEntry) object;
            return Utilities.equals(this.terms, that.terms);
        }
        return false;
    }
}
