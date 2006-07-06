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

import java.sql.SQLException;


/**
 * Marque les {@linkplain Table tables} qui peuvent être partagées par plusieurs utilisateurs.
 * Par "plusieurs utilisateurs", on entend notamment d'autres {@linkplain Table tables}. Par
 * exemple une même instance de {@link MetadataTable} peut être utilisée en interne par plusieurs
 * instances de {@link StationTable}.
 * <p>
 * Les tables partageables ne doivent pas être paramètrables. Autrement dit, elle ne doivent contenir
 * aucun <cite>beans</cite> (c'est-à-dire aucune paire de méthodes {@code getXXX} et {@code setXXX}).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Shareable {
}
