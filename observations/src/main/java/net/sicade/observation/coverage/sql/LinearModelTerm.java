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
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.io.Serializable;

// Sicade dependencies
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.LinearModel;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain LinearModel mod�le lin�aire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see LinearModelEntry
 */
final class LinearModelTerm extends AbstractList<Descriptor> implements LinearModel.Term, Serializable, RandomAccess {
    /**
     * Num�ro de s�rie pour compatibilit� entre diff�rentes versions.
     */
    private static final long serialVersionUID = 5840967233186035005L;

    /**
     * Le coefficient <var>C</var> de ce terme.
     */
    private final double coefficient;

    /**
     * Les descripteurs du paysage oc�anique composant ce terme. Ce tableau doit �tre soit de
     * type {@code String[]}, ou soit de type {@code Descriptor[]}. Le premier cas n'est que
     * temporaire, le temps pour {@link LinearModelTable} de terminer la construction de la
     * liste des termes avant de construire les objets {@link Descriptor} qui y sont associ�s.
     *
     * @see #getDescriptorNames
     * @see #setDescriptors
     * @see #getDescriptors
     */
    private Object[] descriptors;

    /**
     * Le nom de ce terme. Ne sera construit que lorsque n�cessaire.
     */
    private transient String name;

    /**
     * Construit un terme d'un mod�le lin�aire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptor  Le descripteurs du paysage oc�anique � multiplier par le coefficient.
     */
    public LinearModelTerm(final double    coefficient,
                           final Descriptor descriptor)
    {
        this(coefficient, new Descriptor[] {descriptor});
    }

    /**
     * Construit un terme d'un mod�le lin�aire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage oc�anique composant ce terme.
     */
    public LinearModelTerm(final double coefficient,
                           final Collection<Descriptor> descriptors)
    {
        this(coefficient, descriptors.toArray(new Descriptor[descriptors.size()]));
    }

    /**
     * Construit un terme d'un mod�le lin�aire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage oc�anique composant ce terme.
     *                    Ce tableau ne sera pas clon�.
     */
    LinearModelTerm(final double       coefficient,
                    final Descriptor[] descriptors)
    {
        this.coefficient = coefficient;
        this.descriptors = descriptors;
    }

    /**
     * Construit un terme d'un mod�le lin�aire � partir de noms de descripteurs, plut�t que des
     * descripteurs compl�tement form�s. Il est de la responsabilit� de {@link LinearModelTable}
     * de terminer la construction des descripteurs apr�s l'appel de cette m�thode.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage oc�anique composant ce terme.
     *                    Ce tableau ne sera pas clon�.
     *
     * @see #getDescriptorNames
     * @see #setDescriptors
     */
    LinearModelTerm(final double   coefficient,
                    final String[] descriptors)
    {
        this.coefficient = coefficient;
        this.descriptors = descriptors;
    }

    /**
     * Construit un nouveau terme qui sera �gal au produit des deux termes sp�cifi�s, mais en
     * omettant le descripteur du terme 1 qui se trouve � l'index {@code omit1}. Ce constructeur
     * est utilis� par {@link LinearModelEntry#substitute}.
     */
    LinearModelTerm(final LinearModel.Term term1, final LinearModel.Term term2, final int omit1) {
        final List<Descriptor> descriptors1 = term1.getDescriptors();
        final List<Descriptor> descriptors2 = term2.getDescriptors();
        final int length1 = descriptors1.size();
        final int length2 = descriptors2.size();
        descriptors = descriptors1.toArray(new Descriptor[length1 + length2 - 1]);
        System.arraycopy(descriptors, omit1+1, descriptors, omit1 + length2, length1 - (omit1+1));
        for (int j=0; j<length2; j++) {
            descriptors[omit1 + j] = descriptors2.get(j);
        }
        coefficient = term1.getCoefficient() * term2.getCoefficient();
    }

    /**
     * Retourne les noms des descripteurs, afin de pouvoir compl�ter leur construction.
     * Cette m�thode est r�serv�e � un usage interne par {@link LinearModelTable} seulement.
     */
    final String[] getDescriptorNames() {
        return (String[]) descriptors;
    }

    /**
     * D�finit le tableau des descripteurs. Le tableau ne sera pas clon�.
     * Cette m�thode est r�serv�e � un usage interne par {@link LinearModelTable} seulement.
     */
    final void setDescriptors(final Descriptor[] descriptors) {
        if (!(this.descriptors instanceof String[])) {
            throw new IllegalStateException();
        }
        if (descriptors.length > this.descriptors.length) {
            throw new IllegalArgumentException();
        }
        this.descriptors = descriptors;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        if (name == null) {
            boolean temporary = false;
            final StringBuilder buffer = new StringBuilder();
            buffer.append(coefficient);
            for (final Object descriptor : descriptors) {
                buffer.append(" \u00D7 "); // Multiplication sign
                if (descriptor instanceof Descriptor) {
                    buffer.append(((Descriptor) descriptor).getName());
                } else {
                    buffer.append(descriptor);
                    temporary = true;
                }
            }
            if (temporary) {
                // Ne conserve pas le r�sultat dans le champ 'name'.
                return buffer.toString();
            }
            name = buffer.toString();
        }
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getRemarks() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public double getCoefficient() {
        return coefficient;
    }

    /**
     * {@inheritDoc}
     */
    public List<Descriptor> getDescriptors() {
        return this;
    }

    /**
     * Retourne le nombre de descripteurs.
     */
    public int size() {
        return descriptors.length;
    }

    /**
     * Retourne le descripteur � l'index sp�cifi�.
     */
    public Descriptor get(final int index) {
        return (Descriptor) descriptors[index];
    }

    /**
     * Retourne les descripteurs qui composent ce terme.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Retourne un num�ro � peu pr�s unique repr�sentant ce terme.
     */
    @Override
    public int hashCode() {
        final long coeff = Double.doubleToLongBits(coefficient);
        int code = (int)coeff ^ (int)(coeff >>> 32);
        code ^= Arrays.hashCode(descriptors);
        return code;
    }

    /**
     * V�rifie si cet objet est �gal � l'objet sp�cifi�.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof LinearModelTerm) {
            final LinearModelTerm that = (LinearModelTerm) object;
            if (Double.doubleToLongBits(this.coefficient) ==
                Double.doubleToLongBits(that.coefficient))
            {
                return Arrays.equals(this.descriptors, that.descriptors);
            }
        }
        return false;
    }
}
