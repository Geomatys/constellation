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

// J2SE dependencies
import java.io.IOException;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.TileCache;

// OpenGIS dependencies
import org.geotools.resources.Arguments;

// Sicade dependencies
import net.sicade.observation.sql.Database;
import net.sicade.observation.coverage.DynamicCoverage;
import net.sicade.observation.coverage.sql.SeriesTable;
import net.sicade.observation.coverage.sql.DescriptorTable;
import net.sicade.observation.coverage.sql.GridCoverageTable;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.CatalogException;


/**
 * Serveur RMI. La m�thode {@link #main main} de cette classe est � lancer sur le serveur qui
 * ex�cutera les op�rations sur les images et n'enverra que les r�sultats aux clients. Les
 * arguments accept�s sont:
 * <p>
 * <ul>
 *   <li>{@code -start} D�marre le serveur.</li>
 *   <li>{@code -stop} Arr�te le serveur.</li>
 *   <li>{@code -registry} lance l'utilitaire {@code rmiregistry}, de sorte qu'il s'ex�cutera dans
 *       la m�me machine virtuelle que le reste de l'application.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Server extends UnicastRemoteObject implements DataConnectionFactory {
    /**
     * Capacit� de la cache des tuiles.
     */
    private static final long TILE_CACHE_SIZE = 0x8000000L; // 128 Mo.

    /**
     * Connexion vers la base de donn�es.
     */
    private final Database database;

    /**
     * Connexion vers la table des s�ries.
     */
    private final SeriesTable series;

    /**
     * Construit un nouveau serveur de connections vers les donn�es.
     *
     * @param  datasource La source de donn�es.
     * @throws SQLException si la connexion � la base de donn�es a �chou�.
     * @throws IOException si le fichier de configuration n'a pas pu �tre lu.
     * @throws RemoteException si ce serveur n'a pas pu s'exporter.
     */
    protected Server(final DataSource datasource) throws SQLException, IOException {
        database = new Local(datasource);
        series = database.getTable(SeriesTable.class);
    }

    /**
     * Une base de donn�es qui �vitera de d�l�guer son travail � des serveurs distants si le nom
     * du serveur est celui de cette machine.
     */
    private final class Local extends Database {
        /**
         * {@code true} si {@link #REMOTE_SERVER} pointe vers une adresse locale, ou
         * {@code null} si �a n'a pas encore �t� d�termin�.
         */
        private Boolean isLoopback;

        /**
         * Construit une base de donn�es pour la source sp�cifi�e.
         */
        public Local(final DataSource datasource) throws SQLException, IOException {
            super(datasource);
        }

        /**
         * Retourne une des propri�t�e de la base de donn�es. Dans le cas particulier o� la
         * propri�t� demand�e est le nom d'un serveur distant et que l'adresse de se serveur
         * nous ram�ne � la machine locale, retourne {@code null} afin d'�viter les appels
         * recursifs.
         */
        @Override
        public String getProperty(final ConfigurationKey key) {
            String value = super.getProperty(key);
            if (value!=null && REMOTE_SERVER.equals(key)) {
                if (isLoopback == null) try {
                    final InetAddress address = InetAddress.getByName(value);
                    isLoopback = Boolean.valueOf(address.isLoopbackAddress() ||
                                                 address.equals(InetAddress.getLocalHost()));
                } catch (UnknownHostException exception) {
                    isLoopback = Boolean.FALSE;
                }
                if (isLoopback.booleanValue()) {
                    value = null;
                }
            }
            return value;
        }

        /**
         * Toutes les demandes d'un objet {@link DataConnectionFactory} retourneront l'instance de
         * {@link Server} active. Cette m�thode a besoin d'�tre red�finit pour le fonctionnement
         * de {@link SeriesTable#postCreateEntry}.
         */
        @Override
        public Remote getRemote(final String name) throws RemoteException {
            if (REGISTRY_NAME.equals(name)) {
                return Server.this;
            } else {
                return super.getRemote(name);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public DataConnection connectSeries(final String series) throws CatalogException, SQLException, RemoteException {
        final GridCoverageTable data = database.getTable(GridCoverageTable.class);
        data.setSeries(this.series.getEntry(series));
        return new GridCoverageServer(data);
    }

    /**
     * {@inheritDoc}
     */
    public DynamicCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
        return database.getTable(DescriptorTable.class).getEntryLenient(descriptor).getCoverage();
    }

    /**
     * Lance le serveur RMI. Voyez la description de cette classe pour la liste des arguments.
     */
    public static void main(String[] args) throws IOException, SQLException {
        final Arguments arguments = new Arguments(args);
        final boolean start    = arguments.getFlag("-start");
        final boolean stop     = arguments.getFlag("-stop");
        final boolean registry = arguments.getFlag("-registry");
        args = arguments.getRemainingArguments(0);
        if (registry) {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        }
        if (start) try {
            final TileCache cache = JAI.getDefaultInstance().getTileCache();
            if (cache.getMemoryCapacity() < TILE_CACHE_SIZE) {
                cache.setMemoryCapacity(TILE_CACHE_SIZE);
            }
            Naming.bind(REGISTRY_NAME, new Server(null));
            arguments.out.println("Serveur RMI pr�t.");
        } catch (AlreadyBoundException exception) {
            arguments.out.println("Le serveur tourne d�j�.");
        }
        if (stop) try {
            Naming.unbind(REGISTRY_NAME);
            arguments.out.println("Le serveur RMI sera arr�t� d'ici quelques minutes.");
        } catch (NotBoundException exception) {
            arguments.out.println("Le serveur �tait d�j� arr�t�.");
        }
    }
}
