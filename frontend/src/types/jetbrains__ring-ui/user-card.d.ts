/* eslint-disable react/no-multi-comp */
declare module '@jetbrains/ring-ui/components/user-card/user-card' {
  import {DropdownProps} from '@jetbrains/ring-ui/components/dropdown/dropdown'
  import React from 'react'

  interface Base {
    'data-test'?: string
    className?: string
    children?: React.ReactNode
  }

  export interface UserCardUser {
    name: string
    login: string
    avatarUrl: string
    email?: string
    href?: string
    online?: boolean
    banned?: boolean
    banReason?: string
  }

  interface UserCardProps extends Base {
    user: UserCardUser
    wording?: {
      banned: string
      online: string
      offline: string
    }
  }

  export class UserCard extends React.Component<UserCardProps> {}

  interface UserCardTooltipProps extends Base {
    dropdownProps?: DropdownProps
    user?: UserCardUser
    renderUserCard?: (props: UserCardTooltipProps) => React.ReactNode
    renderNoUser?: () => React.ReactNode
  }

  export class UserCardTooltip extends React.Component<UserCardProps> {}

  interface SmartUserCardTooltipProps extends Base {
    userDataSource: () => UserCardUser
  }

  export class SmartUserCardTooltip extends React.Component<UserCardProps> {}
}
