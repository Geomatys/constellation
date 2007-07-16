/*
 * Sicade - Systèmes intégrés de connaissances pour l'aide à la décision en environnement
 * (C) 2005, Institut de Recherche pour le Développement
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
package net.sicade.observation.coverage.sql;

import java.net.URL;
import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.text.ParseException;

import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.geotools.util.NumberRange;
import org.geotools.coverage.Category;
import org.geotools.referencing.ReferencingFactoryFinder;

import net.sicade.observation.CatalogException;
import net.sicade.observation.ServerException;
import net.sicade.observation.IllegalRecordException;
import net.sicade.observation.sql.Shareable;
import net.sicade.observation.sql.Database;
import net.sicade.observation.sql.Table;
import net.sicade.observation.sql.UsedBy;
import net.sicade.image.Utilities;
import net.sicade.observation.sql.Column;
import net.sicade.observation.sql.Parameter;
import net.sicade.observation.sql.QueryType;
import static net.sicade.observation.sql.QueryType.*;


/**
 * Connexion vers une table des {@linkplain Category catégories}. Cette table construit des objets
 * {@link Category} pour une bande individuelle. Les categories sont une des composantes d'un objet
 * {@link org.geotools.coverage.grid.GridCoverage2D}, mais ne correspondent pas directement à un
 * {@linkplain net.sicade.observation.Element élément} du paquet des observations.
 * <p>
 * Cette table est utilisée par {@link SampleDimensionTable}, qui construit des objets de
 * plus haut niveau.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
@UsedBy(SampleDimensionTable.class)
public class CategoryTable extends Table implements Shareable {
    /**
     * Column name declared in the {@linkplain #query query}.
     */
    private final Column name, band, lower, upper, c0, c1, function, colors;

    /**
     * Parameter declared in the {@linkplain #query query}.
     */
    private final Parameter byBand;

    /**
     * Transformation <code>f(x) = 10<sup>x</sup></code>. Utilisée pour le décodage des images de
     * concentrations en chlorophylle-a. Ne sera construite que la première fois où elle sera
     * nécessaire.
     */
    private transient MathTransform1D exponential;

    /**
     * Construit une table en utilisant la connexion spécifiée.
     *
     * @param database  Connexion vers la base de données d'observations.
     */
    public CategoryTable(final Database database) {
        super(database);
        final QueryType[] usage = {LIST, FILTERED_LIST};
        name     = new Column   (query, "Categories", "name",     usage);
        band     = new Column   (query, "Categories", "band",     LIST );
        lower    = new Column   (query, "Categories", "lower",    usage);
        upper    = new Column   (query, "Categories", "upper",    usage);
        c0       = new Column   (query, "Categories", "c0",       usage);
        c1       = new Column   (query, "Categories", "c1",       usage);
        function = new Column   (query, "Categories", "function", usage);
        colors   = new Column   (query, "Categories", "colors",   usage);
        byBand   = new Parameter(query, band, FILTERED_LIST);
        lower.setOrdering("ASC");
    }

    /**
     * Retourne la liste des catégories qui appartiennent à la bande spécifiée.
     *
     * @param  band Identificateur de la bande pour lequel on veut les catégories.
     * @return Les catégories de la bande demandée.
     * @throws IllegalRecordException si une incohérence a été trouvée dans les enregistrements.
     * @throws SQLException si l'interrogation de la table a échoué pour une autre raison.
     */
    public synchronized Category[] getCategories(final String band) throws CatalogException, SQLException {
        final PreparedStatement statement = getStatement(FILTERED_LIST);
        statement.setString(indexOf(byBand), band);
        final int nameIndex     = indexOf(name    );
        final int lowerIndex    = indexOf(lower   );
        final int upperIndex    = indexOf(upper   );
        final int c0Index       = indexOf(c0      );
        final int c1Index       = indexOf(c1      );
        final int functionIndex = indexOf(function);
        final int colorsIndex   = indexOf(colors  );
        final List<Category> categories = new ArrayList<Category>();
        final ResultSet result = statement.executeQuery();
        while (result.next()) {
            boolean isQuantifiable = true;
            final String     name = result.getString(nameIndex);
            final int       lower = result.getInt   (lowerIndex);
            final int       upper = result.getInt   (upperIndex);
            final double       c0 = result.getDouble(c0Index); isQuantifiable &= !result.wasNull();
            final double       c1 = result.getDouble(c1Index); isQuantifiable &= !result.wasNull();
            final String function = result.getString(functionIndex);
            final String  colorID = result.getString(colorsIndex);
            /*
             * Procède maintenant au décodage du champ "colors". Ce champ contient
             * une chaîne de caractère qui indique soit le code RGB d'une couleur
             * uniforme, ou soit l'adresse URL d'une palette de couleurs.
             */
            Color[] colors = null;
            if (colorID != null) try {
                colors = decode(colorID);
            } catch (Exception exception) { // Includes IOException and ParseException
                final String table = result.getMetaData().getTableName(colorsIndex);
                result.close();
                throw new IllegalRecordException(table, exception);
            }
            /*
             * Construit une catégorie correspondant à l'enregistrement qui vient d'être lu.
             * Une catégorie peut être qualitative (premier cas), quantitative mais linéaire
             * (deuxième cas), ou quantitative et logarithmique (troisième cas).
             */
            Category category;
            final NumberRange range = new NumberRange(lower, upper);
            if (!isQuantifiable) {
                // Catégorie qualitative.
                category = new Category(name, colors, range, (MathTransform1D)null);
            } else {
                // Catégorie quantitative
                category = new Category(name, colors, range, c1, c0);
                if (function != null) {
                    if (function.equalsIgnoreCase("log")) try {
                        // Catégorie quantitative et logarithmique.
                        final MathTransformFactory factory = ReferencingFactoryFinder.getMathTransformFactory(null);
                        if (exponential == null) {
                            final ParameterValueGroup param = factory.getDefaultParameters("Exponential");
                            param.parameter("base").setValue(10.0); // Must be a 'double'
                            exponential = (MathTransform1D) factory.createParameterizedTransform(param);
                        }
                        MathTransform1D tr = category.getSampleToGeophysics();
                        tr = (MathTransform1D) factory.createConcatenatedTransform(tr, exponential);
                        category = new Category(name, colors, range, tr);
                    } catch (FactoryException exception) {
                        result.close();
                        throw new ServerException(exception);
                    } else {
                        final String table = result.getMetaData().getTableName(functionIndex);
                        result.close();
                        throw new IllegalRecordException(table, "Fonction inconnue: " + function);
                    }
                }
            }
            categories.add(category);
        }
        result.close();
        return categories.toArray(new Category[categories.size()]);
    }

    /**
     * Optient une couleur uniforme ou une palette de couleur. L'argument {@code colors}
     * peut être un code RGB d'une seule couleur (par exemple {@code "#D2C8A0"}), ou un
     * lien URL vers une palette de couleurs (par exemple {@code "SST-Nasa.pal"}).
     *
     * @param  colors Identificateur de la ou les couleurs désirées.
     * @return Palette de couleurs demandée.
     * @throws IOException si les couleurs n'ont pas pu être lues.
     * @throws ParseException si le fichier de la palette de couleurs a été ouvert,
     *         mais qu'elle contient des caractères qui n'ont pas pus être interprétés.
     */
    private static Color[] decode(String colors) throws IOException, ParseException {
        /*
         * Retire les guillements au début et à la fin de la chaîne, s'il y en a.
         * Cette opération vise à éviter des problèmes de compatibilités lorsque
         * l'importation des thèmes dans la base des données s'est senti obligée
         * de placer des guillemets partout.
         */
        if (true) {
            colors = colors.trim();
            final int length = colors.length();
            if (length>=2 && colors.charAt(0)=='"' && colors.charAt(length-1)=='"') {
                colors = colors.substring(1, length-1);
            }
        }
        /*
         * Vérifie si la chaîne de caractère représente un code de couleurs
         * unique, comme par exemple "#D2C8A0". Si oui, ce code sera retourné
         * dans un tableau de longueur 1.
         */
        try {
            return new Color[] {Color.decode(colors)};
        } catch (NumberFormatException exception) {
            /*
             * Le décodage de la chaîne a échoué. C'est peut-être
             * parce qu'il s'agit d'un nom de fichier.  On ignore
             * l'erreur et on continue en essayant de décoder l'URL.
             */
        }
        final URL url = new URL(colors);
        return Utilities.getPaletteFactory(null).getColors(url.getPath());
    }
}
