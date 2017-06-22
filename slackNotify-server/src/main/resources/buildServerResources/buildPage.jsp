<%@ include file="/include.jsp" %>

<script type="text/javascript">
    jQuery(function() {
        BS.ProgressPopup.showProgress("ajaxContainer", "Loading...");
        BS.ajaxUpdater("ajaxContainer", "/app/slackNotify/buildContentPage.html", {
            onSuccess: function () {
                BS.ProgressPopup.hidePopup()
            }
        });
    });
</script>

<div class="buildSettingsList">
    <div id="ajaxContainer" class="center"></div>
</div>

<bs:modalDialog formId="slackNotifierDialog"
                title="Edit"
                action="#"
                saveCommand="BS.SlackNotifierDialog.close();"
                closeCommand="BS.SlackNotifierDialog.close();">
</bs:modalDialog>
