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
import net.sicade.catalog.Database;
import net.sicade.catalog.ConfigurationKey;
import net.sicade.coverage.catalog.GridCoverage;
import net.sicade.coverage.catalog.sql.LayerTable;
import net.sicade.coverage.model.DescriptorTable;
import net.sicade.coverage.catalog.sql.GridCoverageTable;
import net.sicade.catalog.CatalogException;


/**
 * Serveur RMI. La méthode {@link #main main} de cette classe est à lancer sur le serveur qui
 * exécutera les opérations sur les images et n'enverra que les résultats aux clients. Les
 * arguments acceptés sont:
 * <p>
 * <ul>
 *   <li>{@code -start} Démarre le serveur.</li>
 *   <li>{@code -stop} Arrête le serveur.</li>
 *   <li>{@code -registry} lance l'utilitaire {@code rmiregistry}, de sorte qu'il s'exécutera dans
 *       la même machine virtuelle que le reste de l'application.</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class Server extends UnicastRemoteObject implements DataConnectionFactory {
    /**
     * Capacité de la cache des tuiles.
     */
    private static final long TILE_CACHE_SIZE = 0x8000000L; // 128 Mo.

    /**
     * Connexion vers la base de données.
     */
    private final Database database;

    /**
     * Connexion vers la table des couches.
     */
    private final LayerTable layer;

    /**
     * Construit un nouveau serveur de connections vers les données.
     *
     * @param  datasource La source de données.
     * @throws SQLException si la connexion à la base de données a échoué.
     * @throws IOException si le fichier de configuration n'a pas pu être lu.
     * @throws RemoteException si ce serveur n'a pas pu s'exporter.
     */
    protected Server(final DataSource datasource) throws SQLException, IOException {
        database = new Local(datasource);
        layer = database.getTable(LayerTable.class);
    }

    /**
     * Une base de données qui évitera de déléguer son travail à des serveurs distants si le nom
     * du serveur est celui de cette machine.
     */
    private final class Local extends Database {
        /**
         * {@code true} si {@link #REMOTE_SERVER} pointe vers une adresse locale, ou
         * {@code null} si ça n'a pas encore été déterminé.
         */
        private Boolean isLoopback;

        /**
         * Construit une base de données pour la source spécifiée.
         */
        public Local(final DataSource datasource) throws SQLException, IOException {
            super(datasource);
        }

        /**
         * Retourne une des propriétée de la base de données. Dans le cas particulier où la
         * propriété demandée est le nom d'un serveur distant et que l'adresse de se serveur
         * nous ramène à la machine locale, retourne {@code null} afin d'éviter les appels
         * recursifs.
         */
        @Override
        public String getProperty(final ConfigurationKey key) {
            String value = super.getProperty(key);
            if (value!=null && ConfigurationKey.REMOTE_SERVER.equals(key)) {
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
         * {@link Server} active. Cette méthode a besoin d'être redéfinit pour le fonctionnement
         * de {@link LayerTable#postCreateEntry}.
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
    public DataConnection connectSeries(final String layer) throws CatalogException, SQLException, RemoteException {
        final GridCoverageTable data = database.getTable(GridCoverageTable.class);
        data.setLayer(this.layer.getEntry(layer));
        return new GridCoverageServer(data);
    }

    /**
     * {@inheritDoc}
     */
    public GridCoverage getDescriptorCoverage(final String descriptor) throws CatalogException, SQLException {
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
            arguments.out.println("Serveur RMI prêt.");
        } catch (AlreadyBoundException exception) {
            arguments.out.println("Le serveur tourne déjà.");
        }
        if (stop) try {
            Naming.unbind(REGISTRY_NAME);
            arguments.out.println("Le serveur RMI sera arrêté d'ici quelques minutes.");
        } catch (NotBoundException exception) {
            arguments.out.println("Le serveur était déjà arrêté.");
        }
    }
}
