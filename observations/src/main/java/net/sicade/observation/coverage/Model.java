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

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.observation.CatalogException;


/**
 * Interface de base des mod�les lin�aires ou non-lin�aires.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Model extends Element {
    /**
     * Retourne la s�rie dans laquelle seront stock�es les valeurs de la variable d�pendante <var>y</var>.
     * C'est la s�rie des images qui seront produites � partir de ce mod�le.
     */
    Series getTarget();

    /**
     * Retourne l'ensemble des descripteurs utilis�s comme entr�es au mod�le. Cet ensemble est
     * ordonn�; � chaque index correspond une valeur r�elle qui sera donn�e � la m�thode
     * {@link #evaluate}.
     */
    List<Descriptor> getDescriptors();

    /**
     * {@linkplain Distribution#normalize Normalise} toutes les donn�es sp�cifi�es. Il est de la
     * responsabilit� de l'utilisateur d'appeller cette m�thode exactement une fois avant d'appeller
     * la m�thode {@link #evaluate evaluate}. La normalisation est faite sur place.
     */
    void normalize(double[] values);

    /**
     * Calcule la valeur pr�dite � partir des valeurs donn�es en entr�s. Chaque valeur � un index
     * <var>i</var> doit correspondre au descripteur � ce m�me index <var>i</var> dans l'ensemble
     * retourn�e par {@link #getDescriptors}.
     */
    double evaluate(double[] values);

    /**
     * Retourne une couverture qui �valuera le mod�le aux positions spatio-temporelles qui lui
     * seront donn�es.
     *
     * @throws CatalogException si la couverture n'a pas pu �tre construite.
     */
    Coverage asCoverage() throws CatalogException;
}
