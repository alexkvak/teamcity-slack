BS.SlackNotifierDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('slackNotifierDialog');
    },
});

jQuery(function($) {
    $('.buildSettingsList').on('click', '.add-button', function() {
        BS.SlackNotifierDialog.showCentered();
    });
});
