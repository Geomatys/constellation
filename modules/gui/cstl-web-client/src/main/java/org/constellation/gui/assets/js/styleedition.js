/**
 * Constellation namespace and utility functions.
 *
 * @type {Object}
 */
var CSTL = {

    _locale: 'fr',

    init: function(locale) {
        this._locale = locale;
    },

    i18n: function(key) {
        return CSTL.msg[this._locale][key];
    }
};

/**
 * Constellation message bundle registry.
 *
 * @type {Object}
 */
CSTL.msg = {};
CSTL.msg["fr"] = {
    "edit":           "Editer",
    "no.description": "Aucune description disponible.",
    "delete":         "Supprimer"
}
CSTL.msg["en"] = {
    "edit":           "Edit",
    "no.description": "No description available.",
    "delete":         "Delete"
}

/**
 * JavaScript controller for StyledLayerDescriptor edition pages.
 *
 * @type {Object}
 */
CSTL.SldWorkflow = {

    // Local storage keys.
    LOCAL_STYLE_KEY:       "cstl_local_style",
    LOCAL_RULE_KEY:        "cstl_local_rule",
    LOCAL_SYMBOLIZER_KEY:  "cstl_local_symbolizer",

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

    // Default symbolizers.
    DEFAULT_LINE_SYMBOL: {
        '@symbol': 'line',
        stroke: {
            color:   "#000000",
            opacity: 1.0,
            width:   2,
            dashed:  false
        }
    },
    DEFAULT_POLYGON_SYMBOL: {
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
    DEFAULT_POINT_SYMBOL: {
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
    DEFAULT_TEXT_SYMBOL: {

    },

    updateStyle: function(url, providerId, styleName) {
        $.ajax(url, {
            method: 'POST',
            data:{
                providerId: providerId,
                styleName:  styleName,
                style:      localStorage.getItem(this.LOCAL_STYLE_KEY)
            }
        });
    },

    getStyle: function() {
        return $.parseJSON(localStorage.getItem(this.LOCAL_STYLE_KEY));
    },

    setStyle: function(style) {
        localStorage.setItem(this.LOCAL_STYLE_KEY, $.toJSON(style));
    },

    clearStyle: function() {
        localStorage.removeItem(this.LOCAL_STYLE_KEY);
    },

    getRule: function() {
        return $.parseJSON(localStorage.getItem(this.LOCAL_RULE_KEY));
    },

    setRule: function(rule) {
        localStorage.setItem(this.LOCAL_RULE_KEY, $.toJSON(rule));
    },

    clearRule: function() {
        localStorage.removeItem(this.LOCAL_RULE_KEY);
    },

    getSymbolizer: function() {
        return $.parseJSON(localStorage.getItem(this.LOCAL_SYMBOLIZER_KEY));
    },

    setSymbolizer: function(symbolizer) {
        localStorage.setItem(this.LOCAL_SYMBOLIZER_KEY, $.toJSON(symbolizer));
    },

    clearSymbolizer: function() {
        localStorage.removeItem(this.LOCAL_SYMBOLIZER_KEY);
    }
};

/**
 * JavaScript controller for "style.gtmpl" page.
 *
 * @type {Object}
 */
CSTL.StyleEdition = {

    // The current style.
    _style: null,

    /**
     * Initializes the style edition.
     *
     * @param style - {JSON} the style JSON representation
     */
    init: function(style) {
        if (style) {
            this._style = style;
        } else {
            this._style = CSTL.SldWorkflow.getStyle();
        }
        this._drawRules();
    },

    /**
     * Draw the list of rules.
     *
     * @private
     */
    _drawRules: function() {
        var $rules = $("#rules");

        // Action URL.
        var ruleEditUrl = $("[data-action='edit-rule']").data("url");

        // For each rule
        for (var i = 0; i < this._style.rules.length; i++) {
            var rule = this._style.rules[i];

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
                "href": "#",
                "text": CSTL.i18n("delete")
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.StyleEdition.removeRule($parent.index());
                $parent.popover("destroy").remove();
                return false;
            }).appendTo($row);

            // Separator
            $("<span />", {
                "class": "vertical-separator"
            }).appendTo($row);

            // Edit link
            $("<a />", {
                "href": ruleEditUrl + "&ruleIndex=" + i,
                "text": CSTL.i18n("edit")
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.StyleEdition.editRule($parent.index());
            }).appendTo($row);

            // Add popover
            $row.popover({placement:"bottom", trigger:"hover", title:"Description"});
        }
    },

    /**
     * Removes a rule from the rule list.
     *
     * @param index - {Integer} the rule index
     */
    removeRule: function(index) {
        this._style.rules.splice(index, 1);
        CSTL.SldWorkflow.setStyle(this._style);
    },

    /**
     * Removes all rules from the rule list.
     */
    removeAllRules: function() {
        this._style.rules = [];
        $("#rules").empty();
        CSTL.SldWorkflow.setStyle(this._style);
    },

    /**
     * Prepares a new rule edition.
     */
    newRule: function() {
        CSTL.SldWorkflow.setStyle(this._style);
        CSTL.SldWorkflow.setRule(CSTL.SldWorkflow.DEFAULT_RULE);
    },

    /**
     * Prepares an existing rule edition.
     *
     * @param index - {Integer} the rule index
     */
    editRule: function(index) {
        // Set storage for next step.
        CSTL.SldWorkflow.setStyle(this._style);
        CSTL.SldWorkflow.setRule(this._style.rules[index]);
    },

    /**
     * Validates the current edition.
     */
    validate: function() {
        // TODO: update and submit the style

        // Clear local storage.
        CSTL.SldWorkflow.clearStyle();
        CSTL.SldWorkflow.clearRule();
        CSTL.SldWorkflow.clearSymbolizer();
    },

    /**
     * Cancels the current edition.
     */
    cancel: function() {
        // Clear local storage.
        CSTL.SldWorkflow.clearStyle();
        CSTL.SldWorkflow.clearRule();
        CSTL.SldWorkflow.clearSymbolizer();
    }
};

