/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.gui.observation;

// J2SE dependencies
import java.util.Set;

// Geotools dependencies
import org.geotools.resources.Arguments;

// Constellation dependencies
import org.constellation.coverage.catalog.Catalog;
import org.constellation.catalog.CatalogException;
import org.constellation.coverage.model.Descriptor;
import org.constellation.observation.MeasurementTableFiller;
import org.constellation.observation.fishery.sql.EnvironmentTable;
import org.constellation.observation.sql.MeasurementTable;
import org.constellation.observation.sql.StationTable;


/**
 * Composante graphique pour démarrer le remplissage de la table des mesures. Cette composante
 * graphique demandera à l'utilisateur de sélectionner un sous-ensemble de descripteurs parmis
 * les descripteurs qui ont été spécifiés à {@link MeasurementTableFiller}. Si l'utilisateur
 * appuie sur le bouton "Exécuter" après cette sélection, alors cette objet appelera
 * {@link MeasurementTableFiller#execute} pour les descripteurs sélectionnés.
 * <p>
 * Pour faire apparaître cette composante graphique et permettre ainsi le lancement du
 * remplissage de la table des mesures, appelez {@link #show}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 *
 * @todo Eviter une référence directe à {@link EnvironmentTable}. Il faut trouver un moyen de
 *       laisser ce choix à l'utilisateur.
 */
@SuppressWarnings("serial")
public class FillerWizard extends DescriptorChooser {
    /**
     * Le contrôleur pour remplir la table des mesures.
     */
    private final MeasurementTableFiller filler;

    /**
     * Construit une composante graphique pour les descripteurs actuellement déclarés dans
     * l'objet {@link MeasurementTableFiller}.
     */
    public FillerWizard(final MeasurementTableFiller filler) {
        super(filler.descriptors());
        this.filler = filler;
    }

    /**
     * Appelée automatiquement lorsque l'utilisateur a appuyé sur le bouton "Exécuter".
     * L'implémentation par défaut réduit les descripteur de {@link MeasurementTableFiller}
     * à l'ensemble sélectionné par l'utilisateur, et appelle
     * {@link MeasurementTableFiller#start start()}.
     */
    @Override
    protected void execute() {
        synchronized (filler) {
            final Set<Descriptor> descriptors = filler.descriptors();
            descriptors.clear();
            descriptors.addAll(getDescriptors(true));
            filler.start();
        }
    }

    /**
     * Appelée automatiquement lorsque l'utilisateur a appuyé sur le bouton "Annuler".
     * L'implémentation par défaut interrompt l'exécution lancée par
     * {@link MeasurementTableFiller#start start()}.
     */
    @Override
    protected void cancel() {
        filler.cancel();
        super.cancel();
    }

    /**
     * Fait apparaître la boîte de dialogue qui suggère à l'utilisateur de sélectionner des
     * descripteurs du paysage océanique. La liste des descripteurs sera puisée dans la base
     * de données. Si l'utilisateur appuie sur le bouton "Exécuter", l'exécution démarrera
     * immédiatement.
     */
    public static void main(String[] args) {
        final FillerWizard wizard;
        final Arguments arguments = new Arguments(args);
        final String     provider = arguments.getRequiredString("-provider");
        args = arguments.getRemainingArguments(0);
        final MeasurementTable table;
        final MeasurementTableFiller filler;
        try {
            table  = new EnvironmentTable(Catalog.getDefault().getDatabase(), StationTable.class, provider);
            filler = new MeasurementTableFiller(table);
            filler.addDefaultDescriptors();
            filler.addDefaultStations();
            wizard = new FillerWizard(filler);
        } catch (CatalogException exception) {
            exception.printStackTrace(arguments.err);
            return;
        }
        wizard.show(null);
    }
}
