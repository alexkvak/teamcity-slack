<%@ include file="/include.jsp" %>

<c:url value="${saveConfigSubmitUrl}" var="submitUrl"/>

<c:if test="${not empty error}">
    <b>${error}</b>
</c:if>
<form action="${submitUrl}" method="post">
    <input type="hidden" name="tc-csrf-token" value="${tcCsrfToken}">
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
                    <label for="senderName">Sender name</label>
                </td><td>
                    <input type="text" id="senderName" name="senderName" value="${senderName}" class="longField">
                    <div class="smallNoteAttention">
                        Send message as other user. <br />
                        Please make sure that you granted the scope <code>chat:write.customize</code>
                    </div>
                </td>
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
                        Personal notifications enabled (only if build fails)
                    </label>
                </td>
            </tr>
            <tr class="js-toggable">
                <td colspan="2">
                    <label>
                        <input type="checkbox" id="sendAsAttachment" name="sendAsAttachment" value="1" <c:if test="${not empty sendAsAttachment}">checked</c:if>>
                        Send message as attachment. <br />
                        It adds vertical color line, but does not show message preview in push notifications.
                    </label>
                </td>
            </tr>
        </tbody>
    </table>
    <input type="submit" class="btn btn-default" value="Submit">
</form>
