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

// J2SE dependencies
import java.util.List;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.util.InternationalString;
import org.opengis.spatialschema.geometry.DirectPosition;

// Sicade dependencies
import net.sicade.observation.CatalogException;


/**
 * Couverture de donn�es dans un espace spatio-temporelle. L'aspect "dynamique" vient de l'axe
 * temporel. Sur le plan de l'impl�mentation, chaque appel � une m�thode {@code evaluate} � une
 * date diff�rente se traduira typiquement par le chargement d'une nouvelle image.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface DynamicCoverage extends Coverage {
    /**
     * Retourne un nom pour cette couverture.
     */
    InternationalString getName();

    /**
     * Retourne les coordonn�es au centre du voxel le plus proche des coordonn�es sp�cifi�es.
     * Cette m�thode recherche l'image la plus proche de la date sp�cifi�e, puis recherche le
     * pixel qui contient la coordonn�e g�ographique sp�cifi�e. La date de milieu de l'image,
     * ainsi que les coordonn�es g�ographiques au centre du pixel, sont retourn�es. Appeller
     * la m�thode {@link #evaluate evaluate} avec les coordonn�es retourn�es devrait permettre
     * d'obtenir une valeur non-interpoll�e.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation de la
     *         base de donn�es.
     */
    DirectPosition snap(DirectPosition position) throws CatalogException;

    /**
     * Retourne les couvertures utilis�es par les m�thodes {@code evaluate} pour le temps <var>t</var>
     * sp�cifi�. L'ensemble retourn� comprendra typiquement 0, 1 ou 2 �l�ments.
     *
     * @throws CatalogException si une erreur est survenue lors de l'interrogation de la
     *         base de donn�es.
     */
    List<Coverage> coveragesAt(double t) throws CatalogException;
}
