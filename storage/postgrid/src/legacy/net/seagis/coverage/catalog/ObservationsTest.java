/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2005, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package net.seagis.coverage.catalog;

import junit.framework.TestCase;
import net.seagis.catalog.CatalogException;
import net.seagis.widget.CoverageTableModel;


/**
 * Teste l'utilisation de la classe {@link Catalog}.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 * @todo Ajouter des tests sur le même modèle que ceux que l'on peut trouver dans le projet SICADE.
 */
public class ObservationsTest extends TestCase {
    /**
     * Construit un test.
     */
    public ObservationsTest(final String name) {
        super(name);
    }

    /**
     * Teste la création d'un modèle de table à partir d'une couche.
     */
    public void testCoverageTableModel() throws CatalogException {
        final Layer layer = Catalog.getDefault().getLayer("CHL (Monde - mensuelles)");
        final CoverageTableModel model = new CoverageTableModel();
        model.setLayer(layer);
    }
}
