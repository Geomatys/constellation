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
package net.sicade.sie.type.bbox;

// J2SE dependencies
import java.io.IOException;

// OpenIDE dependencies
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;


/**
 * Lecteur de fichiers {@code .bbox} représentant une région d'intérêt. Ce lecteur construira des
 * objets {@link DataFile} à partir des fichiers {@code .bbox}, que l'on lira un à la fois.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Loader extends UniFileLoader {
    /**
     * Pour compatibilité avec différentes versions de cette classe.
     */
    private static final long serialVersionUID = -5699394674609456107L;

    /**
     * Le type MIME des fichiers {@code .bbox} qui seront à lire.
     */
    public static final String REQUIRED_MIME = "application/x-bbox";

    /**
     * L'extension des fichiers, incluant le point. Cette extension doit être cohérente
     * avec l'extension déclarée dans le fichier {@code BoundingBoxResolver.xml}.
     */
    static final String EXTENSION = "bbox";

    /**
     * Le nom par défaut des nouvelles régions géographiques qui seront créées.
     * Ce nom est utilisé par {@link AddAction}, qui y ajoutera un suffix tel
     * que {@code " (1)"} si le nom sans suffix est déjà utilisé.
     */
    static final String DEFAULT_BASE_NAME = "Nouvelle région";

    /**
     * Construit un lecteur par défaut.
     */
    public Loader() {
        super("net.sicade.sie.type.bbox.DataFile");
    }

    /**
     * Initialise ce lecteur en enregistrant son type MIME. Cette méthode est appelée
     * automatiquement par le système et n'a pas besoin d'être appelée directement.
     */
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    /**
     * Retourne le nom par défaut de ce lecteur. Ce nom sera retourné par
     * {@link #getDisplayName getDisplayName()}, sauf si l'utilisateur l'a
     * modifié avec {@link #setDisplayName setDisplayName(...)}.
     */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(Loader.class, "LBL_BoundingBox_loader_name");
    }

    /**
     * Retourne le nom du contexte où seront listées les actions dans le fichier {@code layer.xml}.
     * Pour ce lecteur, ce nom est {@code "Loaders/application/x-bbox/Actions"} et s'interprète
     * comme un chemin menant à la liste des actions dans le fichier {@code layer.xml}.
     */
    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }

    /**
     * Construit un objet représentant les données du fichier spécifié.
     *
     * @param  primaryFile le fichier {@code .bbox} à lire.
     * @return Un objet {@link DataFile} représentant les données sélectionnées.
     * @throws DataObjectExistsException Si un object {@link DataFile} existe déjà
     *         pour le fichier spécifié.
     * @throws IOException si une erreur est survenue lors de la lecture du fichier {@code .bbox}.
     */
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
            throws DataObjectExistsException, IOException
    {
        return new DataFile(primaryFile, this);
    }
}
