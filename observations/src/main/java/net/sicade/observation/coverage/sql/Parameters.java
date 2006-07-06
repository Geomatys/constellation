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
package net.sicade.observation.coverage.sql;

import java.util.Date;
import java.text.DateFormat;
import java.io.Serializable;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;

import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Operation;


/**
 * Bloc de param�tres pour une table {@link GridCoverageTable}. Les blocs de param�tres doivent
 * �tre immutables. Ce principe d'imutabilit� s'applique aussi aux objets r�f�renc�s par
 * les champs publiques, m�me si ces objets sont en principe mutables ({@link Rectangle2D},
 * {@link Dimension2D}...).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class Parameters implements Serializable {
    /**
     * Num�ro de s�rie (pour compatibilit� avec des versions ant�rieures).
     */
    private static final long serialVersionUID = 6418640591318515042L;

    /**
     * R�ference vers la s�rie d'images. Cette r�f�rence est construite �
     * partir du champ ID dans la table "Series" de la base de donn�es.
     */
    public final Series series;

    /**
     * L'op�ration � appliquer sur les images lue, ou {@code null} s'il n'y en a aucune.
     */
    public final Operation operation;

    /**
     * Format � utiliser pour lire les images.
     */
    public final FormatEntry format;

    /**
     * Chemin relatif des images.
     */
    public final String pathname;

    /**
     * La partie temporelle de {@link #tableCRS}. Ne sera construit que la premi�re fois
     * o� elle sera n�cessaire.
     */
    private transient DefaultTemporalCRS temporalCRS;

    /**
     * Syst�me de r�f�rence des coordonn�es de la table. Le syst�me de coordonn�es de t�te
     * ("head") doit obligatoirement �tre un CRS horizontal. La seconde partie ("tail") sera
     * ignor�e; il s'agira typiquement de l'axe du temps ou de la profondeur.
     */
    public final CoordinateReferenceSystem tableCRS;

    /**
     * Syst�me de r�f�rence des coordonn�es de l'image. Ce sera habituellement
     * (mais pas obligatoirement) le m�me que {@link #tableCRS}.
     */
    public final CoordinateReferenceSystem coverageCRS;

    /**
     * La transformation du syst�me de r�f�rences des coordonn�es de la table
     * ({@link #tableCRS}) vers le syst�me de l'image ({@link #coverageCRS}).
     */
    private transient MathTransform2D tableToCoverageCRS;

    /**
     * Coordonn�es horizontales de la r�gion d'int�ret.  Ces coordonn�es
     * sont exprim�es selon la partie horizontale ("head") du syst�me de
     * coordonn�es {@link #tableCRS}.
     */
    public final Rectangle2D geographicArea;

    /**
     * Dimension logique d�sir�e des pixels de l'images.   Cette information
     * n'est qu'approximative. Il n'est pas garantie que la lecture produira
     * effectivement une image de cette r�solution. Une valeur nulle signifie
     * que la lecture doit se faire avec la meilleure r�solution possible.
     */
    public final Dimension2D resolution;

    /**
     * Root images directory, for access through a local network.
     */
    public final String rootDirectory;

    /**
     * Root URL directory (usually a FTP server), for access through a distant network.
     */
    public final String rootURL;

    /**
     * Encodage des noms de fichiers (typiquement {@code "UTF-8"}), ou {@code null} si aucun
     * encodage ne doit �tre effectu�.
     */
    public final String encoding;

    /**
     * Formatteur � utiliser pour �crire des dates pour l'utilisateur. Les caract�res et
     * les conventions linguistiques d�pendront de la langue de l'utilisateur. Toutefois,
     * le fuseau horaire devrait �tre celui de la r�gion d'�tude plut�t que celui du pays
     * de l'utilisateur.
     */
    private final DateFormat dateFormat;

    /**
     * Construit un bloc de param�tres.
     *
     * @param series R�f�rence vers la s�rie d'images.
     * @param format Format � utiliser pour lire les images.
     * @param pathname Chemin relatif des images.
     * @param operation Op�ration � appliquer sur les images, ou {@code null}.
     * @param tableCRS Syst�me de r�f�rence des coordonn�es de la table. Le syst�me de
     *        coordonn�es de t�te ("head") doit obligatoirement �tre un CRS horizontal.
     * @param coverageCRS Syst�me de r�f�rence des coordonn�es de l'image. Ce sera
     *        habituellement (mais pas obligatoirement) le m�me que {@link #tableCRS}.
     * @param geographicArea Coordonn�es horizontales de la r�gion d'int�ret,
     *        dans le syst�me de r�f�rence des coordonn�es {@code tableCRS}.
     * @param resolution Dimension logique approximative d�sir�e des pixels,
     *        ou {@code null} pour la meilleure r�solution disponible.
     *        Doit �tre exprim� dans le syst�me de coordonn�es {@code tableCRS}.
     * @param dateFormat Formatteur � utiliser pour �crire des dates pour l'utilisateur.
     * @param encoding Encodage des noms de fichiers, ou {@code null} si aucun encodage ne doit �tre
     *        effectu�.
     */
    public Parameters(final Series                    series,
                      final FormatEntry               format,
                      final String                    pathname,
                      final Operation                 operation,
                      final CoordinateReferenceSystem tableCRS,
                      final CoordinateReferenceSystem coverageCRS,
                      final Rectangle2D               geographicArea,
                      final Dimension2D               resolution,
                      final DateFormat                dateFormat,
                      final String                    rootDirectory,
                      final String                    rootURL,
                      final String                    encoding)
    {
        this.series         = series;
        this.format         = format;
        this.pathname       = pathname;
        this.operation      = operation;
        this.tableCRS       = tableCRS;
        this.coverageCRS    = coverageCRS;
        this.geographicArea = geographicArea;
        this.resolution     = resolution;
        this.dateFormat     = dateFormat;
        this.rootDirectory  = rootDirectory;
        this.rootURL        = rootURL;
        this.encoding       = encoding;
    }

    /**
     * Indique si ce bloc de param�tres est identique au bloc sp�cifi�.
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof Parameters) {
            final Parameters that = (Parameters) o;
            return Utilities.equals(this.series         , that.series          ) &&
                   Utilities.equals(this.format         , that.format          ) &&
                   Utilities.equals(this.pathname       , that.pathname        ) &&
                   Utilities.equals(this.operation      , that.operation       ) &&
                   Utilities.equals(this.tableCRS       , that.tableCRS        ) &&
                   Utilities.equals(this.coverageCRS    , that.coverageCRS     ) &&
                   Utilities.equals(this.geographicArea , that.geographicArea  ) &&
                   Utilities.equals(this.resolution     , that.resolution      ) &&
                   Utilities.equals(this.dateFormat     , that.dateFormat      ) &&
                   Utilities.equals(this.rootDirectory  , that.rootDirectory   ) &&
                   Utilities.equals(this.rootURL        , that.rootURL         ) &&
                   Utilities.equals(this.encoding       , that.encoding        );
        }
        return false;
    }

    /**
     * Retourne la partie temporelle de {@link #tableCRS}.
     */
    public DefaultTemporalCRS getTemporalCRS() {
        // Pas besoin de synchroniser; ce n'est pas grave si le m�me CRS est construit deux fois.
        if (temporalCRS == null) {
            temporalCRS = DefaultTemporalCRS.wrap(CRSUtilities.getTemporalCRS(tableCRS));
        }
        return temporalCRS;
    }

    /**
     * Proj�te le rectangle sp�cifi� du syst�me de r�f�rences des coordonn�es de la
     * table ({@link #tableCRS}) vers le syst�me de l'image ({@link #coverageCRS}).
     *
     * @param area Le rectangle � transformer.
     * @param dest Le rectangle dans lequel �crire le r�sultat de la transformation,
     *             <strong>SI</strong> une transformation �tait n�cessaire. La valeur
     *             {@code null} cr�era un nouveau rectangle si n�cessaire.
     * @return Le rectangle transform�, ou {@code area} (et non {@code dest}) si aucune
     *         transformation n'�tait n�cessaire.
     *
     * @todo Attention, getCRS2D ne tient pas compte des dimensions des GridCoverages
     */
    final Rectangle2D tableToCoverageCRS(Rectangle2D area, final Rectangle2D dest)
            throws TransformException
    {
        CoordinateReferenceSystem sourceCRS = tableCRS;
        CoordinateReferenceSystem targetCRS = coverageCRS;
        if (!CRSUtilities.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            sourceCRS = CRSUtilities.getCRS2D(sourceCRS);
            targetCRS = CRSUtilities.getCRS2D(targetCRS);
            if (tableToCoverageCRS == null) try {
                tableToCoverageCRS = (MathTransform2D) CoordinateReferenceSystemTable.TRANSFORMS
                        .createOperation(sourceCRS, targetCRS).getMathTransform();
            } catch (FactoryException exception) {
                throw new TransformException(exception.getLocalizedMessage(), exception);
            }
            area = CRSUtilities.transform(tableToCoverageCRS, area, dest);
        }
        return area;
    }

    /**
     * Formate la date sp�cifi�e.
     */
    public String format(final Date date) {
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    /**
     * Retourne un code repr�sentant ce bloc de param�tres.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (geographicArea != null) code += geographicArea.hashCode();
        if (resolution     != null) code +=     resolution.hashCode();
        return code;
    }
}