/**
 * JavaScript controller for "ruleedition.gtpml" page.
 *
 * @type {Object}
 */
CSTL.RuleEdition = {

    // The current edited rule.
    _rule: null,

    // The edited rule index.
    _ruleIndex: null,

    /**
     * Initializes the "ruleedition.gtpml" page.
     */
    init: function(ruleIndex) {
        this._rule      = CSTL.SldWorkflow.getRule();
        this._ruleIndex = ruleIndex;
        this._drawSymbols();

        // Initializes filter edition.
        CSTL.AttributesFilter.init(this._rule.filter);

        // Set form values.
        $("[name='name']").val(this._rule.name);
        $("[name='title']").val(this._rule.title);
        $("[name='description']").val(this._rule.description);
        $("[name='minScale']").val(this._rule.minScale);
        $("[name='maxScale']").val(this._rule.maxScale);
    },

    /**
     * Draw the list of rules.
     *
     * @private
     */
    _drawSymbols: function() {
        var symbolizers = $("#symbolizers");

        // Action URL.
        var symbolEditUrl = $("[data-action='edit-symbol']").data("url");

        // For each symbolizer.
        for (var i = 0; i < this._rule.symbolizers.length; i++) {
            var symbolizer = this._rule.symbolizers[i];

            // Row.
            var $row = $("<div />", {
                "class": "item symbolizer"
            }).appendTo(symbolizers);

            // Icon.
            $("<div />", {
                "class": "symbol-" + symbolizer['@symbol']
            }).appendTo($row);

            // Remove link.
            $("<a />", {
                "text": CSTL.i18n("delete")
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.RuleEdition.removeSymbol($parent.index());
                $(this).parent().remove();
                return false;
            }).appendTo($row);

            // Separator.
            $("<span />", {
                "class": "vertical-separator"
            }).appendTo($row);

            // Edit link.
            $("<a />", {
                "href": symbolEditUrl + "&type=" + symbolizer['@symbol'] + "&symbolIndex=" + i,
                "text": CSTL.i18n("edit")
            }).click(function() {
                var $parent = $(this).parent();
                CSTL.RuleEdition.editSymbol($parent.index());
            }).appendTo($row);
        }
    },

    /**
     * Removes a symbolizer from the symbolizer list.
     *
     * @param index - {Integer} the symbolizer index
     */
    removeSymbol: function(index) {
        this._rule.symbolizers.splice(index, 1);
    },

    /**
     * Removes all symbolizers from the symbolizer list.
     */
    removeAllSymbols: function() {
        this._rule.symbolizers = [];
        $("#symbolizers").empty();
    },

    /**
     * Prepares a new symbolizer edition.
     *
     * @param type - {String} the symbolizer type to create
     */
    newSymbol: function(type) {
        // Create a default symbolizer.
        var symbolizer;
        switch(type) {
            case "point":
                symbolizer = CSTL.SldWorkflow.DEFAULT_POINT_SYMBOL;
                break;
            case "line":
                symbolizer = CSTL.SldWorkflow.DEFAULT_LINE_SYMBOL;
                break;
            case "polygon":
                symbolizer = CSTL.SldWorkflow.DEFAULT_POLYGON_SYMBOL;
                break;
            case "text":
                symbolizer = CSTL.SldWorkflow.DEFAULT_TEXT_SYMBOL;
                break;
            default:
                symbolizer = CSTL.SldWorkflow.DEFAULT_POINT_SYMBOL;
                break;
        }

        // Set storage for next step.
        CSTL.SldWorkflow.setRule(this._rule);
        CSTL.SldWorkflow.setSymbolizer(symbolizer);
    },

    /**
     * Prepares an existing symbolizer edition.
     *
     * @param index - {Integer} the symbolizer index
     */
    editSymbol: function(index) {
        this.update();

        // Set storage for next step.
        CSTL.SldWorkflow.setRule(this._rule);
        CSTL.SldWorkflow.setSymbolizer(this._rule.symbolizers[index]);
    },

    /**
     * Update the rule from for values.
     */
    update: function() {
        this._rule.name        = $("[name='name']").val();
        this._rule.title       = $("[name='title']").val();
        this._rule.description = $("[name='description']").val();
        this._rule.minScale    = $("[name='minScale']").val();
        this._rule.maxScale    = $("[name='maxScale']").val();
        this._rule.filter      = CSTL.AttributesFilter.build();
    },

    /**
     * Validates the current edition.
     */
    validate: function() {
        this.update();

        // Update the style.
        var style = CSTL.SldWorkflow.getStyle();
        if (this._ruleIndex != null) {
            style.rules[this._ruleIndex] = this._rule;
        } else {
            style.rules.push(this._rule);
        }
        CSTL.SldWorkflow.setStyle(style);

        // Clear local storage.
        CSTL.SldWorkflow.clearRule();
        CSTL.SldWorkflow.clearSymbolizer();
    },

    /**
     * Cancels the current edition.
     */
    cancel: function() {
        // Clear local storage.
        CSTL.SldWorkflow.clearRule();
        CSTL.SldWorkflow.clearSymbolizer();
    }
};

