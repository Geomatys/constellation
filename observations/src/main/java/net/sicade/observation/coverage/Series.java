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
import java.util.Set;
import java.util.Date;

// OpenGIS dependencies
import org.opengis.coverage.Coverage;
import org.opengis.metadata.extent.GeographicBoundingBox;

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.Phenomenon;
import net.sicade.observation.Procedure;
import net.sicade.observation.Observable;
import net.sicade.observation.CatalogException;


/**
 * Repr�sentation une s�rie d'images. Chaque s�rie d'images portent sur un
 * {@linkplain Phenomenon ph�nom�ne} (par exemple la temp�rature) observ� � l'aide d'une certaine
 * {@linkplain Procedure proc�dure} (par exemple une synth�se des donn�es de plusieurs satellites
 * NOAA). Chaque s�rie d'images �tant la combinaison d'un ph�nom�ne avec une proc�dure, elles
 * forment donc des {@linkplain Observable observables}.
 * <p>
 * Dans le contexte particulier des s�ries d'images, le <cite>ph�nom�ne</cite> est appel�
 * {@linkplain Thematic th�matique}.
 * <p>
 * Des op�rations suppl�mentaires peuvent �tre appliqu�es sur une s�rie d'image. Par exemple une
 * s�rie peut repr�senter des images de temp�ratures, et un calcul statistique peut travailler
 * sur les gradients de ces images de temp�rature. Aux yeux d'entit�s de plus haut niveau (tels
 * les {@linkplain Descriptor descripteurs du paysage oc�anique}), une s�rie d'images peut donc
 * �tre consid�r�e comme un ph�nom�ne � combiner avec une autre proc�dure, en l'occurence une
 * {@linkplain Operation op�ration}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public interface Series extends Observable, Phenomenon {
    /**
     * Retourne la th�matique de cette s�rie d'images. Des exemples de th�matiques sont
     * la <cite>temp�rature</cite>, l'<cite>anomalie de la hauteur de l'eau</cite>,
     * la <cite>concentration en chlorophylle-a</cite>, etc.
     */
    Thematic getPhenomenon();

    /**
     * Retourne la proc�dure utilis�e pour collecter les images. Par exemple il peut d'agir d'une
     * synth�se des donn�es capt�es par plusieurs satellites NOAA.
     */
    Procedure getProcedure();

    /**
     * Une s�rie de second recours qui peut �tre utilis�e si aucune donn�es n'est disponible
     * dans cette s�rie � une certaine position spatio-temporelle. Retourne {@code null} s'il
     * n'y a pas de s�rie de second recours.
     */
    Series getFallback();

    /**
     * Retourne les sous-ensembles de cette s�ries.
     */
    Set<SubSeries> getSubSeries();

    /**
     * Retourne l'intervalle de temps typique entre deux images cons�cutives de cette s�rie.
     * Cette information n'est qu'� titre indicative. L'intervalle est exprim�e en nombre de
     * jours. Cette m�thode retourne {@link Double#NaN} si l'intervalle de temps est inconnu.
     */
    double getTimeInterval();

    /**
     * Retourne la plage de temps englobeant toutes les images de cette s�rie.
     *
     * @throws CatalogException si le catalogue n'a pas pu �tre interrog�.
     */
    DateRange getTimeRange() throws CatalogException;

    /**
     * Retourne les coordonn�es g�ographiques englobeant toutes les images de cette s�rie.
     *
     * @throws CatalogException si le catalogue n'a pas pu �tre interrog�.
     */
    GeographicBoundingBox getGeographicBoundingBox() throws CatalogException;

    /**
     * Retourne une image appropri�e pour la date sp�cifi�e.
     *
     * @throws CatalogException si le catalogue n'a pas pu �tre interrog�.
     */
    CoverageReference getCoverageReference(Date time) throws CatalogException;

    /**
     * Retourne la liste des images disponibles dans la plage de coordonn�es spatio-temporelles
     * de cette s�rie. Les images ne seront pas imm�diatement charg�es; seules des r�f�rences
     * vers ces images seront retourn�es.
     *
     * @return Liste d'images qui interceptent la plage de temps et la r�gion g�ographique d'int�r�t.
     * @throws CatalogException si le catalogue n'a pas pu �tre interrog�.
     */
    Set<CoverageReference> getCoverageReferences() throws CatalogException;

    /**
     * Retourne une vue des donn�es de cette s�ries sous forme de fonction. Chaque valeur peut
     * �tre �valu�e � une position (<var>x</var>,<var>y</var>,<var>t</var>), en faisant intervenir
     * des interpolations si n�cessaire. Cette m�thode retourne une fonction moins �labor�e que
     * celle de {@link Descriptor#getCoverage} pour les raisons suivantes:
     * <p>
     * <ul>
     *   <li>Il n'y a ni {@linkplain Operation op�ration}, ni {@link LocationOffset d�calage
     *       spatio-temporel} d'appliqu�s sur les donn�es � �valuer.</li>
     *   <li>Les valeurs sont �valu�es directement sur les images de cette s�rie, jamais sur
     *       celles de la {@linkplain #getFallback s�rie de second recours}.</li>
     *   <li>Des images enti�res peuvent �tre transiter sur le r�seau, plut�t que seulement
     *       les valeurs � �valuer.</li>
     * </ul>
     *
     * @throws CatalogException si la fonction n'a pas pu �tre construite.
     */
    Coverage getCoverage() throws CatalogException;

    /**
     * Si cette s�rie est le r�sultat d'un mod�le num�rique, retourne ce mod�le.
     * Sinon, retourne {@code null}.
     *
     * @throws CatalogException si la base de donn�es n'a pas pu �tre construite.
     */
    Model getModel() throws CatalogException;
}
