declare module '@jetbrains/ring-ui/components/badge/badge' {
  import React, {CSSProperties} from 'react'

  interface BaseProps {
    className?: string
    children?: React.ReactNode
    'data-test'?: string
  }

  export interface BadgeProps extends BaseProps {
    gray?: boolean
    valid?: boolean
    invalid?: boolean
    disabled?: boolean

    title?: string
    style?: CSSProperties
  }

  export default class Badge extends React.Component<BadgeProps> {}
}
