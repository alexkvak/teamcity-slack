declare module '@jetbrains/ring-ui/components/tooltip/tooltip' {
  import React, {MouseEventHandler} from 'react'

  type SpanProps = JSX.IntrinsicElements['span']

  export interface BaseProps {
    delay?: number
    selfOverflowOnly?: boolean
    popupProps?: {}
    title?: string | JSX.Element
    children?: React.ReactNode
    'data-test'?: string
  }

  export type TooltipProps = SpanProps & BaseProps

  export default class Tooltip extends React.Component<TooltipProps> {}
}
