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


/**
 * Indique qu'un enregistrement requis n'a pas �t� trouv� dans la base de donn�es.
 * Cette exception contient le nom de la table dans laquelle l'enregistrement �tait attendu.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class NoSuchRecordException extends CatalogException {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -3105861955682823122L;

    /**
     * Construit une exception signalant qu'un enregistrement n'a pas �t� trouv�.
     *
     * @param message Message d�crivant l'erreur.
     * @param table Nom de la table dans laquelle l'enregistrement �tait attendu, ou {@code null} si inconnu.
     */
    public NoSuchRecordException(final String message, final String table) {
        super(message, table);
    }
}
