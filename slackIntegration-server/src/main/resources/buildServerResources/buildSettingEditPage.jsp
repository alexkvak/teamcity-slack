<%@ taglib prefix="forms" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/include.jsp" %>

<div class="editPane">
  <div class="tabContent" id="generalTab-content">
    <div class="group">
      <label class="tableLabel" for="branchMask">Branch:</label>
      <span><forms:textField className="mediumField textField" name="branchMask" expandable="false" value="${model.branchMask}"/></span>
      <div class="smallNote">Regular expressions allowed</div>
    </div>
    <div class="group">
      <label class="tableLabel" for="slackChannel">Slack channel:</label>
      <span><forms:textField className="mediumField textField" name="slackChannel" expandable="false" value="${model.slackChannel}"/></span>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="success" value="1" <c:if test="${model.success}">checked</c:if>/>
        Trigger when build is Successful
      </label>
      <label class="flagLabel indented">
        <input type="checkbox" data-parent="success" name="failureToSuccess" value="1" <c:if test="${model.failureToSuccess}">checked</c:if>/>
        Only trigger when build changes from Failure to Success
      </label>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="fail" value="1" <c:if test="${model.fail}">checked</c:if>/>
        Trigger when build Fails
      </label>
      <label class="flagLabel indented">
        <input type="checkbox" data-parent="fail" name="successToFailure" value="1" <c:if test="${model.successToFailure}">checked</c:if>/>
        Only trigger when build changes from Success to Failure
      </label>
    </div>
  </div>

  <div class="status">
    <div class="error" style="display: none;"></div>
  </div>

  <div class="buttons saveButtonsBlock">
    <button class="btn btn_primary submitButton">Save</button>
    <button type="button" class="btn cancel closeDialog">Cancel</button>
  </div>
  <input type="hidden" name="key" value="${key}" />
</div>