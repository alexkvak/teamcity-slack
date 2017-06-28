<%@ include file="/include.jsp" %>

<script type="text/javascript">
    window.slackNotifierUrl = {
       buildSettingListUrl: "${buildSettingListUrl}",
       buildSettingEditUrl: "${buildSettingEditUrl}",
       buildSettingSaveUrl: "${buildSettingSaveUrl}",
    }
</script>

<div class="buildSettingsList">
    <div id="ajaxContainer" class="center"></div>
</div>

<bs:modalDialog formId="slackNotifier"
                title="Edit"
                action="#"
                saveCommand="BS.SlackNotifierDialog.saveBuildConfig()"
                closeCommand="BS.SlackNotifierDialog.close()">
    Loading...
</bs:modalDialog>
