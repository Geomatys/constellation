--------------------------------------------------------------------------------------------------
-- French localization.                                                                         --
--------------------------------------------------------------------------------------------------

SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;
SET default_tablespace = '';
SET default_with_oids = false;
SET search_path = postgrid, postgis, pg_catalog;



COMMENT ON SCHEMA postgrid IS
    'Metadata for grid coverages';
COMMENT ON TABLE "Formats" IS
    'Formats des images (PNG, GIF, JPEG, etc...).';
COMMENT ON COLUMN "Formats"."name" IS
    'Nom identifiant le format.';
COMMENT ON COLUMN "Formats"."mime" IS
    'Nom MIME du format.';
COMMENT ON COLUMN "Formats"."encoding" IS
    'Encodage des données de l''image: "geophysics" ou "native".';
COMMENT ON CONSTRAINT "Formats_encoding_check" ON "Formats" IS
    'Énumération des valeurs acceptables.';
COMMENT ON TABLE "SampleDimensions" IS
    'Descriptions des bandes comprises dans chaque format d''images.';
COMMENT ON COLUMN "SampleDimensions"."identifier" IS
    'Nom unique identifiant la bande.';
COMMENT ON COLUMN "SampleDimensions"."format" IS
    'Format qui contient cette bande.';
COMMENT ON COLUMN "SampleDimensions"."band" IS
    'Numéro de la bande (à partir de 1).';
COMMENT ON COLUMN "SampleDimensions"."units" IS
    'Unités des mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON CONSTRAINT "SampleDimensions_format_fkey" ON "SampleDimensions" IS
    'Chaque bande fait partie de la description d''une image.';
COMMENT ON CONSTRAINT "SampleDimensions_band_check" ON "SampleDimensions" IS
    'Le numéro de bande doit être positif.';
COMMENT ON INDEX "SampleDimensions_index" IS
    'Classement des bandes dans leur ordre d''apparition.';
COMMENT ON TABLE "Categories" IS
    'Plage de valeurs des différents thèmes et relation entre les valeurs des pixels et leurs mesures géophysiques.';
COMMENT ON COLUMN "Categories"."name" IS
    'Nom du thème représenté par cette plage de valeurs.';
COMMENT ON COLUMN "Categories"."band" IS
    'Bande auquel s''applique cette plage de valeurs.';
COMMENT ON COLUMN "Categories"."lower" IS
    'Valeur minimale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories"."upper" IS
    'Valeur maximale (inclusive) des pixels de ce thème.';
COMMENT ON COLUMN "Categories"."c0" IS
    'Coefficient C0 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories"."c1" IS
    'Coefficient C1 de l''équation y=C0+C1*x, ou x est la valeur du pixel et y la valeur en mesures géophysiques. Ce champ peut être blanc s''il ne s''applique pas.';
COMMENT ON COLUMN "Categories"."function" IS
    'Fonction appliquée sur les valeurs géophysiques. Par exemple la valeur "log" indique que les valeurs sont exprimées sous la forme log(y)=C0+C1*x.';
COMMENT ON COLUMN "Categories"."colors" IS
    'Ce champ peut être soit un code d''une couleur, ou soit une adresse URL vers une palette de couleurs.';
COMMENT ON CONSTRAINT "Categories_band_fkey" ON "Categories" IS
    'Chaque catégorie est un élément de la description d''une bande.';
COMMENT ON CONSTRAINT "Categories_coefficients" ON "Categories" IS
    'Les coefficients C0 et C1 doivent être nuls ou non-nuls en même temps.';
COMMENT ON INDEX "Categories_index" IS
    'Recherche des catégories appartenant à une bande.';
COMMENT ON VIEW "RangeOfFormats" IS
    'Plage des valeurs de chaque format d''images.';
COMMENT ON TABLE "Layers" IS
    'Ensemble de séries d''images appartenant à une même thématique.';
COMMENT ON COLUMN "Layers"."name" IS
    'Nom identifiant la couche.';
