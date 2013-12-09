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
 * @author Fabien Bernard (Geomatys).
 * @author Benjamin Garcia (Geomatys).
 * @version 0.9
 * @since 0.9
 */

/**
 * Constellation UI data dashboard manager.
 *
 * @type {object}
 */
CSTL.DataDashboard = {

    instance: null,

    init: function() {
        this.instance = new Dashboard({
            $root:    $('#dataDashboard'),
            loadFunc: 'Controller.getAvailableData()',
            params:   {'dataTypes':'vector'},
            onSelect:  $.proxy(this.onSelect, this)
        });
        $('#dataTypesNav').find('a').on('click', $.proxy(this.goTo, this));
    },

    goTo: function (e) {
        var $elt = $(e.currentTarget);
        var type = $elt.data('type');

        this.instance.params['dataTypes'] = type;
        this.instance.fromIndex(0);
        $elt.parent().addClass("active").siblings().removeClass("active");
        $("#dataDashboardTitle").html(CSTL.i18n("data-" + type));
    },


    showServerFileModal: function(){
        var $first = $("[data-panel='1']");
        hideAll($first);
        $first.jzLoad("DataController.getDataFolders()",{"path":"root"}, function(){
            $("#first").find("a").on("click", {parent : $first}, updateChild);
            $("#folderPart").show();
            $("#nextbutton").show();
            $("#serverFileModal").modal({
                backdrop:true
            });
        });

    },

    onSelect: function($elt) {
        $('[data-role="selected"]').jzLoad('DataController.selectData()', {
            name:       $elt.data('name'),
            namespace:  $elt.data('namespace'),
            providerId: $elt.data('provider')
        }, function() {
            var $selected = CSTL.DataDashboard.instance.$root.find('.selected-item');
            $selected.find('.block-header').click(function() {
                var $this = $(this);
                $this.next().slideToggle(200);
                $this.find('i').toggleClass('icon-chevron-down icon-chevron-up');
            });
            $selected.find('[title]').tooltip({delay:{show: 200}})
        });
    },
};