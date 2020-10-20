declare module '@jetbrains/ring-ui/components/dropdown/dropdown' {
  import React, {MouseEventHandler, CSSProperties} from 'react'

  export interface DropdownProps {
    className?: string
    style?: CSSProperties
    'data-test'?: string
    children?: React.ReactNode
    anchor: React.ReactNode
    initShown?: boolean
    activeClassName?: string
    clickMode?: boolean
    hoverMode?: boolean
    hoverShowTimeOut?: number
    hoverHideTimeOut?: number
    onShow?: () => {}
    onHide?: () => {}
    onMouseEnter?: MouseEventHandler
    onMouseLeave?: MouseEventHandler
  }

  export default class Dropdown extends React.Component<DropdownProps> {}
}
