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
 * Lecteur de fichiers {@code .bbox} repr�sentant une r�gion d'int�r�t. Ce lecteur construira des
 * objets {@link DataFile} � partir des fichiers {@code .bbox}, que l'on lira un � la fois.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class Loader extends UniFileLoader {
    /**
     * Pour compatibilit� avec diff�rentes versions de cette classe.
     */
    private static final long serialVersionUID = -5699394674609456107L;

    /**
     * Le type MIME des fichiers {@code .bbox} qui seront � lire.
     */
    public static final String REQUIRED_MIME = "application/x-bbox";

    /**
     * L'extension des fichiers, incluant le point. Cette extension doit �tre coh�rente
     * avec l'extension d�clar�e dans le fichier {@code BoundingBoxResolver.xml}.
     */
    static final String EXTENSION = "bbox";

    /**
     * Le nom par d�faut des nouvelles r�gions g�ographiques qui seront cr��es.
     * Ce nom est utilis� par {@link AddAction}, qui y ajoutera un suffix tel
     * que {@code " (1)"} si le nom sans suffix est d�j� utilis�.
     */
    static final String DEFAULT_BASE_NAME = "Nouvelle r�gion";

    /**
     * Construit un lecteur par d�faut.
     */
    public Loader() {
        super("net.sicade.sie.type.bbox.DataFile");
    }

    /**
     * Initialise ce lecteur en enregistrant son type MIME. Cette m�thode est appel�e
     * automatiquement par le syst�me et n'a pas besoin d'�tre appel�e directement.
     */
    @Override
    protected void initialize() {
        super.initialize();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    /**
     * Retourne le nom par d�faut de ce lecteur. Ce nom sera retourn� par
     * {@link #getDisplayName getDisplayName()}, sauf si l'utilisateur l'a
     * modifi� avec {@link #setDisplayName setDisplayName(...)}.
     */
    @Override
    protected String defaultDisplayName() {
        return NbBundle.getMessage(Loader.class, "LBL_BoundingBox_loader_name");
    }

    /**
     * Retourne le nom du contexte o� seront list�es les actions dans le fichier {@code layer.xml}.
     * Pour ce lecteur, ce nom est {@code "Loaders/application/x-bbox/Actions"} et s'interpr�te
     * comme un chemin menant � la liste des actions dans le fichier {@code layer.xml}.
     */
    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions";
    }

    /**
     * Construit un objet repr�sentant les donn�es du fichier sp�cifi�.
     *
     * @param  primaryFile le fichier {@code .bbox} � lire.
     * @return Un objet {@link DataFile} repr�sentant les donn�es s�lectionn�es.
     * @throws DataObjectExistsException Si un object {@link DataFile} existe d�j�
     *         pour le fichier sp�cifi�.
     * @throws IOException si une erreur est survenue lors de la lecture du fichier {@code .bbox}.
     */
    protected MultiDataObject createMultiObject(final FileObject primaryFile)
            throws DataObjectExistsException, IOException
    {
        return new DataFile(primaryFile, this);
    }
}