CSTL.PointSymbol = {

    // The current edited symbol.
    _symbol: null,

    // The edited symbol index.
    _symbolIndex: null,

    /**
     * Initializes the "ruleedition.gtpml" page.
     */
    init: function(symbolIndex) {
        this._symbol      = CSTL.SldWorkflow.getSymbolizer();
        this._symbolIndex = symbolIndex;

        // Set form values.
        $("[name='graphic.size']").val(this._symbol.graphic.size);
        $("[name='graphic.opacity']").val(this._symbol.graphic.opacity * 100);
        $("[name='graphic.rotation']").val(this._symbol.graphic.rotation);
        $("[name='graphic.mark.geometry']").val(this._symbol.graphic.mark.geometry);
        $("[name='graphic.mark.stroke.color']").val(this._symbol.graphic.mark.stroke.color);
        $("[name='graphic.mark.stroke.width']").val(this._symbol.graphic.mark.stroke.width);
        $("[name='graphic.mark.stroke.opacity']").val(this._symbol.graphic.mark.stroke.opacity * 100);
        $("[name='graphic.mark.fill.color']").val(this._symbol.graphic.mark.fill.color);
        $("[name='graphic.mark.fill.opacity']").val(this._symbol.graphic.mark.fill.opacity * 100);
        if (this._symbol.graphic.mark.stroke.dashed == true) {
            $("[name='graphic.mark.stroke.dashed']").addClass("active")
        } else {
            $("[name='graphic.mark.stroke.continuous']").addClass("active")
        }
        $(".color-overview").each(function() {
            var input = $(this).siblings("input[type=text]");
            $(this).css("background-color", input.val());
        });

        // Create widgets.
        $("[name$='.opacity']").slider({min:0,max:100,step:1});
        $("[name$='.rotation']").slider({min:0,max:360,step:1});
        $("[name$='.color']").colorpicker({format:'hex'}).on('changeColor', function(e) {
            $(this).siblings(".color-overview").css("background-color", e.color.toHex());
        });
        $(".btn-group").button();
    },

    /**
     * Update the symbolizer from for values.
     */
    update: function() {
        this._symbol.graphic.size                = $("[name='graphic.size']").val();
        this._symbol.graphic.opacity             = $("[name='graphic.opacity']").val() / 100;
        this._symbol.graphic.rotation            = $("[name='graphic.rotation']").val();
        this._symbol.graphic.mark.geometry       = $("[name='graphic.mark.geometry']").val();
        this._symbol.graphic.mark.stroke.color   = $("[name='graphic.mark.stroke.color']").val();
        this._symbol.graphic.mark.stroke.width   = $("[name='graphic.mark.stroke.width']").val();
        this._symbol.graphic.mark.stroke.opacity = $("[name='graphic.mark.stroke.opacity']").val() / 100;
        this._symbol.graphic.mark.stroke.dashed  = $("[name='graphic.mark.stroke.dashed']").hasClass("active");
        this._symbol.graphic.mark.fill.color     = $("[name='graphic.mark.fill.color']").val();
        this._symbol.graphic.mark.fill.opacity   = $("[name='graphic.mark.fill.opacity']").val() / 100;
    },

    /**
     * Validates the current edition.
     */
    validate: function() {
        this.update();

        // Update the style.
        var rule = CSTL.SldWorkflow.getRule();
        if (this._symbolIndex != null) {
            rule.symbolizers[this._symbolIndex] = this._symbol;
        } else {
            rule.symbolizers.push(this._symbol);
        }
        CSTL.SldWorkflow.setRule(rule);

        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    },

    /**
     * Cancels the current edition.
     */
    cancel: function() {
        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    }
};

