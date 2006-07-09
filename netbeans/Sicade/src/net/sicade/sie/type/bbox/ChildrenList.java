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
import java.util.Map;
import java.util.LinkedHashMap;

// OpenIDE dependencies
import org.openide.nodes.Children;

// Sicade dependencies
import net.sicade.observation.Element;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.Thematic;
import net.sicade.observation.coverage.sql.TreeDepth;


/**
 * Arborescence des s�ries de donn�es � d�velopper en dessous d'un noeud {@link RootNode}.
 * Les noeuds enfants ne seront cr��s que la premi�re fois o� {@link #addNotify} sera appel�e.
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
     * La r�gion g�ographique qui contiendra la liste des s�ries.
     */
    private final BoundingBox bbox;

    /**
     * Les �l�ments de cette liste (th�matiques, op�rations ou s�ries), avec les �ventuels
     * enfants de chacun d'eux.
     */
    private java.util.Map<Element,ChildrenList> childs;

    /**
     * Pr�pare une liste pour la r�gion sp�cifi�e.
     */
    public ChildrenList(final BoundingBox bbox) {
        this(bbox, bbox.next(null));
    }

    /**
     * Pr�pare une liste pour la r�gion sp�cifi�e.
     *
     * @param bbox La r�gion g�ographique qui contiendra la liste des s�ries.
     * @param type Le type de cette liste.
     */
    private ChildrenList(final BoundingBox bbox, final TreeDepth type) {
        this.bbox = bbox;
        this.type = type;
        childType = bbox.next(type);
    }

    /**
     * Ajoute une s�rie � cette liste. Cette m�thode pr�parera automatiquement une cat�gorie
     * (th�matique ou op�ration) si n�cessaire, ainsi que tous les noeuds enfants. Toutefois,
     * les objets {@link org.openide.nodes.Node} correspondants ne seront r�ellement cr��s
     * que lorsque {@link #addNotify} sera appel�e.
     */
    private void add(final Series series) {
        /*
         * Obtient la cat�gorie dans laquelle placer la s�rie sp�cifi�e. La cat�gorie peut �tre
         * une th�matique ou une op�ration appliqu�e sur les images. Les cat�gories seront les
         * enfants cr��s par cette liste.
         */
        final Element category;
        switch (type) {
            case THEMATIC:  category = series.getPhenomenon(); break;
            case PROCEDURE: category = series.getProcedure();  break;
            case SERIES:    category = series;                 break;
            default: throw new IllegalStateException(String.valueOf(type));
        }
        /*
         * Ajoute la cat�gorie � la liste des enfants (si ce n'�tait pas d�j� fait),
         * puis demande � la liste enfant de cr�er elle-m�me ses propres cat�gories
         * pour la s�rie sp�cifi�e.
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
     * Appel�e automatiquement la premi�re fois o� les noeuds enfants doivent �tre construit.
     * Cette m�thode obtient l'ensemble des s�ries, puis construit un {@linkplain SeriesNode
     * noeud} pour chacune d'elles.
     */
    @Override
    protected void addNotify() {
        super.addNotify();
        if (childs == null) {
            /*
             * Si la liste d'enfants n'a pas encore �t� d�termin�e (�a ne devrait se produire que
             * si cette liste est directement sous RootNode), alors construit la liste d'enfants.
             * L'appel � la m�thode 'add' devrait cascader dans toute la profondeur de l'arborescence.
             * Note: La ligne suivante peut bloquer jusqu'� ce que le serveur fournisse une r�ponse.
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
     * Reconstruit une nouvelle liste d'enfants. Cette m�thode est appel�e apr�s que la structure
     * de l'arborescence aie �t� modifi�e par � un appel � {@link BoundingBox#setTreeLayout}.
     */
    final ChildrenList recreate() {
        return new ChildrenList(bbox);
    }
}
