/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.image.io;

// J2SE dependencies
import java.net.URL;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;


/**
 * Classe de base des fournisseurs de décodeurs {@link FileBasedReader}.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class FileBasedReaderSpi extends ImageReaderSpi {
    /**
     * List of legal input types for {@link AbstractReader}.
     */
    private static final Class[] INPUT_TYPES = new Class[] {File.class, URL.class};

    /**
     * Construit une nouvelle instance de ce fournisseur de service.
     */
    public FileBasedReaderSpi() {
        inputTypes = INPUT_TYPES;
        vendorName = "Institut de Recherche pour le Développement";
    }

    /**
     * Retourne le type d'image que créera le décodeur HDF. Ce type d'image comprend généralement
     * une palette de couleurs qui dépend du type de données (SST, CHL...), et donc de la classe
     * dérivée.
     *
     * @throws IOException si la palette de couleur n'a pas pu être obtenue.
     */
    protected abstract ImageTypeSpecifier getRawImageType() throws IOException;
}