CSTL.LineSymbol = {

    // The current edited symbol.
    _symbol: null,

    // The edited symbol index.
    _symbolIndex: null,

    /**
     * Initializes the "ruleedition.gtpml" page.
     */
    init: function(symbolIndex) {
        this._symbol      = CSTL.SldWorkflow.getSymbolizer();
        this._symbolIndex = symbolIndex;

        // Set form values.
        $("[name='stroke.color']").val(this._symbol.stroke.color);
        $("[name='stroke.width']").val(this._symbol.stroke.width);
        $("[name='stroke.opacity']").val(this._symbol.stroke.opacity * 100);
        if (this._symbol.stroke.dashed == true) {
            $("[name='stroke.dashed']").addClass("active")
        } else {
            $("[name='stroke.continuous']").addClass("active")
        }
        $(".color-overview").each(function() {
            var input = $(this).siblings("input[type=text]");
            $(this).css("background-color", input.val());
        });

        // Create widgets.
        $("[name$='.opacity']").slider({min:0,max:100,step:1});
        $("[name$='.color']").colorpicker({format:'hex'}).on('changeColor', function(e) {
            $(this).siblings(".color-overview").css("background-color", e.color.toHex());
        });
        $(".btn-group").button();
    },

    /**
     * Validates the current edition.
     */
    validate: function() {
        // Apply form values.
        this._symbol.stroke.color   = $("[name='stroke.color']").val();
        this._symbol.stroke.width   = $("[name='stroke.width']").val();
        this._symbol.stroke.opacity = $("[name='stroke.opacity']").val() / 100;
        this._symbol.stroke.dashed  = $("[name='stroke.dashed']").hasClass("active");

        // Update the style.
        var rule = CSTL.SldWorkflow.getRule();
        if (this._symbolIndex != null) {
            rule.symbolizers[this._symbolIndex] = this._symbol;
        } else {
            rule.symbolizers.push(this._symbol);
        }
        CSTL.SldWorkflow.setRule(rule);

        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    },

    /**
     * Cancels the current edition.
     */
    cancel: function() {
        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    }
};

