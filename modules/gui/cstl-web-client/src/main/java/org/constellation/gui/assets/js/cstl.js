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
var appURL = window.location.protocol + "//" + window.location.host + "/" + window.location.pathname.split("/")[1];

/**
 * Constellation UI namespace and utility functions.
 *
 * @type {object}
 */
CSTL = {

    /**
     * {jQuery} Dictionary containing internationalized string for the current
     * locale.
     */
    $dictionary: null,

    URL_CONSTELLATION_PROXY: appURL + "/constellation",
    URL_VECTOR_PICTURE:"url("+appURL+"/images/vector.png)",
    URL_VECTOR_SELECTED_PICTURE:"url("+appURL+"/images/vector-selected.png)",
    URL_RASTER_PICTURE:"url("+appURL+"/images/raster.png)",
    URL_RASTER_SELECTED_PICTURE:"url("+appURL+"/images/raster-selected.png)",
    URL_SENSOR_PICTURE:"url("+appURL+"/images/sensor.png)",
    URL_SENSOR_SELECTED_PICTURE:"url("+appURL+"/images/sensor-selected.png)",
    URL_PYRAMID_PICTURE:"url("+appURL+"/images/pyramid.png)",
    URL_PYRAMID_SELECTED_PICTURE:"url("+appURL+"/images/pyramid-selected.png)",
    URL_STYLE_PICTURE:"url("+appURL+"/images/style.png)",
    URL_STYLE_SELECTED_PICTURE:"url("+appURL+"/images/style-selected.png)",
    URL_CONSTELLATION_PICTURE:appURL+"/images/constellation.png",
    URL_CONSTELLATION_PICTURE_MENU:appURL+"/images/constellation-menu.png",


    /**
     * Looks up a key from a dictionary based on the current language string.
     *
     * @param key - {string} the message key
     * @returns {string} an internationalized string
     */
    i18n: function(key) {
        if (!this.$dictionary) {
            this.$dictionary = $('#dictionary');
        }
        return this.$dictionary.data(key) || key;
    },

    /**
     * Displays a notification with the specified title and text.
     *
     * @param type  - {string} the notification type (info|error|success|warning)
     * @param title - {string} the notification title
     * @param msg   - {string} the notification message
     */
    growl: function(type, title, msg) {
        if (type === 'info') {
            $.growl({title: title, message: msg});
        } else if (type === 'error') {
            $.growl.error({title: title, message: msg});
        } else if (type === 'success') {
            $.growl.notice({title: title, message: msg});
        } else if (type === 'warning') {
            $.growl.warning({title: title, message: msg});
        }
    },

    /**
     * Performs an AJAX request to execute a server site method.
     *
     * @param mid     - {string} the method id to execute
     * @param options - {string} the request parameters
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    jzAjax: function(mid, options) {
        options     = options || {};
        options.url = $('[data-method-id="' + mid + '"]').data('url');
        return $.ajax(options);
    },

    /**
     * Extracts the data body from a Juzu AJAX resource response.
     *
     * @param data - {string} the response data
     * @returns {string} the response body or null
     */
    handleJzData: function(data) {
        if (typeof data === "string") {
            return data.substring(data.indexOf('<body>') + 6, data.indexOf('</body>'));
        }
        return null;
    }
};

// Send multivalue parameter without [].
// Exemple :
// ---------
// for dataType:["vector","raster"],
// send dataType:"vector", dataType:"raster"
// and not dataType[]:"vector", dataType[]:"raster"
jQuery.ajaxSettings.traditional = true;

