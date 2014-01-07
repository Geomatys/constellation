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
 * Constellation UI layer adding workflow manager.
 *
 * @type {object}
 */
CSTL.AddLayerWorkflow = {

    $form:       null,
    $dataList:   null,
    $dataName:   null,
    $nextBtn:    null,

    dataList:    null,

    init: function(param) {
        // Select persistent HTML element.
        this.$form      = $('#addLayerForm');
        this.$dataList  = $('#dataList');
        this.$dataName  = $('#dataName');
        this.$nextBtn   = $('#continue');

        // Event listeners.
        this.$form.find('input[name="layerAlias"]').on('keyup', this.onAliasChanged);

        // Data list dashboard.
        this.dataList = new Dashboard({
            $root:    this.$dataList,
            loadFunc: 'Controller.getAvailableData()',
            params:   param,
            onLoad:   $.proxy(this.onDataLoaded, this),
            onSelect: $.proxy(this.onDataSelect, this)
        });
    },

    onDataLoaded: function() {
        this.$nextBtn.attr('disabled','disabled'); // selection lost, do not allow next step
    },

    onDataSelect: function($elt) {
        this.$form.find('input[name="dataName"]').val($elt.data('name'));
        this.$form.find('input[name="dataProvider"]').val($elt.data('provider'));
        this.$nextBtn.removeAttr('disabled'); // the user has selected an item, allow next step
    },

    onAliasChanged: function() {
        if ($(this).val() === '') {
            CSTL.AddLayerWorkflow.$nextBtn.attr('disabled','disabled');
        } else {
            CSTL.AddLayerWorkflow.$nextBtn.removeAttr('disabled');
        }
    },

    start: function() {
        // Reset form.
        this.$form.find('input[name="dataName"]').val('');
        this.$form.find('input[name="dataProvider"]').val('');
        this.$form.find('input[name="layerAlias"]').val('');

        // Disabled next button (the user should select a data).
        this.$nextBtn.attr('disabled','disabled');

        // Load data list.
        this.dataList.loadItems();

        // Show first workflow panel, hide others.
        $("#dataList").fadeIn("slow");
        $("#dataName").fadeOut("slow", function(){$(this).hide()});
    },

    next: function($clicked) {
        // Do nothing if disabled.
        if ($clicked.is(':disabled')) {
            return;
        }

        // Disabled next button.
        this.$nextBtn.attr('disabled','disabled');

        // Go to next step.
        if (this.$dataList.is(':visible')) {
            $("#dataList").fadeToggle(function() {
                $(this).hide();
                $("#dataName").fadeToggle();
            });
        } else if (this.$dataName.is(':visible')) {
            this.$form.submit();
        }
    }
};