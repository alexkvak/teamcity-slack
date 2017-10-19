jQuery(function ($) {
    function toggle(show) {
        if (show) {
            $('.js-toggable').show();
        } else {
            $('.js-toggable').hide();
        }
    }
    $('.js-enabled').click(function() {
        toggle($(this).prop('checked'));
    });

    toggle($('.js-enabled').prop('checked'));
});