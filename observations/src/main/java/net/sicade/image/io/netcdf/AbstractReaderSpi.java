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
package net.sicade.image.io.netcdf;

// J2SE dependencies
import java.util.Locale;
import java.io.IOException;

// Sicade dependencies
import net.sicade.image.io.FileBasedReaderSpi;


/**
 * Classe de base des fournisseurs de décodeurs d'images NetCDF.
 * 
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public abstract class AbstractReaderSpi extends FileBasedReaderSpi {
    /**
     * List of legal names for NetCDF readers.
     */
    private static final String[] NAMES = new String[] {"netcdf", "NetCDF"};

    /**
     * Default list of file's extensions.
     */
    private static final String[] SUFFIXES = new String[] {"nc", "NC"};

    /**
     * Le nom de la variable à lire.
     */
    final String variable;

    /**
     * Construit une nouvelle instance de ce fournisseur de service.
     *
     * @param variable Le nom de la variable à lire.
     */
    public AbstractReaderSpi(final String variable) {
        names            = NAMES;
        suffixes         = SUFFIXES;
        vendorName       = "Institut de Recherche pour le Développement";
        version          = "1.0";
        pluginClassName  = "net.sicade.image.io.netcdf.DefaultReader";
        this.variable    = variable;
    }

    /**
     * Retourne une description de ce format d'image.
     */
    @Override
    public String getDescription(final Locale locale) {
        return "Decodeur d'images NetCDF";
    }

    /**
     * Vérifie si le flot spécifié semble être un fichier NetCDF lisible.
     * Cette méthode tente simplement de lire les premiers octets du fichier.
     * La valeur retournée par cette méthode n'est qu'à titre indicative.
     * {@code true} n'implique pas que la lecture va forcément réussir,
     * et {@code false} n'implique pas que la lecture va obligatoirement
     * échouer.
     *
     * @param  source Source dont on veut tester la lisibilité.
     * @return {@code true} si la source <u>semble</u> être lisible.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    public boolean canDecodeInput(final Object source) throws IOException {
        // TODO: Effectuer la vérification en utilisant NetcdfFileCache.acquire(...)
        return true;
    }
}
