declare module '@jetbrains/ring-ui/components/tab-trap/tab-trap' {
  import React from 'react'

  export interface TabTrapProps {
    children?: React.ReactNode
    trapDisabled?: boolean
    autoFocusFirst?: boolean
    focusBackOnClose?: boolean
  }

  export default class TabTrap extends React.Component<TabTrapProps> {}
}
