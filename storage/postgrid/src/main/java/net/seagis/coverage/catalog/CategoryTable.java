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
import org.geotools.image.io.PaletteFactory;

import net.seagis.catalog.CatalogException;
import net.seagis.catalog.ServerException;
import net.seagis.catalog.IllegalRecordException;
import net.seagis.catalog.Database;
import net.seagis.catalog.Table;
import net.seagis.catalog.QueryType;


/**
 * Connection to a table of {@linkplain Category categories}. This table creates a list of
 * {@link Category} objects for a given sample dimension. Categories are one of the components
 * required for creating a {@link org.geotools.coverage.grid.GridCoverage2D}; they are not an
 * {@link net.seagis.catalog.Element} subinterface.
 *
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class CategoryTable extends Table {
    /**
     * A transparent color for missing data.
     */
    private static final Color[] TRANSPARENT = new Color[] {
        new Color(0,0,0,0)
    };

    /**
     * Transformation <code>f(x) = 10<sup>x</sup></code>. Utilisée pour le décodage des images de
     * concentrations en chlorophylle-a. Ne sera construite que la première fois où elle sera
     * nécessaire.
     */
    private transient MathTransform1D exponential;

    /**
     * Creates a category table.
     *
     * @param database Connection to the database.
     */
    public CategoryTable(final Database database) {
        super(new CategoryQuery(database));
    }

    /**
     * Returns the list of categories for the given sample dimension.
     *
     * @param  band The name of the sample dimension.
     * @return The categories for the given sample dimension.
     * @throws CatalogException if an inconsistent record is found in the database.
     * @throws SQLException if an error occured while reading the database.
     */
    public synchronized Category[] getCategories(final String band) throws CatalogException, SQLException {
        final PaletteFactory palettes = PaletteFactory.getDefault();
        palettes.setWarningLocale(getDatabase().getLocale());

        final CategoryQuery query = (CategoryQuery) this.query;
        final PreparedStatement statement = getStatement(QueryType.LIST);
        statement.setString(indexOf(query.byBand), band);
        final int nameIndex     = indexOf(query.name    );
        final int lowerIndex    = indexOf(query.lower   );
        final int upperIndex    = indexOf(query.upper   );
        final int c0Index       = indexOf(query.c0      );
        final int c1Index       = indexOf(query.c1      );
        final int functionIndex = indexOf(query.function);
        final int colorsIndex   = indexOf(query.colors  );
        final List<Category> categories = new ArrayList<Category>();
        final ResultSet results = statement.executeQuery();
        while (results.next()) {
            boolean isQuantifiable = true;
            final String     name = results.getString(nameIndex);
            final int       lower = results.getInt   (lowerIndex);
            final int       upper = results.getInt   (upperIndex);
            final double       c0 = results.getDouble(c0Index); isQuantifiable &= !results.wasNull();
            final double       c1 = results.getDouble(c1Index); isQuantifiable &= !results.wasNull();
            final String function = results.getString(functionIndex);
            final String  colorID = results.getString(colorsIndex);
            /*
             * Procède maintenant au décodage du champ "colors". Ce champ contient
             * une chaîne de caractère qui indique soit le code RGB d'une couleur
             * uniforme, ou soit l'adresse URL d'une palette de couleurs.
             */
            Color[] colors = null;
            if (colorID != null) try {
                colors = decode(palettes, colorID);
            } catch (Exception exception) { // Includes IOException and ParseException
                throw new IllegalRecordException(exception, this, results, colorsIndex, name);
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
                if (colors == null) {
                    colors = TRANSPARENT;
                }
                category = new Category(name, colors, range, (MathTransform1D) null);
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
                        results.close();
                        throw new ServerException(exception);
                    } else {
                        throw new IllegalRecordException("Fonction inconnue: " + function,
                                    this, results, functionIndex, name);
                    }
                }
            }
            categories.add(category);
        }
        results.close();
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
    private static Color[] decode(final PaletteFactory palettes, String colors)
            throws IOException, ParseException
    {
        /*
         * Vérifie si la chaîne de caractère représente un code de couleurs
         * unique, comme par exemple "#D2C8A0". Si oui, ce code sera retourné
         * dans un tableau de longueur 1.
         */
        try {
            return new Color[] {
                Color.decode(colors)
            };
        } catch (NumberFormatException exception) {
            /*
             * Le décodage de la chaîne a échoué. C'est peut-être
             * parce qu'il s'agit d'un nom de fichier.  On ignore
             * l'erreur et on continue en essayant de décoder l'URL.
             */
        }
        return palettes.getColors(colors);
    }
}
