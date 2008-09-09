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
package org.constellation.sie.type.bbox;

// J2SE dependencies
import javax.swing.JPanel;


/**
 * Classe de base de la partie visuelle des différentes pages de l'assistant.
 * Chaque sous-classe de {@code WizardStep} correspond à une étape de l'assistant.
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
     * Modifie l'état de ce paneau à partir des informations contenues dans l'objet spécifié.
     */
    public abstract void readSettings(final BoundingBox settings);

    /**
     * Enregistre l'état de ce paneau dans l'objet spécifié.
     */
    public abstract void storeSettings(final BoundingBox settings);
}
