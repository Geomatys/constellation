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
package net.sicade.sie.type.bbox;

// J2SE dependencies
import javax.swing.JPanel;


/**
 * Classe de base de la partie visuelle des diff�rentes pages de l'assistant.
 * Chaque sous-classe de {@code WizardStep} correspond � une �tape de l'assistant.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
abstract class WizardStep extends JPanel {
    /**
     * Construit la partie visuelle de l'assistant.
     */
    public WizardStep() {
    }

    /**
     * Modifie l'�tat de ce paneau � partir des informations contenues dans l'objet sp�cifi�.
     */
    public abstract void readSettings(final BoundingBox settings);

    /**
     * Enregistre l'�tat de ce paneau dans l'objet sp�cifi�.
     */
    public abstract void storeSettings(final BoundingBox settings);
}
