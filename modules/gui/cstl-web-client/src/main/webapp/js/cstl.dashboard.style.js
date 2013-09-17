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
 * Constellation UI style dashboard manager.
 *
 * @type {object}
 */
CSTL.StyleDashboard = {

    instance: null,

    init: function() {
        this.instance = new Dashboard({
            loadFunc: 'StyleController.styleList()',
            $root:     $('#styleDashboard'),
            onLoad:    $.proxy(this.onLoad, this),
            onSelect:  $.proxy(this.onSelect, this)
        });
    },

    onLoad: function() {
        // Check if there is a selected item.
        var $selected = this.instance.$root.find('.selected-item');
        if ($selected.length != 0) {

            // There is a selected item, check if this item exists in the new list.
            var $item = this.instance.$itemList.
                find('[data-style="' + $selected.data('style') + '"]').
                filter('[data-provider="' + $selected.data('provider') + '"]');

            // If item exists in new list keep it selected otherwise cancel it.
            $item.length > 0 ? $item.addClass('selected') : $selected.remove();
        }
    },

    onSelect: function($elt) {
        $('[data-role="selected"]').jzLoad('StyleController.selectStyle()', {
            name:       $elt.data('style'),
            providerId: $elt.data('provider')
        }, function() {
            var $selected = CSTL.StyleDashboard.instance.$root.find('.selected-item');
            $selected.find('.block-header').click(function() {
                var $this = $(this);
                $this.next().slideToggle(200);
                $this.find('i').toggleClass('icon-chevron-down icon-chevron-up');
            });
            $selected.find('[title]').tooltip({delay:{show: 200}})
        });
    },

    removeStyle: function(style, provider) {
        if (confirm(CSTL.i18n('confirm-delete-style'))) {
            CSTL.Providers.deleteStyle(provider, style);
        }
        this.instance.fromIndex(0);
    }
};