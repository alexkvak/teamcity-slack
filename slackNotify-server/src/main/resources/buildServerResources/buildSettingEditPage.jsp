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
  </div>

  <div class="status">
    <div class="error" style="display: none;"></div>
  </div>

  <div class="buttons saveButtonsBlock">
    <button class="btn btn_primary submitButton">Save</button>
    <button type="button" class="btn cancel closeDialog">Cancel</button>
  </div>
  <input type="hidden" name="key" value="${model.key}" />
</div>