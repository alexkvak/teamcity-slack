/* eslint-disable react/no-multi-comp */
interface Theme {
  LIGHT: string
  DARK: string
}

declare module '@jetbrains/ring-ui/components/loader-inline/loader-inline' {
  import * as React from 'react'

  interface LoaderInlineProps {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
  }

  export default class LoaderInline extends React.Component<LoaderInlineProps> {}
}

declare module '@jetbrains/ring-ui/components/panel/panel' {
  import * as React from 'react'

  interface PanelProps extends React.HTMLAttributes<HTMLDivElement> {
    className?: string
  }

  export default class Panel extends React.Component<PanelProps> {}
}

declare module '@jetbrains/ring-ui/components/group/group' {
  import * as React from 'react'

  interface GroupProps extends React.HTMLAttributes<HTMLSpanElement> {
    className?: string
  }

  export default class Group extends React.Component<GroupProps> {}
}

declare module '@jetbrains/ring-ui/components/text/text' {
  import * as React from 'react'

  interface TextProps extends React.HTMLAttributes<HTMLSpanElement> {
    info?: boolean
  }

  export default class Text extends React.Component<TextProps> {}
}

declare module '@jetbrains/ring-ui/components/button/button' {
  import * as React from 'react'
  import React, {
    ButtonHTMLAttributes,
    MouseEvent,
    LinkHTMLAttributes,
    MouseEvent,
    HTMLAttributes,
    CSSProperties,
    CSSProperties,
    HTMLAttributes,
    MouseEventHandler,
  } from 'react'

  export interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
    theme?: string
    active?: boolean
    danger?: boolean
    delayed?: boolean
    loader?: boolean
    primary?: boolean
    short?: boolean
    text?: boolean
    inline?: boolean
    dropdown?: boolean
    href?: string
    icon?: string | Function
    iconSize?: number
    iconClassName?: string
    iconSuppressSizeWarning?: boolean
    target?: string
    download?: string
    rel?: string
  }

  export default class Button extends React.Component<ButtonProps> {}
}

declare module '@jetbrains/ring-ui/components/link/link' {
  export interface LinkProps extends LinkHTMLAttributes<HTMLLinkElement> {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
    innerClassName?: string
    active?: boolean
    inherit?: boolean
    pseudo?: boolean
    hover?: boolean
    href?: string
    target?: string
    disabled?: boolean
    onPlainLeftClick?: Function
    onClick?: (e: MouseEvent) => void
    rel?: string
  }

  export default class Link extends React.Component<LinkProps> {}
}

declare module '@jetbrains/ring-ui/components/button-group/button-group' {
  interface ButtonGroupProps extends HTMLAttributes<HTMLDivElement> {
    className?: string
    'data-test'?: string
  }

  export default class ButtonGroup extends React.Component<ButtonGroupProps> {}
}

declare module '@jetbrains/ring-ui/components/global/theme' {
  const Theme: Theme
  export default Theme
}

declare module '@jetbrains/ring-ui/components/avatar/avatar' {
  interface AvatarProps {
    dpr?: number
    className?: string
    size?: number
    style?: CSSProperties
    url: string
    round?: boolean
  }

  export default class Avatar extends React.Component<AvatarProps> {}
}

declare module '@jetbrains/ring-ui/components/error-message/error-message' {
  interface Props {
    icon?: string
    code?: string
    message: string
    description?: string
  }

  export default class ErrorMessage extends React.Component<Props> {}
}

declare module '@jetbrains/ring-ui/components/heading/heading' {
  interface Props extends HTMLAttributes<HTMLHeadingElement> {
    className?: string
    children?: React.ReactNode
    level?: number
    onClick?: (e: MouseEvent) => void
  }

  export default class Heading extends React.Component<Props> {}

  type WithoutLevelProps = Omit<Props, 'level'>

  export class H1 extends React.Component<WithoutLevelProps> {}
  export class H2 extends React.Component<WithoutLevelProps> {}
  export class H3 extends React.Component<WithoutLevelProps> {}
  export class H4 extends React.Component<WithoutLevelProps> {}
}

declare module '@jetbrains/ring-ui/components/tag/tag' {
  interface Props {
    className?: string
    children: React.ReactNode
    onRemove?: MouseEventHandler<HTMLSpanElement>
    onClick?: MouseEventHandler<HTMLDivElement>
    rgTagIcon?: string | Function
    icon?: string
    avatar?: string
    rgTagTitle?: string
    readOnly?: boolean
    disabled?: boolean
    focused?: boolean
    angled?: boolean
  }

  export default class Tag extends React.Component<Props> {}
}

declare module '@jetbrains/ring-ui/components/analytics/analytics' {
  interface Plugin {
    trackPageView: (path: string) => void
    trackEvent: (category: string, action: string, additionalData?: {}) => void
  }
  class Analytics implements Plugin {
    config: (plugins: Plugin[]) => void
    trackPageView: (path: string) => void
    trackEvent: (category: string, action: string, additionalData?: {}) => void
  }

  const analytics: Analytics
  export default analytics
}

// Wildcard for all Ring UI modules
declare module '@jetbrains/ring-ui/components/*'
declare module '@jetbrains/ring-ui/webpack.config'
