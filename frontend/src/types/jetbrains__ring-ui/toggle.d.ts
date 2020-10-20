declare module '@jetbrains/ring-ui/components/toggle/toggle' {
  import React, {InputHTMLAttributes} from 'react'

  export interface ToggleProps extends InputHTMLAttributes<HTMLInputElement> {
    leftLabel?: React.ReactNode
    pale?: boolean
    theme?: string
    'data-test'?: string
  }

  export default class Toggle extends React.Component<ToggleProps> {}
}
