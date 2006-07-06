/*
 * Sicade - Syst�mes int�gr�s de connaissances pour l'aide � la d�cision en environnement
 * (C) 2005, Institut de Recherche pour le D�veloppement
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

// J2SE
import java.util.List;
import java.util.ArrayList;

// OpenGIS
import org.opengis.coverage.Coverage;
import org.opengis.parameter.ParameterValue;
import org.opengis.parameter.ParameterValueGroup;

// Geotools
import org.geotools.resources.Utilities;
import org.geotools.coverage.processing.AbstractProcessor;

// Sicade
import net.sicade.observation.coverage.Operation;
import net.sicade.observation.sql.ProcedureEntry;


/**
 * Impl�mentation d'une entr�e repr�sentant une {@linkplain Operation op�ration}. La m�thode
 * {@link #doOperation} encha�ne les op�rateurs d'images suivants, dans cet ordre:
 * <p>
 * <ul>
 *   <li>{@link org.geotools.coverage.processing.operation.NodataFilter}</li>
 *   <li>L'op�ration sp�cifi�e au moment de la construction de cette entr�e</li>
 *   <li>{@link org.geotools.coverage.processing.operation.Interpolate}</li>
 * </ul>
 *
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class OperationEntry extends ProcedureEntry implements Operation {
    /**
     * Pour compatibilit�s entre les enregistrements binaires de diff�rentes versions.
     */
    private static final long serialVersionUID = -8790032968708208057L;

    /**
     * R�f�rence vers un processeur d'op�rations.
     */
    private static final AbstractProcessor PROCESSOR = AbstractProcessor.getInstance();

    /**
     * Un nom sp�cial qui �vite l'application des op�rations implicites ({@code "NodataFilter"}
     * et {@code "Interpolate"}). Ce nom est inconnu du {@linkplain #PROCESSOR processeur}.
     * C'est plut�t � l'int�rieur de cette classe que nous le prennons en compte.
     */
    private static final String DIRECT_VALUE = "DirectValue";

    /**
     * Les interpolations � appliquer sur les images retourn�es. D'abord, une interpolation
     * bicubique. Si elle �choue, une interpolation bilin�aire. Si cette derni�re �choue aussi,
     * alors le plus proche voisin.
     */
    private static final String[] INTERPOLATIONS = {
        "Bicubic",
        "Bilinear",
        "NearestNeighbor"
    };

    /**
     * L'op�ration par d�faut. Elle n'est pas tout-�-fait une op�ration identit�e puisqu'elle
     * interpole les valeurs des pixels. Mais du point de vue des analyses statistiques, on
     * peut la consid�rer comme une op�ration identit�e.
     */
    public static final Operation DEFAULT = new OperationEntry();

    /**
     * Le pr�fix � utiliser dans les noms composites. Les noms composites seront de la forme
     * "<var>operation</var> - <var>param�tre</var> - <var>temps</var>" o� <var>operation</var>
     * est cet objet {@code Operation}. Exemple: <code>"&nabla;SST<sub>-15</sub>"</code>.
     */
    private final String prefix;

    /**
     * Les param�tres des op�rations � appliquer. Ces param�tres correspondront typiquement dans
     * l'ordre aux op�rations {@code "Interpolate"}, <cite>op�ration utilisateur</cite>,
     * {@code "NodataFilter"}.
     */
    private final ParameterValueGroup[] parameters;

    /**
     * Index dans le tableau {@code parameters} des param�tres qui s'appliquent � l'op�ration
     * d�finie par l'utilisateur, ou -1 si aucun.
     */
    private final int userParameters;

    /**
     * Construit une op�ration par d�faut. Ce constructeur est diff�rent de ce que l'on peut
     * obtenir avec les constructeurs publiques ou prot�g�es du fait qu'elle n'applique pas
     * d'op�ration "NodataFilter". Elle n'applique que "Interpolate".
     */
    private OperationEntry() {
        super("Default", null);
        prefix = "";
        parameters = new ParameterValueGroup[] {
            PROCESSOR.getOperation("Interpolate").getParameters()
        };
        parameters[0].parameter("Type").setValue(INTERPOLATIONS);
        userParameters = -1;
    }

    /**
     * Construit une op�ration. L'argument {@code prefix} est utilis� dans les noms composites de la
     * forme "<var>operation</var> - <var>param�tre</var> - <var>temps</var>" o� <var>operation</var>
     * est cet objet {@code Operation}. Exemple: <code>"&nabla;SST<sub>-15</sub>"</code>.
     *
     * @param name      Le nom de cette op�ration.
     * @param prefix    Le pr�fix � utiliser dans les noms composites.
     * @param operation Un nom d'op�ration compris par le {@linkplain AbstractProcessor processeur}.
     *                  Exemple: {@code "GradientMagnitude"}.
     * @param remarks   Des commentaires, ou {@code null} s'il n'y en a pas.
     */
    protected OperationEntry(final String name,
                             final String prefix,
                                   String operation,
                             final String remarks)
    {
        super(name, remarks);
        this.prefix = prefix;
        if (operation != null) {
            operation = operation.trim();
        } else {
            operation = "";
        }
        if (operation.equalsIgnoreCase(DIRECT_VALUE)) {
            parameters = new ParameterValueGroup[0];
            userParameters = -1;
            return;
        }
        int userParameters = -1;
        final List<ParameterValueGroup> parameters = new ArrayList<ParameterValueGroup>(3);
        for (int i=0; i<3; i++) {
            final ParameterValueGroup param;
            switch (i) {
                case 0: {
                    /*
                     * Applies the "NodataFilter" operation before any user-specified operation.
                     * Skip this step if the user-specified operation is alreay the "NodataFilter"
                     * operation (no need to apply the same operation twice).
                     */
                    if (operation.equalsIgnoreCase("NodataFilter")) {
                        continue;
                    }
                    param = PROCESSOR.getOperation("NodataFilter").getParameters();
                    break;
                }
                case 1: {
                    /*
                     * Applies the user-specified operation, if any.
                     */
                    if (operation.length() == 0) {
                        continue;
                    }
                    param = PROCESSOR.getOperation(operation).getParameters();
                    userParameters = parameters.size();
                    break;
                }
                case 2: {
                    /*
                     * Applies the interpolation (except if it was the user-specified operation;
                     * we don't want to overwrite the user operation). Since this operation do not
                     * transform the image, we apply it always (at the difference of "NodataFilter").
                     */
                    if (operation.equalsIgnoreCase("Interpolate")) {
                        continue;
                    }
                    param = PROCESSOR.getOperation("Interpolate").getParameters();
                    param.parameter("Type").setValue(INTERPOLATIONS);
                    break;
                }
                default: {
                    throw new AssertionError(i);
                }
            }
            parameters.add(param);
        }
        this.parameters = parameters.toArray(new ParameterValueGroup[parameters.size()]);
        this.userParameters = userParameters;
    }

    /**
     * {@inheritDoc}
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * {@inheritDoc}
     */
    public synchronized Coverage doOperation(Coverage coverage) {
        for (final ParameterValueGroup param : parameters) {
            final ParameterValue p = param.parameter("Source");
            try {
                p.setValue(coverage);
                coverage = PROCESSOR.doOperation(param);
            } finally {
                p.setValue(null);
            }
        }
        return coverage;
    }

    /**
     * Retourne les param�tres de l'op�ration, ou {@code null} si aucun. Ces param�tres peuvent
     * �tre modifi�es imm�diatement apr�s la construction par {@link OperationTable}, mais ne
     * devraient plus changer par la suite.
     */
    protected ParameterValueGroup getParameters() {
        return (userParameters >= 0) ? parameters[userParameters] : null;
    }
    
    /**
     * Compare cette entr�e avec l'objet sp�cifi�.
     */
    @Override
    public boolean equals(final Object object) {
        if (object == this) {
            return true;
        }
        if (super.equals(object)) {
            final OperationEntry that = (OperationEntry) object;
            return Utilities.equals(this.prefix, that.prefix);
            // Note: On ne compare pas les param�tres (on se fiera sur le caract�re unique du nom),
            //       vu que la synchronisation sur les deux objets en m�me temps pose probl�me. Ce
            //       n'est pas du tout insurmontable, mais ne vaut probablement pas la peine.
        }
        return false;
    }
}
