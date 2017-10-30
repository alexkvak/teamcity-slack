# TeamCity Slack plugin

[![Build Status](https://travis-ci.org/alexkvak/teamcity-slack.svg?branch=master)](https://travis-ci.org/alexkvak/teamcity-slack)

This plugin allows you to integrate your CI with Slack.

The main feature is that you can specify not exact branch name but the [regexp branch mask](#build-configuration).

For example you have separate build configurations for feature branches and common branches. Then it is easy to
setup notifications into corresponding Slack channels.

The second big thing is that your can compose your own messages using [template placeholders](#message-placeholders) and [Slack formatting](https://api.slack.com/docs/message-formatting).

And you can easily send notification without running the build with *Try it* feature.

Plugin automatically backups its settings after each modification.

## Table of Contents
1. [Install plugin](#install-plugin)
2. [Build configuration](#build-configuration)
3. [Message placeholders](#message-placeholders)
4. [Artifact links](#artifact-links)

## Install plugin <a name="install-plugin"></a>
Download from [releases](https://github.com/alexkvak/teamcity-slack/releases) or compile 
sources with `mvn package`. 

Next upload `target/slackIntegration.zip` to TeamCity `data/plugins/` folder (restart is needed).

Go to [api.slack.com](https://api.slack.com/) and create new app. Then go to **OAuth & Permissions** and copy **Bot User OAuth Access Token**.

Paste this token into **Administration -> Slack -> OAuth Access Token** field.

![Plugin setup](_doc/plugin-setup.png "Plugin setup")

That's it! Now you can open any build configuration home and choose **Slack** tab.

By default personal notifications (private notifications) are disabled.

![Slack tab](_doc/slack-tab.png "Slack tab")
Only admins and projects admins have rights to access these settings. 

## Build configuration <a name="build-configuration"></a>

Sample configuration:

![Edit settings](_doc/edit-settings.png "Edit settings")

Notifications for branches captured by regular expression will be sent to slack channel.
Message will be compiled according to template.

The build settings number is not limited, so you can set up notifications for feature branches 
in one channel, and for release branches in another one.

## Message placeholders <a name="message-placeholders"></a>

###### {name} 
Full name of the build configuration, has form "project_name :: buildconfig_name".

###### {number}
User defined build number.

###### {branch}
Branch display name, i.e. name to show to user. *Unknown* if not applicable.

###### {status}
*succeeded* if build successful, *canceled* if canceled, *failed* otherwise.

###### {link}
URL to view results page.

###### {mentions}
Slack users mentions. Unknown users will be skipped.

###### {changes}
Concatenated description of head 5 changes from build with author name (from VCS) 
in square braces, e.g. *My awesome feature [John Smith]*.

###### {reason}
Build problems that caused build failure when build is failed. *Unknown* if cannot detect.

###### {artifactLinks}
See [Artifact links](#artifact-links).

###### {allArtifactsDownloadUrl}
Link to download all artifacts as zip archive.

###### {artifactsRelUrl}
Artifacts relative path. It is necessary if you want to construct artifact link manually.

###### Parameters placeholders
You can also use project and build parameters, e.g. *{%my.awesome.teamcity.param%}*


## Artifact links <a name="artifact-links"></a>
In case you want to access build artifacts with third party web server (e.g. nginx) you can use this feature.
Specify *Public artifacts URL* as root path served by your web server. 
And *Artifacts mask* in [Build configuration](#build-configuration).
All other will be done automatically.

Sample nginx configuration:
```
location /art/ {
    alias <teamcity-data-dir>/system/artifacts/;
    autoindex on;
}
```