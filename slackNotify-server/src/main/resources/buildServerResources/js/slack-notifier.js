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
        $.post(window.slackNotifier.buildSettingSaveUrl, formData, function (data) {
            if ('' === data) {
                loadBuildSettingsList();
                BS.SlackNotifierDialog.close();
            } else {
                $('#slackNotifier').find('.error').html(data).show();
            }
        });

        return false;
    };

    function initCheckboxes() {
        function handleDependencies(element) {
            checkboxes.filter('[data-parent=' + element.attr('name') + ']').prop('disabled', !element.prop('checked'))
        }

        var checkboxes = $('.editPane .checkboxes-group input[type=checkbox]');
        checkboxes.on('click', function () {
            handleDependencies($(this))
        });

        // disable all checkboxes which parent are unchecked
        checkboxes.filter('[data-parent]').each(function () {
            var element = $(this);
            if (undefined !== element.data('parent') &&
                (!checkboxes.filter('[name=' + element.data('parent') + ']').prop('checked'))) {
                element.prop('disabled', true);
            }
        })
    }

    function loadBuildSettingsList() {
        BS.ProgressPopup.showProgress("ajaxContainer", "Loading...");
        BS.ajaxUpdater("ajaxContainer", window.slackNotifier.buildSettingListUrl, {
            onSuccess: function () {
                BS.ProgressPopup.hidePopup()
            }
        });
    }

    function buildSettingEdit(id) {
        BS.ajaxRequest(window.slackNotifier.buildSettingEditUrl + (id ? '?id=' + id : ''), {
            onSuccess: function (response) {
                $('#slackNotifier').find('.modalDialogBody').html(response.responseText);
                BS.SlackNotifierDialog.showCentered();
                initCheckboxes();
            }
        });
    }

    function buildSettingDelete(id) {
        BS.confirmDialog.show({
            title: 'Delete notification settings',
            action: function () {
                BS.ajaxRequest(window.slackNotifier.buildSettingDeleteUrl + '?id=' + id, {
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
