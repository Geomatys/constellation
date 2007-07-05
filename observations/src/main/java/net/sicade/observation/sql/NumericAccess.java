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
package net.sicade.observation.sql;

import net.sicade.observation.Element;


/**
 * Marque les {@linkplain SingletonTable tables} dont les entrées peuvent être accédées à partir
 * d'un identifiant numérique. Ces identifiants numériques ne sont pas nécessairement identiques
 * aux identifiants textuels.
 * <p>
 * En général, les enregistrements des tables n'ont qu'un identifiant textuel (le
 * {@linkplain Element#getName nom}), et les identifiants numériques sont inutiles. Une exception
 * à cette règle s'applique aux types d'enregistrements qui peuvent être référencés des millions de
 * fois dans une autre table. Dans ce cas, il peut être utile d'avoir à la fois un identifiant
 * lisible destiné à l'opérateur humain, et un identifiant numérique court (16 bits) pour référencer
 * cet enregistrement de manière plus efficace dans la table qui contient des millions d'enregistrements.
 * <p>
 * Dans la pratique, les principales tables qui implémentent cette interface sont celles qui sont
 * référencées par la {@linkplain ObservationTable table des observations} (habituellement la seule
 * table dont le nombre d'enregistrements en vaut la peine).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @see SingletonTable#getEntry(int)
 * @see SingletonTable#getQuery(QueryType)
 */
public interface NumericAccess {
}
