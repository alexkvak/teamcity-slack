<%@ include file="/include.jsp" %>

<script type="text/javascript">
    window.slackNotifierUrl = {
       buildSettingListUrl: "${buildSettingListUrl}",
       buildSettingEditUrl: "${buildSettingEditUrl}",
    }
</script>

<div class="buildSettingsList">
    <div id="ajaxContainer" class="center"></div>
</div>

<bs:modalDialog formId="slackNotifier"
                title="Edit"
                action="#"
                saveCommand="BS.SlackNotifierDialog.close();"
                closeCommand="BS.SlackNotifierDialog.close();">
    Loading...
</bs:modalDialog>
