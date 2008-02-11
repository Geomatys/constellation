/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2007 Geomatys
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
package net.seagis.console;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;
import org.geotools.resources.Arguments;
import org.geotools.image.io.netcdf.NetcdfImageReader;

import net.seagis.catalog.Database;
import net.seagis.catalog.UpdatePolicy;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.CatalogException;
import net.seagis.coverage.catalog.WritableGridCoverageTable;
import static net.seagis.catalog.UpdatePolicy.*;

import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.Aggregation.Type;
import ucar.nc2.ncml.AggregationExisting;


/**
 * Adds new records to the specified database.
 *
 * @version $Id: Collector.java 90 2008-01-02 13:23:18Z cb1ebc7 $
 * @author Martin Desruisseaux
 */
public class Collector {
    /**
     * Database connection.
     */
    private final Database database;

    /**
     * The table where to write new records.
     */
    protected final WritableGridCoverageTable table;

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    private UpdatePolicy updatePolicy = UpdatePolicy.SKIP_EXISTING;

    /**
     * An output writer for debugging messages, or {@code null} for the standard output stream.
     */
    private PrintWriter out;

    /**
     * Creates a new collector which will adds entries in the default database.
     *
     * @throws CatalogException if the connection failed.
     */
    public Collector() throws CatalogException {
        this(null);
    }

    /**
     * Creates a new collector which will adds entries in the specified database.
     *
     * @param database The database connection.
     * @throws CatalogException if the connection failed.
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
     * Returns the database connection.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Returns the output printer for debugging messages.
     * This method never returns {@code null}.
     */
    public PrintWriter getPrinter() {
        if (out == null) {
            out = new PrintWriter(System.out, true);
        }
        return out;
    }

    /**
     * Sets the output writer for debugging messages. If this method is never invoked,
     * then the default is the {@linkplain System#out standard output stream}.
     * <p>
     * Debugging messages are typically the {@code INSERT} SQL instructions that would
     * be emitted. Those instructions are printed only if {@link #setPretend} has been
     * invoked with value {@code true}.
     */
    public void setPrinter(final PrintWriter out) {
        if (out != null) {
            out.flush();
        }
        this.out = out;
    }

    /**
     * If {@code true}, prints {@code INSERT} statements to the {@linkplain #getPrinter output printer}
     * rather than executing them. This is useful for testing purpose.
     */
    public void setPretend(final boolean pretend) {
        database.setUpdateSimulator(pretend ? getPrinter() : null);
    }

    /**
     * Whatever new records should replace the old ones, or if old records should be keeped as-is.
     */
    public void setPolicy(final UpdatePolicy policy) {
        updatePolicy = policy;
    }

    /**
     * Proceed to the insertion of new records for the specified layer.
     *
     * @param  layer The layer to update.
     * @throws CatalogException If insertion failed.
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
     * @param  layer Le layer.
     * @param  variable La variable choisie à moisonner.
     * @param  ncml Le fichier NcML.
     * @param  allowsNewLayer Autorise le parser à créer une nouvelle couche de données
     *                        si elle n'est pas déjà présente dans la base.
     * @throws CatalogException si l'insertion de données a échouée.
     */
    public void processNcML(final String layer, final String variable, final File ncml, 
                            final boolean allowsNewLayer) throws CatalogException 
    {
        table.setCanInsertNewLayers(allowsNewLayer);
        final Set<URI> locations = new HashSet<URI>();
        try {
            NcMLReading ncmlParsing = new NcMLReading(ncml);
            final List<Aggregation> aggregations = ncmlParsing.getNestedAggregations();
            // Si on a une jointure en début de fichier, et aucune autre aggregation en dessous,
            // alors on sait que les balises <netcdf> contiendront directement le paramètre location
            // et on le récupère directement afin de le spécifier au reader.
            // Sinon on doit parcourir l'ensemble des aggregations et leur fils afin de récupérer
            // les paramètres location des sous balises <netcdf>.
            if (aggregations.get(0).getType().equals(Type.JOIN_EXISTING) &&
                    aggregations.size() == 1)
            {
                final AggregationExisting aggrExist = (AggregationExisting) aggregations.get(0);
                final List<Aggregation.Dataset> datasets = aggrExist.getNestedDatasets();
                for (final Aggregation.Dataset dataset : datasets) {
                    for (Iterator<String> var = aggrExist.getVariables().iterator(); var.hasNext();) {
                        if (variable.toLowerCase().startsWith(var.next().toLowerCase())) {
                            locations.add(new URI(dataset.getLocation()));
                        }
                    }
                }
            } else {
                // Parcours l'ensemble des fils <netcdf location="..."> de cette aggregation.
                for (Iterator<Element> it = 
                        ncmlParsing.getNestedNetcdfElement().iterator(); it.hasNext();) 
                {
                    final Element netcdfWithLocationParam = (Element) it.next();
                    final Namespace ncmlNamespace = Namespace.getNamespace(
                            "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");
                    // Vérifie que la variable à moissonner, spécifiée par l'utilisateur, est bien 
                    // présente parmis les variables trouvées dans le fichier NcML pour la balise 
                    // <netcdf> courante. Si c'est le cas le fichier NetCDF est ajouté à la liste des 
                    // fichiers à traiter, sinon il est ignoré.
                    for (Iterator<Element> i = 
                            netcdfWithLocationParam.getChildren("variable", ncmlNamespace).iterator(); 
                            i.hasNext();) 
                    {
                        Element varNcml = (Element)i.next();
                        if (variable.toLowerCase().
                                startsWith(varNcml.getAttributeValue("name").toLowerCase())) 
                        {
                            locations.add(new URI(netcdfWithLocationParam.getAttributeValue("location")));
                        }
                    }
                }
            }
            // Effectue l'ajout dans la base des fichiers NetCDF sélectionnés.
            for (final URI location : locations) {
                addToLayer(layer, location.toString());
            }
            getDatabase().flush();
        } catch (SQLException e) {
            throw new ServerException(e);
        } catch (IOException e) {
            throw new CatalogException(e);
        } catch (URISyntaxException e) {
            throw new CatalogException(e);
        } 
    }
    
