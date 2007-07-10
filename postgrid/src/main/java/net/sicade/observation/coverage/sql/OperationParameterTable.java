/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2006, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

// J2SE and JAI dependencies
import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import javax.media.jai.KernelJAI;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

// OpenGIS dependencies
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.InvalidParameterValueException;

// Geotools dependencies
import org.geotools.resources.XMath;
import org.geotools.coverage.processing.operation.GradientMagnitude;

// Sicade dependencies
import net.sicade.observation.sql.Use;
import net.sicade.observation.sql.UsedBy;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.ConfigurationKey;
import net.sicade.observation.IllegalRecordException;


/**
 * Connexion vers la table des {@linkplain ParameterValueGroup paramètres des opérations}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@UsedBy(OperationTable.class)
public class OperationParameterTable extends Table implements Shareable {
    /**
     * La requête SQL servant à interroger la table.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("OperationParameters:SELECT",
            "SELECT parameter, value\n"        +
            "  FROM \"OperationParameters\"\n" +
            " WHERE operation=?");

    /** Numéro d'argument. */ private static final int ARGUMENT_OPERATION = 1;
    /** Numéro de colonne. */ private static final int PARAMETER          = 1;
    /** Numéro de colonne. */ private static final int VALUE              = 2;

    /**
     * Construit une table qui interrogera la base de données spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public OperationParameterTable(final Database database) {
        super(database);
    }

    /**
     * Définie les valeurs du groupe de paramètres donné en argument.
     * 
     * @param   operation   L'opération dont on veut connaître les paramètres.
     * @param   parameters  Le groupe de paramètre dans lequel on va stocker les paramètres.
     * 
     * @throw   NullPointerException    Si l'argument {@code parameters} est {@code null}.
     * @throws  SQLException            Si l'interrogation de la base de données a échoué.
     * @throws  IllegalRecordException  Si un des paramètres trouvés dans la base de données
     *          n'est pas connu par le groupe {@code parameters}, ou a une valeur invalide.
     */
    protected synchronized void fillValues(final String operation, final ParameterValueGroup parameters)
            throws SQLException, IllegalRecordException
    {
        final PreparedStatement statement = getStatement(SELECT);
        statement.setString(ARGUMENT_OPERATION, operation);
        final ResultSet results = statement.executeQuery();
        while (results.next()) try {
            final String         name      = results.getString(PARAMETER).trim();
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
                parameter.setValue(results.getBoolean(VALUE));
            }
            /*
             * Cas des entiers entre 8 et 32 bits. Notez que le type Long ne peut pas être
             * géré par ce code, puisqu'il n'y a pas de méthode parameter.setValue(long).
             */
            else if (Byte   .class.isAssignableFrom(type) ||
                     Short  .class.isAssignableFrom(type) ||
                     Integer.class.isAssignableFrom(type))
            {
                parameter.setValue(results.getInt(VALUE));
            }
            /*
             * Cas de tous les autres type de nombres, incluant Long, Float et Double. On
             * les lira comme des Double, qui est à peu près le format le plus générique
             * pour l'API à notre disposition dans ParameterValue.
             */
            else if (Number.class.isAssignableFrom(type)) {
                parameter.setValue(results.getDouble(VALUE));
            }
            /*
             * Cas particulier d'un noyau JAI. Le contenu numérique du noyau sera construit
             * à partir du nom.
             */
            else if (KernelJAI.class.isAssignableFrom(type)) {
                parameter.setValue(createKernel(name, results));
            }
            /*
             * Tous les autres cas.
             */
            else {
                parameter.setValue(results.getString(VALUE));
            }
        } catch (ParameterNotFoundException exception) {
            throw new IllegalRecordException(results.getMetaData().getTableName(PARAMETER), exception);
        } catch (InvalidParameterValueException exception) {
            throw new IllegalRecordException(results.getMetaData().getTableName(VALUE), exception);
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
    private static KernelJAI createKernel(final String parameter, final ResultSet results)
            throws SQLException, IllegalRecordException
    {
        final String value = results.getString(VALUE).trim();
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
                throw new IllegalRecordException(results.getMetaData().getTableName(VALUE), exception);
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
        throw new IllegalRecordException(results.getMetaData().getTableName(VALUE),
                "La valeur \"" + value + "\" n'est pas valide pour le paramètre \"" + parameter +
                "\". Le format attendu est \"mean(3)\" par exemple.");
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
