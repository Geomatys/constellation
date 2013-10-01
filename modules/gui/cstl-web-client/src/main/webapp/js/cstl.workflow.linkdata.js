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
 * Constellation UI link style to data workflow manager.
 *
 * @type {object}
 */
CSTL.LinkDataWorkflow = {

    template:
        '<div class="data" data-name="{name}" data-provider="{provider}">' +
            '<button type="button" class="btn btn-small" onclick="CSTL.LinkDataWorkflow.unlink(\'{name}\',\'{provider}\');">' +
                '<i class="icon-minus"></i>' +
            '</button>' +
            '{name}' +
        '</div>',

    $modal:        null,
    $selected:     null,
    $nextBtn:      null,
    $linkedList:   null,

    styleName:     null,
    styleProvider: null,
    dataList:      null,

    init: function() {
        // Select persistent HTML element.
        this.$modal      = $('#dataModal');
        this.$nextBtn    = this.$modal.find('#associate');

        // Handle events.
        this.$nextBtn.click($.proxy(this.validate, this));

        // Data list dashboard.
        this.dataList = new Dashboard({
            $root:    this.$modal.find('#dataList'),
            loadFunc: 'Controller.getAvailableData()',
            params:   {'dataTypes':['raster','vector']},
            onLoad:    $.proxy(this.onDataLoaded, this),
            onSelect:  $.proxy(this.onDataSelect, this)
        });
    },

    setStyle: function(styleName, styleProvider) {
        this.styleName     = styleName;
        this.styleProvider = styleProvider;
        this.$linkedList   = $('#linkedList');
    },

    onDataLoaded: function() {
        // Selection lost, do not allow next step
        this.$nextBtn.attr('disabled','disabled');

        // Disabled already attached items.
        this.$linkedList.find('.data').each(function() {
            var item = CSTL.LinkDataWorkflow.dataList.$itemList.
                find('[data-provider="' + $(this).data('provider') + '"]').
                filter('[data-name="' + $(this).data('name') + '"]');
            if (item.length > 0) {
                item.addClass('disabled');
            }
        });
    },

    onDataSelect: function($elt) {
        this.$selected = $elt;
        this.$nextBtn.removeAttr('disabled'); // the user has selected an item, allow next step
    },

    start: function() {
        // Disabled next button (the user should select a data).
        this.$nextBtn.attr('disabled','disabled');

        // Load data list.
        this.dataList.loadItems();

        // Display modal.
        this.$modal.modal('toggle')
    },

    unlink: function(dataName, dataProvider) {
        // Break association.
        CSTL.Providers.unlinkStyleFromData(
                this.styleProvider,
                this.styleName,
                dataProvider,
                dataName).

            // Update linked list.
            success($.proxy(function(){
                this.$linkedList.find('[data-provider="' + dataProvider + '"]').
                    filter('[data-name="' + dataName + '"]').remove();
                if (this.$linkedList.children().length === 0) {
                    this.$linkedList.html(CSTL.i18n('no-association'));
                }
            }, this));
    },

    validate: function(e) {
        // Do nothing if disabled.
        if ($(e.currentTarget).is(':disabled')) {
            return;
        }

        // Apply association.
        CSTL.Providers.linkStyleToData(
                this.styleProvider,
                this.styleName,
                this.$selected.data('provider'),
                this.$selected.data('name')).

            // Update linked list.
            success($.proxy(function(){
                if (this.$linkedList.children().length === 0) {
                    this.$linkedList.empty();
                }
                var html = this.template.
                    replace(/\{name\}/g, this.$selected.data('name')).
                    replace(/\{provider\}/g, this.$selected.data('provider'));
                this.$linkedList.append(html);
            }, this));
    }
};