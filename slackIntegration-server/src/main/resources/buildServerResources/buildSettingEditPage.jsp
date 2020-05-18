<%@ taglib prefix="forms" uri="http://www.springframework.org/tags/form" %>
<%@ include file="/include.jsp" %>

<div class="editPane">
  <div class="tabContent" id="generalTab-content">
    <div class="group">
      <label class="tableLabel" for="branchMask">Branch:</label>
      <span><forms:textField className="mediumField textField" name="branchMask" expandable="false" value="${model.branchMask}"/></span>
      <div class="smallNote">Regular expressions are allowed. </div>
      <div class="smallNote">For all branches, use .*</div>
    </div>
    <div class="group">
      <label class="tableLabel" for="slackChannel">Slack channel:</label>
      <span><forms:textField className="mediumField textField" name="slackChannel" expandable="false" value="${model.slackChannel}"/></span>
    </div>
    <div class="group notify-committer">
      <label class="tableLabel" for="notifyCommitter">and/or notify committers:</label>
      <input type="checkbox" name="notifyCommitter" id="notifyCommitter" value="1" <c:if test="${model.notifyCommitter}">checked</c:if>/>
    </div>
    <div class="group">
      <label class="tableLabel" for="maxVcsChanges">Max VCS changes count:</label>
      <span><input className="mediumField textField" name="maxVcsChanges" expandable="false" type="number" min="1" max="300" step="1" value="${model.maxVcsChanges}"/></span>
    </div>
    <div class="message-template">
      <label class="tableLabel" for="messageTemplate">Message template:</label>
      <span><forms:textField className="mediumField textAreaField" name="messageTemplate" expandable="true"
                             value="${model.messageTemplate}" defaultText="${defaultMessage}"/>
      <div class="smallNote">
        See supported variables <a href="https://github.com/alexkvak/teamcity-slack/blob/master/README.md#message-placeholders" target="_blank">here</a>.
      </div>
      </span>
    </div>
    <div class="group">
      <label class="tableLabel" for="artifactsMask">Artifacts mask:</label>
      <span><forms:textField className="mediumField textField" name="artifactsMask" expandable="false" value="${model.artifactsMask}"/></span>
      <div class="smallNote">Regular expressions are allowed</div>
    </div>
    <div class="group">
      <label class="tableLabel" for="deepLookup">Lookup artifacts deep into:</label>
      <input type="checkbox" name="deepLookup" id="deepLookup" value="1" <c:if test="${model.deepLookup}">checked</c:if>/>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="success" value="1" <c:if test="${model.success}">checked</c:if>/>
        Trigger when build is successful
      </label>
      <label class="flagLabel indented">
        <input type="checkbox" data-parent="success" name="failureToSuccess" value="1" <c:if test="${model.failureToSuccess}">checked</c:if>/>
        Only trigger when build changes from Failure to Success
      </label>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="fail" value="1" <c:if test="${model.fail}">checked</c:if>/>
        Trigger when build is failed
      </label>
      <label class="flagLabel indented">
        <input type="checkbox" data-parent="fail" name="successToFailure" value="1" <c:if test="${model.successToFailure}">checked</c:if>/>
        Only trigger when build changes from Success to Failure
      </label>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="started" value="1" <c:if test="${model.started}">checked</c:if>/>
        Trigger when build is started
      </label>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="canceled" value="1" <c:if test="${model.canceled}">checked</c:if>/>
        Trigger when build is canceled
      </label>
    </div>
    <div class="checkboxes-group">
      <label class="flagLabel">
        <input type="checkbox" name="queued" value="1" <c:if test="${model.queued}">checked</c:if>/>
        Trigger when build is queued
	<small>(some placeholders will not be accessible)</small>
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
