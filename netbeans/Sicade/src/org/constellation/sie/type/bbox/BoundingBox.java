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
import java.util.Set;
import java.util.LinkedHashSet;
import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;

// OpenGIS dependencies
import org.opengis.metadata.extent.GeographicBoundingBox;

// OpenIDE dependencies
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;

// Constellation dependencies
import org.constellation.util.DateRange;
import org.constellation.observation.coverage.Series;
import org.constellation.observation.coverage.sql.TreeDepth;
import static org.constellation.observation.coverage.sql.TreeDepth.*;


/**
 * Représente une région géographique d'intérêt tel que spécifiée dans un fichier {@code .bbox}.
 * Cette région d'intérêt détermine les séries de données qui seront disponibles (exemple: SST
 * LAC autour de l'île de la Réunion, SST LAC autour de la Nouvelle-Calédonie, <cite>etc.</cite>).
 * Les objets {@code DataFile} sont construits par {@link Loader} et représentés visuellement par
 * {@link RootNode}.
 * <p>
 * <strong>Note:</strong> Cette classe est séparée de {@link DataFile} car l'enregistrement binaire
 * de cette dernière n'enregistre que le nom du fichier. On pourrait fusionner les deux classes si
 * on lisait un fichier XML (par exemple) plutôt qu'un objet de type {@link Serializable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BoundingBox implements Serializable {
    /**
     * Pour compatibilité avec différentes versions de cette classe.
     */
    private static final long serialVersionUID = -5271456523356557418L;

    /**
     * Nom de cette région d'intérêt, tel que spécifié par l'utilisateur. Ce nom devrait
     * être identique au nom du fichier, mais ça ne sera pas vérifié.
     */
    private transient String name;

    /**
     * La région géographique demandée par l'utilisateur, ou {@code null} s'il n'y a pas de
     * restriction.
     */
    private GeographicBoundingBox area;

    /**
     * La plage de date demandée par l'utilisateur, ou {@code null} s'il n'y a pas de
     * restriction.
     */
    private DateRange timeRange;

    /**
     * La résolution préférée, ou {@code null} si aucune.
     */
    private Dimension2D resolution;

    /**
     * Les séries d'images pour cette région géographique, ou {@code null} si elle n'ont pas encore
     * été obtenue.
     */
    private transient Series[] series;

    /**
     * La structure de l'arborescence selon laquelle organiser les séries. Par défaut, les
     * séries seront placées dans une arborescence de type thematic/procédure/series. Les
     * premiers et derniers elements doivent obligatoirement être {@code null} pour un
     * fonctionnement correct de la méthode {@link #next}.
     *
     * @see #setTreeLayout
     */
    private TreeDepth[] treeLayout = new TreeDepth[] {null, THEMATIC, PROCEDURE, SERIES, null};

    /**
     * Construit une nouvelle instance d'une région géographique.
     */
    public BoundingBox(final String name) {
        this.name = name;
    }

    /**
     * Retourne le nom de cette région d'intérêt, tel que spécifié par l'utilisateur.
     * Ce nom devrait être identique au nom du fichier, mais ça ne sera pas vérifié.
     */
    public String getName() {
        return name;
    }

    /**
     * Définie le nom de cette région d'intérêt. Ce nom servira à nommer le fichier à créer.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Retourne la région géographique demandée par l'utilisateur,
     * ou {@code null} s'il n'y a pas de restriction.
     */
    public GeographicBoundingBox getGeographicBoundingBox() {
        return area;
    }

    /**
     * Spécifie la région géographique demandée par l'utilisateur.
     */
    public synchronized void setGeographicBoundingBox(final GeographicBoundingBox area) {
        this.area = area;
        series = null;
    }

    /**
     * Retourne la plage de date demandée par l'utilisateur,
     * ou {@code null} s'il n'y a pas de restriction.
     */
    public DateRange getTimeRange() {
        return timeRange;
    }

    /**
     * Spécifie la plage de date demandée par l'utilisateur.
     */
    public synchronized void setTimeRange(final DateRange timeRange) {
        this.timeRange = timeRange;
        series = null;
    }

    /**
     * Retourne la résolution préférée, ou {@code null} si aucune.
     */
    public Dimension2D getResolution() {
        return resolution;
    }

    /**
     * Spécifie la résolution préférée.
     */
    public synchronized void setResolution(final Dimension2D resolution) {
        this.resolution = resolution;
        series = null;
    }

    /**
     * Retourne les séries correspondant à cette envelope spatio-temporelle. Si ces séries n'ont
     * pas encore été déterminées, alors cette méthode bloquera jusqu'à ce qu'elles le soient.
     * Cette méthode peut retourner {@code null} si l'obtention des séries a échouée pour une
     * quelconque raison.
     */
    public synchronized Series[] getSeries() {
        if (series == null) try {
            prefetch();
            wait();
        } catch (InterruptedException e) {
            /*
             * L'attente a été interrompue. Ca ne devrait pas se produire, mais si c'est quand
             * même le cas, alors on retournera 'null' tel qu'indiqué dans la documentation.
             */
        }
        return series;
    }

    /**
     * Définie les séries correspondantes à cette enveloppe spatio-temporelle. Cette méthode est
     * appelée automatiquement par {@link BoundingBoxBuilder#run}. Le tableau {@code series} peut
     * être {@code null} si l'obtention des séries a échouée, auquel cas une nouvelle tentative
     * sera faite la prochaine fois où {@link #prefetch} sera appelée.
     */
    final synchronized void setSeries(final Series[] series) {
        this.series = series;
        notifyAll();
    }

    /**
     * Prévient cet objet qu'on lui demandera bientôt la {@linkplain #getSeries liste des séries}.
     * Cette méthode obtiendra d'avance la liste des séries dans un thread en arrière-plan. Cette
     * méthode peut être appelée après que l'enveloppe spatio-temporelle aie été spécifiée.
     */
    final void prefetch() {
        BoundingBoxBuilder.DEFAULT.add(this);
    }

    /**
     * Retourne la catégorie qui suit la catégorie spécifiée dans la structure de l'arborescence.
     * <ul>
     *   <li>Si {@code current} est {@code null}, alors cette méthode retourne la première catégorie.</li>
     *   <li>Si {@code current} est la dernière catégorie, alors cette méthode retourne {@code null}.</li>
     * </ul>
     */
    final TreeDepth next(final TreeDepth current) {
        for (int i=0; i<treeLayout.length; i++) {
            if (treeLayout[i] == current) {
                return treeLayout[i+1];
            }
        }
        throw new IllegalArgumentException(String.valueOf(current));
    }

    /**
     * Définie la structure de l'arborescence. La structure par défaut est {{@code THEMATIC},
     * {@code PROCEDURE}). Il est de la responsabilité de l'appellant de mettre à jour le
     * noeud {@link RootNode} correspondant à cet objet {@code BoundingBox}.
     */
    final void setTreeLayout(final TreeDepth[] layout) {
        final Set<TreeDepth> set = new LinkedHashSet<TreeDepth>();
        set.add(null); // Garantie que la première valeur sera nulle.
        for (final TreeDepth d : layout) {
            set.add(d);
        }
        set.remove(SERIES); // Pour garantir que les séries seront en dernier.
        set.add(SERIES);
        // Construit un tableau d'un élément plus long pour que ce dernier élément soit nul.
        treeLayout = set.toArray(new TreeDepth[set.size() + 1]);
    }

    /**
     * Enregistre cet objet en binaire dans le fichier spécifié.
     *
     * @param  writeTo Le fichier dans lequel enregistrer.
     * @throws IOException si l'enregistrement a échoué.
     */
    public void save(final FileObject writeTo) throws IOException {
        final FileLock lock = writeTo.lock();
        try {
            final ObjectOutputStream out = new ObjectOutputStream(writeTo.getOutputStream(lock));
            out.writeObject(this);
            out.close();
        } finally {
            lock.releaseLock();
        }
    }

    /**
     * Retourne le nom de cette région géographique.
     */
    @Override
    public String toString() {
        return name;
    }
}
