/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2008 Geomatys
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.seagis.catalog.CatalogException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.ncml.Aggregation;
import ucar.nc2.ncml.NcMLReader;

/**
 * Lis un fichier NcML, et récupère les informations stockées dans les balises <netcdf>.
 * Si une aggregation de fichiers NetCDF est présente, il est possible de récupérer celle-ci
 * ainsi que l'ensemble des informations la constituant.
 * 
 * @source $URL$
 * @author Cédric Briançon
 */
final class NcMLReading {
    /**
     * Le logger.
     */
    private static final Logger LOGGER = Logger.getLogger("fr.ifremer.coverage.catalog");

    /**
     * Le {@code namespace} pour les balises <netcdf>.
     */
    private static final Namespace NETCDFNS = Namespace.getNamespace(
            "http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2");

    /**
     * Le fichier NcML à parcourir.
     */
    private final File ncml;
    
    /**
     * Prépare la récupération des informations contenues dans un fichier NcML.
     * 
     * @param ncml Le fichier NcML à traiter.
     */
    public NcMLReading(final File ncml) {
        this.ncml = ncml;
    }
    
    /**
     * Returns a list of {@linkplain ucar.nc2.ncml.Aggregation Aggregation}, which 
     * should be a list of tags containing one or several <netcdf> tags. 
     * If these tags were not found, or an error during the parsing of the NcML file has 
     * occured, it will returns {@code null}. 
     */
    protected List<Aggregation> getNestedAggregations() {
        try {
            final List<Aggregation> aggregations = new ArrayList<Aggregation>();
            final String  ncmlPath     = ncml.getAbsolutePath();
            final Element globalNetcdf = getGlobalNetcdfElement(ncml);
            final Element globalAggr   = globalNetcdf.getChild("aggregation", NETCDFNS);
            if (globalAggr.getChild("netcdf", NETCDFNS).getAttribute("location") == null) {
                for (Iterator<Element> it = 
                        globalAggr.getChildren("netcdf", NETCDFNS).iterator(); it.hasNext();) 
                {
                    final NetcdfDataset netcdfData = NcMLReader.readNcML(
                            ncmlPath, it.next(), null);
                    
                    aggregations.add(netcdfData.getAggregation());
                }
            } else {
                final NetcdfDataset netcdfData = NcMLReader.readNcML(
                        ncmlPath, globalNetcdf, null);
                aggregations.add(netcdfData.getAggregation());
            }
            return aggregations;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to read correctly the NcML file", ex);
            return null;
        } catch (CatalogException ex) {
            LOGGER.log(Level.WARNING, 
                    "The <netcdf> tags was not found in your NcML file.", ex);
            return null;
        }
    }
    
    /**
     * Retourne une liste d'éléments JDOM correspondant à l'ensemble des fils <netcdf> de 
     * l'aggregation principale.
     */
    protected List<Element> getNestedNetcdfElement() {
        try { 
            final List<Element> elements = new ArrayList<Element>();
            final Element globalNetcdf = getGlobalNetcdfElement(ncml);
            final Element globalAggr   = globalNetcdf.getChild("aggregation", NETCDFNS);
            if (globalAggr.getChild("netcdf", NETCDFNS).getAttribute("location") == null) {
                for (Iterator<Element> it = 
                        globalAggr.getChildren("netcdf", NETCDFNS).iterator(); it.hasNext();) 
                {
                    Element mainNetcdf = (Element) it.next();
                    for (Iterator<Element> it2 = 
                            mainNetcdf.getChild("aggregation", NETCDFNS).getChildren().iterator(); 
                            it2.hasNext();) 
                    {
                        elements.add(it2.next());
                    }
                }
            }
            return elements;
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Unable to read correctly the NcML file", ex);
            return null;
        } catch (CatalogException ex) {
            LOGGER.log(Level.WARNING, 
                    "The <netcdf> tags was not found in your NcML file.", ex);
            return null;
        }
    }
    
    /**
     * Renvoit la balise XML {@code <netcdf>} ainsi que son contenu pour le fichier NcML 
     * se trouvant à l'adresse passée en paramètre. 
     * Cette balise doit contenir l'ensemble des informations du fichier NcML dédiées à
     * spécifier des méta données concernant les fichiers netCDF. Un fichier NcML peut
     * contenir plusieurs balises netcdf, cette méthode renverra la première qui englobe
     * toutes les autres.
     *
     * @param  ncmlPath Le chemin vers le fichier NcML.
     * @return Le noeud XML {@code <netcdf>}.
     * @throws CatalogException si l'obtention de la balise a échoué.
     * @throws IOException si la création du document a échoué.
     */
    private Element getGlobalNetcdfElement(final File ncml) 
            throws CatalogException, IOException 
    {
        final Document doc;
        try {
            SAXBuilder builder = new SAXBuilder();
            doc = builder.build(ncml);
        } catch (JDOMException e) {
            throw new CatalogException(e);
        } 
        final Element root = doc.getRootElement();
        if (root.getName().equals("netcdf")) {
            return root;
        } else {
            Element elemDataset = root.getChild("dataset", root.getNamespace());
            return elemDataset.getChild("netcdf", NETCDFNS);
        }
    }
    
}
