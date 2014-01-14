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
 * @author Fabien   Bernard (Geomatys).
 * @author Benjamin Garcia  (Geomatys).
 * @version 0.9
 * @since   0.9
 */

/**
 * Constellation UI link workflow manager.
 *
 * @type {object}
 */

CSTL.LinkWorkflow = {

    styleTemplate : '<div class="linkWorkflow" data-name="{name}" data-provider="{provider}">' +
        '<button type="button" class="btn btn-small" onclick="styleFlow.unlink(\'{name}\',\'{provider}\', dataUnlink);">' +
        '<i class="icon-minus"></i>' +
        '</button>' +
        ' {name}' +
        '</div>',

    dataTemplate:
        '<div class="data" data-name="{name}" data-provider="{provider}">' +
            '<button type="button" class="btn btn-small" onclick="dataFlow.unlink(\'{name}\',\'{provider}\', styleUnlink);">' +
            '<i class="icon-minus"></i>' +
            '</button>' +
            ' {name}' +
            '</div>',


    $modal:        null,
    $selected:     null,
    $nextBtn:      null,
    $linkedList:   null,

    selectedName:     null,
    selectedProvider: null,
    selectedNamespace: null,
    linkedList:      null,


    init: function(loadFunc, validateFunc, template) {
        // Select persistent HTML element.
        this.$modal      = $('#linkedModal');
        this.$nextBtn    = this.$modal.find('#associate');
        this.template = template;
        // Handle events.
        this.$nextBtn.click($.proxy(this.validate, this, validateFunc));

        // Data list dashboard.
        this.linkedList = new Dashboard({
            $root:      this.$modal.find('#linkedList'),
            loadFunc:   loadFunc,
            onLoad:     $.proxy(this.onElementLoaded, this),
            onSelect:   $.proxy(this.onElementSelect, this)
        });
        return this;
    },

    setSelected: function(selectedName, selectedProvider, selectedNamespace) {
        this.selectedName     = selectedName;
        this.selectedProvider = selectedProvider;
        this.selectedNamespace = selectedNamespace;
        this.$linkedList   = $('#linkedList');
    },

    onElementLoaded: function() {
        // Selection lost, do not allow next step
        this.$nextBtn.attr('disabled','disabled');

        // Disabled already attached items.
        this.$linkedList.find('.linkWorkflow').each(function() {
            var item = CSTL.LinkWorkflow.linkedList.$itemList.
                find('[data-provider="' + $(this).data('provider') + '"]').
                filter('[data-name="' + $(this).data('name') + '"]');
            if (item.length > 0) {
                item.addClass('disabled');
            }
        });
    },

    onElementSelect: function($elt) {
        this.$selected = $elt;
        this.$nextBtn.removeAttr('disabled'); // the user has selected an item, allow next step
    },

    start: function() {
        // Disabled next button (the user should select a data).
        this.$nextBtn.attr('disabled','disabled');

        // Load data list.
        this.linkedList.loadItems();

        // Display modal.
        this.$modal.modal('toggle')
    },

    unlink: function(selectedName, selectedProvider, unLinkFunc, namespace) {
        // Break association.
        unLinkFunc(selectedProvider, selectedName, this.selectedProvider, this.selectedName, namespace).
            // Update linked list.
            success($.proxy(function(){
                this.$linkedList.find('[data-provider="' + selectedProvider + '"]').
                    filter('[data-name="' + selectedName + '"]').remove();
                if (this.$linkedList.children().length === 0) {
                    this.$linkedList.html(CSTL.i18n('no-association'));
                }
            }, this));
    },

    validate: function(validateFunc, e) {
        // Do nothing if disabled.
        if ($(e.currentTarget).is(':disabled')) {
            return;
        }

        var namespace = this.selectedNamespace;
        if(this.selectedNamespace===undefined){
            namespace=this.$selected.data('namespace');
        }
        // Apply association.
        validateFunc(this.$selected.data('provider'), this.$selected.data('name'), this.selectedProvider, this.selectedName, namespace).
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