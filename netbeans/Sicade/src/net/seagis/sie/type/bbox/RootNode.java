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
import javax.swing.Action;

// OpenIDE dependencies
import org.openide.nodes.Sheet;
import org.openide.loaders.DataNode;

// Sicade dependencies
import net.seagis.resources.XArray;


/**
 * Un noeud qui représentera le {@linkplain DataFile contenu d'un fichier} de manière visuelle dans
 * l'arboresence. Ces fichiers sont de type {code bbox} et contiennent des informations sur la
 * {@linkplain BoundingBox région spatio-temporelle d'intérêt}, à partir de laquelle seront déduites
 * les séries de données proposées à l'utilisateur dans l'arborescence.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class RootNode extends DataNode {
    /**
     * Chemin vers l'icone représentant ce noeud, or {@code null} si aucun icone n'a été définie.
     */
    static final String ICON_PATH = "toolbarButtonGraphics/development/WebComponent16.gif";

    /**
     * Chemin vers l'icone représentant ce noeud, or {@code null} si aucun icone n'a été définie.
     */
    static final String ICON_32_PATH = "toolbarButtonGraphics/development/WebComponent24.gif";

    /**
     * Les actions à retourner par {@link #addAction}. Ne sera construit que la première
     * fois où elles seront nécessaires.
     */
    private transient Action[] actions;

    /**
     * Construit un noeud qui représentera dans l'arborescence la région d'intérêt spécifiée.
     */
    public RootNode(final DataFile data) {
        super(data, new ChildrenList(data.bbox));
        if (ICON_PATH != null) {
            setIconBaseWithExtension(ICON_PATH);
        }
    }

    /**
     * Reconstruit la totalité de l'arborescence, typiquement après un changement de la structure.
     * Cette méthode est appelée après {@link BoundingBox#setTreeLayout}.
     */
    final void refresh() {
        setChildren(((ChildrenList) getChildren()).recreate());
    }

    /**
     * Retourne la liste des actions qui peuvent être exécutées sur ce noeud. Cette méthode obtient
     * les actions par défaut de la classe parente, et y ajoute des actions particulières juste avant
     * le dernier séparateur. Ca a pour effet de placer les actions supplémentaires avant l'item
     * "Propriétés" dans le menu contextuel.
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (context) {
            return super.getActions(context);
        }
        if (actions == null) {
            final BoundingBox bbox = ((DataFile) getDataObject()).bbox;
            actions = super.getActions(context);
            int insertAt = actions.length;
            do if (--insertAt < 0) {
                insertAt = 0;
                break;
            } while (actions[insertAt] != null);
            actions = XArray.insert(actions, insertAt, 2);
            actions[insertAt++] = null;
            actions[insertAt++] = new TreeLayoutAction(this);
        }
        return actions;
    }

    /**
     * Construit une feuille de propriétés pour ce noeud.
     */
    @Override
    protected Sheet createSheet() {
        final BoundingBox bbox = ((DataFile) getDataObject()).bbox;
        return SimpleProperty.createSheet(bbox.getGeographicBoundingBox(),
                                          bbox.getTimeRange(),
                                          bbox.getResolution());
    }
}