COMMENT ON COLUMN "Layers"."thematic" IS
    'Paramètre géophysique (ou thème) de cette couche.';
COMMENT ON COLUMN "Layers"."procedure" IS
    'Procédure appliquée pour produire les images.';
COMMENT ON COLUMN "Layers"."period" IS
    'Nombre de jours prévus entre deux image. Cette information peut être approximative ou laissée blanc si elle ne s''applique pas.';
COMMENT ON COLUMN "Layers"."fallback" IS
    'Couche de rechange proposée si aucune donnée n''est disponible pour la couche courante.';
COMMENT ON CONSTRAINT "Layers_fallback_fkey" ON "Layers" IS
    'Chaque couche de second recours doit exister.';
COMMENT ON INDEX "Layers_index" IS
    'Recherche des couches appartenant à une thématique.';
COMMENT ON TABLE "Series" IS
    'Séries d''images. Chaque images appartient à une série.';
COMMENT ON COLUMN "Series"."identifier" IS
    'Identifiant unique de la séries.';
COMMENT ON COLUMN "Series"."layer" IS
    'Couche à laquelle appartiennent les images de cette série.';
COMMENT ON COLUMN "Series"."pathname" IS
    'Chemins relatifs des fichiers du groupe. La racine à ces chemins ne doit pas être spécifiée si elle peut varier d''une plateforme à l''autre.';
COMMENT ON COLUMN "Series"."extension" IS
    'Extension des fichiers d''images de cette série.';
COMMENT ON COLUMN "Series"."format" IS
    'Format des images de ce groupe.';
COMMENT ON COLUMN "Series"."visible" IS
    'Indique si les images de ce groupe doivent apparaître dans la liste des images proposées à l''utilisateur.';
COMMENT ON COLUMN "Series"."quicklook" IS
    'Série dont les images sont des aperçus de cette série.';
COMMENT ON CONSTRAINT "Series_quicklook_key" ON "Series" IS
    'Chaque série a une seule autre série d''aperçus.';
COMMENT ON CONSTRAINT "Series_layer_fkey" ON "Series" IS
    'Chaque série appartient à une couche.';
COMMENT ON CONSTRAINT "Series_format_fkey" ON "Series" IS
    'Toutes les images d''une même série utilisent un même séries.';
COMMENT ON CONSTRAINT "Series_quicklook_fkey" ON "Series" IS
    'Les aperçus s''appliquent à une autre séries d''images.';
COMMENT ON INDEX "Series_index" IS
    'Recherche des séries appartenant à une couche.';
COMMENT ON INDEX "HorizontalExtent_index" IS
    'Recherche des geométries interceptant une région géographique.';
COMMENT ON TABLE "GridGeometries" IS
    'Envelope spatiales des images ainsi que la dimension de leurs grilles. La transformation affine doit représenter le coin supérieur gauche des pixels.';
COMMENT ON COLUMN "GridGeometries"."identifier" IS
    'Identifiant unique.';
COMMENT ON COLUMN "GridGeometries"."width" IS
    'Nombre de pixels en largeur dans l''image.';
COMMENT ON COLUMN "GridGeometries"."height" IS
    'Nombre de pixels en hauteur dans l''image.';
COMMENT ON COLUMN "GridGeometries"."translateX" IS
    'Élement (0,2) de la transformation affine. Il correspond habituellement à la coordonnées x du coin supérieur gauche.';
COMMENT ON COLUMN "GridGeometries"."translateY" IS
    'Élement (1,2) de la transformation affine. Il correspond habituellement à la coordonnées y du coin supérieur gauche.';
COMMENT ON COLUMN "GridGeometries"."scaleX" IS
    'Élement (0,0) de la transformation affine. Il correspond habituellement à la taille selon x des pixels.';
COMMENT ON COLUMN "GridGeometries"."scaleY" IS
    'Élement (1,1) de la transformation affine. Il correspond habituellement à la taille selon y des pixels. Cette valeur est souvent négative puisque la numérotation des lignes d''une image augmente vers le bas.';