    /**
     * Essaye d'ajouter la donnée lue depuis le fichier NcML pour la couche souhaitée. 
     * Si une erreur SQL survient, elle peut provenir d'une tentative d'ajout de données
     * déjà présentes dans la base. A ce moment là, on attrape cette exception et on
     * laisse continuer le processus.
     * Ceci est un contournement temporaire qui devra être remplacé par un vrai test avant
     * ajout de l'enregistrement dans la base.
     * 
     * @param layer La couche de données.
     * @param path  Le chemin du fichier NetCDF.
     * @throws CatalogException
     * @throws SQLException Si une erreur SQL, autre qu'une tentative d'ajout dans la base 
     *                      provoquant un doublon, survient.
     * @throws IOException
     */
    private void addToLayer(final String layer, final String path) 
            throws CatalogException, SQLException, IOException 
    {
        table.setLayer(layer);
        NetcdfImageReader reader = addInputToReader(path);
        //System.out.println(path);
        try {
            table.addEntry(reader);
        } catch (SQLException sql) {
            // Si le code d'erreur est 23505, on sait que l'on a obtenu une erreur de postgresql
            // indiquant un ajout dans une table d'un enregistrement déjà présent.
            // Dans ce cas, on ne fait rien car l'enregistrement est déjà dans la base.
            // Sinon on relance l'exception sql, qui pourrait avoir une autre raison.
            if (!sql.getSQLState().equals("23505")) {
                throw sql;
            }
        }
    }

    /**
     * Créé un {@code ImageReader} pour le fichier NetCDF spécifié, et le retourne.
     * 
     * @param netcdf Le chemin du fichier NetCDF lu depuis le NcML. Il peut contenir un
     *               protocole. Dans ce cas il sera considéré comme une URI.
     * @return Le {@code ImageReader} pour le fichier NetCDF spécifié, ou null si une
     *         erreur de génération de l'URI a été renvoyée.
     * @throws IOException Si la création du reader a échoué. 
     */
    private NetcdfImageReader addInputToReader(final String netcdf) throws IOException {
        final NetcdfImageReader reader = createNetcdfImageReader();
        final Object input;
        if (netcdf.indexOf("://") >= 0) {
            try {
                input = new URI(netcdf);
            } catch (URISyntaxException ex) {
                throw new IOException(ex.getLocalizedMessage());
            }
        } else {
            input = new File(netcdf);
        }
        reader.setInput(input);
        return reader;
    }
    
    /**
     * Creates an instance of NetcdfImageReader, using the default Spi for Netcdf files.
     * 
     * @return An instance of NetcdfImageReader.
     * @throws IOException
     */
    protected NetcdfImageReader createNetcdfImageReader() throws IOException {
        return (NetcdfImageReader) new NetcdfImageReader.Spi().createReaderInstance();
    }
        
    /**
     * Dispose collector resources.
     *
     * @throws CatalogException If an error occured while disposing the resources.
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
     * Runs from the command line.
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