CSTL.PolygonSymbol = {

    // The current edited symbol.
    _symbol: null,

    // The edited symbol index.
    _symbolIndex: null,

    /**
     * Initializes the "ruleedition.gtpml" page.
     */
    init: function(symbolIndex) {
        this._symbol      = CSTL.SldWorkflow.getSymbolizer();
        this._symbolIndex = symbolIndex;

        // Set form values.
        $("[name='stroke.color']").val(this._symbol.stroke.color);
        $("[name='stroke.width']").val(this._symbol.stroke.width);
        $("[name='stroke.opacity']").val(this._symbol.stroke.opacity * 100);
        $("[name='fill.color']").val(this._symbol.fill.color);
        $("[name='fill.opacity']").val(this._symbol.fill.opacity * 100);
        if (this._symbol.stroke.dashed == true) {
            $("[name='stroke.dashed']").addClass("active")
        } else {
            $("[name='stroke.continuous']").addClass("active")
        }
        $(".color-overview").each(function() {
            var input = $(this).siblings("input[type=text]");
            $(this).css("background-color", input.val());
        });

        // Create widgets.
        $("[name$='.opacity']").slider({min:0,max:100,step:1});
        $("[name$='.color']").colorpicker({format:'hex'}).on('changeColor', function(e) {
            $(this).siblings(".color-overview").css("background-color", e.color.toHex());
        });
        $(".btn-group").button();
    },

    /**
     * Validates the current edition.
     */
    validate: function() {
        // Apply form values.
        this._symbol.stroke.color   = $("[name='stroke.color']").val();
        this._symbol.stroke.width   = $("[name='stroke.width']").val();
        this._symbol.stroke.opacity = $("[name='stroke.opacity']").val() / 100;
        this._symbol.stroke.dashed  = $("[name='stroke.dashed']").hasClass("active");
        this._symbol.fill.color     = $("[name='fill.color']").val();
        this._symbol.fill.opacity   = $("[name='fill.opacity']").val() / 100;

        // Update the style.
        var rule = CSTL.SldWorkflow.getRule();
        if (this._symbolIndex != null) {
            rule.symbolizers[this._symbolIndex] = this._symbol;
        } else {
            rule.symbolizers.push(this._symbol);
        }
        CSTL.SldWorkflow.setRule(rule);

        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    },

    /**
     * Cancels the current edition.
     */
    cancel: function() {
        // Clear local storage.
        CSTL.SldWorkflow.clearSymbolizer();
    }
};

CSTL.AttributesFilter = {

    $elt: null,

    _attributes: [],

    _operators: {
        "=": "==",
        "!=": "!=",
        "<": "<",
        "<=": "<=",
        ">": ">",
        ">=": ">=",
        "LIKE": "~"
    },

    _treeNode: null,
    
    init: function(filter) {
        this.$elt   = $("#filter");

        if (filter) {
            filter = new OpenLayers.Format.CQL().read(filter);
        }
        this.treeNode = new FilterNode(filter);
        this.treeNode.draw();
    },

    build: function() {
        if (this.treeNode.filter) {
            this.treeNode.updateFilter();
            return new OpenLayers.Format.CQL().write(this.treeNode.filter);
        }
        return null;
    },

    _createButton: function(text) {
        return $("<div />", {
            "class": "btn",
            "text": text
        });
    },

    _createSelect: function(options) {
        var select = $("<select />", {
            "class": "input-small"
        });
        for (var key in options) {
            $("<option />", {
                "value": options[key],
                "text": key
            }).appendTo(select);
        }
        return select;
    },

    _createInput: function() {
        return $("<input />", {
            "type": "text",
            "class": "input-small"
        });
    }
};