COMMENT ON COLUMN "GridGeometries"."shearX" IS
    'Élement (0,1) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';
COMMENT ON COLUMN "GridGeometries"."shearY" IS
    'Élement (1,0) de la transformation affine. Toujours à 0 s''il n''y a pas de rotation.';
COMMENT ON COLUMN "GridGeometries"."horizontalSRID" IS
    'Code du système de référence des coordonnées horizontales.';
COMMENT ON COLUMN "GridGeometries"."horizontalExtent" IS
    'Étendue spatiale à l''horizontal.';
COMMENT ON COLUMN "GridGeometries"."verticalSRID" IS
    'Code du système de référence des coordonnées verticales.';
COMMENT ON COLUMN "GridGeometries"."verticalOrdinates" IS
    'Valeurs z de chacunes des couches d''une image 3D.';
COMMENT ON CONSTRAINT "GridGeometries_size" ON "GridGeometries" IS
    'Les dimensions des images doivent être positives.';
COMMENT ON CONSTRAINT "enforce_dims_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est à deux dimensions.';
COMMENT ON CONSTRAINT "enforce_geotype_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est un polygone.';
COMMENT ON CONSTRAINT "enforce_srid_horizontalExtent" ON "GridGeometries" IS
    'Vérifie que l''étendue horizontale est exprimée selon le CRS attendu.';
COMMENT ON CONSTRAINT "enforce_srid_verticalOrdinates" ON "GridGeometries" IS
    'Les coordonnées verticales et leur SRID doivent être nul ou non-nul en même temps.';
COMMENT ON TRIGGER "addDefaultExtent" ON "GridGeometries" IS
    'Ajoute une enveloppe par défaut si aucune n''était définie explicitement.';
COMMENT ON VIEW "BoundingBoxes" IS
    'Comparaison entre les enveloppes calculées et les enveloppes déclarées.';
COMMENT ON TABLE "GridCoverages" IS
    'Liste de toutes les images disponibles. Chaque enregistrement correspond à un fichier d''image.';
COMMENT ON COLUMN "GridCoverages"."series" IS
    'Série à laquelle appartient l''image.';
COMMENT ON COLUMN "GridCoverages"."filename" IS
    'Nom du fichier contenant l''image.';
COMMENT ON COLUMN "GridCoverages"."index" IS
    'Index de l''image dans les fichiers contenant plusieurs images. Numérotées à partir de 1.';
COMMENT ON COLUMN "GridCoverages"."startTime" IS
    'Date et heure du début de l''acquisition de l''image, en heure universelle (UTC). Dans le cas des moyennes, cette date correspond au début de l''intervalle de temps ayant servit à établir la moyenne.';
COMMENT ON COLUMN "GridCoverages"."endTime" IS
    'Date et heure de la fin de l''acquisition de l''image, en heure universelle (UTC). Cette date doit être supérieure ou égale à la date de début d''acquisition.';
COMMENT ON COLUMN "GridCoverages"."extent" IS
    'Coordonnées de la région géographique couverte par l''image, ainsi que sa résolution approximative. ';
COMMENT ON CONSTRAINT "GridCoverages_series_key" ON "GridCoverages" IS
    'L''envelope de l''image doit être unique dans chaque série.';
COMMENT ON CONSTRAINT "GridCoverages_series_fkey" ON "GridCoverages" IS
    'Chaque image appartient à une série.';
COMMENT ON CONSTRAINT "GridCoverages_extent_fkey" ON "GridCoverages" IS
    'Chaque images doit avoir une étendue spatiale.';
COMMENT ON CONSTRAINT "GridCoverages_check" ON "GridCoverages" IS
    'Les dates de début et de fin doivent être nulles ou non-nulles en même temps, et la date de début doit être inférieure ou égale à la date de fin.';
COMMENT ON CONSTRAINT "GridCoverages_index_check" ON "GridCoverages" IS
    'L''index de l''image doit être strictement positif.';
COMMENT ON INDEX "GridCoverages_index" IS
    'Recherche de toutes les images à l''intérieur d''une certaine plage de temps.';
