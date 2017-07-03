BS.SlackNotifierDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer: function () {
        return $('slackNotifierDialog');
    }
});

jQuery(function ($) {
    loadBuildSettingsList();

    $('.buildSettingsList').on('click', '.add-button', function () {
        buildSettingEdit();
    }).on('click', '.js-edit', function () {
        buildSettingEdit($(this).closest('tr').data('id'));
    }).on('click', '.js-delete', function () {
        buildSettingDelete($(this).closest('tr').data('id'));
    });

    $('#mainContent').on('click', '.closeDialog', function () {
        BS.SlackNotifierDialog.close();
    });

    BS.SlackNotifierDialog.saveBuildConfig = function () {
        var formData = $('#slackNotifier').serialize();
        $.post(window.slackNotifierUrl.buildSettingSaveUrl, formData, function (data) {
            if ('' === data) {
                loadBuildSettingsList();
                BS.SlackNotifierDialog.close();
            } else {
                $('#slackNotifier').find('.error').html(data).show();
            }
        });

        return false;
    };

    function loadBuildSettingsList() {
        BS.ProgressPopup.showProgress("ajaxContainer", "Loading...");
        BS.ajaxUpdater("ajaxContainer", window.slackNotifierUrl.buildSettingListUrl, {
            onSuccess: function () {
                BS.ProgressPopup.hidePopup()
            }
        });
    }

    function buildSettingEdit(id) {
        BS.ajaxRequest(window.slackNotifierUrl.buildSettingEditUrl + (id ? '?id=' + id : ''), {
            onSuccess: function (response) {
                $('#slackNotifier').find('.modalDialogBody').html(response.responseText);
                BS.SlackNotifierDialog.showCentered();
            }
        });
    }

    function buildSettingDelete(id) {
        BS.confirmDialog.show({
            title: 'Delete notification settings',
            action: function () {
                BS.ajaxRequest(window.slackNotifierUrl.buildSettingDeleteUrl + '?id=' + id, {
                    onSuccess: function (response) {
                        if ('' === response.responseText) {
                            loadBuildSettingsList();
                        } else {
                            BS.confirmDialog.show({
                                'title': 'Delete failed',
                                'text': response.responseText
                            });
                        }
                    }
                });
            }
        });
    }
});
