import Plugin from "@jetbrains/teamcity-api/plugin"
import {App} from './App/App'

new Plugin(Plugin.placeIds.SAKURA_BUILD_CONFIGURATION_BEFORE_CONTENT, {
    name: "Sakura UI Plugin",
    content: App,
})
