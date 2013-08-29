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
CSTL.Services = {

    /**
     * Starts a Constellation web service.
     *
     * @param type - {string} the service type
     * @param id   - {string} the service identifier
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    start: function(type, id) {
        return CSTL.jzAjax('Controller.startService', {
            data: {serviceType:type,serviceId:id}
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-service-start'));
            $('[data-state="' + id + '"]').removeClass('stopped').addClass('started');
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-service-start'));
        })
    },

    /**
     * Stops a Constellation web service.
     *
     * @param type - {string} the service type
     * @param id   - {string} the service identifier
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    stop: function(type, id) {
        return CSTL.jzAjax('Controller.stopService', {
            data: {serviceType:type,serviceId:id}
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-service-stop'));
            $('[data-state="' + id + '"]').removeClass('started').addClass('stopped');
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-service-stop'));
        })
    },

    /**
     * Restarts a Constellation web service.
     *
     * @param type - {string} the service type
     * @param id   - {string} the service identifier
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    restart: function(type, id) {
        return CSTL.jzAjax('Controller.reloadService', {
            data: {serviceType:type,serviceId:id}
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-service-restart'));
            $('[data-state="' + id + '"]').removeClass('stopped').addClass('started');
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-service-restart'));
            $('[data-state="' + id + '"]').removeClass('started').addClass('stopped');
        });
    },

    /**
     * Sets a Constellation web service description.
     *
     * @param type - {string} the service type
     * @param id   - {string} the service identifier
     * @param $form - {jQuery} the form to submit
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    setDescription: function(type, id, $form) {
        var data = $form.serialize() || {};
        data['serviceType'] = type;
        data['serviceId']   = id;

        return CSTL.jzAjax('Controller.setServiceDescription', {
            method: 'POST',
            data:   data
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-service-description'));
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-service-description'));
        });
    },

    /**
     * Sets a Constellation web service metadata.
     *
     * @param type - {string} the service type
     * @param id   - {string} the service identifier
     * @param $form - {jQuery} the form to submit
     * @returns {jQuery.ajax} the jQuery.ajax instance
     */
    setMetadata: function(type, id, $form) {
        var data = $form.serialize() || {};
        data['serviceType'] = type;
        data['serviceId']   = id;

        return CSTL.jzAjax('Controller.setServiceMetadata', {
            method: 'POST',
            data:   data
        }).done(function() {
            CSTL.growl('success', CSTL.i18n('success'), CSTL.i18n('success-service-metadata'));
        }).fail(function() {
            CSTL.growl('error', CSTL.i18n('error'), CSTL.i18n('error-service-metadata'));
        });
    }
};

/**
 * Page load end listener used to attach automatically event listener on HTML element
 * with specific flags to perform generic action.
 */
$(function() {
    $('[data-action="restart-service"]').click(function() {
        var $this = $(this).attr('disabled', 'disabled');
        CSTL.Services.restart($this.data('service-type'), $this.data('service-id')).always(function() {
            $this.removeAttr('disabled');
        });
        return false;
    });
    $('[data-action="toggle-service"]').click(function() {
        var $this = $(this).attr('disabled', 'disabled');
        if ($this.hasClass('stopped')) {
            CSTL.Services.start($this.data('service-type'), $this.data('service-id')).success(function() {
                $this.removeClass('stopped').addClass('started');
            }).always(function() {
                $this.removeAttr('disabled');
            });
        } else {
            CSTL.Services.stop($this.data('service-type'), $this.data('service-id')).success(function() {
                $this.removeClass('started').addClass('stopped');
            }).always(function() {
                $this.removeAttr('disabled');
            });
        }
        return false;
    });
    $('[data-action="service-description"]').click(function() {
        var $this = $(this);
        CSTL.Services.setDescription($this.data('service-type'), $this.data('service-id'), $('#' + $this.data('form')));
        return false;
    });
    $('[data-action="service-metadata"]').click(function() {
        var $this = $(this);
        CSTL.Services.setMetadata($this.data('service-type'), $this.data('service-id'), $('#' + $this.data('form')));
        return false;
    });
    $('[data-action="open-capabilities"]').click(function() {
        window.open($(this).data('capabilities'));
        return false;
    });
});