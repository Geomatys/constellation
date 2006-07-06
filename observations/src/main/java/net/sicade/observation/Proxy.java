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
package net.sicade.observation;

import java.io.Serializable;
import org.geotools.resources.Utilities;


/**
 * Classe de base des �l�ments qui d�l�gueront tous ou une partir de leur travail � un
 * autre �l�ment. Ces classes sont utiles lorsque l'on veut modifier seulement quelques
 * aspect du travail d'un {@linkplain #getParent �l�ment parent}.
 * <p>
 * Les proxy peuvent �tre enregistr�s en binaires si l'{@linkplain #getParent �l�ment parent}
 * le peut aussi.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public abstract class Proxy implements Element, Serializable {
    /**
     * Pour compatibilit� entre des enregistrements binaires de versions diff�rentes.
     */
    private static final long serialVersionUID = 6984331646382641188L;

    /**
     * Construit un proxy.
     */
    public Proxy() {
    }

    /**
     * Retourne l'�l�ment envelopp� par ce proxy.
     */
    public abstract Element getParent();

    /**
     * Retourne le nom de cet �l�ment. L'impl�mentation par d�faut d�l�gue le travail
     * au {@linkplain #getParent parent}.
     */
    public String getName() {
        return getParent().getName();
    }

    /**
     * Retourne des remarques s'appliquant � cette entr�e, ou {@code null} s'il n'y en a pas.
     * L'impl�mentation par d�faut d�l�gue le travail au {@linkplain #getParent parent}.
     */
    public String getRemarks() {
        return getParent().getRemarks();
    }

    /**
     * Retourne le nom de cet �l�ment.
     */
    public String toString() {
        return getName();
    }

    /**
     * Retourne un code pour cet �l�ment. L'impl�mentation par d�faut calcule un code � partir
     * de celui du {@linkplain #getParent parent}.
     */
    @Override
    public int hashCode() {
        return getParent().hashCode() ^ (int)serialVersionUID;
    }

    /**
     * Compare cet objet avec l'objet sp�cifi�. L'impl�mentation par d�faut v�rifie si les deux
     * objets sont de la m�me classe et leurs {@linkplain #getParent parents} sont eux-m�me �gaux.
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
