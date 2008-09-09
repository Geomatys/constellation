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
import java.util.Map;
import java.util.LinkedHashMap;

// OpenIDE dependencies
import org.openide.nodes.Children;

// Sicade dependencies
import org.constellation.observation.Element;
import org.constellation.observation.coverage.Series;
import org.constellation.observation.coverage.Thematic;
import org.constellation.observation.coverage.sql.TreeDepth;


/**
 * Arborescence des séries de données à développer en dessous d'un noeud {@link RootNode}.
 * Les noeuds enfants ne seront créés que la première fois où {@link #addNotify} sera appelée.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class ChildrenList extends Children.Array {
    /**
     * Le type de cette liste, comme une des constantes {@link TreeDepth#THEMATIC THEMATIC},
     * {@link TreeDepth#PROCEDURE PROCEDURE} ou {@link TreeDepth#SERIES SERIES}.
     */
    private final TreeDepth type;

    /**
     * Le type pour les liste enfants.
     */
    private final TreeDepth childType;

    /**
     * La région géographique qui contiendra la liste des séries.
     */
    private final BoundingBox bbox;

    /**
     * Les éléments de cette liste (thématiques, opérations ou séries), avec les éventuels
     * enfants de chacun d'eux.
     */
    private java.util.Map<Element,ChildrenList> childs;

    /**
     * Prépare une liste pour la région spécifiée.
     */
    public ChildrenList(final BoundingBox bbox) {
        this(bbox, bbox.next(null));
    }

    /**
     * Prépare une liste pour la région spécifiée.
     *
     * @param bbox La région géographique qui contiendra la liste des séries.
     * @param type Le type de cette liste.
     */
    private ChildrenList(final BoundingBox bbox, final TreeDepth type) {
        this.bbox = bbox;
        this.type = type;
        childType = bbox.next(type);
    }

    /**
     * Ajoute une série à cette liste. Cette méthode préparera automatiquement une catégorie
     * (thématique ou opération) si nécessaire, ainsi que tous les noeuds enfants. Toutefois,
     * les objets {@link org.openide.nodes.Node} correspondants ne seront réellement créés
     * que lorsque {@link #addNotify} sera appelée.
     */
    private void add(final Series series) {
        /*
         * Obtient la catégorie dans laquelle placer la série spécifiée. La catégorie peut être
         * une thématique ou une opération appliquée sur les images. Les catégories seront les
         * enfants créés par cette liste.
         */
        final Element category;
        switch (type) {
            case THEMATIC:  category = series.getPhenomenon(); break;
            case PROCEDURE: category = series.getProcedure();  break;
            case SERIES:    category = series;                 break;
            default: throw new IllegalStateException(String.valueOf(type));
        }
        /*
         * Ajoute la catégorie à la liste des enfants (si ce n'était pas déjà fait),
         * puis demande à la liste enfant de créer elle-même ses propres catégories
         * pour la série spécifiée.
         */
        if (childs == null) {
            childs = new LinkedHashMap<Element,ChildrenList>();
        }
        ChildrenList c = childs.get(category);
        if (c == null) {
            if (childType != null) {
                c = new ChildrenList(bbox, childType);
            }
            childs.put(category, c);
        }
        if (c != null) {
            c.add(series);
        }
    }

    /**
     * Appelée automatiquement la première fois où les noeuds enfants doivent être construit.
     * Cette méthode obtient l'ensemble des séries, puis construit un {@linkplain SeriesNode
     * noeud} pour chacune d'elles.
     */
    @Override
    protected void addNotify() {
        super.addNotify();
        if (childs == null) {
            /*
             * Si la liste d'enfants n'a pas encore été déterminée (ça ne devrait se produire que
             * si cette liste est directement sous RootNode), alors construit la liste d'enfants.
             * L'appel à la méthode 'add' devrait cascader dans toute la profondeur de l'arborescence.
             * Note: La ligne suivante peut bloquer jusqu'à ce que le serveur fournisse une réponse.
             */
            final Series[] series = bbox.getSeries();
            if (series == null) {
                return;
            }
            for (Series s : series) {
                add(s);
            }
        }
        /*
         * Construit maintenant les noeuds.
         */
        int i = 0;
        final ChildNode[] nodes = new ChildNode[childs.size()];
        for (final java.util.Map.Entry<Element,ChildrenList> entry : childs.entrySet()) {
            final Element element = entry.getKey();
            final ChildrenList  c = entry.getValue();
            final ChildNode  node = new ChildNode(element, c, type);
            nodes[i++] = node;
        }
        add(nodes);
    }

    /**
     * Reconstruit une nouvelle liste d'enfants. Cette méthode est appelée après que la structure
     * de l'arborescence aie été modifiée par à un appel à {@link BoundingBox#setTreeLayout}.
     */
    final ChildrenList recreate() {
        return new ChildrenList(bbox);
    }
}
