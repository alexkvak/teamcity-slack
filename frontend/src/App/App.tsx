import {H2} from '@jetbrains/ring-ui/components/heading/heading'
import {React, utils} from "@jetbrains/teamcity-api"
import type {PluginContext} from "@jetbrains/teamcity-api"

import styles from './App.css'

interface Setting {
    buildTypeId: string;
    branchMask: string;
    slackChannel: string;
    messageTemplate: string;
    flags: unknown;
    artifactsMask: string;
    deepLookup: boolean;
    notifyCommitter: boolean;
    maxVcsChanges: number;
}

interface SettingsList {
    [key: string]: Setting;
}

export function App(context: PluginContext) {
    const [expanded, setExpanded] = React.useState(false)
    const toggleExpanded = React.useCallback(() => setExpanded(state => !state), [])

    const [settingsList, setSettingsList] = React.useState<SettingsList>({})

    React.useEffect(() => {
        utils.requestJSON(`/reactPlugin.html?buildTypeId=${context.location.buildTypeId}`).then(response => {
            setSettingsList(response)
        });
    }, [])

    return (
        <div className={styles.wrapper}>
            <H2 className={styles.name} onClick={toggleExpanded}>
                {`Slack configuration`}
            </H2>
            {expanded && (
                <div>
                    {Object.entries(settingsList).map(([key, setting]) => (
                        <div key={key} className={styles.item}>
                            <div>{setting.branchMask}</div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}

