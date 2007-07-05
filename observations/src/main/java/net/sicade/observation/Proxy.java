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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.observation;

import java.io.Serializable;
import org.geotools.resources.Utilities;


/**
 * Classe de base des éléments qui délègueront tous ou une partir de leur travail à un
 * autre élément. Ces classes sont utiles lorsque l'on veut modifier seulement quelques
 * aspect du travail d'un {@linkplain #getParent élément parent}.
 * <p>
 * Les proxy peuvent être enregistrés en binaires si l'{@linkplain #getParent élément parent}
 * le peut aussi.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Proxy implements Element, Serializable {
    /**
     * Pour compatibilité entre des enregistrements binaires de versions différentes.
     */
    private static final long serialVersionUID = 6984331646382641188L;

    /**
     * Construit un proxy.
     */
    public Proxy() {
    }

    /**
     * Retourne l'élément enveloppé par ce proxy.
     */
    public abstract Element getParent();

    /**
     * Retourne le nom de cet élément. L'implémentation par défaut délègue le travail
     * au {@linkplain #getParent parent}.
     */
    public String getName() {
        return getParent().getName();
    }

    /**
     * Retourne des remarques s'appliquant à cette entrée, ou {@code null} s'il n'y en a pas.
     * L'implémentation par défaut délègue le travail au {@linkplain #getParent parent}.
     */
    public String getRemarks() {
        return getParent().getRemarks();
    }

    /**
     * Retourne le nom de cet élément.
     */
    public String toString() {
        return getName();
    }

    /**
     * Retourne un code pour cet élément. L'implémentation par défaut calcule un code à partir
     * de celui du {@linkplain #getParent parent}.
     */
    @Override
    public int hashCode() {
        return getParent().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * Compare cet objet avec l'objet spécifié. L'implémentation par défaut vérifie si les deux
     * objets sont de la même classe et leurs {@linkplain #getParent parents} sont eux-même égaux.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (object!=null && object.getClass().equals(getClass())) {
            final Proxy that = (Proxy) object;
            return Utilities.equals(this.getParent(), that.getParent());
        }
        return false;
    }
}
