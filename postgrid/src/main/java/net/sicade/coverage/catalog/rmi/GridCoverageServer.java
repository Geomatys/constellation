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
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package net.sicade.coverage.catalog.rmi;

import java.util.Set;
import java.util.List;
import java.util.Date;
import java.util.SortedSet;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.opengis.coverage.Coverage;
import org.opengis.geometry.Envelope;
import org.opengis.geometry.DirectPosition;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.util.NumberRange;

import net.sicade.util.DateRange;
import net.sicade.catalog.CatalogException;
import net.sicade.coverage.catalog.Operation;
import net.sicade.coverage.catalog.CoverageReference;
import net.sicade.coverage.catalog.sql.GridCoverageTable;
import net.sicade.coverage.catalog.sql.GridCoverageEntry;


/**
 * Une table d'images qui procèdera aux lectures sur un serveur distant plutôt que de télécharger
 * les images par FTP. Les avantages de ce procédé sont:
 * <p>
 * <ul>
 *   <li>Utilisation moindre de la bande passante si seule une petite zone est extraite,
 *       ou si une décimation a été demandée.</li>
 *   <li>Meilleures performances à la lecture, puisque le serveur peut accéder aux images à partir d'un
 *       {@link java.io.File} (qui permet des accès aléatoires) alors que les clients ont besoin de
 *       passer par un {@link java.net.URL} (qui ne permet pas des accès aléatoires dans un fichier).</li>
 *   <li>Evite d'exiger que le client dispose de décodeurs d'images spécialisés tels que HDF, puisque
 *       le décodage est pris en charge par le serveur.</li>
 *   <li>Plus faible consommation de la mémoire du côté des clients.</li>
 *   <li>Opérations exécutées sur le serveur, et résultats conservés dans une cache au cas où la
 *       même image serait demandée plusieurs fois (potentiellement par des clients différents).</li>
 * </ul>
 * <p>
 * L'inconvénient est une charge de travail accrue sur le serveur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageServer extends UnicastRemoteObject implements DataConnection {
    /**
     * Pour compatibilité entre différentes versions.
     */
    private static final long serialVersionUID = -1288646310349616893L;

    /**
     * La table d'images locale.
     */
    protected final DataConnection table;
    
    /**
     * Construit une table d'image à exécuter sur un serveur distant.
     * <p>
     * <strong>Important:</strong> Pour éviter des comportement inatendues, la connexion {@code table}
     * donnée en argument ne doit pas être partagée avec un autre objet. En d'autres termes, aucune
     * référence vers {@code table} ne devrait être conservée en déhors de cette instance de
     * {@code GridCoverageServer}.
     *
     * @param  table La table d'images locale (habituellement une instance de {@link GridCoverageTable}).
     * @throws RemoteException si cette classe n'est pas pu s'exporter comme serveur RMI.
     */
    public GridCoverageServer(final DataConnection table) throws RemoteException {
        this.table = table;
    }

    /**
     * {@inheritDoc}
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() throws RemoteException {
        return table.getCoordinateReferenceSystem();
    }

    /**
     * {@inheritDoc}
     */
    public Envelope getEnvelope() throws CatalogException, RemoteException {
        return table.getEnvelope();
    }

    /**
     * {@inheritDoc}
     */
    public GeographicBoundingBox getGeographicBoundingBox() throws CatalogException, RemoteException {
        return table.getGeographicBoundingBox();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Date> getAvailableTimes() throws CatalogException, SQLException, RemoteException {
        return table.getAvailableTimes();
    }

    /**
     * {@inheritDoc}
     */
    public SortedSet<Number> getAvailableElevations() throws CatalogException, SQLException, RemoteException {
        return table.getAvailableElevations();
    }

    /**
     * {@inheritDoc}
     */
    public DateRange getTimeRange() throws CatalogException, RemoteException {
        return table.getTimeRange();
    }

    /**
     * {@inheritDoc}
     */
    public boolean setTimeRange(final Date startTime, final Date endTime) throws CatalogException, RemoteException {
        return table.setTimeRange(startTime, endTime);
    }

    /**
     * {@inheritDoc}
     */
    public NumberRange getVerticalRange() throws CatalogException, RemoteException {
        return table.getVerticalRange();
    }

    /**
     * {@inheritDoc}
     */
    public boolean setVerticalRange(double minimum, double maximum)
            throws CatalogException, RemoteException
    {
        return table.setVerticalRange(minimum, maximum);
    }

    /**
     * {@inheritDoc}
     */
    public double evaluate(final double x, final double y, final double t, final short band)
            throws CatalogException, SQLException, IOException
    {
        return table.evaluate(x, y, t, band);
    }

    /**
     * {@inheritDoc}
     */
    public double[] snap(final double x, final double y, final double t)
            throws CatalogException, SQLException, IOException
    {
        return table.snap(x, y, t);
    }

    /**
     * {@inheritDoc}
     */
    public List<Coverage> coveragesAt(final DirectPosition position)
            throws CatalogException, SQLException, IOException
    {
        return table.coveragesAt(position);
    }

    /**
     * {@inheritDoc}
     */
    public Set<CoverageReference> getEntries() throws CatalogException, SQLException, RemoteException {
        Set<CoverageReference> entries = table.getEntries();
        for (final CoverageReference entry : entries) {
            if (entry instanceof GridCoverageEntry) {
                ((GridCoverageEntry) entry).export();
            }
        }
        return entries;
    }

    /**
     * {@inheritDoc}
     */
    public CoverageReference getEntry() throws CatalogException, SQLException, RemoteException {
        CoverageReference entry = table.getEntry();
        if (entry instanceof GridCoverageEntry) {
            ((GridCoverageEntry) entry).export();
        }
        return entry;
    }

    /**
     * {@inheritDoc}
     */
    public DataConnection newInstance(final Operation operation) throws RemoteException {
        return new GridCoverageServer(table.newInstance(operation));
    }
}
