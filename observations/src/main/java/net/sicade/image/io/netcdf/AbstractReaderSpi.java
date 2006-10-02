/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
 * Classe de base des fournisseurs de d�codeurs d'images NetCDF.
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
     * Le nom de la variable � lire.
     */
    final String variable;

    /**
     * Construit une nouvelle instance de ce fournisseur de service.
     *
     * @param variable Le nom de la variable � lire.
     */
    public AbstractReaderSpi(final String variable) {
        names            = NAMES;
        suffixes         = SUFFIXES;
        vendorName       = "Institut de Recherche pour le D�veloppement";
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
     * V�rifie si le flot sp�cifi� semble �tre un fichier NetCDF lisible.
     * Cette m�thode tente simplement de lire les premiers octets du fichier.
     * La valeur retourn�e par cette m�thode n'est qu'� titre indicative.
     * {@code true} n'implique pas que la lecture va forc�ment r�ussir,
     * et {@code false} n'implique pas que la lecture va obligatoirement
     * �chouer.
     *
     * @param  source Source dont on veut tester la lisibilit�.
     * @return {@code true} si la source <u>semble</u> �tre lisible.
     * @throws IOException si une erreur est survenue lors de la lecture.
     */
    public boolean canDecodeInput(final Object source) throws IOException {
        // TODO: Effectuer la v�rification en utilisant NetcdfFileCache.acquire(...)
        return true;
    }
}
