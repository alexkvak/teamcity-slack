<%@ include file="/include.jsp" %>

<c:if test="${not empty error}">
    <b>${error}</b>
</c:if>
<form action="/app/slackIntegration/config" method="post">
    <table class="runnerFormTable">
        <tbody>
            <tr>
                <td colspan="2">
                    <label>
                        <input type="checkbox" class="js-enabled" id="enabled" name="enabled" value="1" <c:if test="${not empty enabled}">checked</c:if>>
                        Enabled
                    </label>
                </td>
            </tr>
            <tr class="groupingTitle js-toggable">
                <td colspan="2">Slack Credentials</td>
            </tr>
            <tr class="js-toggable">
                <td>
                    <label for="oauthKey">OAuth Access Token</label>
                </td><td>
                    <input type="text" id="oauthKey" name="oauthKey" value="${oauthKey}" class="longField">
                </td>
            </tr>
            <tr class="groupingTitle js-toggable">
                <td colspan="2">Common settings</td>
            </tr>
            <tr class="js-toggable">
                <td>
                    <label for="publicUrl">Public artifacts URL</label>
                </td><td>
                    <input type="text" id="publicUrl" name="publicUrl" value="${publicUrl}" class="longField">
                </td>
            </tr>
            <tr class="js-toggable">
                <td colspan="2">
                    <label>
                        <input type="checkbox" id="personalEnabled" name="personalEnabled" value="1" <c:if test="${not empty personalEnabled}">checked</c:if>>
                        Personal notifications enabled
                    </label>
                </td>
            </tr>
        </tbody>
    </table>
    <input type="submit" class="btn btn-default" value="Submit">
</form>
