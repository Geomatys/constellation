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
        @Alias(of = "/org/constellation/gui/templates/menu.gtmpl", as = "menu.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmsdescription.gtmpl", as = "wmsdescription.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmsmetadata.gtmpl", as = "wmsmetadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmscreate.gtmpl", as = "wmscreate.gtmpl"),

        // Style
        @Alias(of = "/org/constellation/gui/templates/style_dashboard.gtmpl",      as = "style_dashboard.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_overview.gtmpl",       as = "style_overview.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_edition.gtmpl",        as = "style_edition.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_manual.gtmpl",    as = "style_rule_manual.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_ai.gtmpl",        as = "style_rule_ai.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_rule_auv.gtmpl",       as = "style_rule_auv.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_point.gtmpl",   as = "style_symbol_point.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_line.gtmpl",    as = "style_symbol_line.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_polygon.gtmpl", as = "style_symbol_polygon.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_text.gtmpl",    as = "style_symbol_text.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/style_symbol_raster.gtmpl",  as = "style_symbol_raster.gtmpl")})

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
        scripts = {@Script(id = "jquery", src = "js/jquery-2.0.0.js"),
                @Script(id = "json", src = "js/jquery.json-2.4.js", depends = "jquery"),
                @Script(id = "bootstrap", src = "js/bootstrap.min.js", depends = "jquery"),
                @Script(id = "colorpicker", src = "js/bootstrap-colorpicker.js", depends = "bootstrap"),
                @Script(id = "slider", src = "js/bootstrap-slider.js", depends = "bootstrap"),
                @Script(id = "upload", src = "js/bootstrap-fileupload.js", depends = "bootstrap"),
                @Script(id = "openlayers", src = "js/openlayers.js", depends = "jquery"),
                @Script(id = "openlayers-ext", src = "js/openlayers-ext.js", depends = "openlayers"),
                @Script(id = "service", src = "js/services.js", depends = "jquery"),
                @Script(id = "wmsedition", src = "js/wmsedition.js", depends = "jquery"),
                @Script(id = "styleedition", src = "js/styleedition.js", depends = "jquery")})

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
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.less.Less;