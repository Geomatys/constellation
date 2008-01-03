/*
 * (C) 2007, IFREMER
 * (C) 2007, Geomatys
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
package net.seagis.coverage.catalog;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.NcMLReader;

import org.geotools.resources.Arguments;
import org.geotools.image.io.netcdf.NetcdfImageReader;
// TODO import fr.ifremer.image.io.netcdf.IfremerReader;

import net.seagis.catalog.Database;
import net.seagis.catalog.UpdatePolicy;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;
import static net.seagis.catalog.UpdatePolicy.*;


/**
 * Ajoute à la base de données les enregistrements correspondants aux fichiers spécifiés.
 *
 * @version $Id: Collector.java 90 2008-01-02 13:23:18Z cb1ebc7 $
 * @author Martin Desruisseaux
 */
public class Collector {
    /**
     * Connexion à la base de données.
     */
    private final Database database;

    /**
     * La table dans laquelle effectuer les insertions.
     */
    private final WritableGridCoverageTable table;

    /**
     * Indique s'il faut écraser les anciennes images avec les nouvelles, ou ignorer
     * les nouvelles qui existent déjà.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.SKIP_EXISTING;

    /**
     * La sortie pour les messages de déboguages, ou {@code null} si elle n'a pas
     * encore été définie.
     */
    private PrintWriter out;

    /**
     * Construit un moissonneur qui ajoutera des entrées dans la base de données par défaut.
     */
    public Collector() throws CatalogException {
        this(null);
    }

    /**
     * Construit un moissonneur qui ajoutera des entrées dans la base de données spécifiée.
     *
     * @param database La base de données à utiliser.
     * @throws CatalogException si la connexion a échouée.
     */
    public Collector(final Database database) throws CatalogException {
        if (database != null) {
            this.database = database;
        } else {
            try {
                this.database = new Database();
            } catch (IOException e) {
                throw new CatalogException(e);
            }
        }
        table = new WritableGridCoverageTable(this.database.getTable(WritableGridCoverageTable.class));
    }

    /**
     * Définit le périphérique de sortie vers lequel écrire d'éventuels messages.
     * Si cette méthode n'est jamais appelée, alors la valeur par défaut est la
     * {@linkplain System#out sortie standard}.
     *
     * @param out Périphérique de sorties vers lequel écrire des messages, s'il y en a.
     *            Il s'agira typiquement des instructions {@code INSERT} mémorisées si
     *            la méthode {@link #pretend} a été appelée.
     */
    public void setPrinter(final PrintWriter out) {
        if (out != null) {
            out.flush();
        }
        this.out = out;
    }

    /**
     * Retourne le périphérique de sortie vers lequel écrire d'éventuels messages.
     */
    public PrintWriter getPrinter() {
        if (out == null) {
            out = new PrintWriter(System.out, true);
        }
        return out;
    }

    /**
     * Retourne l'objet Database utilisé, afin de permettre l'écriture dans la table Series
     * par l'interface d'upload.
     */
    public Database getDatabase() {
        return database;
    }
    
    /**
     * Fait en sorte que les commandes SQL soit écrites dans un buffer plutôt que exécutées.
     * Cette méthode sert lors des tests, quand on ne veut pas que la base de données soit
     * modifiée.
     */
    public void setPretend(final boolean pretend) {
        database.setUpdateSimulator(pretend ? getPrinter() : null);
    }

    /**
     * Indique s'il faut écraser les anciennes images avec les nouvelles, ou ignorer
     * les nouvelles qui existent déjà.
     */
    public void setPolicy(final UpdatePolicy policy) {
        updatePolicy = policy;
    }

    /**
     * Procède à l'insertion des nouveaux fichiers pour la couche spécifiée.
     *
     * @param  layer La couche pour laquelle ajouter des données.
     * @throws CatalogException si l'insertion de données a échouée.
     */
    public void process(final String layer) throws CatalogException {
        final int count;
        try {
            table.setLayer(layer);
            count = table.updateLayer(true, updatePolicy);
            database.flush();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        final PrintWriter out = getPrinter();
        if (out != null) {
            // TODO: localize
            out.print(count);
            out.print(" images ajoutées");
            if (!SKIP_EXISTING.equals(updatePolicy)) {
                out.print(" ou remplacées");
            }
            out.print(" pour la couche \"");
            out.print(layer);
            out.println("\".");
        }
    }

    /**
     * Insère dans la base de données les informations provenant d'un fichier NcML.
     *
     * @param  layer La couche de données choisie.
     * @param  ncmlPath Le chemin du fichier NcML.
     * @throws CatalogException si l'insertion de données a échouée.
     */
    public void processNcML(final String layer, final String ncmlPath, 
                            final boolean allowsNewLayer) throws CatalogException {
        try {
            table.setCanInsertNewLayers(allowsNewLayer);
            table.setLayer(layer);
            final NetcdfImageReader reader;
            reader = null;// TODO (NetcdfImageReader) new IfremerReader.Spi().createReaderInstance();
            final NetcdfDataset netcdfData = NcMLReader.readNcML(ncmlPath, getNetcdfElement(ncmlPath), null);
            final List<Aggregation.Dataset> data = netcdfData.getAggregation().getNestedDatasets();
            for (final Aggregation.Dataset aggrData: data) {
                String netcdfPath = aggrData.getLocation();
                final Object input;
                if (netcdfPath.indexOf("://") >= 0) {
                    input = new URI(netcdfPath);
                } else {
                    input = new File(netcdfPath);
                }
                reader.setInput(input);
                table.addEntry(reader);
            }
            database.flush();
        } catch (URISyntaxException ex) {
            throw new ServerException(ex);
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
    }

    /**
     * Renvoit la balise XML {@code <netcdf>} ainsi que son contenu pour le fichier NcML se trouvant
     * à l'adresse passée en paramètre.
     *
     * @param  ncmlPath Le chemin vers le fichier NcML.
     * @return Le noeud XML {@code <netcdf>}.
     * @throws CatalogException si l'obtention de la balise a échouée.
     */
    private Element getNetcdfElement(final String ncmlPath) throws CatalogException {
        final Namespace netcdf = Namespace.getNamespace(
                "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
        final Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(ncmlPath);
        } catch (JDOMException e) {
            throw new CatalogException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        }
        final Element root = doc.getRootElement();
        if (root.getName().equals("netcdf")) {
            return root;
        } else {
            Element elemDataset = root.getChild("dataset", root.getNamespace());
            return elemDataset.getChild("netcdf", netcdf);
        }
    }

    /**
     * Dispose des ressources allouées.
     *
     * @throws CatalogException si la disposition des ressources a échouée.
     */
    public void close() throws CatalogException {
        try {
            database.close();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        if (out != null) {
            out.flush();
        }
    }

    /**
     * Lance l'ajout de fichiers à partir de la ligne de commande.
     */
    public static void main(String[] args) throws CatalogException {
        final Arguments arguments = new Arguments(args);
        final boolean pretend = arguments.getFlag("-pretend");
        final boolean replace = arguments.getFlag("-replace");
        final boolean clear   = arguments.getFlag("-clear");
        final String  layer   = arguments.getRequiredString("-layer");
        args = arguments.getRemainingArguments(0);
        final Collector collector = new Collector();
        collector.setPrinter(arguments.out);
        collector.setPretend(pretend);
        collector.setPolicy(clear ? CLEAR_BEFORE_UPDATE : replace ? REPLACE_EXISTING : SKIP_EXISTING);
        collector.process(layer);
        collector.close();
    }
}
