/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
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
package org.constellation.numeric.table;

// J2SE dependencies
import java.util.Arrays;
import java.util.Random;
import java.io.IOException;
import java.nio.DoubleBuffer;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Teste les sous-classes de {@link Table}.
 *
 * @author Martin Desruisseaux
 */
public class TableTest extends TestCase {
    /**
     * Exécute la suite de tests à partir de la ligne de commande.
     */
    public static void main(final String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    /**
     * Retourne la suite de tests.
     */
    public static Test suite() {
        return new TestSuite(TableTest.class);
    }

    /**
     * Construit une nouvelle suite de tests.
     */
    public TableTest() {
    }

    /**
     * Teste en utilisant l'interpolation de type "plus proche voisin".
     */
    public void testNearest() throws ExtrapolationException, IOException {
        /*
         * Construit un vecteur de valeurs aléatoires. Note: la valeur 'seed' utilisé ci-dessous
         * a été choisie empiriquement de manière à éviter de produire des valeurs trop proches
         * des limites du tableau. On évite ainsi de compliquer les vérifications des méthodes
         * testées ici. La vérification des méthodes aux limites devrait être effectuée par un
         * code explicite.
         */
        final Random random = new Random(4895268);
        final double[] x = new double[1000];
        final double[] y = new double[x.length];
        for (int i=0; i<x.length; i++) {
            x[i] = i + (random.nextDouble() - 0.5);
            y[i] = i + random.nextGaussian();
        }
        final double EPS = 1E-8; // Petite valeur pour les comparaisons qui se veulent exactes.
        final Table table = TableFactory.getDefault().create(x, y, Interpolation.NEAREST);
        assertEquals(x.length, table.getNumRow());
        assertEquals(2,        table.getNumCol());
        assertFalse (          table.isIdentity());
        /*
         * Teste 'locate'.
         */
        final int[] index = new int[3];
        table.locate(75, index);
        assertEquals(74, index[0]);
        assertEquals(75, index[1]);
        assertEquals(76, index[2]);
        /*
         * Teste 'interpolate'
         */
        assertEquals(y[300], table.interpolate(300), EPS);
    }
}
