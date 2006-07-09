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

// Sicade dependencies
import net.sicade.util.DateRange;
import net.sicade.observation.coverage.Series;
import net.sicade.observation.coverage.sql.TreeDepth;
import static net.sicade.observation.coverage.sql.TreeDepth.*;


/**
 * Repr�sente une r�gion g�ographique d'int�r�t tel que sp�cifi�e dans un fichier {@code .bbox}.
 * Cette r�gion d'int�r�t d�termine les s�ries de donn�es qui seront disponibles (exemple: SST
 * LAC autour de l'�le de la R�union, SST LAC autour de la Nouvelle-Cal�donie, <cite>etc.</cite>).
 * Les objets {@code DataFile} sont construits par {@link Loader} et repr�sent�s visuellement par
 * {@link RootNode}.
 * <p>
 * <strong>Note:</strong> Cette classe est s�par�e de {@link DataFile} car l'enregistrement binaire
 * de cette derni�re n'enregistre que le nom du fichier. On pourrait fusionner les deux classes si
 * on lisait un fichier XML (par exemple) plut�t qu'un objet de type {@link Serializable}.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class BoundingBox implements Serializable {
    /**
     * Pour compatibilit� avec diff�rentes versions de cette classe.
     */
    private static final long serialVersionUID = -5271456523356557418L;

    /**
     * Nom de cette r�gion d'int�r�t, tel que sp�cifi� par l'utilisateur. Ce nom devrait
     * �tre identique au nom du fichier, mais �a ne sera pas v�rifi�.
     */
    private transient String name;

    /**
     * La r�gion g�ographique demand�e par l'utilisateur, ou {@code null} s'il n'y a pas de
     * restriction.
     */
    private GeographicBoundingBox area;

    /**
     * La plage de date demand�e par l'utilisateur, ou {@code null} s'il n'y a pas de
     * restriction.
     */
    private DateRange timeRange;

    /**
     * La r�solution pr�f�r�e, ou {@code null} si aucune.
     */
    private Dimension2D resolution;

    /**
     * Les s�ries d'images pour cette r�gion g�ographique, ou {@code null} si elle n'ont pas encore
     * �t� obtenue.
     */
    private transient Series[] series;

    /**
     * La structure de l'arborescence selon laquelle organiser les s�ries. Par d�faut, les
     * s�ries seront plac�es dans une arborescence de type thematic/proc�dure/series. Les
     * premiers et derniers elements doivent obligatoirement �tre {@code null} pour un
     * fonctionnement correct de la m�thode {@link #next}.
     *
     * @see #setTreeLayout
     */
    private TreeDepth[] treeLayout = new TreeDepth[] {null, THEMATIC, PROCEDURE, SERIES, null};

    /**
     * Construit une nouvelle instance d'une r�gion g�ographique.
     */
    public BoundingBox(final String name) {
        this.name = name;
    }

    /**
     * Retourne le nom de cette r�gion d'int�r�t, tel que sp�cifi� par l'utilisateur.
     * Ce nom devrait �tre identique au nom du fichier, mais �a ne sera pas v�rifi�.
     */
    public String getName() {
        return name;
    }

    /**
     * D�finie le nom de cette r�gion d'int�r�t. Ce nom servira � nommer le fichier � cr�er.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Retourne la r�gion g�ographique demand�e par l'utilisateur,
     * ou {@code null} s'il n'y a pas de restriction.
     */
    public GeographicBoundingBox getGeographicBoundingBox() {
        return area;
    }

    /**
     * Sp�cifie la r�gion g�ographique demand�e par l'utilisateur.
     */
    public synchronized void setGeographicBoundingBox(final GeographicBoundingBox area) {
        this.area = area;
        series = null;
    }

    /**
     * Retourne la plage de date demand�e par l'utilisateur,
     * ou {@code null} s'il n'y a pas de restriction.
     */
    public DateRange getTimeRange() {
        return timeRange;
    }

    /**
     * Sp�cifie la plage de date demand�e par l'utilisateur.
     */
    public synchronized void setTimeRange(final DateRange timeRange) {
        this.timeRange = timeRange;
        series = null;
    }

    /**
     * Retourne la r�solution pr�f�r�e, ou {@code null} si aucune.
     */
    public Dimension2D getResolution() {
        return resolution;
    }

    /**
     * Sp�cifie la r�solution pr�f�r�e.
     */
    public synchronized void setResolution(final Dimension2D resolution) {
        this.resolution = resolution;
        series = null;
    }

    /**
     * Retourne les s�ries correspondant � cette envelope spatio-temporelle. Si ces s�ries n'ont
     * pas encore �t� d�termin�es, alors cette m�thode bloquera jusqu'� ce qu'elles le soient.
     * Cette m�thode peut retourner {@code null} si l'obtention des s�ries a �chou�e pour une
     * quelconque raison.
     */
    public synchronized Series[] getSeries() {
        if (series == null) try {
            prefetch();
            wait();
        } catch (InterruptedException e) {
            /*
             * L'attente a �t� interrompue. Ca ne devrait pas se produire, mais si c'est quand
             * m�me le cas, alors on retournera 'null' tel qu'indiqu� dans la documentation.
             */
        }
        return series;
    }

    /**
     * D�finie les s�ries correspondantes � cette enveloppe spatio-temporelle. Cette m�thode est
     * appel�e automatiquement par {@link BoundingBoxBuilder#run}. Le tableau {@code series} peut
     * �tre {@code null} si l'obtention des s�ries a �chou�e, auquel cas une nouvelle tentative
     * sera faite la prochaine fois o� {@link #prefetch} sera appel�e.
     */
    final synchronized void setSeries(final Series[] series) {
        this.series = series;
        notifyAll();
    }

    /**
     * Pr�vient cet objet qu'on lui demandera bient�t la {@linkplain #getSeries liste des s�ries}.
     * Cette m�thode obtiendra d'avance la liste des s�ries dans un thread en arri�re-plan. Cette
     * m�thode peut �tre appel�e apr�s que l'enveloppe spatio-temporelle aie �t� sp�cifi�e.
     */
    final void prefetch() {
        BoundingBoxBuilder.DEFAULT.add(this);
    }

    /**
     * Retourne la cat�gorie qui suit la cat�gorie sp�cifi�e dans la structure de l'arborescence.
     * <ul>
     *   <li>Si {@code current} est {@code null}, alors cette m�thode retourne la premi�re cat�gorie.</li>
     *   <li>Si {@code current} est la derni�re cat�gorie, alors cette m�thode retourne {@code null}.</li>
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
     * D�finie la structure de l'arborescence. La structure par d�faut est {{@code THEMATIC},
     * {@code PROCEDURE}). Il est de la responsabilit� de l'appellant de mettre � jour le
     * noeud {@link RootNode} correspondant � cet objet {@code BoundingBox}.
     */
    final void setTreeLayout(final TreeDepth[] layout) {
        final Set<TreeDepth> set = new LinkedHashSet<TreeDepth>();
        set.add(null); // Garantie que la premi�re valeur sera nulle.
        for (final TreeDepth d : layout) {
            set.add(d);
        }
        set.remove(SERIES); // Pour garantir que les s�ries seront en dernier.
        set.add(SERIES);
        // Construit un tableau d'un �l�ment plus long pour que ce dernier �l�ment soit nul.
        treeLayout = set.toArray(new TreeDepth[set.size() + 1]);
    }

    /**
     * Enregistre cet objet en binaire dans le fichier sp�cifi�.
     *
     * @param  writeTo Le fichier dans lequel enregistrer.
     * @throws IOException si l'enregistrement a �chou�.
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
     * Retourne le nom de cette r�gion g�ographique.
     */
    @Override
    public String toString() {
        return name;
    }
}
