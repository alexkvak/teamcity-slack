<%@ include file="/include.jsp" %>

<script type="text/javascript">
    window.slackNotifier = {
       buildSettingListUrl: "${buildSettingListUrl}?buildTypeId=${buildTypeId}",
       buildSettingEditUrl: "${buildSettingEditUrl}?buildTypeId=${buildTypeId}",
       buildSettingTryUrl: "${buildSettingTryUrl}",
       buildSettingSaveUrl: "${buildSettingSaveUrl}?buildTypeId=${buildTypeId}",
       buildSettingDeleteUrl: "${buildSettingDeleteUrl}"
    };
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
