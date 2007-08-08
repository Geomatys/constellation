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
package net.sicade.coverage.catalog.sql;

import java.util.Date;
import java.text.DateFormat;
import java.io.Serializable;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.Utilities;

import net.sicade.coverage.catalog.Layer;
import net.sicade.coverage.catalog.Operation;


/**
 * Bloc de paramètres pour une table {@link GridCoverageTable}. Les blocs de paramètres doivent
 * être immutables. Ce principe d'imutabilité s'applique aussi aux objets référencés par
 * les champs publiques, même si ces objets sont en principe mutables ({@link Rectangle2D},
 * {@link Dimension2D}...).
 * 
 * @author Martin Desruisseaux
 * @version $Id$
 */
final class GridCoverageSettings implements Serializable {
    /**
     * Numéro de série (pour compatibilité avec des versions antérieures).
     */
    private static final long serialVersionUID = 6418640591318515042L;

    /**
     * Réference vers la couche d'images. Cette référence est construite à
     * partir du champ ID dans la table "Layers" de la base de données.
     */
    public final Layer layer;

    /**
     * L'opération à appliquer sur les images lue, ou {@code null} s'il n'y en a aucune.
     */
    public final Operation operation;

    /**
     * Format à utiliser pour lire les images.
     */
    public final FormatEntry format;

    /**
     * Chemin relatif des images.
     */
    public final String pathname;

    /**
     * Extension (sans le point) des noms de fichier des images à lire.
     */
    public final String extension;

    /**
     * La partie temporelle de {@link #tableCRS}. Ne sera construit que la première fois
     * où elle sera nécessaire.
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
     * Root images directory, for access through a local network.
     */
    public final String rootDirectory;

    /**
     * Root URL directory (usually a FTP server), for access through a distant network.
     */
    public final String rootURL;

    /**
     * Encodage des noms de fichiers (typiquement {@code "UTF-8"}), ou {@code null} si aucun
     * encodage ne doit être effectué.
     */
    public final String encoding;

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
     * @param layer Référence vers la couche d'images.
     * @param format Format à utiliser pour lire les images.
     * @param pathname Chemin relatif des images.
     * @param extension Extension (sans le point) des noms de fichier des images à lire.
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
     * @param encoding Encodage des noms de fichiers, ou {@code null} si aucun encodage ne doit être
     *        effectué.
     */
    public GridCoverageSettings(final Layer                     layer,
                      final FormatEntry               format,
                      final String                    pathname,
                      final String                    extension,
                      final Operation                 operation,
                      final CoordinateReferenceSystem tableCRS,
                      final CoordinateReferenceSystem coverageCRS,
                      final Rectangle2D               geographicArea,
                      final Dimension2D               resolution,
                      final DateFormat                dateFormat,
                      final String                    rootDirectory,
                      final String                    rootURL,
                      final String                    encoding)
    {
        this.layer          = layer;
        this.format         = format;
        this.pathname       = pathname;
        this.extension      = extension;
        this.operation      = operation;
        this.tableCRS       = tableCRS;
        this.coverageCRS    = coverageCRS;
        this.geographicArea = geographicArea;
        this.resolution     = resolution;
        this.dateFormat     = dateFormat;
        this.rootDirectory  = rootDirectory;
        this.rootURL        = rootURL;
        this.encoding       = encoding;
    }

    /**
     * Indique si ce bloc de paramètres est identique au bloc spécifié.
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof GridCoverageSettings) {
            final GridCoverageSettings that = (GridCoverageSettings) o;
            return Utilities.equals(this.layer          , that.layer         ) &&
                   Utilities.equals(this.format         , that.format        ) &&
                   Utilities.equals(this.pathname       , that.pathname      ) &&
                   Utilities.equals(this.extension      , that.extension     ) &&
                   Utilities.equals(this.operation      , that.operation     ) &&
                   Utilities.equals(this.tableCRS       , that.tableCRS      ) &&
                   Utilities.equals(this.coverageCRS    , that.coverageCRS   ) &&
                   Utilities.equals(this.geographicArea , that.geographicArea) &&
                   Utilities.equals(this.resolution     , that.resolution    ) &&
                   Utilities.equals(this.dateFormat     , that.dateFormat    ) &&
                   Utilities.equals(this.rootDirectory  , that.rootDirectory ) &&
                   Utilities.equals(this.rootURL        , that.rootURL       ) &&
                   Utilities.equals(this.encoding       , that.encoding      );
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
     * Projète le rectangle spécifié du système de références des coordonnées de la
     * table ({@link #tableCRS}) vers le système de l'image ({@link #coverageCRS}).
     *
     * @param area Le rectangle à transformer.
     * @param dest Le rectangle dans lequel écrire le résultat de la transformation,
     *             <strong>SI</strong> une transformation était nécessaire. La valeur
     *             {@code null} créera un nouveau rectangle si nécessaire.
     * @return Le rectangle transformé, ou {@code area} (et non {@code dest}) si aucune
     *         transformation n'était nécessaire.
     *
     * @todo Attention, getCRS2D ne tient pas compte des dimensions des GridCoverages
     */
    final Rectangle2D tableToCoverageCRS(Rectangle2D area, final Rectangle2D dest)
            throws TransformException
    {
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
            area = CRS.transform(tableToCoverageCRS, area, dest);
        }
        return area;
    }

    /**
     * Formate la date spécifiée.
     */
    public String format(final Date date) {
        synchronized (dateFormat) {
            return dateFormat.format(date);
        }
    }

    /**
     * Retourne un code représentant ce bloc de paramètres.
     */
    @Override
    public int hashCode() {
        int code = (int)serialVersionUID;
        if (geographicArea != null) code += geographicArea.hashCode();
        if (resolution     != null) code +=     resolution.hashCode();
        return code;
    }
}
