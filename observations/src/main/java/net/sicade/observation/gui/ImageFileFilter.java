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
package net.sicade.observation.gui;

// Collections
import java.util.List;
import java.util.Locale;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;

// Other J2SE dependencies
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.IIOException;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.swing.filechooser.FileFilter;

// Geotools dependencies
import org.geotools.resources.Utilities;


/**
 * Filtre des fichiers en fonction du type d'images d�sir�.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ImageFileFilter extends FileFilter {
    /**
     * Format d'image de ce filtre.
     */
    private final ImageReaderWriterSpi spi;

    /**
     * Nom et extensions de ce format d'image.
     */
    private final String name;

    /**
     * Liste des extensions des fichiers qu'accepte ce filtre, ou {@code null}
     * si les extensions ne sont pas connues. Ces extensions ne devraient pas commencer
     * par de point ('.').
     */
    private final String[] suffix;

    /**
     * Construit un filtre d'images.
     *
     * @param spi    Objet d�crivant un format d'image.
     * @param name   Un des noms du format.
     * @param suffix Liste des extensions des fichiers qu'accepte ce filtre.
     *               Ces extensions ne devraient pas commencer par le point ('.').
     */
    private ImageFileFilter(final ImageReaderWriterSpi spi, final String name, final String[] suffix) {
        this.spi    = spi;
        this.suffix = suffix;
        if (suffix!=null && suffix.length>=1) {
            final String separator = System.getProperty("path.separator", ":");
            final StringBuilder buffer = new StringBuilder(name);
            buffer.append(" (");
            for (int i=0; i<suffix.length; i++) {
                if (suffix[i].startsWith(".")) {
                    suffix[i] = suffix[i].substring(1);
                }
                if (i != 0) {
                    buffer.append(separator);
                }
                buffer.append("*.");
                buffer.append(suffix[i]);
            }
            buffer.append(')');
            this.name = buffer.toString();
        } else {
            this.name = name;
        }
    }

    /**
     * Retourne une liste de filtres pour les lecture d'images. Les �l�ments de la liste appara�tront dans
     * l'ordre alphab�tique de leur description, en ignorant les diff�rences entre majuscules et minuscules.
     *
     * @param locale Langue dans laquelle retourner les descriptions des filtres,
     *               ou {@code null} pour utiliser les conventions locales.
     */
    public static ImageFileFilter[] getReaderFilters(final Locale locale) {
        return getFilters(ImageReaderSpi.class, locale);
    }

    /**
     * Retourne une liste de filtres pour les �critures d'images. Les �l�ments de la liste appara�tront dans
     * l'ordre alphab�tique de leur description, en ignorant les diff�rences entre majuscules et minuscules.
     *
     * @param locale Langue dans laquelle retourner les descriptions des filtres,
     *               ou {@code null} pour utiliser les conventions locales.
     */
    public static ImageFileFilter[] getWriterFilters(final Locale locale) {
        return getFilters(ImageWriterSpi.class, locale);
    }

    /**
     * Retourne une liste de filtres d'images. Les �l�ments de la liste appara�tront dans l'ordre
     * alphab�tique de leur description, en ignorant les diff�rences entre majuscules et minuscules.
     *
     * @param category Cat�gorie des filtres d�sir�s (lecture ou �criture).
     * @param loc Langue dans laquelle retourner les descriptions des filtres,
     *            ou {@code null} pour utiliser les conventions locales.
     */
    private static <T extends ImageReaderWriterSpi> ImageFileFilter[] getFilters(final Class<T> category, final Locale loc) {
        final Locale locale = (loc!=null) ? loc : Locale.getDefault();
        final List<ImageFileFilter> set = new ArrayList<ImageFileFilter>();
        for (final Iterator<? extends ImageReaderWriterSpi> it=IIORegistry.getDefaultInstance().getServiceProviders(category, false); it.hasNext();) {
            final ImageReaderWriterSpi spi = it.next();
            final String       description = spi.getDescription(locale);
            final String[]          suffix = spi.getFileSuffixes();
            set.add(new ImageFileFilter(spi, description, suffix));
        }
        final ImageFileFilter[] array = set.toArray(new ImageFileFilter[set.size()]);
        Arrays.sort(array, new Comparator<ImageFileFilter>() {
            public int compare(final ImageFileFilter a, final ImageFileFilter b) {
                return a.name.toLowerCase(locale).compareTo(b.name.toLowerCase(locale));
            }
        });
        return array;
    }

    /**
     * Construit et retourne un objet qui lira les images dans le format de ce filtre.
     * Cette m�thode ne peut �tre appel�e que si ce filtre a �t� construit par un appel
     * � {@link #getReaderFilters}.
     *
     * @return Un d�codeur � utiliser pour lire les images.
     * @throws IOException si le d�codeur n'a pas pu �tre construit.
     */
    public ImageReader getImageReader() throws IOException {
        if (spi instanceof ImageReaderSpi) {
            return ((ImageReaderSpi) spi).createReaderInstance();
        } else {
            throw new IIOException(spi.toString());
        }
    }

    /**
     * Construit et retourne un objet qui �crira les images dans le format de ce filtre.
     * Cette m�thode ne peut �tre appel�e que si ce filtre a �t� construit par un appel
     * � {@link #getWriterFilters}.
     *
     * @return Un codeur � utiliser pour �crire les images.
     * @throws IOException si le codeur n'a pas pu �tre construit.
     */
    public ImageWriter getImageWriter() throws IOException {
        if (spi instanceof ImageWriterSpi) {
            return ((ImageWriterSpi) spi).createWriterInstance();
        } else {
            throw new IIOException(spi.toString());
        }
    }

    /**
     * Retourne une extension par d�faut pour les noms de fichiers
     * de ce format d'image. La cha�ne retourn�e ne commencera pas
     * par un point.
     *
     * @return L'extension, ou {@code null} si l'extension n'est pas connue.
     */
    public String getExtension() {
        String ext = null;
        if (suffix != null) {
            int length = -1;
            for (final String cmp : suffix) {
                final int cmpl = cmp.length();
                if (cmpl > length) {
                    length = cmpl;
                    ext = cmp;
                }
            }
        }
        return ext;
    }

    /**
     * Indique si ce filtre accepte le fichier sp�cifi�.
     */
    public boolean accept(final File file) {
        if (file != null) {
            if (suffix == null) {
                return true;
            }
            final String filename = file.getName();
            final int length = filename.length();
            if (length>0 && filename.charAt(0)!='.') {
                if (file.isDirectory()) {
                    return true;
                }
                int i = filename.lastIndexOf('.');
                if (i>0 && i<length-1) {
                    final String extension = filename.substring(i);
                    for (String s : suffix) {
                        if (s.equalsIgnoreCase(extension)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Retourne la description de ce filtre. La description comprendra le
     * nom du format des images accept�es ainsi que leurs extensions.
     */
    public String getDescription() {
        return name;
    }

    /**
     * Retourne une cha�ne de caract�res d�crivant ce filtre.
     * Cette information ne sert qu'� des fins de d�boguage.
     */
    @Override
    public String toString() {
        return Utilities.getShortClassName(this)+'['+name+']';
    }

    /**
     * Envoie vers le p�riph�rique de sortie standard une
     * liste des filtres disponibles par d�faut. La liste
     * est construites � partir des encodeurs et d�codeurs
     * fournit sur le syst�me.
     */
    public static void main(final String[] args) {
        final ImageFileFilter[] filters = getReaderFilters(null);
        for (int i=0; i<filters.length; i++) {
            System.out.println(filters[i]);
        }
    }
}
