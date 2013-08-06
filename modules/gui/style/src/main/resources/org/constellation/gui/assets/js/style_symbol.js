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
 * Symbolizer edition namespace.
 */
CSTL.STYLE.SymbolEdition = {

    initFormStroke: function($form, stroke) {
        $form.find("[name$='stroke.color']").val(stroke.color).colorpicker('setValue', stroke.color);
        $form.find("[name$='stroke.width']").val(stroke.width);
        $form.find("[name$='stroke.opacity']").val(stroke.opacity * 100).slider('setValue', stroke.opacity * 100);
        $form.find("[name$='stroke.dashed']").removeClass("active");
        $form.find("[name$='stroke.continuous']").removeClass("active");
        if (stroke.dashed == true) {
            $form.find("[name$='stroke.dashed']").addClass("active")
        } else {
            $form.find("[name$='stroke.continuous']").addClass("active")
        }
    },

    initFormFill: function($form, fill) {
        $form.find("[name$='fill.color']").val(fill.color).colorpicker('setValue', fill.color);
        $form.find("[name$='fill.opacity']").val(fill.opacity * 100).slider('setValue', fill.opacity * 100);
    },

    initFormMark: function($form, mark) {
        $form.find("[name$='mark.geometry']").val(mark.geometry);
        this.initFormStroke($form, mark.stroke || {});
        this.initFormFill($form, mark.fill || {});
    },

    initFormGraphic: function($form, graphic) {
        $form.find("[name$='graphic.size']").val(graphic.size);
        $form.find("[name$='graphic.opacity']").val(graphic.opacity * 100).slider('setValue', graphic.opacity * 100);
        $form.find("[name$='graphic.rotation']").val(graphic.rotation).slider('setValue', graphic.rotation);
        this.initFormMark($form, graphic.mark || {});
    },

    initFormFont: function($form, font) {
        $form.find("[name$='font.size']").val(font.size);
        $form.find("[name$='font.bold']").removeClass("active");
        $form.find("[name$='font.italic']").removeClass("active");
        if (font.bold) {
            $form.find("[name$='font.bold']").addClass("active");
        }
        if (font.italic == true) {
            $form.find("[name$='font.italic']").addClass("active");
        }
    },

    readFormStroke: function($form) {
        return {
            color:   $form.find("[name$='stroke.color']").val(),
            width:   $form.find("[name$='stroke.width']").val(),
            opacity: $form.find("[name$='stroke.opacity']").val() / 100,
            dashed:  $form.find("[name$='stroke.dashed']").hasClass("active")
        }
    },

    readFormFill: function($form) {
        return {
            color:   $form.find("[name$='fill.color']").val(),
            opacity: $form.find("[name$='fill.opacity']").val() / 100
        }
    },

    readFormMark: function($form) {
        return {
            geometry: $form.find("[name$='mark.geometry']").val(),
            stroke:   this.readFormStroke($form),
            fill:     this.readFormFill($form)
        }
    },

    readFormGraphic: function($form) {
        return {
            size:     $form.find("[name$='graphic.size']").val(),
            opacity:  $form.find("[name$='graphic.opacity']").val() / 100,
            rotation: $form.find("[name$='graphic.rotation']").val(),
            mark:     this.readFormMark($form)
        }
    },

    readFormFont: function($form) {
        return {
            size:   $form.find("[name$='font.size']").val(),
            bold:   $form.find("[name$='font.bold']").hasClass("active"),
            italic: $form.find("[name$='font.italic']").hasClass("active")
        }
    }
};


/******************************************************************************
 *                             Line symbolizer                                *
 ******************************************************************************/

/**
 * Line symbolizer edition controller.
 */
