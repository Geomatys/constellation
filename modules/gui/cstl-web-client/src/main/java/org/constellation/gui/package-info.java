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
        @Alias(of = "/org/constellation/gui/templates/menu.gtmpl",                      as = "menu.gtmpl"),

        //MAP
        @Alias(of = "/org/constellation/gui/templates/layer.gtmpl",                     as = "layer.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/layer_listings.gtmpl",            as = "layer_listings.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/description.gtmpl",               as = "description.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/metadata.gtmpl",                  as = "metadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/map_service.gtmpl",               as = "map_service.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/create_service_description.gtmpl",as = "create_service_description.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/create_service_metadata.gtmpl",   as = "create_service_metadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/create_map_service.gtmpl",        as = "create_map_service.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/layer_selected.gtmpl",             as = "layer_selected.gtmpl"),

        // Style
        @Alias(of = "/org/constellation/gui/templates/style_dashboard.gtmpl",           as = "style_dashboard.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_edition.gtmpl",             as = "style_edition.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_fts.gtmpl",                 as = "style_fts.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_list.gtmpl",                as = "style_list.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_overview.gtmpl",            as = "style_overview.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_ai.gtmpl",             as = "style_rule_ai.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_auv.gtmpl",            as = "style_rule_auv.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_manual.gtmpl",         as = "style_rule_manual.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_selected.gtmpl",            as = "style_selected.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_line.gtmpl",         as = "style_symbol_line.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_point.gtmpl",        as = "style_symbol_point.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_polygon.gtmpl",      as = "style_symbol_polygon.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_text.gtmpl",         as = "style_symbol_text.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_raster.gtmpl",       as = "style_symbol_raster.gtmpl"),

        //Administration
        @Alias(of = "/org/constellation/gui/templates/administration.gtmpl",            as = "administration.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/database.gtmpl",                  as = "database.gtmpl")})



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
@Assets({@Asset("cstl-web-client.css"),
        @Asset(id = "github",               value = "css/github.css",                                                                               location = AssetLocation.SERVER),
        @Asset(id = "selectizecss",         value = "css/selectize.bootstrap2.css",                                                                 location = AssetLocation.SERVER),
        @Asset(id = "jquery",               value = "js/jquery-2.0.0.js",                                                                           location = AssetLocation.SERVER),
        @Asset(id = "json",                 value = "js/jquery.json-2.4.js",                    depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "jquery-growl",         value = "js/jquery.growl.js",                       depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "jquery-fullscreen",    value = "js/jquery.fullscreen.js",                  depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "bootstrap",            value = "js/bootstrap.min.js",                      depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "colorpicker",          value = "js/bootstrap-colorpicker.js",              depends = "bootstrap",                              location = AssetLocation.SERVER),
        @Asset(id = "slider",               value = "js/bootstrap-slider.js",                   depends = "bootstrap",                              location = AssetLocation.SERVER),
        @Asset(id = "upload",               value = "js/bootstrap-fileupload.js",               depends = "bootstrap",                              location = AssetLocation.SERVER),
        @Asset(id = "datepicker",           value = "js/bootstrap-datepicker.js",               depends = "bootstrap",                              location = AssetLocation.SERVER),
        @Asset(id = "datepickerfr",         value = "js/locales/bootstrap-datepicker.fr.js",    depends = "datepicker",                             location = AssetLocation.SERVER),
        @Asset(id = "validation",           value = "js/bootstrap-validation.js",               depends = "bootstrap",                              location = AssetLocation.SERVER),
        @Asset(id = "openlayers",           value = "js/openlayers.js",                         depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "openlayers-ext",       value = "js/openlayers-ext.js",                     depends = "openlayers",                             location = AssetLocation.SERVER),
        @Asset(id = "pagination",           value = "js/pagination.js",                         depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "selection",            value = "js/selection.js",                          depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "sifter",               value = "js/sifter.min.js",                         depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "microplugin",          value = "js/microplugin.js",                        depends = "jquery",                                 location = AssetLocation.SERVER),
        @Asset(id = "selectize",            value = "js/selectize.min.js",                      depends = {"jquery", "sifter", "microplugin"},      location = AssetLocation.SERVER),

        // Constellation
        @Asset(id = "cstl",                     value = "js/cstl.js",                       depends = "jquery",         location = AssetLocation.SERVER),
        @Asset(id = "cstl.data",                value = "js/cstl.data.js",                  depends = "cstl",           location = AssetLocation.SERVER),
        @Asset(id = "cstl.providers",           value = "js/cstl.providers.js",             depends = "cstl",           location = AssetLocation.SERVER),
        @Asset(id = "cstl.services",            value = "js/cstl.services.js",              depends = "cstl",           location = AssetLocation.SERVER),
        @Asset(id = "cstl.viewer",              value = "js/cstl.viewer.js",                depends = "cstl",           location = AssetLocation.SERVER),
        @Asset(id = "cstl.dashboard",           value = "js/cstl.dashboard.js",             depends = "cstl",           location = AssetLocation.SERVER),
        @Asset(id = "cstl.dashboard.map",       value = "js/cstl.dashboard.map.js",         depends = "cstl.dashboard", location = AssetLocation.SERVER),
        @Asset(id = "cstl.dashboard.data",      value = "js/cstl.dashboard.data.js",        depends = "cstl.dashboard", location = AssetLocation.APPLICATION),
        @Asset(id = "cstl.dashboard.style",     value = "js/cstl.dashboard.style.js",       depends = "cstl.dashboard", location = AssetLocation.SERVER),
        @Asset(id = "cstl.workflow.addlayer",   value = "js/cstl.workflow.addlayer.js",     depends = "cstl.dashboard", location = AssetLocation.SERVER),
        @Asset(id = "cstl.workflow",            value = "js/cstl.workflow.link.js",         depends = "cstl.dashboard", location = AssetLocation.SERVER),
        @Asset(id = "cstl.filemodal",           value = "js/cstl.filemodal.js",             depends = "jquery",         location = AssetLocation.APPLICATION),
        @Asset(id = "cstl.netcdf",              value = "js/cstl.netcdf.js",                depends = "openlayers",     location = AssetLocation.SERVER),
        @Asset(id = "highlight",                value = "js/highlight.pack.js",                                         location = AssetLocation.SERVER),

        // Style
        @Asset(id = "style_edition",    value = "js/style_edition.js",  depends = "jquery"),
        @Asset(id = "style_filter",     value = "js/style_filter.js",   depends = "style_edition"),
        @Asset(id = "style_rule",       value = "js/style_rule.js",     depends = "style_edition"),
        @Asset(id = "style_symbol",     value = "js/style_symbol.js",   depends = "style_edition")})

/**
 * Constellation web client main part.
 * It's the start point and declaration of all other part used
 *
 * @author Benjamin Garcia (Geomatys)
 * @version 0.9
 * @since 0.9
 */
@WithAssets
package org.constellation.gui;
import juzu.Alias;
import juzu.asset.AssetLocation;
import juzu.plugin.asset.Asset;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.WithAssets;
import juzu.plugin.less.Less;
