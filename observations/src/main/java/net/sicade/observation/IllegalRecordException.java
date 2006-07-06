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

import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;


/**
 * Indique qu'une incoh�rence a �t� d�tect�e dans un enregistrement d'une table de la base
 * de donn�es. Cette exception peut �tre lev�e par exemple si une valeur n�gative a �t� trouv�e
 * dans un champ qui ne devrait contenir que des valeurs positives, ou si une cl� �trang�re n'a
 * pas �t� trouv�e. Dans plusieurs cas, cette exception ne devrait pas �tre soul�v�e si la base
 * de donn�es � bien v�rifi� toutes les contraintes (par exemple les cl�s �trang�res).
 * <p>
 * Cette exception contient le nom de la table contenant un enregistrement invalide.
 * Ce nom appara�t dans le message format� par {@link #getLocalizedMessage}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class IllegalRecordException extends CatalogException {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -8491590864510381052L;
    
    /**
     * Nom de la table qui contient l'enregistrement invalide, ou {@code null} si inconnu.
     */
    private final String table;

    /**
     * Construit une exception signalant qu'un enregistrement n'est pas valide.
     *
     * @param table Nom de la table qui contient l'enregistrement invalide, ou {@code null} si inconnu.
     * @param message Message d�crivant l'erreur, ou {@code null} si aucun.
     */
    public IllegalRecordException(final String table, final String message) {
        super(message);
        this.table = table;
    }

    /**
     * Construit une exception signalant qu'un enregistrement n'est pas valide.
     *
     * @param table Nom de la table qui contient l'enregistrement invalide ou {@code null} si inconnu.
     * @param exception Exception rencontr�e lors de l'analyse de l'enregistrement.
     */
    public IllegalRecordException(final String table, final Exception exception) {
        this(table, exception.getLocalizedMessage());
        initCause(exception);
    }

    /**
     * Retourne le nom de la table qui contient un enregistrement invalide.
     * Peut retourner {@code null} si le nom de la table n'est pas connu.
     */
    public String getTable() {
        return table;
    }

    /**
     * Retourne une cha�ne de caract�re qui contiendra le
     * nom de la table et un message d�crivant l'erreur.
     */
    @Override
    public String getLocalizedMessage() {
        final String message = super.getLocalizedMessage();
        final String table   = getTable();
        if (table == null) {
            return message;
        }
        return Resources.format(ResourceKeys.TABLE_ERROR_$2, table, message);
    }
}
