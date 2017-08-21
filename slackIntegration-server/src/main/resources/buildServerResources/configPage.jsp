<%@ include file="/include.jsp" %>

<c:if test="${not empty error}">
    <b>${error}</b>
</c:if>
<form action="/app/slackIntegration/config" method="post">
    <table class="runnerFormTable">
        <tbody>
            <tr class="groupingTitle">
                <td colspan="2">Slack Credentials</td>
            </tr>
            <tr>
                <td>
                    <label for="oauthKey">OAuth Access Token</label>
                </td><td>
                    <input type="text" id="oauthKey" name="oauthKey" value="${oauthKey}" class="longField">
                </td>
            </tr>
            <tr class="groupingTitle">
                <td colspan="2">Common settings</td>
            </tr>
            <tr>
                <td>
                    <label for="publicUrl">Public artifacts URL</label>
                </td><td>
                    <input type="text" id="publicUrl" name="publicUrl" value="${publicUrl}" class="longField">
                </td>
            </tr>
        </tbody>
    </table>
    <input type="submit" class="btn btn-default" value="Submit">
</form>
