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
import java.io.ObjectInputStream;
import java.io.InvalidClassException;

// OpenIDE dependencies
import org.openide.nodes.Node;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;

/**
 * Repr�sente une r�gion g�ographique d'int�r�t tel que sp�cifi�e dans un fichier {@code .bbox}.
 * Cette r�gion d'int�r�t d�termine les s�ries de donn�es qui seront disponibles (exemple: SST
 * LAC autour de l'�le de la R�union, SST LAC autour de la Nouvelle-Cal�donie, <cite>etc.</cite>).
 * Les objets {@code DataFile} sont construits par {@link Loader} et repr�sent�s visuellement par
 * {@link RootNode}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class DataFile extends MultiDataObject implements SaveCookie {
    /**
     * La r�gion spatio-temporelle d'int�r�t.
     */
    final BoundingBox bbox;

    /**
     * Construit une r�gion g�ographique d'int�r�t � partir du fichier {@code .bbox} sp�cifi�.
     *
     * @param  primaryFile le fichier {@code .bbox} � lire.
     * @param  loader Le lecteur qui a demand� la construction de cet objet.
     * @throws DataObjectExistsException Si un object {@code DataFile} existe d�j�
     *         pour le fichier sp�cifi�.
     * @throws IOException si une erreur est survenue lors de la lecture du fichier {@code .bbox}.
     */
    public DataFile(final FileObject primaryFile, final Loader loader)
            throws DataObjectExistsException, IOException
    {
        super(primaryFile, loader);
        final ObjectInputStream in = new ObjectInputStream(primaryFile.getInputStream());
        try {
            bbox = (BoundingBox) in.readObject();
        } catch (ClassNotFoundException cause) {
            final InvalidClassException e = new InvalidClassException(cause.getLocalizedMessage());
            e.initCause(cause);
            throw e;
        }
        in.close();
        bbox.prefetch();
        bbox.setName(primaryFile.getName());
    }

    /**
     * Retourne un noeud qui repr�sentera graphiquement cette r�gion d'int�r�t dans l'arborescence
     * de l'explorateur. Le noeud retourn� sera une instance de {@link RootNode}.
     */
    @Override
    protected Node createNodeDelegate() {
        return new RootNode(this);
    }

    /**
     * Enregistre l'enveloppe dans son fichier binaire.
     */
    public void save() throws IOException {
        bbox.save(getPrimaryFile());
        setModified(false);
    }
}