COMMENT ON INDEX "GridCoverages_extent_index" IS
    'Recherche de toutes les images dans une région géographique.';
COMMENT ON VIEW "DomainOfSeries" IS
    'Liste des régions géographiques utilisées par chaque sous-série.';
COMMENT ON VIEW "DomainOfLayers" IS
    'Nombre d''images et région géographique pour chacune des couches utilisées.';
COMMENT ON COLUMN "Layers"."description" IS
    'Description optionnelle de la couche.';
COMMENT ON TABLE "Thematics" IS
    'Paramètres géophysiques représentés par les images (température, hauteur de l''eau...).';
COMMENT ON COLUMN "Thematics"."name" IS
    'Nom identifiant le paramètre géophysique.';
COMMENT ON COLUMN "Thematics"."description" IS
    'Description du paramètre géophysique.';
COMMENT ON CONSTRAINT "Layers_thematic_fkey" ON "Layers" IS
    'Chaque couche représente les données observées pour une thématique.';
COMMENT ON TABLE "Procedures" IS
    'Procédures utilisées pour effectuer une observation.';
COMMENT ON COLUMN "Procedures".name IS
    'Nom unique identifiant cette procédure.';
COMMENT ON COLUMN "Procedures".description IS
    'Description de la procédure.';
COMMENT ON TABLE "Operations" IS
    'Opérations mathématique ayant servit à produire les images.';
COMMENT ON COLUMN "Operations"."name" IS
    'Nom identifiant l''opération.';
COMMENT ON COLUMN "Operations"."prefix" IS
    'Préfix à utiliser dans les noms composites. Les noms composites seront de la forme "operation - paramètre - temps", par exemple "∇SST₋₁₅".';
COMMENT ON COLUMN "Operations"."operation" IS
    'Nom OpenGIS ou JAI. identifiant l''opération. Ce nom sera transmis en argument à la méthode "GridCoverageProcessor.doOperation(...)".';
COMMENT ON COLUMN "Operations"."kernelSize" IS
    'Nombre de pixels selon x et y nécessaire à l''application de l''opération.';
COMMENT ON COLUMN "Operations"."description" IS
    'Description optionnelle de l''opération.';
COMMENT ON TABLE "OperationParameters" IS
    'Valeur des paramètres des opérations d''images.';
COMMENT ON COLUMN "OperationParameters"."operation" IS
    'Nom de l''opération sur lequel s''appliquera un ou plusieurs paramètres.';
COMMENT ON COLUMN "OperationParameters"."parameter" IS
    'Nom du paramètre, tel que déclaré dans CoverageProcessor ou JAI.';
COMMENT ON COLUMN "OperationParameters"."value" IS
    'Valeur du paramètre.';
COMMENT ON TABLE "RegionOfInterests" IS
    'Positions spatio-temporelles relatives à des observations.';
COMMENT ON COLUMN "RegionOfInterests"."name" IS
    'Nom unique identifiant cette position relative.';
COMMENT ON COLUMN "RegionOfInterests"."dx" IS
    'Décalage Est-Ouest, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dy" IS
    'Décalage Nord-Sud, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dz" IS
    'Décalage vertical, en mètres.';
COMMENT ON COLUMN "RegionOfInterests"."dt" IS
    'Décalage temporel, en nombre de jours.';
COMMENT ON TABLE "Distributions" IS
    'Distributions approximatives (normale, log-normale...) des descripteurs.';
COMMENT ON COLUMN "Distributions"."name" IS
    'Nom de la distribution.';
COMMENT ON COLUMN "Distributions"."scale" IS
    'Facteur par lequel multiplier les valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';
COMMENT ON COLUMN "Distributions"."offset" IS
    'Constantes à ajouter aux valeurs avant l''analyse statistiques. Utile surtout si le logarithme doit être calculé.';
COMMENT ON COLUMN "Distributions"."log" IS
    'Indique si les analyses statistiques doivent se faire sur le logarithme des valeurs transformées. La transformation complète sera alors x''=log(x*scale + offset), ou "log" est le logarithme népérien.';
