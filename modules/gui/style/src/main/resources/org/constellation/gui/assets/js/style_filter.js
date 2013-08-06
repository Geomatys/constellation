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

CSTL.STYLE.FilterEdition = {

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
        this.$elt = $("#filter");
        this.$elt.empty();

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
            CSTL.STYLE.FilterEdition.$elt.append(this.li);
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
        CSTL.STYLE.FilterEdition._createSelect({"AND":"&&","OR":"||"}).
            val(this.filter.type).
            appendTo(this.panel);
    }

    // Comparison filter (=, !=, <, <=, >=, >, like, nil, null, between...).
    else if (this.filter instanceof OpenLayers.Filter.Comparison) {
        CSTL.STYLE.FilterEdition._createInput().
            val(this.filter.property).
            appendTo(this.panel);
        CSTL.STYLE.FilterEdition._createSelect(CSTL.STYLE.FilterEdition._operators).
            val(this.filter.type).
            appendTo(this.panel);
        CSTL.STYLE.FilterEdition._createInput().
            val(this.filter.value).
            appendTo(this.panel);
    }

    // Undefined filter.
    else {
        isUndefined = true;
        this.clear();
        CSTL.STYLE.FilterEdition._createButton("Logical").
            click($.proxy(this.toLogical, this)).
            appendTo(this.panel);
        CSTL.STYLE.FilterEdition._createButton("Comparison").
            click($.proxy(this.toComparison, this)).
            appendTo(this.panel);
    }

    // Redefined node button.
    if (!isUndefined) {
        CSTL.STYLE.FilterEdition._createButton().
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
        CSTL.STYLE.FilterEdition._createButton().
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
        CSTL.STYLE.FilterEdition._createButton().
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