/**
 * Create a new node.
 *
 * @param filter - {OpenLayers.Filter} the filter to edit
 * @constructor
 */
function FilterNode(filter) {
    this.children = [];
    this.parent   = null;
    this.filter   = filter;
    this.li       = $("<li />");
    this.panel    = $("<div />").appendTo(this.li);
    this.ul       = $("<ul />").appendTo(this.li);
}

/**
 * Adds a child node to current node.
 *
 * @param node - {Node} the node to be removed
 */
FilterNode.prototype.addChild = function(node) {
    var index = $.inArray(node, this.children);
    if (index == -1) {
        this.children.push(node);
        node.parent = this;
        node.onNodeAdded();
        if (OpenLayers.Util.indexOf(this.filter.filters, node.filter) === -1) {
            this.filter.filters.push(node.filter);
        }
    }
    for (var i = 0; i < this.children.length; i++) {
        if (this.children[i] != node) {
            this.children[i].onSiblingsChanged();
        }
    }
};

/**
 * Removes a child node from current node.
 *
 * @param node - {Node} the node to be removed
 */
FilterNode.prototype.removeChild = function(node) {
    var index = $.inArray(node, this.children);
    if (index != -1) {
        this.children.splice(index, 1);
        node.parent = null;
        node.onNodeRemoved();
        OpenLayers.Util.removeItem(this.filter.filters, node.filter);
    }
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].onSiblingsChanged();
    }
};

/**
 * Removes all node children.
 */
FilterNode.prototype.clear = function() {
    this.children = [];
    this.ul.empty();
};

/**
 * Adds a new children to the node parent (a new node sibling).
 */
FilterNode.prototype.newSibling = function() {
    this.parent.addChild(new FilterNode(null));
};

/**
 * Removes the current node from its parent.
 */
FilterNode.prototype.selfRemove = function() {
    this.parent.removeChild(this);
};

/**
 * Checks if the node is the last sibling.
 *
 * @returns {boolean}
 */
FilterNode.prototype.isLast = function() {
    return this.parent && (this.li.index() === this.parent.children.length - 1);
};

/**
 * Checks if the node has more than one sibling.
 *
 * @returns {boolean}
 */
FilterNode.prototype.hasMoreThanOneSibling = function() {
    return this.parent && this.parent.children.length > 2;
};

/**
 * Callback function when the node is added to a new parent.
 */
FilterNode.prototype.onNodeAdded = function() {
    this.draw();
};

/**
 * Callback function when the node is removed from his parent.
 */
FilterNode.prototype.onNodeRemoved = function() {
    this.undraw();
};

/**
 * Callback function when the number of siblings has changed.
 */
FilterNode.prototype.onSiblingsChanged = function() {
    if (this.isLast()) {
        this.drawAddButton();
    } else {
        this.panel.find("[data-action='add']").remove();
    }
    if (this.hasMoreThanOneSibling()) {
        this.drawRemoveButton();
    } else {
        this.panel.find("[data-action='remove']").remove();
    }
};

/**
 * Draw/redraw the node.
 */
