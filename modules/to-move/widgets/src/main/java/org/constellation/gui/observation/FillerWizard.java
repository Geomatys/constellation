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

// J2SE dependencies

import org.constellation.catalog.CatalogException;
import org.constellation.coverage.catalog.Catalog;
import org.constellation.coverage.model.Descriptor;
import org.constellation.observation.MeasurementTableFiller;
import org.constellation.observation.fishery.sql.EnvironmentTable;
import org.constellation.observation.sql.MeasurementTable;
import org.constellation.observation.sql.StationTable;
import org.geotools.resources.Arguments;

import java.util.Set;

// Geotools dependencies
// Constellation dependencies


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
