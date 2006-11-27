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
package net.sicade.observation.coverage.rmi;

import java.util.Set;
import java.util.List;
import java.util.Date;
import java.io.IOException;
import java.sql.SQLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.opengis.coverage.Coverage;
import org.opengis.spatialschema.geometry.Envelope;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import net.sicade.util.DateRange;
import net.sicade.observation.CatalogException;
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.coverage.CoverageReference;
import net.sicade.observation.coverage.sql.GridCoverageTable;
import net.sicade.observation.coverage.sql.GridCoverageEntry;


/**
 * Une table d'images qui proc�dera aux lectures sur un serveur distant plut�t que de t�l�charger
 * les images par FTP. Les avantages de ce proc�d� sont:
 * <p>
 * <ul>
 *   <li>Utilisation moindre de la bande passante si seule une petite zone est extraite,
 *       ou si une d�cimation a �t� demand�e.</li>
 *   <li>Meilleures performances � la lecture, puisque le serveur peut acc�der aux images � partir d'un
 *       {@link java.io.File} (qui permet des acc�s al�atoires) alors que les clients ont besoin de
 *       passer par un {@link java.net.URL} (qui ne permet pas des acc�s al�atoires dans un fichier).</li>
 *   <li>Evite d'exiger que le client dispose de d�codeurs d'images sp�cialis�s tels que HDF, puisque
 *       le d�codage est pris en charge par le serveur.</li>
 *   <li>Plus faible consommation de la m�moire du c�t� des clients.</li>
 *   <li>Op�rations ex�cut�es sur le serveur, et r�sultats conserv�s dans une cache au cas o� la
 *       m�me image serait demand�e plusieurs fois (potentiellement par des clients diff�rents).</li>
 * </ul>
 * <p>
 * L'inconv�nient est une charge de travail accrue sur le serveur.
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class GridCoverageServer extends UnicastRemoteObject implements DataConnection {
    /**
     * Pour compatibilit� entre diff�rentes versions.
     */
    private static final long serialVersionUID = -1288646310349616893L;

    /**
     * La table d'images locale.
     */
    protected final DataConnection table;
    
    /**
     * Construit une table d'image � ex�cuter sur un serveur distant.
     * <p>
     * <strong>Important:</strong> Pour �viter des comportement inatendues, la connexion {@code table}
     * donn�e en argument ne doit pas �tre partag�e avec un autre objet. En d'autres termes, aucune
     * r�f�rence vers {@code table} ne devrait �tre conserv�e en d�hors de cette instance de
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
    public List<Coverage> coveragesAt(final double t)
            throws CatalogException, SQLException, IOException
    {
        return table.coveragesAt(t);
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
