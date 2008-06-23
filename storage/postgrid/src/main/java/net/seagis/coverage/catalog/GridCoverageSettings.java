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
package net.seagis.coverage.catalog;

import java.util.Date;
import java.text.DateFormat;
import java.io.Serializable;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;
import net.seagis.coverage.model.Operation;


/**
 * Set of parameters for a {@link GridCoverageTable} to be shared by every {@link GridCoverageEntry}
 * created from that table. {@code GridCoverageSettings} instances <strong>must</strong> be immutable.
 * Public mutable fields like {@link Rectangle2D} and {@link Dimension2D} must not be changed after
 * construction.
 *
 * @author Martin Desruisseaux
 * @author Sam Hiatt
 * @version $Id$
 */
final class GridCoverageSettings implements Serializable {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 6418640591318515042L;

    /**
     * Operation to apply on image after reading, or {@code null} if none.
     */
    public final Operation operation;

    /**
     * The temporal part of {@link #tableCRS}. Will be computed only when first needed.
     */
    private transient DefaultTemporalCRS temporalCRS;

    /**
     * Système de référence des coordonnées de la table. Le système de coordonnées de tête
     * ("head") doit obligatoirement être un CRS horizontal. La seconde partie ("tail") sera
     * ignorée; il s'agira typiquement de l'axe du temps ou de la profondeur.
     */
    public final CoordinateReferenceSystem tableCRS;

    /**
     * Système de référence des coordonnées de l'image. Ce sera habituellement
     * (mais pas obligatoirement) le même que {@link #tableCRS}.
     */
    public final CoordinateReferenceSystem coverageCRS;

    /**
     * La transformation du système de références des coordonnées de la table
     * ({@link #tableCRS}) vers le système de l'image ({@link #coverageCRS}).
     */
    private transient MathTransform2D tableToCoverageCRS;

    /**
     * Coordonnées horizontales de la région d'intéret.  Ces coordonnées
     * sont exprimées selon la partie horizontale ("head") du système de
     * coordonnées {@link #tableCRS}.
     */
    public final Rectangle2D geographicArea;

    /**
     * Dimension logique désirée des pixels de l'images.   Cette information
     * n'est qu'approximative. Il n'est pas garantie que la lecture produira
     * effectivement une image de cette résolution. Une valeur nulle signifie
     * que la lecture doit se faire avec la meilleure résolution possible.
     */
    public final Dimension2D resolution;

    /**
     * Formatteur à utiliser pour écrire des dates pour l'utilisateur. Les caractères et
     * les conventions linguistiques dépendront de la langue de l'utilisateur. Toutefois,
     * le fuseau horaire devrait être celui de la région d'étude plutôt que celui du pays
     * de l'utilisateur.
     */
    private final DateFormat dateFormat;

    /**
     * Construit un bloc de paramètres.
     *
     * @param operation Opération à appliquer sur les images, ou {@code null}.
     * @param tableCRS Système de référence des coordonnées de la table. Le système de
     *        coordonnées de tête ("head") doit obligatoirement être un CRS horizontal.
     * @param coverageCRS Système de référence des coordonnées de l'image. Ce sera
     *        habituellement (mais pas obligatoirement) le même que {@link #tableCRS}.
     * @param geographicArea Coordonnées horizontales de la région d'intéret,
     *        dans le système de référence des coordonnées {@code tableCRS}.
     * @param resolution Dimension logique approximative désirée des pixels,
     *        ou {@code null} pour la meilleure résolution disponible.
     *        Doit être exprimé dans le système de coordonnées {@code tableCRS}.
     * @param dateFormat Formatteur à utiliser pour écrire des dates pour l'utilisateur.
     */
    public GridCoverageSettings(final Operation                 operation,
                                final CoordinateReferenceSystem tableCRS,
                                final CoordinateReferenceSystem coverageCRS,
                                final Rectangle2D               geographicArea,
                                final Dimension2D               resolution,
                                final DateFormat                dateFormat)
    {
        this.operation      = operation;
        this.tableCRS       = tableCRS;
        this.coverageCRS    = coverageCRS;
        this.geographicArea = geographicArea;
        this.resolution     = resolution;
        this.dateFormat     = dateFormat;
    }

    /**
     * Indique si ce bloc de paramètres est identique au bloc spécifié.
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof GridCoverageSettings) {
            final GridCoverageSettings that = (GridCoverageSettings) o;
            return Utilities.equals(this.operation      , that.operation     ) &&
                   Utilities.equals(this.tableCRS       , that.tableCRS      ) &&
                   Utilities.equals(this.coverageCRS    , that.coverageCRS   ) &&
                   Utilities.equals(this.geographicArea , that.geographicArea) &&
                   Utilities.equals(this.resolution     , that.resolution    ) &&
                   Utilities.equals(this.dateFormat     , that.dateFormat    );
        }
        return false;
    }

    /**
     * Retourne la partie temporelle de {@link #tableCRS}.
     */
    public DefaultTemporalCRS getTemporalCRS() {
        // Pas besoin de synchroniser; ce n'est pas grave si le même CRS est construit deux fois.
        if (temporalCRS == null) {
            temporalCRS = DefaultTemporalCRS.wrap(CRS.getTemporalCRS(tableCRS));
        }
        return temporalCRS;
    }

    /**
     * Project the given shape from the {@linkplain #tableCRS table CRS} to the
     * {@linkplain #coverageCRS coverage CRS}. If no transformation are needed,
     * then this method returns the given shape unchanged.
     *
     * @param  area The shape to transform.
     * @return The transformed shape, or {@code area} if no transformation was needed.
     *
     * @todo Attention, getCRS2D ne tient pas compte des dimensions des GridCoverages
     */
    final Shape tableToCoverageCRS(Shape area, final boolean inverse) throws TransformException {
        CoordinateReferenceSystem sourceCRS = tableCRS;
        CoordinateReferenceSystem targetCRS = coverageCRS;
        if (!CRS.equalsIgnoreMetadata(sourceCRS, targetCRS)) {
            sourceCRS = CRSUtilities.getCRS2D(sourceCRS);
            targetCRS = CRSUtilities.getCRS2D(targetCRS);
            if (tableToCoverageCRS == null) try {
                tableToCoverageCRS = (MathTransform2D) CRS.findMathTransform(sourceCRS, targetCRS);
            } catch (FactoryException exception) {
                throw new TransformException(exception.getLocalizedMessage(), exception);
            }
            MathTransform2D mt = tableToCoverageCRS;
            if (inverse) {
                mt = mt.inverse();
            }
            area = mt.createTransformedShape(area);
        }
        return area;
    }

    /**
     * Formats the specified date using a shared formatter.
     */
    public String format(final Date date) {
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    /**
     * Returns a hash code value for this parameter block.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (geographicArea != null) code += geographicArea.hashCode();
        if (resolution     != null) code +=     resolution.hashCode();
        return code;
    }
}
