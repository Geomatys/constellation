angular.module('ajaxUpload', []).

    factory('$ajaxUpload', function($timeout) {
        var frameTemplate = '<iframe name="upload_iframe" style="display:none;height:0;width:0;" />';

        return function(url, form, callback) {
            // Ensure jQuery or angular element.
            form = ('addClass' in form) ? form : angular.element(form);

            // Create the <iframe />.
            var iframe = angular.element(frameTemplate);
            iframe.attr("src", "about:blank");
            iframe.attr("width", "0");
            iframe.attr("height", "0");
            iframe.attr("border", "0");

            // Add to document.
            form.parent().append(iframe);
            window.frames['upload_iframe'].name = 'upload_iframe';

            // Add event listeners.
            var loadHandler = function () {

                // Remove event listeners.
                if (iframe[0].removeEventListener) {
                    iframe[0].removeEventListener('load', loadHandler, false);
                } else {
                    iframe[0].detachEvent('onload', loadHandler);
                }

                // Message from server.
                var message;
                if (iframe[0].contentDocument) {
                    message = iframe[0].contentDocument.body.innerHTML;
                } else if (iframe[0].contentWindow) {
                    message = iframe[0].contentWindow.document.body.innerHTML;
                } else if (iframe[0].document) {
                    message = iframe[0].document.body.innerHTML;
                }

                // Remove <iframe />.
                $timeout(function() {
                    iframe[0].parentNode.removeChild(iframe[0]);
                });

                // Callback.
                if (typeof callback === "function") {
                    callback.call(this, message);
                }
            };

            // Attach event listeners.
            if (iframe[0].addEventListener) {
                iframe[0].addEventListener('load', loadHandler, true);
            } else {
                iframe[0].attachEvent('onload', loadHandler);
            }

            // Set form attributes.
            form.attr('target', 'upload_iframe');
            form.attr('action', url);
            form.attr('method', 'post');
            form.attr('enctype', 'multipart/form-data');
            form.attr('encoding', 'multipart/form-data');

            // Submit the form.
            form[0].submit();
        };
    });