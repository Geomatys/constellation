/*
 * Copyright 2013 eXo Platform SAS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


@juzu.Application(defaultController = Controller.class, resourceAliases = {
        @Alias(of = "/org/constellation/gui/templates/commonIndex.gtmpl", as = "commonIndex.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/menu.gtmpl", as = "menu.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmsdescription.gtmpl", as = "wmsdescription.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmsmetadata.gtmpl", as = "wmsmetadata.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmssuccess.gtmpl", as = "wmssuccess.gtmpl"),
        @Alias(of = "/org/constellation/gui/templates/wmscreate.gtmpl", as = "wmscreate.gtmpl")})

@juzu.plugin.servlet.Servlet(value = "/", resourceBundle = "locale.cstl")

@Less(value = "bootstrap/bootstrap.less", minify = true)

@Assets(stylesheets = @Stylesheet(src = "bootstrap/bootstrap.css"),
        scripts = {@Script(id = "jQuery", src = "js/jquery-2.0.0.js"),
                @Script(id = "collapse", src = "js/bootstrap-collapse.js", depends = "jQuery"),
                @Script(id = "tooltip", src = "js/bootstrap-tooltip.js", depends = "jQuery"),
                @Script(id = "alert", src = "js/bootstrap-alert.js", depends = "jQuery"),
                @Script(id = "dropdown", src = "js/bootstrap-dropdown.js", depends = "jQuery")}) package org.constellation.gui;

import juzu.Alias;
import juzu.plugin.asset.Assets;
import juzu.plugin.asset.Script;
import juzu.plugin.asset.Stylesheet;
import juzu.plugin.less.Less;