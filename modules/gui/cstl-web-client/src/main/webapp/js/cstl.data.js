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

/**
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */

/**
 * Constellation UI datas manager.
 *
 * @type {object}
 */
CSTL.Data = {

    type: ["raster, vector, sensor"],

    goTo: function ($element, title, researchtype){
        $("#dataDashboardTitle").empty();
        $("#dataDashboardTitle").append(title);
        CSTL.Data.type = researchtype;
        loadDataDashboard(0, 10, null, null, null);
        this.setActive($element.parent());
    },

    setActive : function($parent){
        var $Ul = $parent.parent();
        $Ul.children().removeClass("active")
        $parent.addClass("active")
    }
}