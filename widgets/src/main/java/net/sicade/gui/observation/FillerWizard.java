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
package net.sicade.gui.observation;

// J2SE dependencies
import java.util.Set;

// Geotools dependencies
import org.geotools.resources.Arguments;

// Sicade dependencies
import net.sicade.observation.Observations;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Descriptor;
import net.sicade.observation.coverage.MeasurementTableFiller;
import net.sicade.observation.fishery.sql.EnvironmentTable;
import net.sicade.observation.sql.MeasurementTable;
import net.sicade.observation.sql.StationTable;


/**
 * Composante graphique pour d�marrer le remplissage de la table des mesures. Cette composante
 * graphique demandera � l'utilisateur de s�lectionner un sous-ensemble de descripteurs parmis
 * les descripteurs qui ont �t� sp�cifi�s � {@link MeasurementTableFiller}. Si l'utilisateur
 * appuie sur le bouton "Ex�cuter" apr�s cette s�lection, alors cette objet appelera
 * {@link MeasurementTableFiller#execute} pour les descripteurs s�lectionn�s.
 * <p>
 * Pour faire appara�tre cette composante graphique et permettre ainsi le lancement du
 * remplissage de la table des mesures, appelez {@link #show}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
@SuppressWarnings("serial")
public class FillerWizard extends DescriptorChooser {
    /**
     * Le contr�leur pour remplir la table des mesures.
     */
    private final MeasurementTableFiller filler;

    /**
     * Construit une composante graphique pour les descripteurs actuellement d�clar�s dans
     * l'objet {@link MeasurementTableFiller}.
     */
    public FillerWizard(final MeasurementTableFiller filler) {
        super(filler.descriptors());
        this.filler = filler;
    }

    /**
     * Appel�e automatiquement lorsque l'utilisateur a appuy� sur le bouton "Ex�cuter".
     * L'impl�mentation par d�faut r�duit les descripteur de {@link MeasurementTableFiller}
     * � l'ensemble s�lectionn� par l'utilisateur, et appelle
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
     * Appel�e automatiquement lorsque l'utilisateur a appuy� sur le bouton "Annuler".
     * L'impl�mentation par d�faut interrompt l'ex�cution lanc�e par
     * {@link MeasurementTableFiller#start start()}.
     */
    @Override
    protected void cancel() {
        filler.cancel();
        super.cancel();
    }

    /**
     * Fait appara�tre la bo�te de dialogue qui sugg�re � l'utilisateur de s�lectionner des
     * descripteurs du paysage oc�anique. La liste des descripteurs sera puis�e dans la base
     * de donn�es. Si l'utilisateur appuie sur le bouton "Ex�cuter", l'ex�cution d�marrera
     * imm�diatement.
     */
    public static void main(String[] args) {
        final FillerWizard wizard;
        final Arguments arguments = new Arguments(args);
        final String     provider = arguments.getRequiredString("-provider");
        args = arguments.getRemainingArguments(0);
        final MeasurementTable table;
        final MeasurementTableFiller filler;
        try {
            table  = new EnvironmentTable(Observations.getDefault().getDatabase(), StationTable.class, provider);
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
