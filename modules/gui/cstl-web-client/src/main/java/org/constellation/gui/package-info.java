/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2007 - 2012, Geomatys
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

/*
 * Juzu application declaration. List all alias need to run cstl-web-client
 */
@juzu.Application(defaultController = Controller.class, resourceAliases = {
        @Alias(of = "/org/constellation/gui/templates/menu.gtmpl",                  as = "menu.gtmpl"),

        //WMS
        @Alias(of = "/org/constellation/gui/templates/layer.gtmpl",                 as = "layer.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/layer_listings.gtmpl",        as = "layer_listings.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms_description.gtmpl",       as = "wms_description.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms_metadata.gtmpl",          as = "wms_metadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms.gtmpl",                   as = "wms.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms_create_description.gtmpl",as = "wms_create_description.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms_create_metadata.gtmpl",   as = "wms_create_metadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wms_create.gtmpl",            as = "wms_create.gtmpl"),

        //Raster
        @Alias(of = "/org/constellation/gui/templates/raster_description.gtmpl",    as = "raster_description.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/raster_bands.gtmpl",          as = "raster_bands.gtmpl"),

        // Style
        @Alias(of = "/org/constellation/gui/templates/style_dashboard.gtmpl",       as = "style_dashboard.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_overview.gtmpl",        as = "style_overview.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_edition.gtmpl",         as = "style_edition.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_fts.gtmpl",             as = "style_fts.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_manual.gtmpl",     as = "style_rule_manual.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_ai.gtmpl",         as = "style_rule_ai.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_auv.gtmpl",        as = "style_rule_auv.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_point.gtmpl",    as = "style_symbol_point.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_line.gtmpl",     as = "style_symbol_line.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_polygon.gtmpl",  as = "style_symbol_polygon.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_text.gtmpl",     as = "style_symbol_text.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_raster.gtmpl",   as = "style_symbol_raster.gtmpl")})



/*
 * Declare servlet Base URL and which bundle used
 */
@juzu.plugin.servlet.Servlet(value = "/", resourceBundle = "locale.cstl")

/*
 * Less file loaded
 */
@Less(value = "cstl-web-client.less", minify = true)

/*
 * javascript and css loaded
 */
@Assets(stylesheets = @Stylesheet(src = "cstl-web-client.css"),
        scripts = {@Script(id = "jquery", src = "js/jquery-2.0.0.js", location = AssetLocation.SERVER),
                @Script(id = "json", src = "js/jquery.json-2.4.js", depends = "jquery", location = AssetLocation.SERVER),
                @Script(id = "jquery-growl", src = "js/jquery.growl.js", depends = "jquery", location = AssetLocation.SERVER),
                @Script(id = "bootstrap", src = "js/bootstrap.min.js", depends = "jquery", location = AssetLocation.SERVER),
                @Script(id = "colorpicker", src = "js/bootstrap-colorpicker.js", depends = "bootstrap", location = AssetLocation.SERVER),
                @Script(id = "slider", src = "js/bootstrap-slider.js", depends = "bootstrap", location = AssetLocation.SERVER),
                @Script(id = "upload", src = "js/bootstrap-fileupload.js", depends = "bootstrap", location = AssetLocation.SERVER),
                @Script(id = "openlayers", src = "js/openlayers.js", depends = "jquery", location = AssetLocation.SERVER),
                @Script(id = "openlayers-ext", src = "js/openlayers-ext.js", depends = "openlayers", location = AssetLocation.SERVER),
                @Script(id = "wmsedition", src = "js/wmsedition.js", depends = "jquery", location = AssetLocation.SERVER),

                // Constellation commons
                @Script(id = "cstl",            src = "js/cstl.js",             depends = "jquery", location = AssetLocation.SERVER),
                @Script(id = "cstl.services",   src = "js/cstl.services.js",    depends = "jquery", location = AssetLocation.SERVER),

                // Style
                @Script(id = "style_edition",   src = "js/style_edition.js",  depends = "jquery"),
                @Script(id = "style_filter",    src = "js/style_filter.js",   depends = "style_edition"),
                @Script(id = "style_rule",      src = "js/style_rule.js",     depends = "style_edition"),
                @Script(id = "style_symbol",    src = "js/style_symbol.js",   depends = "style_edition")})

/**
 * Constellation web client main part.
 * It's the start point and declaration of all other part used
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
package org.constellation.gui;

import juzu.Alias;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.less.Less;