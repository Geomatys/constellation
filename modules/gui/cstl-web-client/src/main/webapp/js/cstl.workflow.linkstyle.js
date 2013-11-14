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
 * Constellation UI link style to data workflow manager.
 *
 * @type {object}
 */
CSTL.LinkStyleWorkflow = {

    template:
        '<div class="style" data-name="{name}" data-provider="{provider}">' +
            '<button type="button" class="btn btn-small" onclick="CSTL.LinkDataWorkflow.unlink(\'{name}\',\'{provider}\');">' +
            '<i class="icon-minus"></i>' +
            '</button>' +
            '{name}' +
            '</div>',

    $modal:        null,
    $selected:     null,
    $nextBtn:      null,
    $linkedList:   null,

    dataName:     null,
    dataProvider: null,
    styleList:      null,

    init: function() {
        // Select persistent HTML element.
        this.$modal      = $('#styleModal');
        this.$nextBtn    = this.$modal.find('#associate');

        // Handle events.
        this.$nextBtn.click($.proxy(this.validate, this));

        // Data list dashboard.
        this.styleList = new Dashboard({
            $root:    this.$modal.find('#styleList'),
            loadFunc: 'StyleController.styleList()',
            onLoad:    $.proxy(this.onStyleLoaded, this),
            onSelect:  $.proxy(this.onStyleSelect, this)
        });
    },

    setData: function(dataName, dataProvider) {
        this.dataName     = dataName;
        this.dataProvider = dataProvider;
        this.$linkedList   = $('#linkedList');
    },

    onStyleLoaded: function() {
        // Selection lost, do not allow next step
        this.$nextBtn.attr('disabled','disabled');

        // Disabled already attached items.
        this.$linkedList.find('.style').each(function() {
            var item = CSTL.LinkStyleWorkflow.styleList.$itemList.
                find('[data-provider="' + $(this).data('provider') + '"]').
                filter('[data-name="' + $(this).data('name') + '"]');
            if (item.length > 0) {
                item.addClass('disabled');
            }
        });
    },

    onStyleSelect: function($elt) {
        this.$selected = $elt;
        this.$nextBtn.removeAttr('disabled'); // the user has selected an item, allow next step
    },

    start: function() {
        // Disabled next button (the user should select a data).
        this.$nextBtn.attr('disabled','disabled');

        // Load data list.
        this.styleList.loadItems();

        // Display modal.
        this.$modal.modal('toggle')
    },

    unlink: function(styleName, styleProvider) {
        // Break association.
        CSTL.Providers.unlinkStyleFromData(
                styleProvider,
                styleName,
                this.dataProvider,
                this.dataName).

            // Update linked list.
            success($.proxy(function(){
                this.$linkedList.find('[data-provider="' + styleProvider + '"]').
                    filter('[data-name="' + styleName + '"]').remove();
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
                this.$selected.data('provider'),
                this.$selected.data('name'),
                this.dataProvider,
                this.dataName).

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