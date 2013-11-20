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
 * @version 0.9
 * @since 0.9
 */

/**
 * Constellation UI map service dashboard manager.
 *
 * @type {object}
 */
CSTL.MapDashboard = {

    instance: null,

    init: function(identifier) {
        this.instance = new Dashboard({
            $root:    $('#mapDashboard'),
            loadFunc: 'Controller.generateDataList()',
            params:   {serviceId: identifier},
            onSelect:  $.proxy(this.onSelect, this)
        });
    },

    onSelect: function($elt) {
        $('[data-role="selected"]').jzLoad('MapController.selectLayer()', {
            layerAlias: $elt.data('alias'),
            providerId: $elt.data('provider')
        }, function() {
            var $selected = CSTL.MapDashboard.instance.$root.find('.selected-item');
            $selected.find('.block-header').click(function() {
                var $this = $(this);
                $this.next().slideToggle(200);
                $this.find('i').toggleClass('icon-chevron-down icon-chevron-up');
            });
            $selected.find('[title]').tooltip({delay:{show: 200}})
        });
    }
};