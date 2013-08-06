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
 * Rule edition namespace.
 */
CSTL.STYLE.RuleEdition = {};


/******************************************************************************
 *                              Manual edition                                *
 ******************************************************************************/

/**
 * Manual rule edition controller.
 */
CSTL.STYLE.RuleEdition['m'] = {

    $panel: null,
    rule: null,
    ruleIndex: null,

    init: function() {
        this.$panel = $('[data-edition="rule-m"]');
    },

    start: function(rule, ruleIndex) {
        this.rule = $.extend(true, {}, rule); // clone
        this.ruleIndex = ruleIndex;

        // Display first tab.
        this.$panel.find('[href="#tab1"]').trigger('click');

        // Set form values.
        this.$panel.find("[name='name']").val(this.rule.name);
        this.$panel.find("[name='title']").val(this.rule.title);
        this.$panel.find("[name='description']").val(this.rule.description);
        this.$panel.find("[name='minScale']").val(this.rule.minScale);
        this.$panel.find("[name='maxScale']").val(this.rule.maxScale);
        CSTL.STYLE.FilterEdition.init(rule.filter);

        // Draw symbolizer list.
        this.drawSymbols();

        // Display panel.
        this.show();
    },

    drawSymbols: function() {
        var $symbols = this.$panel.find("#symbolizers");
        $symbols.empty();

        // For each symbolizer.
        for (var i = 0; i < this.rule.symbolizers.length; i++) {
            var symbolizer = this.rule.symbolizers[i];

            // Row.
            var $row = $("<div />", {
                "class": "item symbolizer"
            }).appendTo($symbols);

            // Icon.
            $("<div />", {
                "class": "symbol-" + symbolizer['@symbol']
            }).appendTo($row);

            // Remove link.
            $("<a />", {
                "text": CSTL.i18n("delete"),
                "href": "#"
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.STYLE.RuleEdition['m'].removeSymbol($parent.index());
                $(this).parent().remove();
                return false;
            }).appendTo($row);

            // Separator.
            $("<span />", {
                "class": "vertical-separator"
            }).appendTo($row);

            // Edit link.
            $("<a />", {
                "text": CSTL.i18n("edit"),
                "href": "#"
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.STYLE.RuleEdition['m'].editSymbol($parent.index());
                return false;
            }).appendTo($row);
        }
    },
    
    show: function() {
        this.$panel.show();
        CSTL.STYLE.Workflow.current = this;
    },

    hide: function() {
        this.$panel.hide();
    },

    removeSymbol: function(index) {
        this.rule.symbolizers.splice(index, 1);
        CSTL.STYLE.Workflow.updateOverview();
    },

    removeAllSymbols: function() {
        this.rule.symbolizers = [];
        this.drawSymbols();
        CSTL.STYLE.Workflow.updateOverview();
    },

    newSymbol: function(type) {
        this.hide();
        var symbol = CSTL.STYLE.DEFAULT_SYMBOL[type];
        CSTL.STYLE.SymbolEdition[type].start(symbol, null);
    },

    editSymbol: function(index) {
        this.hide();
        var symbol = this.rule.symbolizers[index];
        var type   = symbol['@symbol'];
        CSTL.STYLE.SymbolEdition[type].start(symbol, index);
    },

    updateStyle: function(style) {
        if (this.rule != null) {
            // Update local rule.
            this.rule.name        = this.$panel.find("[name='name']").val();
            this.rule.title       = this.$panel.find("[name='title']").val();
            this.rule.description = this.$panel.find("[name='description']").val();
            this.rule.minScale    = this.$panel.find("[name='minScale']").val();
            this.rule.maxScale    = this.$panel.find("[name='maxScale']").val();
            this.rule.filter      = CSTL.STYLE.FilterEdition.build();

            // Clone rule.
            var clone = $.extend(true, {}, this.rule);

            // Apply symbolizer changes if any.
            CSTL.STYLE.SymbolEdition['point'].updateRule(clone);
            CSTL.STYLE.SymbolEdition['line'].updateRule(clone);
            CSTL.STYLE.SymbolEdition['polygon'].updateRule(clone);
            CSTL.STYLE.SymbolEdition['text'].updateRule(clone);
            CSTL.STYLE.SymbolEdition['raster'].updateRule(clone);

            // Update parent style.
            if (this.ruleIndex != null) {
                style.rules[this.ruleIndex] = clone;
            } else {
                style.rules.push(clone);
            }
        }
    },

    previous: function() {
        this.rule = null;
        this.ruleIndex = null;

        // Redraw the rule list.
        CSTL.STYLE.StyleEdition.drawRules();

        // Return to style edition panel.
        this.hide();
        CSTL.STYLE.StyleEdition.show();
    },

    validate: function() {
        // Apply the edition on parent style.
        this.updateStyle(CSTL.STYLE.StyleEdition.style);

        // Return to style edition.
        this.previous();
    }
};


/******************************************************************************
 *                      Automatic by unique values edition                    *
 ******************************************************************************/

/**
 * Automatic by unique values rule edition controller.
 */
CSTL.STYLE.RuleEdition['auv'] = {

    $panel: null,

    init: function() {
        this.$panel = $('[data-edition="rule-auv"]');
    },

    updateStyle: function(style) {

    }
};


/******************************************************************************
 *                         Automatic by intervals edition                     *
 ******************************************************************************/

/**
 * Automatic by intervals rule edition controller.
 */
CSTL.STYLE.RuleEdition['ai'] = {

    $panel: null,

    init: function() {
        this.$panel = $('[data-edition="rule-ai"]');
    },

    updateStyle: function(style) {

    }
};