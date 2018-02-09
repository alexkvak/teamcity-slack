<%@ include file="/include.jsp" %>

<script type="text/javascript">
    window.slackNotifier = {
       buildSettingListUrl: "${serverSummary.rootURL}${buildSettingListUrl}?buildTypeId=${buildTypeId}",
       buildSettingEditUrl: "${serverSummary.rootURL}${buildSettingEditUrl}?buildTypeId=${buildTypeId}",
       buildSettingTryUrl: "${serverSummary.rootURL}${buildSettingTryUrl}",
       buildSettingSaveUrl: "${serverSummary.rootURL}${buildSettingSaveUrl}?buildTypeId=${buildTypeId}",
       buildSettingDeleteUrl: "${serverSummary.rootURL}${buildSettingDeleteUrl}"
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
