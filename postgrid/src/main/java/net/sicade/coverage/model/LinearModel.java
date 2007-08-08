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
package net.sicade.coverage.model;

import java.util.List;
import java.util.Locale;
import java.io.Writer;
import java.io.IOException;
import net.sicade.catalog.Element;


/**
 * A linear model.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface LinearModel extends Model {
    /**
     * Returns the terms of this linear model. Note that a term may contains a product
     * of more than one descriptor, like the last one in the example below:
     *
     * <p align="center">{@code PP} = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;{@code SST} +
     * <var>C</var><sub>2</sub>&times;{@code SLA} +
     * <var>C</var><sub>3</sub>&times;{@code SST}&times;{@code SLA} + ...</p>
     *
     * Each term on the right side of {@code =} is represented by a
     * {@link net.sicade.observation.coverage.LinearModel.Term} instance.
     * The term contains a <var>C</var><sub>n</sub> coefficient, often
     * computed from a multiple linear regression.
     *
     * @return An immutable list of all terms in this linear model.
     */
    List<Term> getTerms();

    /**
     * Prints the linear model to the specified writer.
     *
     * @param  out The stream where to write.
     * @param  locale The locale to use for formatting, or {@code null} for the default locale.
     * @throws IOException if an error occured while writting to the stream.
     */
    void print(final Writer out, final Locale locale) throws IOException;

    /**
     * A linear model term. A {@linkplain LinearModel linear model} can be represented in the
     * following form:
     *
     * <p align="center"><var>y</var> = <var>C</var><sub>0</sub> +
     * <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub> +
     * <var>C</var><sub>2</sub>&times;<var>x</var><sub>2</sub> +
     * <var>C</var><sub>3</sub>&times;<var>x</var><sub>3</sub> + ...</p>
     *
     * where <var>C</var><sub>0</sub> is an instance of {@code LinearModel.Term},
     * <var>C</var><sub>1</sub>&times;<var>x</var><sub>1</sub> is an other instance of
     * {@code LinearModel.Term}, <cite>etc.</cite>
     * <p>
     * The <var>x</var><sub>1</sub>, <var>x</var><sub>2</sub>, <cite>etc.</cite> independant
     * variables are {@linkplain Descriptor descriptors} derived from {@linkplain Layer layers}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public interface Term extends Element {
        /**
         * Returns the coefficient <var>C</var> for this term. This coefficient
         * is often the result of a multiple linear regression.
         */
        double getCoefficient();

        /**
         * Returns the descriptors for this term. In most cases, the list contains exactly one element.
         * However this interface allows more elements to be returned. For example a term could be
         * <var>C</var>&times;{@code SST}&times;{@code SLA} where <var>C</var> is the {@linkplain
         * #getCoefficient coefficient} determined from a linear regression, while {@code SST} and
         * {@code SLA} are {@linkplain Distribution#normalize normalized values} of <cite>Sea
         * Surface Temperature</cite> and <cite>Sea Level Anomaly</cite> respectively.
         */
        List<Descriptor> getDescriptors();
    }
}
