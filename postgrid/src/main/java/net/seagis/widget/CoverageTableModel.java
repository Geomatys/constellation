/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
 */
package net.seagis.widget;

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

import org.geotools.util.DateRange;
import org.geotools.image.io.IIOListeners;
import org.geotools.coverage.grid.GridCoverage2D;

import net.seagis.resources.XArray;
import net.seagis.resources.i18n.Resources;
import net.seagis.resources.i18n.ResourceKeys;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.Layer;
import net.seagis.coverage.catalog.CoverageReference;


/**
 * Modèle de tableau pour un affichage graphique d'informations sur des images. Ce modèle
 * fait le lien une {@linkplain Layer couche} d'images et l'afficheur {@link JTable} de
 * <cite>Swing</cite>. Les données d'une table d'images peuvent être affichées comme suit:
 *
 * <blockquote><pre>
 * final {@linkplain Layer}      layer = ...;
 * final {@linkplain TableModel} model = new CoverageTableModel(layer);
 * final {@linkplain JTable}     view  = new JTable(model);
 * </pre></blockquote>
 *
 * Les cellules de la table peuvent être affichées de différentes couleurs. Par
 * exemple les images qui ont été vues peuvent être écrites en bleu, tandis que
 * les images manquantes peuvent être écrites en rouge. Cet affichage coloré en
 * fonction des images peut être activé avec le code suivant:
 *
 * <blockquote><pre>
 * {@linkplain TableCellRenderer} renderer = new {@linkplain CellRenderer}();
 * view.setDefaultRenderer({@linkplain String}.class, renderer);
 * view.setDefaultRenderer(  {@linkplain Date}.class, renderer);
 * </pre></blockquote>
 *
 * La classe {@code CoverageTableModel} garde une trace des images qui sont ajoutées ou retirées
 * de la table. Ces opérations peuvent être annulées. Les fonctions "annuler" et "refaire" peuvent
 * être activées avec le code suivant:
 *
 * <blockquote><pre>
 * final {@linkplain UndoManager} undoManager = new UndoManager();
 * ((CoverageTableModel) model).addUndoableEditListener(undoManager);
 * </pre></blockquote>
 *
 * On peut ensuite utiliser les méthodes {@link UndoManager#undo} et {@link UndoManager#redo}
 * pour défaire ou refaire une opération.
 * <p>
 * La plupart des méthodes de cette classe peuvent être appelée de n'importe quel thread (pas
 * nécessairement celui de <cite>Swing</cite>). Si l'appel d'une méthode a changée le contenu
 * de la table, <cite>Swing</cite> en sera informé dans son propre thread même si les méthodes
 * ont été appelées d'un autre thread.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class CoverageTableModel extends AbstractTableModel {
    /**
     * Pour compatibilités entre les enregistrements binaires de différentes versions.
     */
    private static final long serialVersionUID = 6723633134014245147L;

    /**
     * Indique s'il faut inverser l'ordre des enregistrements.
     */
    private static final boolean REVERSE_ORDER = true;

    /** Numéro de colonne des noms de fichiers.   */ private static final int NAME     = 0;
    /** Numéro de colonne des dates des images.   */ private static final int DATE     = 1;
    /** Numéro de colonne de la durée des images. */ private static final int DURATION = 2;

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
     * Couche d'images représenté par cette table.
     */
    private Layer layer;

    /**
     * Liste des entrées contenues dans cette table. La longueur
     * de ce tableau est le nombre de lignes dans la table.
     */
    private CoverageReference[] entries;

    /**
     * La langue à utiliser.
     */
    private final Locale locale = Locale.getDefault();

    /**
     * Objet à utiliser pour formatter les dates des images.
     */
    private final DateFormat dateFormat;

    /**
     * Objet à utiliser pour formatter les durées des images.
     */
    private final DateFormat timeFormat;

    /**
     * Objet à utiliser pour formatter les nombres.
     */
    private final NumberFormat numberFormat;

    /**
     * Objet à utiliser pour obtenir la position d'un champ formatté.
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
     * Construit une table pour la couche d'image spécifiée. Toutes les images de la couches seront
     * ajoutées à cette table.
     *
     * @param  layer Couche que représentera cette table, ou {@code null} si elle n'est pas connue.
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    public CoverageTableModel(final Layer layer) throws CatalogException {
        this();
        this.layer = layer;
        if (layer != null) {
            final Collection<CoverageReference> entryList = layer.getCoverageReferences();
            entries = entryList.toArray(new CoverageReference[entryList.size()]);
            if (REVERSE_ORDER) {
                reverse(entries);
            }
        }
    }

    /**
     * Construit une table qui contiendra une copie du contenu de la table spécifiée.
     * La nouvelle table ne contiendra initialement aucun {@code Listener} (c'est
     * à dire que les {@code Listener} de la table spécifiée ne seront pas copiés).
     *
     * @param table Table dont on veut copier le contenu.
     */
    public CoverageTableModel(final CoverageTableModel table) {
        synchronized (table) {
            layer        =                       table.layer;
            numberFormat =    (NumberFormat)     table.numberFormat.clone();
            dateFormat   =      (DateFormat)     table.  dateFormat.clone();
            timeFormat   =      (DateFormat)     table.  timeFormat.clone();
            entries      = (CoverageReference[]) table.     entries.clone();
            final CoverageReference[] entries = this.entries;
            for (int i=entries.length; --i>=0;) {
                if (entries[i] instanceof CoverageProxy) {
                    final CoverageProxy oldProxy = (CoverageProxy) entries[i];
                    final CoverageProxy newProxy = new CoverageProxy(unwrap(oldProxy.getBackingElement()));
                    newProxy.flags = oldProxy.flags;
                    entries[i] = newProxy;
                }
            }
        }
    }

    /**
     * Renverse l'ordre des éléments du tableau spécifié.
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
     * Retourne la couche d'images représentée par cette table. Si la couche n'est pas connue,
     * alors cette méthode peut retourner {@code null}.
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * Remplace toutes les références vers les images par celles de la couche spécifiée.
     * Cette méthode peut être appelée de n'importe quel thread (pas nécessairement celui
     * de <cite>Swing</cite>).
     *
     * @param  layer La nouvelle couche d'images, ou {@code null} si aucune.
     * @throws CatalogException si l'interrogation du catalogue a échoué.
     */
    public void setLayer(final Layer layer) throws CatalogException {
        final Collection<CoverageReference> entryList;
        if (layer != null) {
            entryList = layer.getCoverageReferences();
        } else {
            entryList = Collections.emptyList();
        }
        this.layer = layer;
        setCoverageReferences(entryList);
    }

    /**
     * Remplace toutes les références vers les images par celles de la liste spécifiée.
     * Cette méthode peut être appelée de n'importe quel thread (pas nécessairement celui
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
     * Retourne l'ensemble des objets {@link CoverageProxy} qui se trouvent dans le tableau spécifié.
     * Cette méthode retourne {@code null} si aucun objets {@link CoverageProxy} n'a été trouvé.
     *
     * @param  entries Entrées dans lequel vérifier s'il y a des {@link CoverageProxy}.
     * @param  proxies Dictionnaire dans lequel ajouter les {@link CoverageProxy} trouvés,
     *         ou {@code null} si aucun dictionnaire n'a encore été créé.
     * @return L'argument {@code proxies}, ou un nouvel objet {@link Map} si {@code proxies} était nul.
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
                    proxies.put(proxy.getBackingElement(), proxy);
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
            entry = ((CoverageProxy) entry).getBackingElement();
        }
        return entry;
    }

    /**
     * Retourne les références vers toutes les images présentes dans la table. Les opérations de
     * lectures effectuées sur les références retournées ne seront pas indiquées dans cette table
     * (contrairement aux entrées retournées par {@link #getCoverageReferenceAt}, qui écrive en
     * bleu les images lues).
     *
     * @return Les références vers toutes les images de cette table. Ce tableau peut
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
     * Retourne la référence de l'image qui se trouve à la ligne spécifiée. Pour économiser la mémoire,
     * il est recommandé de ne pas retenir cette référence plus longtemps que la durée de vie de cette
     * table.
     *
     * @param  row Index de l'entré désiré.
     * @return Référence vers l'image à la ligne spécifiée.
     */
    public synchronized CoverageReference getCoverageReferenceAt(final int row) {
        CoverageReference entry = entries[row];
        if (!(entry instanceof CoverageProxy)) {
            entries[row] = entry = new CoverageProxy(entry);
        }
        return entry;
    }

    /**
     * Retourne les noms des images présentes dans cette table. Les noms sont obtenus par
     * {@link #getCoverageName} et sont habituellement unique pour une couche donnée. Cette
     * méthode peut retourner un tableau de longueur 0, mais ne retourne jamais {@code null}.
     */
    public synchronized String[] getCoverageNames() {
        final String[] names = new String[(entries!=null) ? entries.length : 0];
        for (int i=0; i<names.length; i++) {
            names[i] = getCoverageName(entries[i]);
        }
        return names;
    }

    /**
     * Retourne les noms des images aux lignes spécifiées. Les noms sont obtenus par
     * {@link #getCoverageName}. Cette méthode peut retourner un tableau de longueur 0,
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
     * Retourne les numéros de lignes qui correspondent aux images spécifiées. Les images sont
     * désignées par leurs noms tels que retournés par {@link #getCoverageName}. Cette méthode
     * est l'inverse de {@link #getCoverageNames(int[])}.
     *
     * @param  names Noms des images.
     * @return Numéro de lignes des images demandées. Ce tableau aura toujours la même longueur que
     *         {@code names}. Les images qui n'ont pas été trouvées dans la table auront l'index -1.
     */
    public synchronized int[] indexOf(final String[] names) {
        final Map<String,int[]> map = new HashMap<String,int[]>(names.length*2);
        for (int i=0; i<names.length; i++) {
            int[] index = map.put(names[i], new int[]{i});
            if (index != null) {
                // Cas où le même nom serait demandé plusieurs fois.
                final int length = index.length;
                index = XArray.resize(index, length+1);
                index[length] = i;
                map.put(names[i], index);
            }
        }
        final int[] rows = new int[names.length];
        Arrays.fill(rows, -1);
        // Fait la boucle en sens inverse de façon à ce qu'en cas de doublons,
        // l'occurence retenue soit la première apparaissant dans la liste.
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
     * nul ou n'apparaît pas dans la table, alors il sera ignoré.
     */
    public synchronized void remove(final CoverageReference toRemove) {
        remove(Collections.singleton(unwrap(toRemove)));
    }

    /**
     * Retire l'image qui se trouve à l'index spécifié. L'index {@code row} correspond
     * au numéro (à partir de 0) de la ligne à supprimer.
     */
    public synchronized void remove(final int row) {
        remove(entries[row]);
    }

    /**
     * Retire plusieurs images de cette table. Les références nulles ainsi que celles
     * qui n'apparaissent pas dans cette table seront ignorées.
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
     * Retire plusieurs images désignés par les index des lignes. Les index {@code rows} correspondent
     * aux numéros (à partir de 0) des lignes à supprimer. Ces numéros de lignes peuvent être dans
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
     * Retire plusieurs images de cette table. Les références nulles ainsi que celles qui
     * n'apparaissent pas dans cette table seront ignorées. Cette méthode peut être appelée
     * de n'importe quel thread (pas nécessairement celui de <cite>Swing</cite>).
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
                        // Créé une copie, de façon à ne pas modifier le tableau 'entries' original.
                        entries = XArray.remove(entries, lower, upper-lower);
                    } else {
                        // Si le tableau est déjà une copie, travaille directement sur lui.
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
     * Copie les données de certaines lignes dans un objet transférable. Cet objet pourra être
     * placé dans le presse papier pour être ensuite collé dans un tableur commercial par exemple.
     * Le presse-papier du système peut être obtenu par un appel à:
     *
     * <blockquote><pre>
     * Toolkit.getDefaultToolkit().getSystemClipboard()
     * </pre></blockquote>
     *
     * @param  rows Ligne à copier.
     * @return Objet transférable contenant les lignes copiées.
     */
    public synchronized Transferable copy(final int[] rows) {
        if (fieldPosition == null) {
            fieldPosition = new FieldPosition(0);
        }
        final StringBuffer buffer = new StringBuffer(256); // On n'utilise pas le buffer des cellules.
        final int[] clés = new int[] {
            ResourceKeys.NAME,
            ResourceKeys.START_TIME,
            ResourceKeys.END_TIME
        };
        for (int i=0; i<clés.length;) {
            buffer.append(Resources.format(clés[i++]));
            buffer.append((i<clés.length) ? '\t' : '\n');
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
            //       mais ça donne un résultat bizarre quand on colle dans Excel. Il
            //       met une ligne vierge entre chaque ligne de données.
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
     * Retourne le nom de la colonne spécifiée.
     */
    @Override
    public String getColumnName(final int column) {
        return titles[column];
    }

    /**
     * Retourne la classe des objets de la colonne spécifiée.
     */
    @Override
    public Class getColumnClass(final int column) {
        return CLASS[column];
    }

    /**
     * Retourne la valeur de la cellule aux index spécifiés.
     *
     * @param  row    Numéro de ligne de la cellule, à partir de 0.
     * @param  column Numéro de colonne de la cellule, à partir de 0.
     * @return Valeur de la cellule aux index spécifiés.
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
     * Retourne le nom de l'entrée spécifiée à utiliser pour l'affichage. L'implémentation par
     * défaut retourne le {@linkplain CoverageReference#getName nom de l'entrée} en ne retenant
     * que la partie qui suit le premier caractère {@code :}. Ca a pour effet d'omettre le nom
     * de la série qui précède le nom de fichier. Les classes dérivées peuvent redéfinir
     * cette méthode si elles veulelent construire un nom différement.
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
     * Convertit une date en chaîne de caractères.
     */
    private String format(final Date date) {
        if (buffer        == null) buffer        = new StringBuffer ( );
        if (fieldPosition == null) fieldPosition = new FieldPosition(0);
        buffer.setLength(0);
        dateFormat.format(date, buffer, fieldPosition);
        return buffer.toString();
    }

    /**
     * Retourne le fuseau horaire utilisé pour les écritures de dates.
     */
    public synchronized TimeZone getTimeZone() {
        return dateFormat.getTimeZone();
    }

    /**
     * Définit le fuseau horaire à utiliser pour l'écriture des dates.
     */
    public synchronized void setTimeZone(final TimeZone timezone) {
        dateFormat.setTimeZone(timezone);
        if (entries.length != 0) {
            fireTableChanged(new TableModelEvent(this, 0, entries.length-1, DATE));
        }
    }

    /**
     * Ajoute un objet à la liste des objets intéressés à être
     * informés chaque fois qu'une édition anulable a été faite.
     */
    public void addUndoableEditListener(final UndoableEditListener listener) {
        listenerList.add(UndoableEditListener.class, listener);
    }

    /**
     * Retire un objet de la liste des objets intéressés à être
     * informés chaque fois qu'une édition anulable a été faite.
     */
    public void removeUndoableEditListener(final UndoableEditListener listener) {
        listenerList.remove(UndoableEditListener.class, listener);
    }

    /**
     * Prend en compte des changements qui viennent d'être apportées à la table.
     * Cette méthode mettra à jour la variable {@link #backup} et préviendra tous
     * les objets qui étaient intéressés à être informés des changements anulables.
     */
    private void commitEdit(final CoverageReference[] oldEntries,
                            final CoverageReference[] newEntries,
                            final int clé) // NO synchronized!
    {
        final String name = Resources.format(clé).toLowerCase();
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
     * Indique que la référence {@link #entry} a changé. Cette méthode recherche la ligne
     * correspondant à cette référence et lance l'événement appropriée. Cette méthode peut
     * être appelée à partir de n'importe quel thread (pas nécessairement celui de
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
     * Classe des références vers des images. Cette classe redirige la plupart des appels de ses
     * méthodes vers un autre objet {@link CoverageReference}. La principale exception est la méthode
     * {@link #getCoverage}, qui intercepte les appels pour mettre à jour des variables internes
     * indiquant si une image a été vue ou si sa lecture a échoué.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final class CoverageProxy extends CoverageReference.Proxy {
        /**
         * Numéro de série (pour compatibilité avec des versions antérieures).
         */
        private static final long serialVersionUID = 8398851451224196337L;

        /** Drapeau indiquant qu'une image a été vue.        */ public static final byte VIEWED      = 1;
        /** Drapeau indiquant qu'un fichier est introuvable. */ public static final byte MISSING     = 2;
        /** Drapeau indiquant qu'un fichier est mauvais.     */ public static final byte CORRUPTED   = 4;
        /** Drapeau indiquant qu'un appel RMI a échoué.      */ public static final byte RMI_FAILURE = 8;
        /** Drapeau indiquant l'état de l'image courante.    */ public              byte flags;

        /**
         * Construit un proxy.
         */
        public CoverageProxy(final CoverageReference entry) {
            super(entry);
            FileChecker.add(this);
        }

        /**
         * Procède à la lecture d'une image. Si la lecture a réussi sans avoir été
         * annulée par l'utilisateur, alors le drapeau {@link #VIEWED} sera levé.
         * Si la lecture a échoué, alors le drapeau {@link #CORRUPTED} sera levé.
         */
        @Override
        public GridCoverage2D getCoverage(final IIOListeners listeners) throws IOException {
            try {
                final GridCoverage2D image = getBackingElement().getCoverage(listeners);
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
         * Place ou retire les drapeaux spécifiés. Si l'appel de cette méthode a modifié
         * l'état des drapeaux, alors {@link #fireTableRowsUpdated} sera appelée.
         */
        public synchronized void setFlag(byte f, final boolean set) {
            if (set) f |= flags;
            else     f  = (byte) (flags & ~f);
            if (flags != f) {
                flags = f;
                fireTableRowsUpdated(getBackingElement());
            }
        }
    }

    /**
     * Classe du thread qui aura la charge de vérifier si les fichiers des images existent.
     * Lorsqu'un nouvel objet {@link CoverageProxy} est créé, il peut appeler la méthode statique
     * {@link #add} pour s'ajouter lui-même à la liste des images dont on vérifiera l'existence.
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    private final static class FileChecker extends Thread {
        /**
         * Thread ayant la charge de vérifier si des fichiers existent.
         */
        private static FileChecker thread;

        /**
         * Liste des fichiers dont on veut vérifier l'existence.
         */
        private final LinkedList<CoverageProxy> list = new LinkedList<CoverageProxy>();

        /**
         * Construit un thread qui vérifiera l'existence des fichiers. Le processus démarrera
         * immédiatement, mais bloquera presque aussitôt sur la méthode {@link #next}  (parce
         * qu'elle est synchronisée sur le même moniteur que {@link #add}, la méthode qui
         * appelle ce constructeur). L'exécution continuera lorsque la méthode {@link #add}
         * aura terminé, ce qui garantit qu'il y aura au moins une image à vérifier.
         */
        private FileChecker() {
            super("FileChecker");
            setPriority(MIN_PRIORITY);
            setDaemon(true);
            start();
        }

        /**
         * Ajoute une entrée à la liste des images à vérifier.
         */
        public static synchronized void add(final CoverageProxy entry) {
            if (thread == null) {
                thread = new FileChecker();
            }
            thread.list.add(entry);
        }

        /**
         * Retourne la prochaine image à vérifier, ou {@code null}
         * s'il n'en reste plus. S'il ne reste plus d'images, alors cette
         * méthode signalera que le thread va mourrir en donnant la valeur
         * {@code null} à {@link #thread].
         */
        private static synchronized CoverageProxy next(final LinkedList<CoverageProxy> list) {
            if (list.isEmpty()) {
                thread = null;
                return null;
            }
            return list.removeFirst();
        }

        /**
         * Vérifie si les fichiers de la liste existent. Si un fichier
         * n'existe pas, le drapeau {@link CoverageProxy#MISSING} sera lévé
         * pour l'objet {@link CoverageProxy} correspondant. Cette vérification
         * n'est pas effectuée pour les objets résidant sur un serveur distant.
         */
        @Override
        public void run() {
            CoverageProxy entry;
            while ((entry=next(list)) != null) {
                CoverageReference check = entry;
                do {
                    check = ((CoverageReference.Proxy) check).getBackingElement();
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
     * {@link JTable}. Par défaut, cette classe affiche le texte des cellules avec
     * leur couleur habituelle (noir). Elle peut toutefois utiliser des couleurs
     * différentes si l'image a été vue (bleu) ou si elle est manquante (rouge).
     *
     * @version $Id$
     * @author Martin Desruisseaux
     */
    @SuppressWarnings("serial")
    public static class CellRenderer extends DefaultTableCellRenderer {
        /**
         * Couleur par défaut de la police.
         */
        private Color foreground;

        /**
         * Couleur par défaut de l'arrière plan.
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
         * Définit la couleur de la police.
         */
        @Override
        public void setForeground(final Color foreground) {
            super.setForeground(this.foreground=foreground);
        }

        /**
         * Définit la couleur de l'arrière-plan.
         */
        @Override
        public void setBackground(final Color background) {
            super.setBackground(this.background=background);
        }

        /**
         * Retourne une composante à utiliser pour dessiner le contenu des
         * cellules de la table.  Cette méthode utilise une composante par
         * défaut, mais en changeant la couleur du texte si l'entrée correspond
         * à une image qui a déjà été lue.
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