COMMENT ON TABLE "Descriptors" IS
    'Descripteurs du paysage océanique, chacun étant une combinaison d''une couche, d''une opération et d''un décalage spatio-temporel.';
COMMENT ON COLUMN "Descriptors"."identifier" IS
    'Clé primaire identifiant ce descripteur du paysage océanique.';
COMMENT ON COLUMN "Descriptors"."symbol" IS
    'Symbole unique identifiant ce descripteur, pour une lecture plus humaine que le numéro d''identifiant.';
COMMENT ON COLUMN "Descriptors"."layer" IS
    'Phénomène (température, chlorophylle...) étudié par ce descripteur.';
COMMENT ON COLUMN "Descriptors"."operation" IS
    'Opération appliquée sur les mesures du phénomène pour obtenir le descripteur (exemple: opérateur de gradient).';
COMMENT ON COLUMN "Descriptors"."region" IS
    'Décalage spatio-temporelle entre la position de l''observation et celle à laquelle sera évaluée le descripteur.';
COMMENT ON COLUMN "Descriptors"."band" IS
    'Numéro (à partir de 1) de la bande à prendre en compte.';
COMMENT ON COLUMN "Descriptors"."distribution" IS
    'Distribution approximative des données. La distribution "Amplitude" résulte d''une combinaison de distributions normales de la forme x²+y². Les distributions normales ne sont généralement pas indépendantes, ce qui distingue cette distribution de X².';
COMMENT ON CONSTRAINT "Descriptors_band_check" ON "Descriptors" IS
    'Les numéros de bandes doivent être des entiers positifs non-nuls.';
COMMENT ON CONSTRAINT "Descriptors_layer_fkey" ON "Descriptors" IS
    'Chaque descripteur concerne un phénomène.';
COMMENT ON CONSTRAINT "Descriptors_operation_fkey" ON "Descriptors" IS
    'Chaque descripteur est le résultat d''une certaine opération appliquée sur les données du phénomène observé.';
COMMENT ON CONSTRAINT "Descriptors_region_fkey" ON "Descriptors" IS
    'Chaque descripteur peut être évalué à une position spatio-temporelle décalée par rapport à la position de la station.';
COMMENT ON CONSTRAINT "Descriptors_distribution_fkey" ON "Descriptors" IS
    'Chaque descripteur possède des valeurs suivant une loi de distribution.';
COMMENT ON TABLE "LinearModelTerms" IS
    'Poids à donner aux différentes combinaisons de descripteurs pour calculer une nouvelle image.';
COMMENT ON COLUMN "LinearModelTerms"."target" IS
    'Couche d''images pour laquelle ce modèle linéaire effectue ses calculs.';
COMMENT ON COLUMN "LinearModelTerms"."source1" IS
    'Premier descripteur entrant dans le terme.';
COMMENT ON COLUMN "LinearModelTerms"."source2" IS
    'Deuxième descripteur entrant dans le terme. S''il n''est pas le descripteur identité, il sera multiplié par le premier descripteur.';
COMMENT ON COLUMN "LinearModelTerms"."coefficient" IS
    'Facteur par lequel multiplier le terme du modèle linéaire.';
COMMENT ON CONSTRAINT "LinearModelTerms_target_fkey" ON "LinearModelTerms" IS
    'La variable à expliquer doit être une série d''images.';
COMMENT ON CONSTRAINT "LinearModelTerms_source1_fkey" ON "LinearModelTerms" IS
    'Le premier terme doit être un des descripteurs du paysage océanique.';
COMMENT ON CONSTRAINT "LinearModelTerms_source2_fkey" ON "LinearModelTerms" IS
    'Le second terme doit être un des descripteurs du paysage océanique.';
COMMENT ON TRIGGER "ReplaceModelDescriptors_trigger" ON "LinearModelTerms" IS
    'Remplace les identifieurs numériques par leurs symboles pour les colonnes "descriptors".';
