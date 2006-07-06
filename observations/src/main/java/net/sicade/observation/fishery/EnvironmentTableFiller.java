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
 * Remplit la table des donn�es environnementales � partir des descripteurs s�lectionn�s
 * par l'utilisateur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class EnvironmentTableFiller extends MeasurementTableFiller {
    /**
     * Connexion vers la base de donn�es.
     */
    private final Observations observations;

    /**
     * Construit un objet qui servira au remplissage en utilisant les tables
     * provenant de la base de donn�es sp�cifi�e.
     *
     * @param  database Connexion vers la base de donn�es des observations.
     * @param  type Type de table des stations que devra utiliser cette instance. Un argument
     *         typique est <code>{@linkplain LongLineTable}.class</code>.
     * @param  providers Si les stations doivent �tre limit�es � celles d'un fournisseur,
     *         liste de ces fournisseurs.
     * @throws CatalogException si l'acc�s � la base de donn�es a �chou�.
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
     * Fait appara�tre la bo�te de dialogue qui sugg�re � l'utilisateur de s�lectionner des
     * descripteurs du paysage oc�anique. La liste des descripteurs sera puis�e dans la base
     * de donn�es. Si l'utilisateur appuie sur le bouton "Ex�cuter", l'ex�cution d�marrera
     * imm�diatement.
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
     * Lance le remplissage de la table d'environnement � partir de la ligne de commande.
     * L'utilisateur sera d'abord interrog� afin de choisir les descripteurs du paysage
     * oc�anique.
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
