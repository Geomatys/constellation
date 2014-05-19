/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.gui.observation;

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
import org.geotoolkit.util.Utilities;


/**
 * Filtre des fichiers en fonction du type d'images désiré.
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
     * @param spi    Objet décrivant un format d'image.
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
     * Retourne une liste de filtres pour les lecture d'images. Les éléments de la liste apparaîtront dans
     * l'ordre alphabétique de leur description, en ignorant les différences entre majuscules et minuscules.
     *
     * @param locale Langue dans laquelle retourner les descriptions des filtres,
     *               ou {@code null} pour utiliser les conventions locales.
     */
    public static ImageFileFilter[] getReaderFilters(final Locale locale) {
        return getFilters(ImageReaderSpi.class, locale);
    }

    /**
     * Retourne une liste de filtres pour les écritures d'images. Les éléments de la liste apparaîtront dans
     * l'ordre alphabétique de leur description, en ignorant les différences entre majuscules et minuscules.
     *
     * @param locale Langue dans laquelle retourner les descriptions des filtres,
     *               ou {@code null} pour utiliser les conventions locales.
     */
    public static ImageFileFilter[] getWriterFilters(final Locale locale) {
        return getFilters(ImageWriterSpi.class, locale);
    }

    /**
     * Retourne une liste de filtres d'images. Les éléments de la liste apparaîtront dans l'ordre
     * alphabétique de leur description, en ignorant les différences entre majuscules et minuscules.
     *
     * @param category Catégorie des filtres désirés (lecture ou écriture).
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
     * Cette méthode ne peut être appelée que si ce filtre a été construit par un appel
     * à {@link #getReaderFilters}.
     *
     * @return Un décodeur à utiliser pour lire les images.
     * @throws IOException si le décodeur n'a pas pu être construit.
     */
    public ImageReader getImageReader() throws IOException {
        if (spi instanceof ImageReaderSpi) {
            return ((ImageReaderSpi) spi).createReaderInstance();
        } else {
            throw new IIOException(spi.toString());
        }
    }

    /**
     * Construit et retourne un objet qui écrira les images dans le format de ce filtre.
     * Cette méthode ne peut être appelée que si ce filtre a été construit par un appel
     * à {@link #getWriterFilters}.
     *
     * @return Un codeur à utiliser pour écrire les images.
     * @throws IOException si le codeur n'a pas pu être construit.
     */
    public ImageWriter getImageWriter() throws IOException {
        if (spi instanceof ImageWriterSpi) {
            return ((ImageWriterSpi) spi).createWriterInstance();
        } else {
            throw new IIOException(spi.toString());
        }
    }

    /**
     * Retourne une extension par défaut pour les noms de fichiers
     * de ce format d'image. La chaîne retournée ne commencera pas
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
     * Indique si ce filtre accepte le fichier spécifié.
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
     * nom du format des images acceptées ainsi que leurs extensions.
     */
    public String getDescription() {
        return name;
    }

    /**
     * Retourne une chaîne de caractères décrivant ce filtre.
     * Cette information ne sert qu'à des fins de déboguage.
     */
    @Override
    public String toString() {
        return Utilities.getShortClassName(this)+'['+name+']';
    }

    /**
     * Envoie vers le périphérique de sortie standard une
     * liste des filtres disponibles par défaut. La liste
     * est construites à partir des encodeurs et décodeurs
     * fournit sur le système.
     */
    public static void main(final String[] args) {
        final ImageFileFilter[] filters = getReaderFilters(null);
        for (int i=0; i<filters.length; i++) {
            System.out.println(filters[i]);
        }
    }
}
