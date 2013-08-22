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
     * Performs an AJAX request to execute a method returning data.
     *
     * @param mid    - {string} the method id to execute
     * @param params - {string} the request parameters
     * @returns {string} the response body or null
     */
    jzData: function(mid, params) {
        var result = null;
        $.ajax({
            async: false,
            url: $('.jz').jzURL(mid),
            data: params
        }).done(function(data) {
            result = data.substring(data.indexOf('<body>') + 6, data.indexOf('</body>'));
        });
        return result;
    },

    /**
     * Performs an AJAX request to execute a void method.
     *
     * @param mid    - {string} the method id to execute
     * @param params - {string} the request parameters
     * @returns {boolean} true on action success otherwise false
     */
    jzAction: function(mid, params) {
        var status = true;
        $.ajax({
            async: false,
            url: $('.jz').jzURL(mid),
            data: params
        }).error(function() {
            status = false;
        });
        return status;
    }
};