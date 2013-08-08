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
 * Constellation namespace and utility functions.
 *
 * @type {Object}
 */
var CSTL = {

    i18n: function(key) {
        return CSTL.msg[OpenLayers.Lang.getCode()][key];
    }
};

/**
 * Constellation message bundle registry.
 *
 * @type {Object}
 */
CSTL.msg = {};
CSTL.msg["fr"] = {
    "comparison":     "Comparaison",
    "delete":         "Supprimer",
    "edit":           "Editer",
    "logical":        "Logique",
    "no.description": "Aucune description disponible."
};
CSTL.msg["en"] = {
    "comparison":     "Comparison",
    "delete":         "Delete",
    "edit":           "Edit",
    "logical":        "Logical",
    "no.description": "No description available."
};

/**
 * Constellation UI Style namespace.
 */
CSTL.STYLE = {

    // Default style rule.
    DEFAULT_RULE: {
        name: "Change me!",
        title: "",
        description: "",
        symbolizers: [],
        minScale: 0,
        maxScale: 1.7976931348623157E308,
        filter: null
    },

    // Default symbolizer by type.
    DEFAULT_SYMBOL: {
        'point': {
            '@symbol': 'point',
            graphic: {
                size:     10,
                opacity:  1.0,
                rotation: 0,
                mark: {
                    geometry: 'circle',
                    stroke: {
                        color:   "#000000",
                        opacity: 1.0,
                        width:   2,
                        dashed:  false
                    },
                    fill: {
                        color:   "#ffffff",
                        opacity: 1.0
                    }
                }
            }
        },
        'line': {
            '@symbol': 'line',
            stroke: {
                color:   "#000000",
                opacity: 1.0,
                width:   2,
                dashed:  false
            }
        },
        'polygon': {
            '@symbol': 'polygon',
            stroke: {
                color:   "#000000",
                opacity: 1.0,
                width:   2,
                dashed:  false
            },
            fill: {
                color:     "#ffffff",
                opacity:   1.0
            }
        },
        'text': {
            '@symbol': 'text',
            label: "",
            font: {
                size: 12,
                bold: false,
                italic: false
            },
            fill: {
                color:   "#000000",
                opacity: 1.0
            }
        },
        'raster': {
            '@symbol': 'raster',
            opacity: 1.0,
            colorMap: null,
            channelSelection: {
                greyChannel: null,
                rgbChannels: null
            }
        }
    }
};

CSTL.STYLE.Workflow = {

    current: CSTL.STYLE.StyleEdition,

    validate: function() {
        this.current.validate();
        this.updateOverview();
    },

    updateOverview: function() {
        map.layers[0].params['SLD_BODY'] = $.toJSON(CSTL.STYLE.StyleEdition.getCurrent());
        map.layers[0].redraw(true);
    },

    previous: function() {
        this.current.previous();
        this.updateOverview();
    }
};

/**
 * Global style edition controller.
 */
CSTL.STYLE.StyleEdition = {

    $panel: $('[data-edition="style"]'),
    style: null,
    dataDescription: null,

    init: function() {
        this.$panel = $('[data-edition="style"]');

        // Init all panels.
        CSTL.STYLE.RuleEdition['m'].init();
        CSTL.STYLE.RuleEdition['auv'].init();
        CSTL.STYLE.RuleEdition['ai'].init();
        CSTL.STYLE.SymbolEdition['point'].init();
        CSTL.STYLE.SymbolEdition['line'].init();
        CSTL.STYLE.SymbolEdition['polygon'].init();
        CSTL.STYLE.SymbolEdition['text'].init();
        CSTL.STYLE.SymbolEdition['raster'].init();

        // Opacity sliders.
        $("[name$='.opacity']").slider({min:0,max:100,step:1});

        // Rotation sliders.
        $("[name$='.rotation']").slider({min:0,max:360,step:1});

        // Colorpickers.
        $("[name$='.color']").colorpicker({format:'hex'}).on('changeColor', function(e) {
            $(this).siblings(".color-overview").css("background-color", e.color.toHex());
        });

        // Button groups.
        $('[data-toggle="buttons-radio"] [data-toggle="buttons-checkbox"]').button();
    },

    start: function(style, dataExtent) {
        this.style = style;

        // Draw rule list.
        this.drawRules();

        // Display panel.
        this.$panel.show();

        // Zoom to data extent.
        if (dataExtent) {
            map.zoomToExtent(new OpenLayers.Bounds(dataExtent));
        } else {
            map.zoomToExtent(new OpenLayers.Bounds(-180, -90, 180, 90));
        }

        // Display overview.
        CSTL.STYLE.Workflow.updateOverview();
    },

    drawRules: function() {
        var $rules = this.$panel.find("#rules");
        $rules.empty();

        // For each rule
        for (var i = 0; i < this.style.rules.length; i++) {
            var rule = this.style.rules[i];

            // Row
            var $row = $("<div />", {
                "class": "item rule",
                "data-content": rule.description || CSTL.i18n("no.description")
            }).appendTo($rules);

            // Title
            $("<span />", {
                "class": "title",
                "text": rule.title || rule.name
            }).appendTo($row);

            // Remove link
            $("<a />", {
                "text": CSTL.i18n("delete"),
                "href": "#"
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.STYLE.StyleEdition.removeRule($parent.index());
                $parent.popover("destroy").remove();
                return false;
            }).appendTo($row);

            // Separator
            $("<span />", {
                "class": "vertical-separator"
            }).appendTo($row);

            // Edit link
            $("<a />", {
                "text": CSTL.i18n("edit"),
                "href": "#"
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.STYLE.StyleEdition.editRule($parent.index());
                return false;
            }).appendTo($row);

            // Add popover
            $row.popover({placement:"bottom", trigger:"hover", title:"Description"});
        }
    },

    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    removeRule:function(index) {
        this.style.rules.splice(index, 1);
        CSTL.STYLE.Workflow.updateOverview();
    },

    removeAllRules:function() {
        this.style.rules = [];
        this.drawRules();
        CSTL.STYLE.Workflow.updateOverview();
    },

    newRule: function(method) {
        this.hide();
        if (method === 'm') {
            CSTL.STYLE.RuleEdition['m'].start(CSTL.STYLE.DEFAULT_RULE, null);
        } else {
            CSTL.STYLE.RuleEdition[method].start();
        }
    },

    editRule: function(index) {
        this.hide();
        CSTL.STYLE.RuleEdition['m'].start(this.style.rules[index], index);
    },

    getCurrent: function() {
        var clone = $.extend(true, {}, this.style);

        // Apply rule changes if any.
        CSTL.STYLE.RuleEdition['m'].updateStyle(clone);
        CSTL.STYLE.RuleEdition['auv'].updateStyle(clone);
        CSTL.STYLE.RuleEdition['ai'].updateStyle(clone);

        return clone;
    },

    previous: function() {

    },

    validate: function() {
        var form = $('#updateForm');
        form.find('input[type=hidden]').val($.toJSON(this.style));
        form.find('input[type=submit]').trigger('click');
    }
};