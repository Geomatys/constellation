/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2006, Institut de Recherche pour le D�veloppement
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
 * Connexion vers la table des {@linkplain ParameterValueGroup param�tres des op�rations}.
 *
 * @version $Id$
 * @author Antoine Hnawia
 * @author Martin Desruisseaux
 */
@UsedBy(OperationTable.class)
public class OperationParameterTable extends Table implements Shareable {
    /**
     * La requ�te SQL servant � interroger la table.
     */
    private static final ConfigurationKey SELECT = new ConfigurationKey("OperationParameters:SELECT",
            "SELECT parameter, value\n"        +
            "  FROM \"OperationParameters\"\n" +
            " WHERE operation=?");

    /** Num�ro d'argument. */ private static final int ARGUMENT_OPERATION   = 1;
    /** Num�ro de colonne. */ private static final int PARAMETER            = 1;
    /** Num�ro de colonne. */ private static final int VALUE                = 2;

    /**
     * Construit une table qui interrogera la base de donn�es sp�cifi�e.
     *
     * @param database  Connexion vers la base de donn�es d'observations.
     */
    public OperationParameterTable(final Database database) {
        super(database);
    }

    /**
     * D�finie les valeurs du groupe de param�tres donn� en argument.
     * 
     * @param   operation   L'op�ration dont on veut conna�tre les param�tres.
     * @param   parameters  Le groupe de param�tre dans lequel on va stocker les param�tres.
     * 
     * @throw   NullPointerException    Si l'argument {@code parameters} est {@code null}.
     * @throws  SQLException            Si l'interrogation de la base de donn�es a �chou�.
     * @throws  IllegalRecordException  Si un des param�tres trouv�s dans la base de donn�es
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
             * Cas des bool�ens. Certaines bases de donn�es se chargent d'interpr�ter des
             * caract�res comme 'Y' et 'N'. On laissera la base base de donn�es faire elle-
             * m�me la conversion du champ texte en bool�en, plut�t que de tenter cette
             * conversion en Java. La m�me remarque s'applique pour tous les cas suivants.
             */
            if (Boolean.class.isAssignableFrom(type)) {
                parameter.setValue(results.getBoolean(VALUE));
            }
            /*
             * Cas des entiers entre 8 et 32 bits. Notez que le type Long ne peut pas �tre
             * g�r� par ce code, puisqu'il n'y a pas de m�thode parameter.setValue(long).
             */
            else if (Byte   .class.isAssignableFrom(type) ||
                     Short  .class.isAssignableFrom(type) ||
                     Integer.class.isAssignableFrom(type))
            {
                parameter.setValue(results.getInt(VALUE));
            }
            /*
             * Cas de tous les autres type de nombres, incluant Long, Float et Double. On
             * les lira comme des Double, qui est � peu pr�s le format le plus g�n�rique
             * pour l'API � notre disposition dans ParameterValue.
             */
            else if (Number.class.isAssignableFrom(type)) {
                parameter.setValue(results.getDouble(VALUE));
            }
            /*
             * Cas particulier d'un noyau JAI. Le contenu num�rique du noyau sera construit
             * � partir du nom.
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
     * Retourne un noyau de convolution pour le nom sp�cifi�. Le nom doit �tre compris d'un mot
     * cl� (par exemple {@code "mean"} ou {@code "gauss"}) suivit de la taille du noyau entre
     * parenth�ses. Exemple: {@code "mean(3)"}.
     *
     * @param  parameter Nom du param�tre. Utilis� uniquement en cas d'erreur pour construire un message.
     * @param  results   Le r�sultat de la requ�te SQL. Seul l'enregistrement courant sera pris en compte.
     * @return           Un objet {@link KernelJAI} correspondant au type voulue.
     *
     * @throws  SQLException            Si l'interrogation de la base de donn�es a �chou�.
     * @throws  IllegalRecordException  Si la cha�ne de caract�res {@code value} n'est pas reconnue.
     */
    private static KernelJAI createKernel(final String parameter, final ResultSet results)
            throws SQLException, IllegalRecordException
    {
        final String value = results.getString(VALUE).trim();
        final int lp = value.indexOf('(');
        final int rp = value.indexOf(')');
        if (lp > 0  &&  rp > lp       &&  // V�rifie la disposition des parenth�ses.
            rp == value.length()-1    &&  // V�rifie que ')' se trouve � la fin (implique qu'il y en a qu'une seule).
            lp == value.lastIndexOf('(')) // V�rifie qu'il n'y a qu'une seule parenth�se ouvrante.
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
                "La valeur \"" + value + "\" n'est pas valide pour le param�tre \"" + parameter +
                "\". Le format attendu est \"mean(3)\" par exemple.");
    }

    /**
     * Cr�e un noyau de convolution de la taille sp�cifi�e pour le calcul de moyenne.
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
     * Cr�e un noyau de convolution de la taille sp�cifi�e pour le calcul de moyenne
     * pond�r�e par une gaussienne.
     *
     * @param   size    La taille de la matrice.
     * @return          Le noyau de convolution.
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
     * Retourne la racine carr�e d'une extension de l'op�rateur de Sobel. Pour chaque �l�ment
     * dont la position par rapport � l'�l�ment central est (x,y), on calcule la composante
     * horizontale avec le cosinus de l'angle divis� par la distance. On peut l'�crire comme
     * suit:
     *
     * <blockquote><pre>
     *     cos(atan(y/x)) / sqrt(x�+y�)
     * </pre></blockquote>
     *
     * En utilisant l'identit� 1/cos� = (1+tan�), on peut r��crire l'�quation comme suit:
     *
     * <blockquote><pre>
     *     x / (x�+y�)
     * </pre></blockquote>
     *
     * Cette m�thode prend la racine carr�e de tous les �l�ments.
     *
     * @param size Taille de la matrice. Doit �tre un nombre positif et impair.
     * @param horizontal {@code true} pour l'op�rateur horizontal,
     *        or {@code false} pour l'op�rateur vertical.
     *
     * @todo Ce code est une copie d'une m�thode d�finie dans
     *       {@link org.geotools.gui.swing.GradientKernelEditor},
     *       except� pour l'appel de {@code sqrt}.
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
     * Normalise le tableau sp�cifi� en argument de fa�on � ce que la somme
     * de tous les �l�ments soit �gale � 1.
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
