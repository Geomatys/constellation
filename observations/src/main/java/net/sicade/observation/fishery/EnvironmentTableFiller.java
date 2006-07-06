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
package net.sicade.observation.fishery;

// J2SE dependencies
import java.awt.Component;
import java.io.IOException;

// Geotools dependencies
import org.geotools.resources.Arguments;
import org.geotools.gui.swing.ExceptionMonitor;

// Sicade dependencies
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.StationTable;
import net.sicade.observation.fishery.sql.LongLineTable;
import net.sicade.observation.fishery.sql.EnvironmentTable;
import net.sicade.observation.coverage.MeasurementTableFiller;
import net.sicade.observation.coverage.MeasurementTableFiller.Starter;


/**
 * Remplit la table des données environnementales à partir des descripteurs sélectionnés
 * par l'utilisateur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EnvironmentTableFiller extends MeasurementTableFiller {
    /**
     * Connexion vers la base de données.
     */
    private final Observations observations;

    /**
     * Construit un objet qui servira au remplissage en utilisant les tables
     * provenant de la base de données spécifiée.
     *
     * @param  database Connexion vers la base de données des observations.
     * @param  type Type de table des stations que devra utiliser cette instance. Un argument
     *         typique est <code>{@linkplain LongLineTable}.class</code>.
     * @param  providers Si les stations doivent être limitées à celles d'un fournisseur,
     *         liste de ces fournisseurs.
     * @throws CatalogException si l'accès à la base de données a échoué.
     */
    public EnvironmentTableFiller(final Database                  database,
                                  final Class<? extends StationTable> type,
                                  final String...                providers)
            throws CatalogException
    {
        super(new EnvironmentTable(database, type, providers));
        observations = new Observations(database);
        addDefaultStations();
    }

    /**
     * Fait apparaître la boîte de dialogue qui suggère à l'utilisateur de sélectionner des
     * descripteurs du paysage océanique. La liste des descripteurs sera puisée dans la base
     * de données. Si l'utilisateur appuie sur le bouton "Exécuter", l'exécution démarrera
     * immédiatement.
     */
    public void showStarter(final Component owner) {
        try {
            addDescriptors(observations.getDescriptors());
        } catch (Exception exception) {
            ExceptionMonitor.show(owner, exception);
            return;
        }
        final Starter starter = new Starter();
        starter.show(owner);
    }

    /**
     * Lance le remplissage de la table d'environnement à partir de la ligne de commande.
     * L'utilisateur sera d'abord interrogé afin de choisir les descripteurs du paysage
     * océanique.
     */
    public static void main(String[] args) {
        final Arguments arguments = new Arguments(args);
        final String provider = arguments.getRequiredString("-provider");
        args = arguments.getRemainingArguments(0);
        final Database database;
        try {
            database = new Database();
        } catch (IOException exception) {
            exception.printStackTrace(arguments.err);
            return;
        }
        final EnvironmentTableFiller filler;
        try {
            filler = new EnvironmentTableFiller(database, StationTable.class, provider);
        } catch (CatalogException exception) {
            exception.printStackTrace(arguments.err);
            return;
        }
        filler.showStarter(null);
    }
}
