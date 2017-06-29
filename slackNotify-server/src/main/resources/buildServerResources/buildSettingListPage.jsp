<%@ include file="/include.jsp" %>

<c:if test="${fn:length(list) > 0 }">
    <table class="runnerFormTable">
        <tr>
            <th>Branch mask</th>
            <th>Channel</th>
            <th>Options</th>
            <th></th>
        </tr>
        <c:forEach items="${list}" var="item">
            <tr data-id="${item.key}">
                <td>${item.value.branchMask}</td>
                <td>${item.value.slackChannel}</td>
                <td></td>
                <td>
                    <a href="#" class="js-edit">Edit</a>
                    <a href="#" class="js-delete">Remove</a>
                </td>
            </tr>
        </c:forEach>
    </table>
</c:if>

<div class="saveButtonsBlock">
    <input value="Add" class="btn btn_primary add-button submitButton" name="addButton" type="submit">
</div>