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
 * Creates a Constellation UI dashboard manager.
 *
 * @param config - {object} the dashboard configuration
 *
 * {
 *     loadFunc: (mandatory) {string}   the Juzu function name to call
 *     $root:    (optional)  {jQuery}   the dashboard root element
 *     onLoad:   (optional)  {function} items loading callback function
 *     onSelect: (optional)  {function} item selection callback function
 *     params:   (optional)  {object}   static parameters for loading request
 * }
 */
function Dashboard(config) {
    config = config || {};
    this.params   = config.params || {};                            // static loading request parameters
    this.loadFunc = config.loadFunc;                                // function to be called for items loading
    this.$root    = config.$root || $('body');                      // dashboard list root element
    this.onLoad   = config.onLoad;                                  // items loading complete function
    this.onSelect = config.onSelect;                                // item selection callback function

    // Persistent HTML elements.
    this.$itemList    = this.$root.find('[data-role="list"]');      // <div>    to display items
    this.$sortLinks   = this.$root.find('[data-order-by]');         // <a>      to sort items
    this.$filterInput = this.$root.find('[data-role="search"]');    // <input>  to filter items
    this.$ajaxLoader  = this.$root.find('.ajax-loader');            // ajax loader element

    // Sort parameters (cache).
    this.startIndex    = 0;                                         // current start index
    this.nbItems       = 10;                                        // current nb/page
    this.sortCriteria  = null;                                      // current sort criteria
    this.sortDirection = null;                                      // current sort direction

    // Event handling.
    this.$sortLinks.on('click', $.proxy(this.sort, this));                                  // listen sort button click
    this.$filterInput.on('keyup', $.proxy(this.filter, this));                              // listen search input key up
    this.$root.find('[data-role="reset"]').on('click', $.proxy(this.reset, this));          // listen reset filter click
    this.$root.find('[data-role="nb-items"]').on('change', $.proxy(this.nbPerPage, this));  // listen nb/page select change
    this.$root.find('[data-page-index]').on('click',  $.proxy(this.goTo, this));            // listen pagination click

    // Handle selection.
    this.handleSelection();
}

/**
 * Resets all applied filter/sort criteria.
 */
Dashboard.prototype.handleSelection = function() {
    this.$itemList.find(".item").click($.proxy(function(e) {
        var $this = $(e.currentTarget);
        $this.addClass('selected').siblings().removeClass('selected');
        this.onSelect && this.onSelect.call(this, $this);
    }, this));
};

/**
 * Display items from specified index.
 *
 * @param index - {number} the first item index
 */
Dashboard.prototype.fromIndex = function(index) {
    this.startIndex = index;
    this.loadItems();
};

/**
 * Resets all applied filter/sort criteria.
 */
Dashboard.prototype.reset = function() {
    this.startIndex    = 0;
    this.sortCriteria  = null;
    this.sortDirection = null;
    this.$sortLinks.removeClass('descending ascending');
    this.$filterInput.val('');
    this.loadItems();
};

/**
 * Applies a name/title filter on results.
 */
Dashboard.prototype.filter = function() {
    this.startIndex = 0;
    if (this._loadTimeout) {
        clearTimeout(this._loadTimeout);
    }
    this._loadTimeout = setTimeout($.proxy(this.loadItems, this), 200);
    this.$ajaxLoader.show();
};

/**
 * Applies sort criteria and direction.
 *
 * @param e - {event} the "click" event
 */
Dashboard.prototype.sort = function(e) {
    var $source = $(e.currentTarget);
    this.sortCriteria  = $source.data('order-by');
    this.sortDirection = $source.hasClass('descending') ? 'ascending' : 'descending';
    this.$sortLinks.removeClass('descending ascending');
    $source.addClass(this.sortDirection);
    this.loadItems();
};

/**
 * Updates the number of items displayed per page.
 *
 * @param e - {event} the "click" event
 */
Dashboard.prototype.nbPerPage = function(e) {
    var $source = $(e.currentTarget);
    this.startIndex = 0;
    this.nbItems = $source.val();
    this.loadItems();
};

/**
 * Updates the current page index.
 *
 * @param e - {event} the "click" event
 */
Dashboard.prototype.goTo = function(e) {
    var $source = $(e.currentTarget);
    var page = $source.data('page-index');
    this.startIndex = this.nbItems * (page - 1);
    this.loadItems();
};

/**
 * Displays the AJAX loader and performs a jzLoad request on specified loading function
 * with entered/selected parameters.
 */
Dashboard.prototype.loadItems = function() {
    // Display ajax loader.
    this.$ajaxLoader.show();

    // Prepare parameters.
    var parameters = {
        filter:    this.$filterInput.val(),
        orderBy:   this.sortCriteria,
        direction: this.sortDirection,
        count:     this.nbItems,
        start:     this.startIndex
    };
    for (var key in this.params) {
        if (typeof this.params[key] === 'function') {
            parameters[key] = this.params[key].call();
        } else {
            parameters[key] = this.params[key];
        }
    }

    // Load new list.
    this.$itemList.jzLoad(this.loadFunc, parameters, $.proxy(this.loadEnd, this));
};

/**
 * jzLoad callback used to hide the AJAX loader to reattach event handler on replaced
 * elements and then to call the specified complete function.
 */
Dashboard.prototype.loadEnd = function() {
    // Hide ajax loader.
    this.$ajaxLoader.hide();

    // Handle selection.
    this.handleSelection();

    // Reattach event handling for non-persistent/replaced elements.
    this.$root.find('[data-role="nb-items"]').on('change', $.proxy(this.nbPerPage, this));
    this.$root.find('[data-page-index]').on('click', $.proxy(this.goTo, this));

    // Call registered callback.
    this.onLoad && this.onLoad.call();
};
