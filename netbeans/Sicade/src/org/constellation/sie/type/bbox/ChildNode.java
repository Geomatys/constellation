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
import javax.swing.Action;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

// OpenIDE dependencies
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.NodeOperation;
import org.openide.util.NbBundle;
import org.openide.ErrorManager;

// Constellation dependencies
import org.constellation.observation.Element;
import org.constellation.observation.CatalogException;
import org.constellation.observation.coverage.Series;
import org.constellation.observation.coverage.sql.TreeDepth;
import org.constellation.sie.window.mosaic.OpenAction;


/**
 * Noeud représentant une thématique, une opération ou une série d'images. Ces noeuds ont
 * soit {@link RootNode} ou soit un autre {@code ChildNode} comme parent.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ChildNode extends AbstractNode {
    /**
     * Chemin vers l'icone représentant ce noeud, or {@code null} si aucun icone n'a été définie.
     */
    private static final String THEMATIC_ICON = "toolbarButtonGraphics/general/Zoom16.gif";

    /**
     * Chemin vers l'icone représentant ce noeud, or {@code null} si aucun icone n'a été définie.
     */
    private static final String OPERATION_ICON = "toolbarButtonGraphics/development/Host16.gif";

    /**
     * Chemin vers l'icone représentant ce noeud, or {@code null} si aucun icone n'a été définie.
     */
    private static final String SERIES_ICON = "toolbarButtonGraphics/media/Movie16.gif";

    /**
     * Les actions applicable sur ce noeud.
     */
    private transient Action[] actions;

    /**
     * La thématique, opération ou série de données représentée par ce noeud.
     */
    private final Element element;

    /**
     * Construit un noeud pour la thématique, l'opération ou la série d'images spécifiée.
     *
     * @param element La thématique, opération ou série d'images.
     * @param children Les enfants, ou {@code null} si aucun.
     */
    public ChildNode(final Element element, final Children children, final TreeDepth type) {
        super(children!=null ? children : Children.LEAF);
        this.element = element;
        setName(element.getName());
        final String path;
        switch (type) {
            case THEMATIC:  path = THEMATIC_ICON;  break;
            case PROCEDURE: path = OPERATION_ICON; break;
            case SERIES:    path = SERIES_ICON;    break;
            default:        path = null;           break;
        }
        if (path != null) {
            setIconBaseWithExtension(path);
        }
    }

    /**
     * Construit une feuille de propriétés pour ce noeud.
     *
     * @todo Prendre en compte la résolution.
     * @todo La description ne semble pas fonctionner (excepté si le texte est vraiment très court)...
     */
    @Override
    protected Sheet createSheet() {
        if (element instanceof Series) {
            final Sheet sheet;
            final Series series = (Series) element;
            try {
                sheet = SimpleProperty.createSheet(series.getGeographicBoundingBox(),
                                                   series.getTimeRange(), null);
            } catch (CatalogException e) {
                ErrorManager.getDefault().notify(e);
                return super.createSheet();
            }
            final Sheet.Set s = sheet.get(Sheet.PROPERTIES);
            s.setShortDescription(series.getRemarks());
            return sheet;
        }
        return super.createSheet();
    }

    /**
     * Retourne la liste des actions disponibles pour ce noeud. Si l'élément enveloppé par ce
     * noeud est une {@linkplain Series séries d'images}, alors le menu proposé comprendra les
     * items "Ouvrir" et "Propriétés".
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (context || !(element instanceof Series)) {
            return super.getActions(context);
        }
        if (actions == null) {
            final Series series = (Series) element;
            actions = new Action[] {
                new OpenAction(series),
                null,
                new PropertiesAction()
            };
        }
        return actions;
    }

    /**
     * Retourne l'action par défaut. Cette action sera exécutée lorsque l'utilisateur
     * effectuera un double-clique sur ce noeud. L'implémentation par défaut retourne
     * la première action de celles qui sont retournées par {@link #getActions}, qui
     * correspondra habituellement à l'action "Ouvrir".
     */
    @Override
    public Action getPreferredAction() {
        final Action[] actions = getActions(false);
        return (actions!=null && actions.length!=0) ? actions[0] : super.getPreferredAction();
    }

    /** Action affichant les propriétés du noeud. */
    private final class PropertiesAction extends AbstractAction {
        /** Construit l'action. */
        public PropertiesAction() {
            super(NbBundle.getMessage(ChildNode.class, "Properties"));
        }

        /** Exécute l'action. */
        public void actionPerformed(final ActionEvent event) {
            NodeOperation.getDefault().showProperties(ChildNode.this);
        }
    }
}