CSTL.STYLE.SymbolEdition['line'] = {

    $panel: null,
    symbol: null,
    symbolIndex: null,

    init: function() {
        this.$panel = $('[data-edition="symbol-line"]');
    },

    start: function(symbol, symbolIndex) {
        this.symbol = $.extend(true, {}, symbol); // clone
        this.symbolIndex = symbolIndex;

        // Set form values.
        CSTL.STYLE.SymbolEdition.initFormStroke(this.$panel, this.symbol.stroke);

        // Display panel.
        this.show();
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    updateRule: function(rule) {
        if (this.symbol != null) {
            // Apply form values.
            this.symbol.stroke = CSTL.STYLE.SymbolEdition.readFormStroke(this.$panel);

            // Update parent rule.
            if (this.symbolIndex != null) {
                rule.symbolizers[this.symbolIndex] = this.symbol;
            } else {
                rule.symbolizers.push(this.symbol);
            }
        }
    },

    previous: function() {
        this.symbol = null;
        this.symbolIndex = null;

        // Redraw the symbolizer list.
        CSTL.STYLE.RuleEdition['m'].drawSymbols();

        // Return to manual rule edition panel.
        this.hide();
        CSTL.STYLE.RuleEdition['m'].show();
    },

    validate: function() {
        // Apply the edition on parent rule.
        this.updateRule(CSTL.STYLE.RuleEdition['m'].rule);

        // Return to manual rule edition.
        this.previous();
    }
};


/******************************************************************************
 *                           Polygon symbolizer                                *
 ******************************************************************************/

/**
 * Polygon symbolizer edition controller.
 */
CSTL.STYLE.SymbolEdition['polygon'] = {

    $panel: null,
    symbol: null,
    symbolIndex: null,

    init: function() {
        this.$panel = $('[data-edition="symbol-polygon"]');
    },

    start: function(symbol, symbolIndex) {
        this.symbol = $.extend(true, {}, symbol); // clone
        this.symbolIndex = symbolIndex;

        // Set form values.
        CSTL.STYLE.SymbolEdition.initFormStroke(this.$panel, this.symbol.stroke);
        CSTL.STYLE.SymbolEdition.initFormFill(this.$panel, this.symbol.fill);

        // Display panel.
        this.show();
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    updateRule: function(rule) {
        if (this.symbol != null) {
            // Apply form values.
            this.symbol.stroke = CSTL.STYLE.SymbolEdition.readFormStroke(this.$panel);
            this.symbol.fill   = CSTL.STYLE.SymbolEdition.readFormFill(this.$panel);
            
            // Update parent rule.
            if (this.symbolIndex != null) {
                rule.symbolizers[this.symbolIndex] = this.symbol;
            } else {
                rule.symbolizers.push(this.symbol);
            }
        }
    },

    previous: function() {
        this.symbol = null;
        this.symbolIndex = null;

        // Redraw the symbolizer list.
        CSTL.STYLE.RuleEdition['m'].drawSymbols();

        // Return to manual rule edition panel.
        this.hide();
        CSTL.STYLE.RuleEdition['m'].show();
    },

    validate: function() {
        // Apply the edition on parent rule.
        this.updateRule(CSTL.STYLE.RuleEdition['m'].rule);

        // Return to manual rule edition.
        this.previous();
    }
};


/******************************************************************************
 *                            Point symbolizer                                *
 ******************************************************************************/

/**
 * Point symbolizer edition controller.
 */
CSTL.STYLE.SymbolEdition['point'] = {

    $panel: null,
    symbol: null,
    symbolIndex: null,

    init: function() {
        this.$panel = $('[data-edition="symbol-point"]');
    },

    start: function(symbol, symbolIndex) {
        this.symbol = $.extend(true, {}, symbol); // clone
        this.symbolIndex = symbolIndex;

        // Set form values.
        CSTL.STYLE.SymbolEdition.initFormGraphic(this.$panel, this.symbol.graphic);

        // Display panel.
        this.show();
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    updateRule: function(rule) {
        if (this.symbol != null) {
            // Apply form values.
            this.symbol.graphic = CSTL.STYLE.SymbolEdition.readFormGraphic(this.$panel);

            // Update parent rule.
            if (this.symbolIndex != null) {
                rule.symbolizers[this.symbolIndex] = this.symbol;
            } else {
                rule.symbolizers.push(this.symbol);
            }
        }
    },

    previous: function() {
        this.symbol = null;
        this.symbolIndex = null;

        // Redraw the symbolizer list.
        CSTL.STYLE.RuleEdition['m'].drawSymbols();

        // Return to manual rule edition panel.
        this.hide();
        CSTL.STYLE.RuleEdition['m'].show();
    },

    validate: function() {
        // Apply the edition on parent rule.
        this.updateRule(CSTL.STYLE.RuleEdition['m'].rule);

        // Return to manual rule edition.
        this.previous();
    }
};


/******************************************************************************
 *                             Text symbolizer                                *
 ******************************************************************************/

/**
 * Text symbolizer edition controller.
 */
CSTL.STYLE.SymbolEdition['text'] = {

    $panel: null,
    symbol: null,
    symbolIndex: null,

    init: function() {
        this.$panel = $('[data-edition="symbol-text"]');
    },

    start: function(symbol, symbolIndex) {
        this.symbol = $.extend(true, {}, symbol); // clone
        this.symbolIndex = symbolIndex;

        // Set form values.
        this.$panel.find("[name='label']").val(this.symbol.label);
        CSTL.STYLE.SymbolEdition.initFormFont(this.$panel, this.symbol.font);
        CSTL.STYLE.SymbolEdition.initFormFill(this.$panel, this.symbol.fill);

        // Display panel.
        this.show();
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    updateRule: function(rule) {
        if (this.symbol != null) {
            // Apply form values.
            this.symbol.label = this.$panel.find("[name='label']").val();
            this.symbol.font  = CSTL.STYLE.SymbolEdition.readFormFont(this.$panel);
            this.symbol.fill  = CSTL.STYLE.SymbolEdition.readFormFill(this.$panel);

            // Update parent rule.
            if (this.symbolIndex != null) {
                rule.symbolizers[this.symbolIndex] = this.symbol;
            } else {
                rule.symbolizers.push(this.symbol);
            }
        }
    },

    previous: function() {
        this.symbol = null;
        this.symbolIndex = null;

        // Redraw the symbolizer list.
        CSTL.STYLE.RuleEdition['m'].drawSymbols();

        // Return to manual rule edition panel.
        this.hide();
        CSTL.STYLE.RuleEdition['m'].show();
    },

    validate: function() {
        // Apply the edition on parent rule.
        this.updateRule(CSTL.STYLE.RuleEdition['m'].rule);

        // Return to manual rule edition.
        this.previous();
    }
};


/******************************************************************************
 *                            Raster symbolizer                               *
 ******************************************************************************/

/**
 * Raster symbolizer edition controller.
 */
CSTL.STYLE.SymbolEdition['raster'] = {

    $panel: null,
    symbol: null,
    symbolIndex: null,

    init: function() {
        this.$panel = $('[data-edition="symbol-raster"]');
    },

    start: function(symbol, symbolIndex) {
        this.symbol = $.extend(true, {}, symbol); // clone
        this.symbolIndex = symbolIndex;

        // Set form values.
        if (this.symbol.channelSelection && this.symbol.channelSelection.rgbChannels) {
            this.showRGBSelection();
            this.$panel.find("[name='select.channel']").val("rgb");
            this.$panel.find("[name='channel.red.name']").val(this.symbol.channelSelection.rgbChannels[0].name);
            this.$panel.find("[name='channel.green.name']").val(this.symbol.channelSelection.rgbChannels[1].name);
            this.$panel.find("[name='channel.blue.name']").val(this.symbol.channelSelection.rgbChannels[2].name);
        } else if(this.symbol.channelSelection.greyChannel) {
            this.showGreySelection();
            this.$panel.find("[name='select.channel']").val("grey");
            this.$panel.find("[name='channel.grey.name']").val(this.symbol.channelSelection.greyChannel.name);
        } else {
            this.showGreySelection();
        }

        // Display panel.
        this.show();
    },

    showGreySelection: function() {
        this.$panel.find(".panel-grey").removeClass("hide");
        this.$panel.find(".panel-rgb").addClass("hide");
    },

    showRGBSelection: function() {
        this.$panel.find(".panel-rgb").removeClass("hide");
        this.$panel.find(".panel-grey").addClass("hide");
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    updateRule: function(rule) {
        if (this.symbol != null) {
            // Apply form values.
            this.symbol.channelSelection = {};
            if ($("[name='select.channel']").val() === "rgb") {
                this.symbol.colorMap = null;
                this.symbol.channelSelection.rgbChannels = [];
                this.symbol.channelSelection.rgbChannels[0] = {};
                this.symbol.channelSelection.rgbChannels[0].name = this.$panel.find("[name='channel.red.name']").val();
                this.symbol.channelSelection.rgbChannels[1] = {};
                this.symbol.channelSelection.rgbChannels[1].name = this.$panel.find("[name='channel.green.name']").val();
                this.symbol.channelSelection.rgbChannels[2] = {};
                this.symbol.channelSelection.rgbChannels[2].name = this.$panel.find("[name='channel.blue.name']").val();
            } else {
                this.symbol.channelSelection.greyChannel = {};
                this.symbol.channelSelection.greyChannel.name = this.$panel.find("[name='channel.grey.name']").val();
            }

            // Update parent rule.
            if (this.symbolIndex != null) {
                rule.symbolizers[this.symbolIndex] = this.symbol;
            } else {
                rule.symbolizers.push(this.symbol);
            }
        }
    },

    classify: function(providerId, layerName) {
        var url = this.$panel.find('[data-action="classify"]').html();
        $.ajax({
            url: url,
            data: {
                providerId: providerId,
                layerName: layerName,
                bandIndex: this.$panel.find("[name='channel.grey.name']").val(),
                nbIntervals: this.$panel.find("[name='nbIntervals']").val()
            }
        }).success(function(data) {
            var content = data.match(/<body(.|\s)*?\/body>/g)[0];
            CSTL.STYLE.SymbolEdition['raster'].symbol.colorMap = $.parseJSON(content.substring(6, content.length - 7));
        });
    },

    previous: function() {
        this.symbol = null;
        this.symbolIndex = null;

        // Redraw the symbolizer list.
        CSTL.STYLE.RuleEdition['m'].drawSymbols();

        // Return to manual rule edition panel.
        this.hide();
        CSTL.STYLE.RuleEdition['m'].show();
    },

    validate: function() {
        // Apply the edition on parent rule.
        this.updateRule(CSTL.STYLE.RuleEdition['m'].rule);

        // Return to manual rule edition.
        this.previous();
    }
};