BS.SlackNotifierDialog = OO.extend(BS.AbstractModalDialog, {
    getContainer : function() {
        return $('slackNotifierDialog');
    },
});

jQuery(function($) {
    $('.add-button').on('click', function() {
        BS.SlackNotifierDialog.showCentered();
    });
});
