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
        @Alias(of = "/org/constellation/gui/templates/style/overview.gtmpl", as = "overview.gtmpl")})

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
                @Script(id = "collapse", src = "js/bootstrap-collapse.js", depends = "jquery"),
                @Script(id = "tooltip", src = "js/bootstrap-tooltip.js", depends = "jquery"),
                @Script(id = "popover", src = "js/bootstrap-popover.js", depends = "tooltip"),
                @Script(id = "alert", src = "js/bootstrap-alert.js", depends = "jquery"),
                @Script(id = "tab", src = "js/bootstrap-tab.js", depends = "jquery"),
                @Script(id = "buttons", src = "js/bootstrap-buttons.js", depends = "jquery"),
                @Script(id = "dropdown", src = "js/bootstrap-dropdown.js", depends = "jquery"),
                @Script(id = "colorpicker", src = "js/bootstrap-colorpicker.js", depends = "jquery"),
                @Script(id = "slider", src = "js/bootstrap-slider.js", depends = "jquery"),
                @Script(id = "openlayers", src = "js/openlayers.js", depends = "jquery"),
                @Script(id = "openlayers-ext", src = "js/openlayers-ext.js", depends = "openlayers"),
                @Script(id = "service", src = "js/services.js", depends = "jquery"),
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