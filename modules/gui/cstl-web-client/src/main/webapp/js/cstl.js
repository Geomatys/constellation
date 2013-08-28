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

    URL_VECTEUR_PICTURE:"url(../../images/vecteur.png)",
    URL_VECTEUR_SELECTED_PICTURE:"url(../../images/vecteur-selected.png)",
    URL_RASTER_PICTURE:"url(../../images/raster.png)",
    URL_RASTER_SELECTED_PICTURE:"url(../../images/raster-selected.png)",
    URL_SENSOR_PICTURE:"url(../../images/raster.png)",
    URL_SENSOR_SELECTED_PICTURE:"url(../../images/sensor-selected.png)",
    URL_STYLE_PICTURE:"url(../../images/style.png)",
    URL_STYLE_SELECTED_PICTURE:"url(../../images/style-selected.png)",


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
     * @returns {boolean} true on action success otherwise false
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