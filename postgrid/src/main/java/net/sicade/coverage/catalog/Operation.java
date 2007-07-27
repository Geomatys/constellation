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
package net.sicade.coverage.catalog;

import org.opengis.coverage.Coverage;
import net.sicade.observation.Procedure;


/**
 * Une opération à appliquer sur les images d'une même {@linkplain Layer couche}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Operation extends Procedure {
    /**
     * Retourne le préfix à utiliser dans les noms composites. Les noms composites
     * seront de la forme "<cite>operation - paramètre - temps</cite>", par exemple
     * <code>"&nabla;SST<sub>-15</sub>"</code>. Dans l'exemple précédent, le préfix
     * est <code>"&nabla;"</code>.
     */
    String getPrefix();

    /**
     * Applique l'opération sur une image.
     *
     * @param  coverage L'image sur laquelle appliquer l'opération.
     * @return Le résultat de l'opération appliquée sur l'image.
     */
    Coverage doOperation(Coverage coverage);




    /**
     * Une opération qui délègue son travail à une autre instance de {@link Operation}. Cette
     * classe est utile lorsque l'on ne souhaite redéfinir qu'une ou deux méthodes, notamment
     * {@link #doOperation}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends net.sicade.coverage.catalog.Proxy implements Operation {
        /**
         * Pour compatibilités entre les enregistrements binaires de différentes versions.
         */
        private static final long serialVersionUID = -2285791043646792332L;

        /**
         * L'opération envelopée.
         */
        protected final Operation parent;

        /**
         * Construit une nouvelle opération enveloppant l'opération spécifiée.
         */
        protected Proxy(final Operation parent) {
            this.parent = parent;
        }

        /**
         * Retourne l'opération envelopée.
         */
        public Operation getParent() {
            return parent;
        }

        /**
         * Retourne le préfix à utiliser dans les noms composites.
         * L'implémentation par défaut délègue le travail au {@linkplain #getParent parent}.
         */
        public String getPrefix() {
            return parent.getPrefix();
        }

        /**
         * Applique l'opération sur une image.
         * L'implémentation par défaut délègue le travail au {@linkplain #getParent parent}.
         */
        public Coverage doOperation(final Coverage coverage) {
            return parent.doOperation(coverage);
        }
    }
}
