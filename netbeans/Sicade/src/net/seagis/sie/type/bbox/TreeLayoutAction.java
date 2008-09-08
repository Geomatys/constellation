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
package net.seagis.sie.type.bbox;

// J2SE dependencies
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

// OpenIDE dependencies
import org.openide.util.NbBundle;
import org.openide.DialogDisplayer;
import org.openide.DialogDescriptor;


/**
 * Une action permettant de configurer la structure de l'arborescence. Cette action est proposée
 * dans le menu contextuel de {@link RootNode}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class TreeLayoutAction extends AbstractAction {
    /**
     * Le noeud sur lequel cette action sera exécutée.
     */
    private final RootNode node;

    /**
     * Construit une nouvelle action.
     */
    public TreeLayoutAction(final RootNode node) {
        this.node = node;
        putValue(NAME, getTitle());
    }

    /**
     * Retourne le titre à utiliser pour l'item de cette action dans le menu contextuel,
     * ainsi que pour le titre de la boîte de dialogue de l'assistant.
     */
    private static String getTitle() {
        return NbBundle.getMessage(TreeLayoutAction.class, "CTL_TreeLayoutAction");
    }

    /**
     * Fait apparaître la boîte de dialogue qui permet à l'utilisateur de définir
     * la structure de l'arborescence.
     */
    public void actionPerformed(final ActionEvent event) {
        final DataFile       data = (DataFile) node.getDataObject();
        final TreeLayoutChooser c = new TreeLayoutChooser(data.bbox);
        final DialogDescriptor  d = new DialogDescriptor(c, getTitle());
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(d);
        dialog.setTitle(getTitle());
        dialog.setVisible(true);
        if (d.getValue() == DialogDescriptor.OK_OPTION) {
            data.bbox.setTreeLayout(c.getTreeLayout());
            data.setModified(true);
            node.refresh();
        }
    }
}
