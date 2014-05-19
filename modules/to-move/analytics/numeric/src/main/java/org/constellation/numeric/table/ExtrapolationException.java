/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.numeric.table;


/**
 * Exception lancée lorsqu'une extrapolation a été effectuée alors que ce n'était pas permis.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class ExtrapolationException extends Exception {
    /**
     * Pour compatibilité avec des enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = -7105249847450063936L;

    /**
     * Valeur <var>x</var> pour laquelle une interpolation avait été demandée.
     */
    public final double xi;

    /**
     * Construit une exception déclarant que le vecteur
     * des <var>x</var> ne contient pas suffisament de données.
     */
    public ExtrapolationException() {
        super("Le vecteur ne contient pas suffisament de données.");
        xi = Double.NaN;
    }

    /**
     * Construit une exception déclarant que le vecteur des <var>x</var> ne contient pas
     * suffisament de données ou que la donnée <var>xi</var> demandée n'est pas valide,
     * si celle-ci est un NaN.
     *
     * @param xi valeur de <var>x</var> pour laquelle on voulait interpoler un <var>y</var>.
     */
    public ExtrapolationException(final double xi) {
        super(Double.isNaN(xi) ? "Ne peux pas interpoler à x=NaN."
                               : "La donnée demandée est en dehors de la plage de valeurs du vecteur des X.");
        this.xi = xi;
    }
}
