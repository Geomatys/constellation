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
 * Constellation UI services manager.
 *
 * @type {object}
 */
CSTL.Providers = {

    /**
     * Delete a style from a style provider.
     *
     * @param providerId - {string} the style provider identifier
     * @param styleName  - {string} the style name
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    deleteStyle: function(providerId, styleName){
        return CSTL.jzAjax('StyleController.deleteStyle', {
            data: {providerId:providerId,styleName:styleName}
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-style-delete'));
            $('[data-provider="' + providerId + '"]').filter('[data-style="' + styleName + '"]').remove();
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-style-delete'));
        });
    },

    /**
     * Links a style to an existing data.
     *
     * @param styleProvider - {string} the style provider identifier
     * @param styleName     - {string} the style name
     * @param dataProvider  - {string} the data provider identifier
     * @param dataName      - {string} the data name
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    linkStyleToData: function(styleProvider, styleName, dataProvider, dataName) {
        return CSTL.jzAjax('StyleController.linkStyleToData', {
            data: {
                styleProvider: styleProvider,
                styleName:     styleName,
                dataProvider:  dataProvider,
                dataName:      dataName
            }
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-style-link-data'));
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-style-link-data'));
        });
    },

    /**
     * Links a style to an existing data.
     *
     * @param styleProvider - {string} the style provider identifier
     * @param styleName     - {string} the style name
     * @param dataProvider  - {string} the data provider identifier
     * @param dataName      - {string} the data name
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    unlinkStyleFromData: function(styleProvider, styleName, dataProvider, dataName) {
        return CSTL.jzAjax('StyleController.unlinkStyleFromData', {
            data: {
                styleProvider: styleProvider,
                styleName:     styleName,
                dataProvider:  dataProvider,
                dataName:      dataName
            }
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-style-unlink-data'));
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-style-unlink-data'));
        });
    }

//    linkDataToService: function(dataProvider, dataName, )
};