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
package net.sicade.observation.coverage;

import org.opengis.coverage.Coverage;
import net.sicade.observation.Procedure;


/**
 * Une op�ration � appliquer sur les images d'une m�me {@linkplain Series series}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Antoine Hnawia
 */
public interface Operation extends Procedure {
    /**
     * Retourne le pr�fix � utiliser dans les noms composites. Les noms composites
     * seront de la forme "<cite>operation - param�tre - temps</cite>", par exemple
     * <code>"&nabla;SST<sub>-15</sub>"</code>. Dans l'exemple pr�c�dent, le pr�fix
     * est <code>"&nabla;"</code>.
     */
    String getPrefix();

    /**
     * Applique l'op�ration sur une image.
     *
     * @param  coverage L'image sur laquelle appliquer l'op�ration.
     * @return Le r�sultat de l'op�ration appliqu�e sur l'image.
     */
    Coverage doOperation(Coverage coverage);




    /**
     * Une op�ration qui d�l�gue son travail � une autre instance de {@link Operation}. Cette
     * classe est utile lorsque l'on ne souhaite red�finir qu'une ou deux m�thodes, notamment
     * {@link #doOperation}.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    public static class Proxy extends net.sicade.observation.Proxy implements Operation {
        /**
         * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
         */
        private static final long serialVersionUID = -2285791043646792332L;

        /**
         * L'op�ration envelop�e.
         */
        protected final Operation parent;

        /**
         * Construit une nouvelle op�ration enveloppant l'op�ration sp�cifi�e.
         */
        protected Proxy(final Operation parent) {
            this.parent = parent;
        }

        /**
         * Retourne l'op�ration envelop�e.
         */
        public Operation getParent() {
            return parent;
        }

        /**
         * Retourne le pr�fix � utiliser dans les noms composites.
         * L'impl�mentation par d�faut d�l�gue le travail au {@linkplain #getParent parent}.
         */
        public String getPrefix() {
            return parent.getPrefix();
        }

        /**
         * Applique l'op�ration sur une image.
         * L'impl�mentation par d�faut d�l�gue le travail au {@linkplain #getParent parent}.
         */
        public Coverage doOperation(final Coverage coverage) {
            return parent.doOperation(coverage);
        }
    }
}
