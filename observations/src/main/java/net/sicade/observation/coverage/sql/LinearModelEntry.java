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
package net.sicade.observation.coverage.sql;

// J2SE dependencies
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

// Geotools dependencies
import org.geotools.io.TableWriter;
import org.geotools.resources.Utilities;

// Sicade dependencies
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LinearModel;
import net.sicade.observation.coverage.LinearModel.Term;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain LinearModel mod�le lin�aire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class LinearModelEntry extends ModelEntry implements LinearModel {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 3859434295505042982L;

    /**
     * Les termes d'un mod�le lin�aire calculant le param�tre.
     */
    private final List<Term> terms;

    /**
     * Une vue non-modifiable de la liste des termes.
     */
    private final List<Term> unmodifiable;

    /**
     * Les descripteurs. La liste ne sera construite que la premi�re fois o� elle sera n�cessaire.
     */
    private transient List<Descriptor> descriptors;

    /**
     * Pour chaque terme du mod�le lin�aire, les index des descripteurs � utiliser dans la
     * liste {@link #descriptors}. Ne sera construit que la premi�re fois o� ces index seront
     * n�cessaires.
     */
    private transient int[][] index;

    /**
     * Les coefficients extrait de la liste {@link #terms} une fois pour toute.
     */
    private transient double[] coefficients;

    /**
     * Construit un mod�le lin�aire.
     *
     * @param target  La s�rie dans laquelle seront stock�es les valeurs de la variable d�pendante.
     * @param terms   Les termes d'un mod�le lin�aire calculant le param�tre.
     */
    public LinearModelEntry(final Series     target,
                            final List<Term> terms)
    {
        super(target);
        this.terms = new ArrayList<Term>(terms);
        unmodifiable = Collections.unmodifiableList(this.terms);
    }

    /**
     * V�rifie que les deux listes sp�cifi�es contiennent les m�me �l�ments, mais sans tenir compte
     * de l'ordre. Cette op�ration n'est valide que si l'op�ration appliqu�e entre chaque item est
     * commucatif (par exemple la multiplication). Cette comparaison est diff�rente de celle effectu�e
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
     * Ajoute le terme sp�cifi� � ce mod�le lin�aire. S'il existe un terme <var>t</var> dont le
     * {@linkplain Term#getDescriptors produit de descripteurs} est le m�me que celui de {@code
     * term}, alors le {@linkplain Term#getCoefficient coefficient} de {@code term} sera ajout�
     * au coefficient de ce terme <var>t</var>. Sinon, {@code term} sera simplement ajout� � la
     * liste des termes de ce mod�le.
     * <p>
     * Cette m�thode ne devrait �tre appel�e que pendant la phase de construction du mod�le
     * lin�aire. Une fois cette construction termin�e, le mod�le ne devrait plus �tre modifi�.
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
     * Remplace toutes les occurences du descripteur sp�cifi� par la somme des termes sp�cifi�.
     *
     * @param  descriptor Descripteur � rechercher.
     * @param  expansion  Termes rempla�ant le descripteur sp�cifi�.
     * @return Nombre d'occurences du descripteurs.
     * @throws IllegalArgumentException si {@code expansion} contient le descripteur � remplacer.
     */
    protected int substitute(final Descriptor descriptor, final Term[] expansion)
            throws IllegalArgumentException
    {
        /*
         * V�rification des arguments.
         */
        if (!descriptor.getDistribution().isIdentity()) {
            throw new IllegalArgumentException("Distribution non-support�e.");
        }
        for (final Term term : expansion) {
            final List<Descriptor> termDescriptors = term.getDescriptors();
            if (termDescriptors.contains(descriptor)) {
                throw new IllegalArgumentException("Recursivit� d�tect�e.");
            }
            for (final Descriptor check : termDescriptors) {
                if (!check.getDistribution().isIdentity()) {
                    throw new IllegalArgumentException("Distribution non-support�e.");
                }
            }
        }
        /*
         * Proc�de � la substitution.
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
                         * Nous avons trouv� un terme qui contient le descripteur recherch�.  Supprime
                         * le terme trouv� (puisqu'il sera remplac� par une somme de termes) et ajoute
                         * tous les termes sp�cifi�s en argument multipli�s par l'ancien terme (sans
                         * le descripteur que l'on remplace).
                         */
                        if (terms.remove(i) != term) {
                            throw new AssertionError(); // Paranoiac check
                        }
                        for (final Term substitution : expansion) {
                            // Ajoute term*substitution, mais dans lequel on a retir� le descripteur
                            // de 'term' qui se trouve � l'index 'toReplace'.
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
     * Pr�vient cette impl�mentation que la liste des termes a chang�e.
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
             * Construit les index. Note: il n'est pas n�cessaire de synchroniser;
             * ce n'est pas grave si les index sont construit deux fois, puisque le
             * r�sultat devrait �tre identique � chaque ex�cution.
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
         * Calcule maintenant le mod�le lin�aire en utilisant les index cr��s pr�c�dements.
         * La plupart du temps, l'ex�cution de cette m�thode commencera directement ici sans
         * ex�cuter tout le bloc pr�c�dent.
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
         * Obtient le nombre maximal de symboles (par exemple le symbole de l'op�rateur
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
         * Proc�de au formattage.
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
                table.write(Utilities.spaces(leadingSymbols[i] - leadingSymbols(name) + 1));
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
     * Retourne le nombre de caract�res autre qu'une lettre au d�but du symbole sp�cifi�.
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
     * V�rifie si cet objet est �gal � l'objet sp�cifi�.
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
