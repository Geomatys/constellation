/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 * Copyright 2014 Geomatys.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.constellation.numeric.table;

// J2SE dependencies
import java.util.Arrays;
import java.util.Random;
import java.nio.DoubleBuffer;

// JUnit dependencies
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Teste les sous-classes de {@link OrderedVector}.
 *
 * @author Martin Desruisseaux
 */
public class OrderedVectorTest extends TestCase {
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
        return new TestSuite(OrderedVectorTest.class);
    }

    /**
     * Construit une nouvelle suite de tests.
     */
    public OrderedVectorTest() {
    }

    /**
     * Teste la version bufferisée.
     */
    public void testBuffered() {
        /*
         * Construit un vecteur de valeurs aléatoires. Note: la valeur 'seed' utilisé ci-dessous
         * a été choisie empiriquement de manière à éviter de produire des valeurs trop proches
         * des limites du tableau. On évite ainsi de compliquer les vérifications des méthodes
         * testées ici. La vérification des méthodes aux limites devrait être effectuée par un
         * code explicite.
         */
        final Random random = new Random(563874);
        final double[] data = new double[1000];
        for (int i=0; i<data.length; i++) {
            data[i] = i + (random.nextDouble() - 0.5);
        }
        final OrderedVector vector = new BufferedOrderedVector(DoubleBuffer.wrap(data));
        final double EPS = 1E-8; // Petite valeur pour les comparaisons qui se veulent exactes.
        /*
         * Vérification des propriétées les plus simples.
         */
        assertEquals(data.length,                   vector.length());
        assertEquals(data[500],                     vector.get(500), EPS);
        assertEquals(DataOrder.STRICTLY_ASCENDING,  vector.getDataOrder());
        /*
         * Vérifie le fonctionnement des méthode 'locate', 'locateAroundIndex', 'copyIndexInto'
         * en l'absence de valeurs manquantes.
         */
        final int[] index0 = new int[0];
        final int[] index1 = new int[1];
        final int[] index2 = new int[2];
        final int[] index3 = new int[3];
        final int[] index4 = new int[4];
        for (int i=0; i<100; i++) {
            int index, b;

            // locateAroundIndex
            index = random.nextInt(data.length);
            assertTrue(vector.locateAroundIndex(index));
            assertEquals(index - 1, vector.lower);
            assertEquals(index + 1, vector.upper);
            assertEquals(index, vector.value, 1.0);

            // locate
            index = random.nextInt(data.length);
            assertTrue(vector.locate(index));
            assertTrue(vector.lower <= vector.upper);
            assertTrue(vector.upper  - vector.lower <= 1);
            assertTrue(vector.lower == index || vector.upper == index);
            assertTrue(data[vector.lower] <= index);
            assertTrue(data[vector.upper] >= index);
            assertEquals(index, Math.round(vector.value));

            // copyIndexInto
            assertTrue  (vector.copyIndexInto(index0));
            assertTrue  (vector.copyIndexInto(index1));
            assertEquals(vector.lower, index1[0]);
            assertTrue  (vector.copyIndexInto(index2));
            assertEquals(vector.lower, index2[0]);
            assertEquals(vector.upper, index2[1]);
            assertTrue  (vector.copyIndexInto(index3));
            assertTrue  ((b=Arrays.binarySearch(index3, index)) >= 0);
            assertTrue  (b==0 || b==1);
            assertTrue  (vector.copyIndexInto(index4));
            assertTrue  ((b=Arrays.binarySearch(index4, index)) >= 0);
            assertTrue  (b==1 || b==2);
            assertOneIncrement(index0);
            assertOneIncrement(index1);
            assertOneIncrement(index2);
            assertOneIncrement(index3);
            assertOneIncrement(index4);
        }
        /*
         * Vérifie le fonctionnement des méthode 'locate', 'locateAroundIndex', 'copyIndexInto'
         * en présence de valeurs manquantes.
         */
        for (int i=0; i<10; i++) {
            int index, b;

            // locateAroundIndex
            index = random.nextInt(data.length);
            if (index <= 1) continue;
            data[index - 2] = Double.NaN;
            data[index - 1] = Double.NaN;
            data[index + 1] = Double.NaN;
            assertTrue(vector.locateAroundIndex(index));
            assertEquals(index - 3, vector.lower);
            assertEquals(index + 2, vector.upper);
            assertEquals(index, vector.value, 1.0);

            // locate (same index)
            assertTrue(vector.locate(index));
            assertTrue(vector.lower <= vector.upper);
            assertTrue(vector.upper  - vector.lower >= 2);
            assertTrue(vector.lower == index || vector.upper == index);
            assertTrue(data[vector.lower] <= index);
            assertTrue(data[vector.upper] >= index);
            assertEquals(index, vector.value, EPS);

            // copyIndexInto
            assertTrue  (vector.copyIndexInto(index0));
            assertTrue  (vector.copyIndexInto(index1));
            assertEquals(vector.lower, index1[0]);
            assertTrue  (vector.copyIndexInto(index2));
            assertEquals(vector.lower, index2[0]);
            assertEquals(vector.upper, index2[1]);
            assertTrue  (vector.copyIndexInto(index3));
            assertTrue  ((b=Arrays.binarySearch(index3, index)) >= 0);
            assertTrue  (b==0 || b==1);
            assertTrue  (vector.copyIndexInto(index4));
            assertTrue  ((b=Arrays.binarySearch(index4, index)) >= 0);
            assertTrue  (b==1 || b==2);
        }
    }

    /**
     * Vérifie que l'intervalle entre chaque valeurs du tableau spécifié est 1.
     */
    private static void assertOneIncrement(final int[] index) {
        for (int i=1; i<index.length; i++) {
            assertEquals(1, index[i] - index[i-1]);
        }
    }
}
