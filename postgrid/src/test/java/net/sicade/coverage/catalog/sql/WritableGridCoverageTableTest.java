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
package net.sicade.coverage.catalog.sql;

import java.io.File;
import java.util.Set;

import org.opengis.metadata.extent.GeographicBoundingBox;
import org.geotools.coverage.CoverageStack;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;

import net.sicade.coverage.catalog.Layer;
import net.sicade.catalog.DatabaseTest;


/**
 * Teste le fonctionnement de {@link CoverageStack#evaluate} avec des {@link Layer}.
 * Ce test est un peu plus direct que {@link DescriptorTest} du fait qu'il construit
 * lui même le {@link CoverageStack} dans plusieurs cas.
 *
 * @author Cédric Briançon
 * @version $Id$
 */
@Deprecated
public class WritableGridCoverageTableTest extends DatabaseTest {
    /**
     * {@code true} pour désactiver tous les tests (sauf typiquement un test en particulier que l'on
     * souhaite suivre pas à pas). La valeur de ce champ devrait être toujours {@code false} sauf en
     * cas de déboguage d'une méthode bien spécifique.
     */
    private static final boolean DISABLED = true;

    /**
     * Etablit la connexion avec la base de données. Cette connexion ne sera établie que la
     * première fois où un test sera exécuté. Pour la fermeture des connections, on se fiera
     * au rammase-miettes et aux "shutdown hooks" mis en place par {@code Database}.
     */
//    @Override
//    protected void setUp() throws SQLException, IOException {
//        super.setUp();
//        if (layers == null) {
//            layers = database.getTable(LayerTable.class);
//        }
//    }

    /**
     * Teste l'obtention de la liste des couches, incluant un filtrage par région géographique.
     */
    public void testWritableGCT() throws Exception {
        if (DISABLED) return;
        final LayerTable table = database.getTable(LayerTable.class);
        final Set<Layer> all = table.getEntries();
        final File file = new File("C:\\images\\Contrôles\\Afrique.png");
        final String fileNameWithExt = file.getName();
        final String fileName = fileNameWithExt.substring(0, fileNameWithExt.indexOf("."));
        assertFalse(all.isEmpty());
        final GeographicBoundingBox bbox = new GeographicBoundingBoxImpl(-180.0, 180.0, -90.0, 90.0);
        table.setGeographicBoundingBox(bbox);
        assertEquals(bbox, table.getGeographicBoundingBox());
//        table.trimEnvelope(); // Devrait n'avoir aucun effet lorsque la sélection contient des image mondiales.
//        assertEquals(bbox, table.getGeographicBoundingBox());
//        final Layer selected = table.getEntry("Images de tests");
//        System.out.println(selected.getSeries());
//        WritableGridCoverageTable writableGCT = new WritableGridCoverageTable(database);
//        writableGCT.setLayer(selected);
//        writableGCT.addEntry(fileName, dateFormat.parse("17/06/2007"), 
//                dateFormat.parse("18/06/2007"), bbox, new Dimension(1024, 768));
    }
}