FilterNode.prototype.draw = function() {
    var isUndefined = false;

    // Attach to its parent if not already attached.
    if (this.li.index() === -1) {
        if (this.parent == null) {
            CSTL.AttributesFilter.$elt.append(this.li);
        } else {
            this.parent.ul.append(this.li);
        }
    }

    // Clear panel in case of redraw.
    this.panel.empty();

    // Logical filter (OR, AND, NOT).
    if (this.filter instanceof OpenLayers.Filter.Logical) {
        for (var i = 0; i < this.filter.filters.length; i++) {
            this.addChild(new FilterNode(this.filter.filters[i]));
        }
        CSTL.AttributesFilter._createSelect({"AND":"&&","OR":"||"}).
            val(this.filter.type).
            appendTo(this.panel);
    }

    // Comparison filter (=, !=, <, <=, >=, >, like, nil, null, between...).
    else if (this.filter instanceof OpenLayers.Filter.Comparison) {
        CSTL.AttributesFilter._createInput().
            val(this.filter.property).
            appendTo(this.panel);
        CSTL.AttributesFilter._createSelect(CSTL.AttributesFilter._operators).
            val(this.filter.type).
            appendTo(this.panel);
        CSTL.AttributesFilter._createInput().
            val(this.filter.value).
            appendTo(this.panel);
    }

    // Undefined filter.
    else {
        isUndefined = true;
        this.clear();
        CSTL.AttributesFilter._createButton("Logical").
            click($.proxy(this.toLogical, this)).
            appendTo(this.panel);
        CSTL.AttributesFilter._createButton("Comparison").
            click($.proxy(this.toComparison, this)).
            appendTo(this.panel);
    }

    // Redefined node button.
    if (!isUndefined) {
        CSTL.AttributesFilter._createButton().
            addClass("icon-reply").
            click($.proxy(this.toUndefined, this)).
            appendTo(this.panel);
    }

    // Remove and add button.
    if (this.hasMoreThanOneSibling()) {
        this.drawRemoveButton();
    }
    if (this.isLast()) {
        this.drawAddButton();
    }
};

/**
 * Updates the node filter and all sub node filter recursively.
 */
FilterNode.prototype.updateFilter = function() {
    // Logical filter (OR, AND, NOT).
    if (this.filter instanceof OpenLayers.Filter.Logical) {
        this.filter.type = this.panel.children().eq(0).val();
    }

    // Comparison filter (=, !=, <, <=, >=, >, like, nil, null, between...).
    else if (this.filter instanceof OpenLayers.Filter.Comparison) {
        this.filter.property = this.panel.children().eq(0).val();
        this.filter.type     = this.panel.children().eq(1).val();
        this.filter.value    = this.panel.children().eq(2).val();
    }

    // Undefined filter.
    else {
        alert("Incomplete tree.");
        throw "Incomplete tree.";
    }

    // Update children.
    for (var i = 0; i < this.children.length; i++) {
        this.children[i].updateFilter();
    }
};

/**
 * Draw a button allowing to remove the node.
 */
FilterNode.prototype.drawRemoveButton = function() {
    if (this.panel.find("[data-action='remove']").length == 0) {
        CSTL.AttributesFilter._createButton().
            addClass("icon-minus").
            attr("data-action", "remove").
            click($.proxy(this.selfRemove, this)).
            appendTo(this.panel);
    }
};

/**
 * Draw a button allowing to add a new sibling.
 */
FilterNode.prototype.drawAddButton = function() {
    if (this.panel.find("[data-action='add']").length == 0) {
        CSTL.AttributesFilter._createButton().
            addClass("icon-plus").
            attr("data-action", "add").
            click($.proxy(this.newSibling, this)).
            appendTo(this.panel);
    }
};

/**
 * Undraw the node.
 */
FilterNode.prototype.undraw = function() {
    this.li.remove();
};

/**
 * Mutates the node to an undefined filter.
 */
FilterNode.prototype.toUndefined = function() {
    this.filter = null;
    if (this.parent) {
        this.parent.filter.filters[this.li.index()] = this.filter;
    }
    this.draw();
};

/**
 * Mutates the node to an comparison filter.
 */
FilterNode.prototype.toComparison = function() {
    this.filter = new OpenLayers.Filter.Comparison();
    if (this.parent) {
        this.parent.filter.filters[this.li.index()] = this.filter;
    }
    this.draw();
};

/**
 * Mutates the node to an logical filter.
 */
FilterNode.prototype.toLogical = function() {
    this.filter = new OpenLayers.Filter.Logical({
        filters: [null, null]
    });
    if (this.parent) {
        this.parent.filter.filters[this.li.index()] = this.filter;
    }
    this.draw();
};