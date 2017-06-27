<%@ include file="/include.jsp" %>

<div class="editPane">
  <div class="tabContent" id="generalTab-content">
    <div class="group">
      <span class="title first" for="branchMask">Branch:</span>
      <span><forms:textField className="fullSize" name="branchMask" expandable="false" value="${model.branchMask}"/></span>
      <div class="smallNote">Regular expressions allowed</div>
    </div>
    <div class="group">
      <span class="title first" for="slackChannel">Slack channel:</span>
      <span><forms:textField className="fullSize" name="slackChannel" expandable="false" value="${model.slackChannel}"/></span>
    </div>
  </div>

  <div class="status">
    <div class="successMessage" style="display: none;"></div>
    <div class="error" style="display: none;"></div>
  </div>

  <div class="buttons">
    <button class="btn btn_primary submitButton" data-id="${model.key}">Save</button>
    <button class="btn cancel closeDialog">Cancel</button>
  </div>
</div>