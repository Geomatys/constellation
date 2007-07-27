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
package net.sicade.coverage.catalog.sql;

// J2SE dependencies
import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.AbstractList;
import java.util.RandomAccess;
import java.io.Serializable;

// Sicade dependencies
import net.sicade.coverage.catalog.Descriptor;
import net.sicade.coverage.catalog.LinearModel;


/**
 * Implémentation d'une entrée représentant une {@linkplain LinearModel modèle linéaire}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see LinearModelEntry
 */
final class LinearModelTerm extends AbstractList<Descriptor> implements LinearModel.Term, Serializable, RandomAccess {
    /**
     * Numéro de série pour compatibilité entre différentes versions.
     */
    private static final long serialVersionUID = 5840967233186035005L;

    /**
     * Le coefficient <var>C</var> de ce terme.
     */
    private final double coefficient;

    /**
     * Les descripteurs du paysage océanique composant ce terme. Ce tableau doit être soit de
     * type {@code String[]}, ou soit de type {@code Descriptor[]}. Le premier cas n'est que
     * temporaire, le temps pour {@link LinearModelTable} de terminer la construction de la
     * liste des termes avant de construire les objets {@link Descriptor} qui y sont associés.
     *
     * @see #getDescriptorNames
     * @see #setDescriptors
     * @see #getDescriptors
     */
    private Object[] descriptors;

    /**
     * Le nom de ce terme. Ne sera construit que lorsque nécessaire.
     */
    private transient String name;

    /**
     * Construit un terme d'un modèle linéaire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptor  Le descripteurs du paysage océanique à multiplier par le coefficient.
     */
    public LinearModelTerm(final double    coefficient,
                           final Descriptor descriptor)
    {
        this(coefficient, new Descriptor[] {descriptor});
    }

    /**
     * Construit un terme d'un modèle linéaire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage océanique composant ce terme.
     */
    public LinearModelTerm(final double coefficient,
                           final Collection<Descriptor> descriptors)
    {
        this(coefficient, descriptors.toArray(new Descriptor[descriptors.size()]));
    }

    /**
     * Construit un terme d'un modèle linéaire.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage océanique composant ce terme.
     *                    Ce tableau ne sera pas cloné.
     */
    LinearModelTerm(final double       coefficient,
                    final Descriptor[] descriptors)
    {
        this.coefficient = coefficient;
        this.descriptors = descriptors;
    }

    /**
     * Construit un terme d'un modèle linéaire à partir de noms de descripteurs, plutôt que des
     * descripteurs complètement formés. Il est de la responsabilité de {@link LinearModelTable}
     * de terminer la construction des descripteurs après l'appel de cette méthode.
     *
     * @param coefficient Le coefficient <var>C</var> de ce terme.
     * @param descriptors Les descripteurs du paysage océanique composant ce terme.
     *                    Ce tableau ne sera pas cloné.
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
     * Construit un nouveau terme qui sera égal au produit des deux termes spécifiés, mais en
     * omettant le descripteur du terme 1 qui se trouve à l'index {@code omit1}. Ce constructeur
     * est utilisé par {@link LinearModelEntry#substitute}.
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
     * Retourne les noms des descripteurs, afin de pouvoir compléter leur construction.
     * Cette méthode est réservée à un usage interne par {@link LinearModelTable} seulement.
     */
    final String[] getDescriptorNames() {
        return (String[]) descriptors;
    }

    /**
     * Définit le tableau des descripteurs. Le tableau ne sera pas cloné.
     * Cette méthode est réservée à un usage interne par {@link LinearModelTable} seulement.
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
                // Ne conserve pas le résultat dans le champ 'name'.
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
     * Retourne le descripteur à l'index spécifié.
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
     * Retourne un numéro à peu près unique représentant ce terme.
     */
    @Override
    public int hashCode() {
        final long coeff = Double.doubleToLongBits(coefficient);
        int code = (int)coeff ^ (int)(coeff >>> 32);
        code ^= Arrays.hashCode(descriptors);
        return code;
    }

    /**
     * Vérifie si cet objet est égal à l'objet spécifié.
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
