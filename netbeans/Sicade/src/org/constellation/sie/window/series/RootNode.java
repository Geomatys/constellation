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
package net.seagis.sie.window.series;

// J2SE dependencies
import java.awt.Image;
import javax.swing.Action;

// OpenIDE dependencies
import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.Utilities;
import org.openide.util.NbBundle;

// Sicade dependencies
import net.seagis.sie.type.bbox.AddAction;


/**
 * Le noeud à la racine de toutes l'arborescence qui apparaîtra dans la fenêtre {@link ExplorerWindow}.
 * Cette racine ne sera pas nécessairement visible; ça dépend de la configuration de l'afficheur de
 * l'arborescence. Cette racine enveloppe un noeud du {@linkplain FileSystem système de fichier} de
 * Netbeans. Ce système de fichiers est en quelque sorte virtuel, et sa racine déclarée par l'élément
 * {@value #ROOT_NAME} du fichier {@code layer.xml} (tous les éléments du fichier {@code layer.xml}
 * peuvent être un vus comme un système de fichiers par Netbeans).
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public final class RootNode extends FilterNode {
    /**
     * Le nom de la racine dans le fichier {@code layer.xml}.
     */
    public static final String ROOT_NAME = "Series";

    /**
     * Icône représentant cette racine. Ne sera construit que la première fois où il sera
     * nécessaire.
     */
    private transient Image icon;

    /**
     * Les actions à retourner par {@link #addAction}. Ne sera construit que la première
     * fois où elles seront nécessaires.
     */
    private transient Action[] actions;

    /**
     * Construit une nouvelle racine pour l'élément {@value #ROOT_NAME} déclaré dans
     * le fichier {@code layer.xml}.
     *
     * @throws DataObjectNotFoundException si l'élément {@value #ROOT_NAME} n'a pas été trouvé.
     */
    public RootNode() throws DataObjectNotFoundException {
        super(getSystemRoot());
        disableDelegation(DELEGATE_GET_DISPLAY_NAME    |
                          DELEGATE_SET_DISPLAY_NAME    |
                          DELEGATE_GET_CONTEXT_ACTIONS |
                          DELEGATE_GET_ACTIONS         |
                          DELEGATE_DESTROY);
        setDisplayName(NbBundle.getMessage(RootNode.class, "RootNode"));
        DataNode.setShowFileExtensions(false);
    }

    /**
     * Retourne la racine {@value #ROOT_NAME} vue par le {@linkplain FileSystem système de fichier}
     * par défaut de Netbeans. Ce noeud devra être enveloppé dans une instance de {@link RootNode}
     * afin d'offrir les services spécifiques à cette classe.
     *
     * @throws DataObjectNotFoundException si l'élément {@value #ROOT_NAME} n'a pas été trouvé.
     */
    private static Node getSystemRoot() throws DataObjectNotFoundException {
        final FileSystem fs   = Repository.getDefault().getDefaultFileSystem();
        final DataObject data = DataObject.find(fs.getRoot().getFileObject(ROOT_NAME));
        return data.getNodeDelegate();
    }

    /**
     * Retourne l'icône représentant cette racine avant que le dossier ne soit ouvert.
     */
    @Override
    public Image getIcon(final int type) {
        if (icon == null) {
            icon = Utilities.loadImage(ExplorerWindow.ICON_PATH, true);
        }
        return icon;
    }

    /**
     * Retourne l'icône représentant cette racine après que le dossier aie été ouvert.
     * L'implémentation par défaut retourne <code>{@linkplain #getIcon getIcon}(type)</code>
     * (autrement dit nous ne faisons pas de distinction entre les dossiers ouverts et fermés).
     */
    @Override
    public Image getOpenedIcon(final int type) {
        return getIcon(type);
    }

    /**
     * Retourne {@code false} pour interdire tout renommage de cette racine. Un renommage peut
     * provoquer des résultats inatendus puisque le nom d'affichage spécifié par le constructeur
     * n'est pas le même que le nom spécifié interne spécifié dans le fichier {@code layer.xml}.
     */
    @Override
    public boolean canRename() {
        return false;
    }

    /**
     * Retourne {@code false} pour interdire toute destruction (ou effacement) de la racine.
     */
    @Override
    public boolean canDestroy() {
        return false;
    }

    /**
     * Masque les propriétés pour ce noeud, car ces informations sont sans intérêt pour
     * l'utilisateur et leur modification peut provoquer un dysfonctionnement du programme.
     */
    @Override
    public Node.PropertySet[] getPropertySets() {
        return new Node.PropertySet[0];
    }

    /**
     * Retourne l'ensemble des actions disponibles pour cette racine. Cette méthode est
     * appelée pour construire les menus "popup". Le tableau retourné peut contenir des
     * valeurs {@code null} pour insérer des séparateurs. Ce menu devra contenir au moins
     * un item pour créer un nouvel objet {@code .bbox}.
     */
    @Override
    public Action[] getActions(final boolean context) {
        if (actions == null) {
            final DataFolder df = (DataFolder) getLookup().lookup(DataFolder.class);
            actions = new Action[] {
                new AddAction(df)
            };
        }
        return actions;
    }
}
