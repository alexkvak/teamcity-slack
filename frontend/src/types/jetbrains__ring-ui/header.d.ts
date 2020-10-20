/* eslint-disable react/no-multi-comp */

declare module '@jetbrains/ring-ui/components/header/header' {
  import Auth from '@jetbrains/ring-ui/components/auth/auth__core'
  import {ButtonProps} from '@jetbrains/ring-ui/components/button/button'
  import {ProfileProps} from '@jetbrains/ring-ui/components/header/profile'
  import {IconProps} from '@jetbrains/ring-ui/components/icon/icon'
  import * as React from 'react'

  interface HeaderProps {
    className?: string
    children?: React.ReactNode
    spaced?: boolean
    theme?: string
  }

  interface TrayProps {
    className?: string
    children?: React.ReactNode
  }

  export default class Header extends React.Component<HeaderProps> {}

  export class Tray extends React.Component<TrayProps> {}
  export class Logo extends React.Component<IconProps> {
    static Size: {[k: string]: number}
  }
  export class TrayIcon extends React.Component<ButtonProps> {}
}

declare module '@jetbrains/ring-ui/components/header/profile' {
  import React from 'react'

  export interface ProfileProps {
    className?: string
    closeOnSelect?: boolean
    hasUpdates?: boolean
    loading?: boolean
    onLogin?: () => void
    onLogout?: () => void
    onSwitchUser?: Function
    profileUrl?: string
    renderPopupItems?: Function
    LinkComponent?: React.Component | React.FunctionComponent | string
    translations?: {
      applyChangedUser?: string
      login?: string
      profile?: string
      switchUser?: string
      logout?: string
      certificateMismatch?: string
    }
    user?: {
      guest: boolean
      profile: string
    }
    size?: number
    round?: boolean
    showLogIn?: boolean
    showLogOut?: boolean
    showSwitchUser?: boolean
    showApplyChangedUser?: boolean
    onRevertPostponement?: () => void
    renderGuest?: (props: ProfileProps) => React.ReactNode
  }

  export default class Profile extends React.Component<ProfileProps> {}
}

declare module '@jetbrains/ring-ui/components/header/smart-profile' {
  export interface SmartProfileProps extends ProfileProps {
    auth: Auth
  }

  export default class SmartProfile extends React.Component<SmartProfileProps> {}
}
