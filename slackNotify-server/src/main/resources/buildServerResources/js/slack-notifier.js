BS.SlackNotifierDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('slackNotifierDialog');
    }
});

jQuery(function($) {
    BS.ProgressPopup.showProgress("ajaxContainer", "Loading...");
    BS.ajaxUpdater("ajaxContainer", window.slackNotifierUrl.buildSettingListUrl, {
        onSuccess: function () {
            BS.ProgressPopup.hidePopup()
        }
    });

    function buildSettingEdit(id) {
        BS.ajaxRequest(window.slackNotifierUrl.buildSettingEditUrl + (id ? '?id=' + id : ''), {
            onSuccess: function (response) {
                $('#slackNotifier').find('.modalDialogBody').html(response);
                BS.SlackNotifierDialog.showCentered();
            }
        });
    }

    $('.buildSettingsList').on('click', '.add-button', function() {
        buildSettingEdit();
    });

    $('#mainContent').on('click', '.closeDialog', function() {
        BS.SlackNotifierDialog.close();
    });
});
