/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.seagis.coverage.model;

import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import javax.media.jai.KernelJAI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterValueException;
import org.geotools.resources.XMath;

import net.seagis.catalog.Table;
import net.seagis.catalog.Database;
import net.seagis.catalog.QueryType;
import net.seagis.catalog.CatalogException;
import net.seagis.catalog.IllegalRecordException;


/**
 * Connection to a table of {@linkplain ParameterValueGroup operation parameters}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
public class OperationParameterTable extends Table {
    /**
     * Creates a operation parameter table.
     * 
     * @param database Connection to the database.
     */
    public OperationParameterTable(final Database database) {
        super(new OperationParameterQuery(database));
    }

    /**
     * Fills the specified parameter group with the values found in the database for the given
     * operation name.
     * 
     * @param   operation   The operation name in the database.
     * @param   parameters  The parameter group where to store the parameter values.
     * 
     * @throw   NullPointerException    If {@code parameters} is {@code null}.
     * @throws  SQLException            If an error occured while reading the database.
     * @throws  IllegalRecordException  If the database contains an invalid parameter.
     */
    protected synchronized void fillValues(final String operation, final ParameterValueGroup parameters)
            throws SQLException, CatalogException
    {
        final PreparedStatement statement = getStatement(QueryType.SELECT);
        final OperationParameterQuery query = (OperationParameterQuery) super.query;
        final int paramIndex = indexOf(query.parameter);
        final int valueIndex = indexOf(query.value    );
        statement.setString(indexOf(query.byOperation), operation);
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            final String name = results.getString(paramIndex).trim();
            try {
                final ParameterValue parameter = parameters.parameter(name);
                Class type = ((ParameterDescriptor) parameter.getDescriptor()).getValueClass();
                type = XMath.primitiveToWrapper(type);
                /*
                 * Cas des booléens. Certaines bases de données se chargent d'interpréter des
                 * caractères comme 'Y' et 'N'. On laissera la base base de données faire elle-
                 * même la conversion du champ texte en booléen, plutôt que de tenter cette
                 * conversion en Java. La même remarque s'applique pour tous les cas suivants.
                 */
                if (Boolean.class.isAssignableFrom(type)) {
                    parameter.setValue(results.getBoolean(valueIndex));
                }
                /*
                 * Cas des entiers entre 8 et 32 bits. Notez que le type Long ne peut pas être
                 * géré par ce code, puisqu'il n'y a pas de méthode parameter.setValue(long).
                 */
                else if (Byte   .class.isAssignableFrom(type) ||
                         Short  .class.isAssignableFrom(type) ||
                         Integer.class.isAssignableFrom(type))
                {
                    parameter.setValue(results.getInt(valueIndex));
                }
                /*
                 * Cas de tous les autres type de nombres, incluant Long, Float et Double. On
                 * les lira comme des Double, qui est à peu près le format le plus générique
                 * pour l'API à notre disposition dans ParameterValue.
                 */
                else if (Number.class.isAssignableFrom(type)) {
                    parameter.setValue(results.getDouble(valueIndex));
                }
                /*
                 * Cas particulier d'un noyau JAI. Le contenu numérique du noyau sera construit
                 * à partir du nom.
                 */
                else if (KernelJAI.class.isAssignableFrom(type)) {
                    parameter.setValue(createKernel(name, results, valueIndex));
                }
                /*
                 * Tous les autres cas.
                 */
                else {
                    parameter.setValue(results.getString(valueIndex));
                }
            } catch (ParameterNotFoundException exception) {
                throw new IllegalRecordException(exception, this, results, paramIndex, name);
            } catch (InvalidParameterValueException exception) {
                throw new IllegalRecordException(exception, this, results, valueIndex, name);
            }
        }
    }

    /**
     * Retourne un noyau de convolution pour le nom spécifié. Le nom doit être compris d'un mot
     * clé (par exemple {@code "mean"} ou {@code "gauss"}) suivit de la taille du noyau entre
     * parenthèses. Exemple: {@code "mean(3)"}.
     *
     * @param  parameter Nom du paramètre. Utilisé uniquement en cas d'erreur pour construire un message.
     * @param  results   Le résultat de la requête SQL. Seul l'enregistrement courant sera pris en compte.
     * @return           Un objet {@link KernelJAI} correspondant au type voulue.
     *
     * @throws  SQLException            Si l'interrogation de la base de données a échoué.
     * @throws  IllegalRecordException  Si la chaîne de caractères {@code value} n'est pas reconnue.
     */
    private KernelJAI createKernel(final String parameter, final ResultSet results, final int valueIndex)
            throws SQLException, IllegalRecordException
    {
        final String value = results.getString(valueIndex).trim();
        final int lp = value.indexOf('(');
        final int rp = value.indexOf(')');
        if (lp > 0  &&  rp > lp       &&  // Vérifie la disposition des parenthèses.
            rp == value.length()-1    &&  // Vérifie que ')' se trouve à la fin (implique qu'il y en a qu'une seule).
            lp == value.lastIndexOf('(')) // Vérifie qu'il n'y a qu'une seule parenthèse ouvrante.
        {
            final int size;
            try {
                size = Integer.parseInt(value.substring(lp+1, rp));
            } catch (NumberFormatException exception) {
                throw new IllegalRecordException(exception, this, results, valueIndex, parameter);
            }
            final String name = value.substring(0, lp).trim();
            if (name.equalsIgnoreCase("mean")) {
                return createMeanKernel(size);
            }
            if (name.equalsIgnoreCase("gauss")) {
                return createGaussKernel(size);
            }
            if (name.equalsIgnoreCase("isotropic.x")) {
                return createIsotropicKernel(size, true);
            }
            if (name.equalsIgnoreCase("isotropic.y")) {
                return createIsotropicKernel(size, false);
            }
        }
        throw new IllegalRecordException("La valeur \"" + value + "\" n'est pas reconnue.",
                    this, results, valueIndex, parameter);
    }

    /**
     * Crée un noyau de convolution de la taille spécifiée pour le calcul de moyenne.
     * 
     * @param   size    La taille de la matrice.
     * @return          Le noyau de convolution.
     */
    private static KernelJAI createMeanKernel(int size) {
        final float[] data = new float[size*size];
        Arrays.fill(data, 1f / (size*size));
        return new KernelJAI(size, size, data);
    }

    /**
     * Crée un noyau de convolution de la taille spécifiée pour le calcul de moyenne
     * pondérée par une gaussienne.
     *
     * @param  size La taille de la matrice.
     * @return      Le noyau de convolution.
     */
    private static KernelJAI createGaussKernel(final int size) {
        final float[] data = new float[size*size];
        final int max = (size+1) / 2;
        final int min = max - size;
        int c = 0;
        for (int j=min; j<max; j++) {
            final int j2 = j*j;
            for (int i=min; i<max; i++) {
                final int i2 = i*i;
                data[c++] = (float) exp(-(sqrt(i2 + j2)));
            }
        }
        normalize(data);
        return new KernelJAI(size, size, data);
    }

    /**
     * Retourne la racine carrée d'une extension de l'opérateur de Sobel. Pour chaque élément
     * dont la position par rapport à l'élément central est (x,y), on calcule la composante
     * horizontale avec le cosinus de l'angle divisé par la distance. On peut l'écrire comme
     * suit:
     *
     * <blockquote><pre>
     *     cos(atan(y/x)) / sqrt(x²+y²)
     * </pre></blockquote>
     *
     * En utilisant l'identité 1/cos² = (1+tan²), on peut réécrire l'équation comme suit:
     *
     * <blockquote><pre>
     *     x / (x²+y²)
     * </pre></blockquote>
     *
     * Cette méthode prend la racine carrée de tous les éléments.
     *
     * @param size Taille de la matrice. Doit être un nombre positif et impair.
     * @param horizontal {@code true} pour l'opérateur horizontal,
     *        or {@code false} pour l'opérateur vertical.
     *
     * @todo Ce code est une copie d'une méthode définie dans
     *       {@link org.geotools.gui.swing.GradientKernelEditor},
     *       excepté pour l'appel de {@code sqrt}.
     */
    private static KernelJAI createIsotropicKernel(final int size, final boolean horizontal) {
        final int key = size/2;
        final float[] data = new float[size*size];
        for (int y=key; y>=0; y--) {
            int row1 = (key-y)*size + key;
            int row2 = (key+y)*size + key;
            final int y2 = y*y;
            for (int x=key; x!=0; x--) {
                final int x2 = x*x;
                final float v = (float) sqrt(2.0*x / (x2+y2));
                if (horizontal) {
                    data[row1-x] = data[row2-x] = -v;
                    data[row1+x] = data[row2+x] = +v;
                } else {
                    // Swap x and y.
                    row1 = (key-x)*size + key;
                    row2 = (key+x)*size + key;
                    data[row1-y] = data[row1+y] = -v;
                    data[row2-y] = data[row2+y] = +v;
                }
            }
        }
        return new KernelJAI(size, size, key, key, data);
    }

    /**
     * Normalise le tableau spécifié en argument de façon à ce que la somme
     * de tous les éléments soit égale à 1.
     */
    private static void normalize(final float[] data) {
        double sum = 0;
        for (int i=0; i<data.length; i++) {
            sum += data[i];
        }
        for (int i=0; i<data.length; i++) {
            data[i] /= sum;
        }
    }
}
