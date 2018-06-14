<%@ include file="/include.jsp" %>

<script type="text/javascript">
    var rootUrl = "${serverSummary.rootURL}/".replace(/^https?:\/\//, '').split('/', 2)[1];

    window.slackNotifier = {
       buildSettingListUrl: rootUrl + "${buildSettingListUrl}?buildTypeId=${buildTypeId}",
       buildSettingEditUrl: rootUrl + "${buildSettingEditUrl}?buildTypeId=${buildTypeId}",
       buildSettingTryUrl: rootUrl + "${buildSettingTryUrl}",
       buildSettingSaveUrl: rootUrl + "${buildSettingSaveUrl}?buildTypeId=${buildTypeId}",
       buildSettingDeleteUrl: rootUrl + "${buildSettingDeleteUrl}"
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
