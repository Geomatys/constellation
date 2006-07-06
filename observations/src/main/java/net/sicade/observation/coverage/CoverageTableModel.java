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
package net.sicade.observation.coverage;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Date;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import javax.swing.JTable;
import javax.swing.table.TableModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

import org.geotools.image.io.IIOListeners;
import org.geotools.coverage.grid.GridCoverage2D;

import net.sicade.util.DateRange;
import net.sicade.resources.XArray;
import net.sicade.resources.seagis.Resources;
import net.sicade.resources.seagis.ResourceKeys;
import net.sicade.observation.CatalogException;


/**
 * Mod�le de tableau pour un affichage graphique d'informations sur des images. Ce mod�le
 * fait le lien une {@linkplain Series s�ries d'images} et l'afficheur {@link JTable} de
 * <cite>Swing</cite>. Les donn�es d'une table d'images peuvent �tre affich�es comme suit:
 *
 * <blockquote><pre>
 * final {@linkplain Series}     series = ...;
 * final {@linkplain TableModel} model  = new CoverageTableModel(series);
 * final {@linkplain JTable}     view   = new JTable(model);
 * </pre></blockquote>
 *
 * Les cellules de la table peuvent �tre affich�es de diff�rentes couleurs. Par
 * exemple les images qui ont �t� vues peuvent �tre �crites en bleu, tandis que
 * les images manquantes peuvent �tre �crites en rouge. Cet affichage color� en
 * fonction des images peut �tre activ� avec le code suivant:
 *
 * <blockquote><pre>
 * {@linkplain TableCellRenderer} renderer = new {@linkplain CellRenderer}();
 * view.setDefaultRenderer({@linkplain String}.class, renderer);
 * view.setDefaultRenderer(  {@linkplain Date}.class, renderer);
 * </pre></blockquote>
 *
 * La classe {@code CoverageTableModel} garde une trace des images qui sont ajout�es ou retir�es
 * de la table. Ces op�rations peuvent �tre annul�es. Les fonctions "annuler" et "refaire" peuvent
 * �tre activ�es avec le code suivant:
 *
 * <blockquote><pre>
 * final {@linkplain UndoManager} undoManager = new UndoManager();
 * ((CoverageTableModel) model).addUndoableEditListener(undoManager);
 * </pre></blockquote>
 *
 * On peut ensuite utiliser les m�thodes {@link UndoManager#undo} et {@link UndoManager#redo}
 * pour d�faire ou refaire une op�ration.
 * <p>
 * La plupart des m�thodes de cette classe peuvent �tre appel�e de n'importe quel thread (pas
 * n�cessairement celui de <cite>Swing</cite>). Si l'appel d'une m�thode a chang�e le contenu
 * de la table, <cite>Swing</cite> en sera inform� dans son propre thread m�me si les m�thodes
 * ont �t� appel�es d'un autre thread.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageTableModel extends AbstractTableModel {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = 6723633134014245147L;

    /**
     * Indique s'il faut inverser l'ordre des enregistrements.
     */
    private static final boolean REVERSE_ORDER = true;

    /** Num�ro de colonne des noms de fichiers.   */ private static final int NAME     = 0;
    /** Num�ro de colonne des dates des images.   */ private static final int DATE     = 1;
    /** Num�ro de colonne de la dur�e des images. */ private static final int DURATION = 2;

    /**
     * Liste des titres des colonnes.
     */
    private final String[] titles = new String[] {
        /*[0]*/ Resources.format(ResourceKeys.NAME),
        /*[1]*/ Resources.format(ResourceKeys.END_TIME),
        /*[2]*/ Resources.format(ResourceKeys.DURATION)
    };

    /**
     * Liste des classes des valeurs des colonnes.
     */
    private static final Class[] CLASS = new Class[] {
        /*[0]:Name*/ String.class,
        /*[1]:Date*/   Date.class,
        /*[2]:Time*/ String.class
    };

    /**
     * S�rie d'images repr�sent� par cette table.
     */
    private Series series;

    /**
     * Liste des entr�es contenues dans cette table. La longueur
     * de ce tableau est le nombre de lignes dans la table.
     */
    private CoverageReference[] entries;

    /**
     * La langue � utiliser.
     */
    private final Locale locale = Locale.getDefault();

    /**
     * Objet � utiliser pour formatter les dates des images.
     */
    private final DateFormat dateFormat;

    /**
     * Objet � utiliser pour formatter les dur�es des images.
     */
    private final DateFormat timeFormat;

    /**
     * Objet � utiliser pour formatter les nombres.
     */
    private final NumberFormat numberFormat;

    /**
     * Objet � utiliser pour obtenir la position d'un champ formatt�.
     */
    private transient FieldPosition fieldPosition;

    /**
     * Buffer dans lequel formater les champs.
     */
    private transient StringBuffer buffer;

    /**
     * Mot "jour" dans la langue de l'utilisateur.
     */
    private static final String DAY = Resources.format(ResourceKeys.DAY);

    /**
     * Mot "jours" dans la langue de l'utilisateur.
     */
    private static final String DAYS = Resources.format(ResourceKeys.DAYS);

    /**
     * Construit une table initialement vide.
     */
    public CoverageTableModel() {
        entries      = new CoverageReference[0];
        numberFormat = NumberFormat.getNumberInstance(locale);
        dateFormat   = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
        timeFormat   = new SimpleDateFormat("HH:mm", locale);
        timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Construit une table pour la s�rie d'image sp�cifi�e. Toutes les images de la s�ries seront
     * ajout�es � cette table.
     *
     * @param  series S�ries que repr�sentera cette table, ou {@code null} si elle n'est pas connue.
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    public CoverageTableModel(final Series series) throws CatalogException {
        this();
        this.series = series;
        if (series != null) {
            final Collection<CoverageReference> entryList = series.getCoverageReferences();
            entries = entryList.toArray(new CoverageReference[entryList.size()]);
            if (REVERSE_ORDER) {
                reverse(entries);
            }
        }
    }

    /**
     * Construit une table qui contiendra une copie du contenu de la table sp�cifi�e.
     * La nouvelle table ne contiendra initialement aucun {@code Listener} (c'est
     * � dire que les {@code Listener} de la table sp�cifi�e ne seront pas copi�s).
     *
     * @param table Table dont on veut copier le contenu.
     */
    public CoverageTableModel(final CoverageTableModel table) {
        synchronized (table) {
            series       =                       table.series;
            numberFormat =    (NumberFormat)     table.numberFormat.clone();
            dateFormat   =      (DateFormat)     table.  dateFormat.clone();
            timeFormat   =      (DateFormat)     table.  timeFormat.clone();
            entries      = (CoverageReference[]) table.     entries.clone();
            final CoverageReference[] entries = this.entries;
            for (int i=entries.length; --i>=0;) {
                if (entries[i] instanceof CoverageProxy) {
                    final CoverageProxy oldProxy = (CoverageProxy) entries[i];
                    final CoverageProxy newProxy = new CoverageProxy(unwrap(oldProxy.getParent()));
                    newProxy.flags = oldProxy.flags;
                    entries[i] = newProxy;
                }
            }
        }
    }

    /**
     * Renverse l'ordre des �l�ments du tableau sp�cifi�.
     */
    private static void reverse(final CoverageReference[] entries) {
        for (int i=entries.length/2; --i>=0;) {
            final int j = entries.length-1-i;
            final CoverageReference tmp = entries[i];
            entries[i] = entries[j];
            entries[j] = tmp;
        }
    }

    /**
     * Retourne la s�rie d'images repr�sent�e par cette table. Si la s�rie n'est pas connue,
     * alors cette m�thode peut retourner {@code null}.
     */
    public Series getSeries() {
        return series;
    }

    /**
     * Remplace toutes les r�f�rences vers les images par celles de la s�rie sp�cifi�e.
     * Cette m�thode peut �tre appel�e de n'importe quel thread (pas n�cessairement celui
     * de <cite>Swing</cite>).
     *
     * @param  series La nouvelle s�rie d'images, ou {@code null} si aucune.
     * @throws CatalogException si l'interrogation du catalogue a �chou�.
     */
    public void setSeries(final Series series) throws CatalogException {
        final Collection<CoverageReference> entryList;
        if (series != null) {
            entryList = series.getCoverageReferences();
        } else {
            entryList = Collections.emptyList();
        }
        this.series = series;
        setCoverageReferences(entryList);
    }

    /**
     * Remplace toutes les r�f�rences vers les images par celles de la liste sp�cifi�e.
     * Cette m�thode peut �tre appel�e de n'importe quel thread (pas n�cessairement celui
     * de <cite>Swing</cite>).
     *
     * @param entryList Liste des nouvelles images.
     */
    public synchronized void setCoverageReferences(final Collection<CoverageReference> entryList) {
        final CoverageReference[] newEntries = entryList.toArray(new CoverageReference[entryList.size()]);
        if (REVERSE_ORDER) {
            reverse(newEntries);
        }
        final CoverageReference[] oldEntries = entries;
        this.entries = newEntries;
        final Map<CoverageReference,CoverageProxy> proxies = getProxies(oldEntries, null);
        if (proxies != null) {
            for (int i=newEntries.length; --i>=0;) {
                final CoverageProxy proxy = proxies.get(newEntries[i]);
                if (proxy != null) {
                    newEntries[i] = proxy;
                }
            }
        }
        if (EventQueue.isDispatchThread()) {
            fireTableDataChanged();
            commitEdit(oldEntries, newEntries, ResourceKeys.DEFINE);
        } else EventQueue.invokeLater(new Runnable() {
            public void run() {
                fireTableDataChanged();
                commitEdit(oldEntries, newEntries, ResourceKeys.DEFINE);
            }
        });
    }

    /**
     * Retourne l'ensemble des objets {@link CoverageProxy} qui se trouvent dans le tableau sp�cifi�.
     * Cette m�thode retourne {@code null} si aucun objets {@link CoverageProxy} n'a �t� trouv�.
     *
     * @param  entries Entr�es dans lequel v�rifier s'il y a des {@link CoverageProxy}.
     * @param  proxies Dictionnaire dans lequel ajouter les {@link CoverageProxy} trouv�s,
     *         ou {@code null} si aucun dictionnaire n'a encore �t� cr��.
     * @return L'argument {@code proxies}, ou un nouvel objet {@link Map} si {@code proxies} �tait nul.
     */
    private static Map<CoverageReference,CoverageProxy> getProxies(final CoverageReference[] entries,
                   Map<CoverageReference,CoverageProxy> proxies)
    {
        if (entries != null) {
            for (int i=entries.length; --i>=0;) {
                final CoverageReference entry = entries[i];
                if (entry instanceof CoverageProxy) {
                    if (proxies == null) {
                        proxies = new HashMap<CoverageReference,CoverageProxy>();
                    }
                    final CoverageProxy proxy = (CoverageProxy) entry;
                    proxies.put(proxy.getParent(), proxy);
                }
            }
        }
        return proxies;
    }

    /**
     * Si {@code entry} est de la classe {@link CoverageProxy},
     * retourne l'objet {@link CoverageReference} qu'il enveloppait.
     */
    private static CoverageReference unwrap(CoverageReference entry) {
        while (entry instanceof CoverageProxy) {
            entry = ((CoverageProxy) entry).getParent();
        }
        return entry;
    }

    /**
     * Retourne les r�f�rences vers toutes les images pr�sentes dans la table. Les op�rations de
     * lectures effectu�es sur les r�f�rences retourn�es ne seront pas indiqu�es dans cette table
     * (contrairement aux entr�es retourn�es par {@link #getCoverageReferenceAt}, qui �crive en
     * bleu les images lues).
     *
     * @return Les r�f�rences vers toutes les images de cette table. Ce tableau peut
     *         avoir une longueur de 0, mais ne sera jamais {@code null}.
     */
    public synchronized CoverageReference[] getCoverageReferences() {
        final CoverageReference[] entries = this.entries;
        final CoverageReference[] out = new CoverageReference[(entries!=null) ? entries.length : 0];
        for (int i=out.length; --i>=0;) {
            out[i] = unwrap(entries[i]);
        }
        return out;
    }

    /**
     * Retourne la r�f�rence de l'image qui se trouve � la ligne sp�cifi�e. Pour �conomiser la m�moire,
     * il est recommand� de ne pas retenir cette r�f�rence plus longtemps que la dur�e de vie de cette
     * table.
     *
     * @param  row Index de l'entr� d�sir�.
     * @return R�f�rence vers l'image � la ligne sp�cifi�e.
     */
    public synchronized CoverageReference getCoverageReferenceAt(final int row) {
        CoverageReference entry = entries[row];
        if (!(entry instanceof CoverageProxy)) {
            entries[row] = entry = new CoverageProxy(entry);
        }
        return entry;
    }

    /**
     * Retourne les noms des images pr�sentes dans cette table. Les noms sont obtenus par
     * {@link #getCoverageName} et sont habituellement unique pour une s�rie donn�e. Cette
     * m�thode peut retourner un tableau de longueur 0, mais ne retourne jamais {@code null}.
     */
    public synchronized String[] getCoverageNames() {
        final String[] names = new String[(entries!=null) ? entries.length : 0];
        for (int i=0; i<names.length; i++) {
            names[i] = getCoverageName(entries[i]);
        }
        return names;
    }

    /**
     * Retourne les noms des images aux lignes sp�cifi�es. Les noms sont obtenus par
     * {@link #getCoverageName}. Cette m�thode peut retourner un tableau de longueur 0,
     * mais ne retourne jamais {@code null}.
     */
    public synchronized String[] getCoverageNames(int[] rows) {
        final String[] names = new String[rows.length];
        for (int i=0; i<names.length; i++) {
            names[i] = getCoverageName(entries[rows[i]]);
        }
        return names;
    }

    /**
     * Retourne les num�ros de lignes qui correspondent aux images sp�cifi�es. Les images sont
     * d�sign�es par leurs noms tels que retourn�s par {@link #getCoverageName}. Cette m�thode
     * est l'inverse de {@link #getCoverageNames(int[])}.
     *
     * @param  names Noms des images.
     * @return Num�ro de lignes des images demand�es. Ce tableau aura toujours la m�me longueur que
     *         {@code names}. Les images qui n'ont pas �t� trouv�es dans la table auront l'index -1.
     */
    public synchronized int[] indexOf(final String[] names) {
        final Map<String,int[]> map = new HashMap<String,int[]>(names.length*2);
        for (int i=0; i<names.length; i++) {
            int[] index = map.put(names[i], new int[]{i});
            if (index != null) {
                // Cas o� le m�me nom serait demand� plusieurs fois.
                final int length = index.length;
                index = XArray.resize(index, length+1);
                index[length] = i;
                map.put(names[i], index);
            }
        }
        final int[] rows = new int[names.length];
        Arrays.fill(rows, -1);
        // Fait la boucle en sens inverse de fa�on � ce qu'en cas de doublons,
        // l'occurence retenue soit la premi�re apparaissant dans la liste.
        for (int i=entries.length; --i>=0;) {
            final int[] index = map.get(getCoverageName(entries[i]));
            if (index != null) {
                for (int j=0; j<index.length; j++) {
                    rows[index[j]] = i;
                }
            }
        }
        return rows;
    }

    /**
     * Retire une image de cette table. Si {@code toRemove} est
     * nul ou n'appara�t pas dans la table, alors il sera ignor�.
     */
    public synchronized void remove(final CoverageReference toRemove) {
        remove(Collections.singleton(unwrap(toRemove)));
    }

    /**
     * Retire l'image qui se trouve � l'index sp�cifi�. L'index {@code row} correspond
     * au num�ro (� partir de 0) de la ligne � supprimer.
     */
    public synchronized void remove(final int row) {
        remove(entries[row]);
    }

    /**
     * Retire plusieurs images de cette table. Les r�f�rences nulles ainsi que celles
     * qui n'apparaissent pas dans cette table seront ignor�es.
     */
    public synchronized void remove(final CoverageReference[] toRemove) {
        final Set<CoverageReference> toRemoveSet;
        toRemoveSet = new HashSet<CoverageReference>(2*toRemove.length);
        for (int i=0; i<toRemove.length; i++) {
            toRemoveSet.add(unwrap(toRemove[i]));
        }
        remove(toRemoveSet);
    }

    /**
     * Retire plusieurs images d�sign�s par les index des lignes. Les index {@code rows} correspondent
     * aux num�ros (� partir de 0) des lignes � supprimer. Ces num�ros de lignes peuvent �tre dans
     * n'importe quel ordre.
     */
    public synchronized void remove(final int[] rows) {
        final Set<CoverageReference> toRemoveSet;
        toRemoveSet = new HashSet<CoverageReference>(2*rows.length);
        for (int i=0; i<rows.length; i++) {
            toRemoveSet.add(unwrap(entries[rows[i]]));
        }
        remove(toRemoveSet);
    }

    /**
     * Retire plusieurs images de cette table. Les r�f�rences nulles ainsi que celles qui
     * n'apparaissent pas dans cette table seront ignor�es. Cette m�thode peut �tre appel�e
     * de n'importe quel thread (pas n�cessairement celui de <cite>Swing</cite>).
     */
    private synchronized void remove(final Set<CoverageReference> toRemove) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    remove(toRemove);
                }
            });
            return;
        }
        final CoverageReference[] oldEntries = entries;
        CoverageReference[] entries = oldEntries;
        int entriesLength = entries.length;
        int upper = entriesLength;
        for (int i=upper; --i>=-1;) {
            if (i<0 || !toRemove.contains(unwrap(entries[i]))) {
                final int lower = i+1;
                if (upper != lower) {
                    if (entries == oldEntries) {
                        // Cr�� une copie, de fa�on � ne pas modifier le tableau 'entries' original.
                        entries = XArray.remove(entries, lower, upper-lower);
                    } else {
                        // Si le tableau est d�j� une copie, travaille directement sur lui.
                        System.arraycopy(entries, upper, entries, lower, entriesLength-upper);
                    }
                    entriesLength -= (upper-lower);
                    fireTableRowsDeleted(lower, upper-1);
                }
                upper=i;
            }
        }
        this.entries = XArray.resize(entries, entriesLength);
        commitEdit(oldEntries, this.entries, ResourceKeys.DELETE);
    }

    /**
     * Copie les donn�es de certaines lignes dans un objet transf�rable. Cet objet pourra �tre
     * plac� dans le presse papier pour �tre ensuite coll� dans un tableur commercial par exemple.
     * Le presse-papier du syst�me peut �tre obtenu par un appel �:
     *
     * <blockquote><pre>
     * Toolkit.getDefaultToolkit().getSystemClipboard()
     * </pre></blockquote>
     *
     * @param  rows Ligne � copier.
     * @return Objet transf�rable contenant les lignes copi�es.
     */
    public synchronized Transferable copy(final int[] rows) {
        if (fieldPosition == null) {
            fieldPosition = new FieldPosition(0);
        }
        final StringBuffer buffer = new StringBuffer(256); // On n'utilise pas le buffer des cellules.
        final int[] cl�s = new int[] {
            ResourceKeys.NAME,
            ResourceKeys.START_TIME,
            ResourceKeys.END_TIME
        };
        for (int i=0; i<cl�s.length;) {
            buffer.append(Resources.format(cl�s[i++]));
            buffer.append((i<cl�s.length) ? '\t' : '\n');
        }
        for (int i=0; i<rows.length; i++) {
            Date date;
            final CoverageReference entry = unwrap(entries[rows[i]]);
            final DateRange timeRange = entry.getTimeRange();
            buffer.append(getCoverageName(entry));
            buffer.append('\t');
            if ((date=(Date)timeRange.getMinValue()) != null) {
                dateFormat.format(date, buffer, fieldPosition);
            }
            buffer.append('\t');
            if ((date=(Date)timeRange.getMaxValue()) != null) {
                dateFormat.format(date, buffer, fieldPosition);
            }
            buffer.append('\n');
            // Note: on devrait utiliser System.getProperty("line.separator", "\n"),
            //       mais �a donne un r�sultat bizarre quand on colle dans Excel. Il
            //       met une ligne vierge entre chaque ligne de donn�es.
        }
        return new StringSelection(buffer.toString());
        // TODO: Dans une version future, on pourra supporter une plus
        //       grande gamme de types: 'javaSerializedObjectMimeType',
        //       'javaJVMLocalObjectMimeType', etc...
    }

    /**
     * Retourne le nombre de lignes de ce tableau.
     */
    public int getRowCount() {
        return (entries!=null) ? entries.length : 0;
    }

    /**
     * Retourne le nombre de colonnes de ce tableau.
     */
    public int getColumnCount() {
        return titles.length;
    }

    /**
     * Retourne le nom de la colonne sp�cifi�e.
     */
    @Override
    public String getColumnName(final int column) {
        return titles[column];
    }

    /**
     * Retourne la classe des objets de la colonne sp�cifi�e.
     */
    @Override
    public Class getColumnClass(final int column) {
        return CLASS[column];
    }

    /**
     * Retourne la valeur de la cellule aux index sp�cifi�s.
     *
     * @param  row    Num�ro de ligne de la cellule, � partir de 0.
     * @param  column Num�ro de colonne de la cellule, � partir de 0.
     * @return Valeur de la cellule aux index sp�cifi�s.
     */
    public synchronized Object getValueAt(final int row, final int column) {
        CoverageReference entry = entries[row];
        if (!(entry instanceof CoverageProxy)) {
            entries[row] = entry = new CoverageProxy(entry);
        }
        switch (column) {
            default:   return null;
            case NAME: return getCoverageName(entry);
            case DATE: return entry.getTimeRange().getMaxValue();
            case DURATION: {
                if (buffer        == null) buffer        = new StringBuffer ( );
                if (fieldPosition == null) fieldPosition = new FieldPosition(0);
                buffer.setLength(0);
                final DateRange range = entry.getTimeRange();
                final Date      time  = range.getMaxValue();
                final Date      start = range.getMinValue();
                if (time!=null && start!=null) {
                    final long millis = time.getTime()-start.getTime();
                    final long days   = millis/(24L*60*60*1000);
                    time.setTime(millis);
                    numberFormat.format(days, buffer, fieldPosition);
                    buffer.append(' ');
                    buffer.append((days>1) ? DAYS : DAY);
                    buffer.append(' ');
                    timeFormat.format(time, buffer, fieldPosition);
                }
                return buffer.toString();
            }
        }
    }

    /**
     * Retourne le nom de l'entr�e sp�cifi�e � utiliser pour l'affichage. L'impl�mentation par
     * d�faut retourne le {@linkplain CoverageReference#getName nom de l'entr�e} en ne retenant
     * que la partie qui suit le premier caract�re {@code :}. Ca a pour effet d'omettre le nom
     * de la sous-s�rie qui pr�c�de le nom de fichier. Les classes d�riv�es peuvent red�finir
     * cette m�thode si elles veulelent construire un nom diff�rement.
     */
    protected String getCoverageName(final CoverageReference entry) {
        String name = entry.getName();
        final int sep = name.indexOf(':');
        if (sep >= 0) {
            name = name.substring(sep+1);
        }
        return name;
    }

    /**
     * Convertit une date en cha�ne de caract�res.
     */
    private String format(final Date date) {
        if (buffer        == null) buffer        = new StringBuffer ( );
        if (fieldPosition == null) fieldPosition = new FieldPosition(0);
        buffer.setLength(0);
        dateFormat.format(date, buffer, fieldPosition);
        return buffer.toString();
    }

    /**
     * Retourne le fuseau horaire utilis� pour les �critures de dates.
     */
    public synchronized TimeZone getTimeZone() {
        return dateFormat.getTimeZone();
    }

    /**
     * D�finit le fuseau horaire � utiliser pour l'�criture des dates.
     */
    public synchronized void setTimeZone(final TimeZone timezone) {
        dateFormat.setTimeZone(timezone);
        if (entries.length != 0) {
            fireTableChanged(new TableModelEvent(this, 0, entries.length-1, DATE));
        }
    }

    /**
     * Ajoute un objet � la liste des objets int�ress�s � �tre
     * inform�s chaque fois qu'une �dition anulable a �t� faite.
     */
    public void addUndoableEditListener(final UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }

    /**
     * Retire un objet de la liste des objets int�ress�s � �tre
     * inform�s chaque fois qu'une �dition anulable a �t� faite.
     */
    public void removeUndoableEditListener(final UndoableEditListener listener) {
        listenerList.remove(UndoableEditListener.class, listener);
    }

    /**
     * Prend en compte des changements qui viennent d'�tre apport�es � la table.
     * Cette m�thode mettra � jour la variable {@link #backup} et pr�viendra tous
     * les objets qui �taient int�ress�s � �tre inform�s des changements anulables.
     */
    private void commitEdit(final CoverageReference[] oldEntries,
                            final CoverageReference[] newEntries,
                            final int cl�) // NO synchronized!
    {
        final String name = Resources.format(cl�).toLowerCase();
        if (oldEntries != newEntries) {
            final Object[] listeners=listenerList.getListenerList();
            if (listeners.length != 0) {
                UndoableEditEvent event = null;
                for (int i=listeners.length; (i-=2)>=0;) {
                    if (listeners[i]==UndoableEditListener.class) {
                        if (event==null) event=new UndoableEditEvent(this, new AbstractUndoableEdit() {
                            public void undo() throws CannotUndoException {super.undo(); entries=oldEntries; fireTableDataChanged();}
                            public void redo() throws CannotRedoException {super.redo(); entries=newEntries; fireTableDataChanged();}
                            public String getPresentationName() {return name;}
                        });
                        ((UndoableEditListener) listeners[i+1]).undoableEditHappened(event);
                    }
                }
            }
        }
    }

    /**
     * Indique que la r�f�rence {@link #entry} a chang�. Cette m�thode recherche la ligne
     * correspondant � cette r�f�rence et lance l'�v�nement appropri�e. Cette m�thode peut
     * �tre appel�e � partir de n'importe quel thread (pas n�cessairement celui de
     * <cite>Swing</cite>).
     */
    private void fireTableRowsUpdated(CoverageReference entry) { // NO synchronized
        entry = unwrap(entry);
        final CoverageReference[] entries = this.entries;
        for (int i=entries.length; --i>=0;) {
            if (entry.equals(unwrap(entries[i]))) {
                final int row = i;
                if (EventQueue.isDispatchThread()) {
                    fireTableRowsUpdated(row, row);
                } else {
                    EventQueue.invokeLater(new Runnable() {
                        public void run() {
                            fireTableRowsUpdated(row, row);
                        }
                    });
                }
            }
        }
    }

    /**
     * Classe des r�f�rences vers des images. Cette classe redirige la plupart des appels de ses
     * m�thodes vers un autre objet {@link CoverageReference}. La principale exception est la m�thode
     * {@link #getCoverage}, qui intercepte les appels pour mettre � jour des variables internes
     * indiquant si une image a �t� vue ou si sa lecture a �chou�.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class CoverageProxy extends CoverageReference.Proxy {
        /**
         * Num�ro de s�rie (pour compatibilit� avec des versions ant�rieures).
         */
        private static final long serialVersionUID = 8398851451224196337L;

        /** Drapeau indiquant qu'une image a �t� vue.        */ public static final byte VIEWED      = 1;
        /** Drapeau indiquant qu'un fichier est introuvable. */ public static final byte MISSING     = 2;
        /** Drapeau indiquant qu'un fichier est mauvais.     */ public static final byte CORRUPTED   = 4;
        /** Drapeau indiquant qu'un appel RMI a �chou�.      */ public static final byte RMI_FAILURE = 8;
        /** Drapeau indiquant l'�tat de l'image courante.    */ public              byte flags;

        /**
         * Construit un proxy.
         */
        public CoverageProxy(final CoverageReference entry) {
            super(entry);
            FileChecker.add(this);
        }

        /**
         * Proc�de � la lecture d'une image. Si la lecture a r�ussi sans avoir �t�
         * annul�e par l'utilisateur, alors le drapeau {@link #VIEWED} sera lev�.
         * Si la lecture a �chou�, alors le drapeau {@link #CORRUPTED} sera lev�.
         */
        @Override
        public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
            try {
                final GridCoverage2D image = getParent().getCoverage(listeners);
                setFlag((byte)(MISSING|CORRUPTED|RMI_FAILURE), false);
                setFlag(VIEWED, image!=null);
                return image;
            } catch (RemoteException exception) {
                setFlag(RMI_FAILURE, true);
                throw exception;
            } catch (FileNotFoundException exception) {
                setFlag(MISSING, true);
                throw exception;
            } catch (IOException exception) {
                setFlag(CORRUPTED, true);
                throw exception;
            }
        }

        /**
         * Place ou retire les drapeaux sp�cifi�s. Si l'appel de cette m�thode a modifi�
         * l'�tat des drapeaux, alors {@link #fireTableRowsUpdated} sera appel�e.
         */
        public synchronized void setFlag(byte f, final boolean set) {
            if (set) f |= flags;
            else     f  = (byte) (flags & ~f);
            if (flags != f) {
                flags = f;
                fireTableRowsUpdated(getParent());
            }
        }
    }

    /**
     * Classe du thread qui aura la charge de v�rifier si les fichiers des images existent.
     * Lorsqu'un nouvel objet {@link CoverageProxy} est cr��, il peut appeler la m�thode statique
     * {@link #add} pour s'ajouter lui-m�me � la liste des images dont on v�rifiera l'existence.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final static class FileChecker extends Thread {
        /**
         * Thread ayant la charge de v�rifier si des fichiers existent.
         */
        private static FileChecker thread;

        /**
         * Liste des fichiers dont on veut v�rifier l'existence.
         */
        private final LinkedList<CoverageProxy> list = new LinkedList<CoverageProxy>();

        /**
         * Construit un thread qui v�rifiera l'existence des fichiers. Le processus d�marrera
         * imm�diatement, mais bloquera presque aussit�t sur la m�thode {@link #next}  (parce
         * qu'elle est synchronis�e sur le m�me moniteur que {@link #add}, la m�thode qui
         * appelle ce constructeur). L'ex�cution continuera lorsque la m�thode {@link #add}
         * aura termin�, ce qui garantit qu'il y aura au moins une image � v�rifier.
         */
        private FileChecker() {
            super("FileChecker");
            setPriority(MIN_PRIORITY);
            setDaemon(true);
            start();
        }

        /**
         * Ajoute une entr�e � la liste des images � v�rifier.
         */
        public static synchronized void add(final CoverageProxy entry) {
            if (thread == null) {
                thread = new FileChecker();
            }
            thread.list.add(entry);
        }

        /**
         * Retourne la prochaine image � v�rifier, ou {@code null}
         * s'il n'en reste plus. S'il ne reste plus d'images, alors cette
         * m�thode signalera que le thread va mourrir en donnant la valeur
         * {@code null} � {@link #thread].
         */
        private static synchronized CoverageProxy next(final LinkedList<CoverageProxy> list) {
            if (list.isEmpty()) {
                thread = null;
                return null;
            }
            return list.removeFirst();
        }

        /**
         * V�rifie si les fichiers de la liste existent. Si un fichier
         * n'existe pas, le drapeau {@link CoverageProxy#MISSING} sera l�v�
         * pour l'objet {@link CoverageProxy} correspondant. Cette v�rification
         * n'est pas effectu�e pour les objets r�sidant sur un serveur distant.
         */
        @Override
        public void run() {
            CoverageProxy entry;
            while ((entry=next(list)) != null) {
                CoverageReference check = entry;
                do {
                    check = ((CoverageReference.Proxy) check).getParent();
                } while (check instanceof CoverageReference.Proxy);
                final File file = entry.getFile();
                if (file != null) {
                    entry.setFlag(CoverageProxy.MISSING, !file.isFile());
                }
            }
        }
    }

    /**
     * Classe pour afficher des cellules de {@link CoverageTableModel} dans une table
     * {@link JTable}. Par d�faut, cette classe affiche le texte des cellules avec
     * leur couleur habituelle (noir). Elle peut toutefois utiliser des couleurs
     * diff�rentes si l'image a �t� vue (bleu) ou si elle est manquante (rouge).
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    @SuppressWarnings("serial")
    public static class CellRenderer extends DefaultTableCellRenderer {
        /**
         * Couleur par d�faut de la police.
         */
        private Color foreground;

        /**
         * Couleur par d�faut de l'arri�re plan.
         */
        private Color background;

        /**
         * Construit un objet {@code CellRenderer}.
         */
        public CellRenderer() {
            super();
            foreground = super.getForeground();
            background = super.getBackground();
        }

        /**
         * D�finit la couleur de la police.
         */
        @Override
        public void setForeground(final Color foreground) {
            super.setForeground(this.foreground=foreground);
        }

        /**
         * D�finit la couleur de l'arri�re-plan.
         */
        @Override
        public void setBackground(final Color background) {
            super.setBackground(this.background=background);
        }

        /**
         * Retourne une composante � utiliser pour dessiner le contenu des
         * cellules de la table.  Cette m�thode utilise une composante par
         * d�faut, mais en changeant la couleur du texte si l'entr�e correspond
         * � une image qui a d�j� �t� lue.
         */
        @Override
        public Component getTableCellRendererComponent(final JTable table, Object value, final boolean isSelected,
                                                       final boolean hasFocus, final int row, final int column)
        {
            Color foreground = this.foreground;
            Color background = this.background;
            if (row >= 0) {
                final TableModel model=table.getModel();
                if (model instanceof CoverageTableModel) {
                    final CoverageTableModel imageTable = (CoverageTableModel) model;
                    if (value instanceof Date) {
                        value = imageTable.format((Date) value);
                    }
                    final CoverageReference entry = imageTable.entries[row];
                    if (entry instanceof CoverageProxy) {
                        final byte flags = ((CoverageProxy) entry).flags;
                        if ((flags & CoverageProxy.VIEWED     ) != 0) {foreground=Color.BLUE ;                         }
                        if ((flags & CoverageProxy.MISSING    ) != 0) {foreground=Color.RED  ;                         }
                        if ((flags & CoverageProxy.CORRUPTED  ) != 0) {foreground=Color.WHITE; background=Color.RED;   }
                        if ((flags & CoverageProxy.RMI_FAILURE) != 0) {foreground=Color.BLACK; background=Color.YELLOW;}
                    }
                }
            }
            super.setBackground(background);
            super.setForeground(foreground);
            return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
    }
}
