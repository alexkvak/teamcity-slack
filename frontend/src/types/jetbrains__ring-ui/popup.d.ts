declare module '@jetbrains/ring-ui/components/popup/position' {
  export enum Directions {
    BOTTOM_RIGHT = 'BOTTOM_RIGHT',
    BOTTOM_LEFT = 'BOTTOM_LEFT',
    BOTTOM_CENTER = 'BOTTOM_CENTER',
    TOP_LEFT = 'TOP_LEFT',
    TOP_RIGHT = 'TOP_RIGHT',
    TOP_CENTER = 'TOP_CENTER',
    RIGHT_TOP = 'RIGHT_TOP',
    RIGHT_BOTTOM = 'RIGHT_BOTTOM',
    RIGHT_CENTER = 'RIGHT_CENTER',
    LEFT_TOP = 'LEFT_TOP',
    LEFT_BOTTOM = 'LEFT_BOTTOM',
    LEFT_CENTER = 'LEFT_CENTER',
  }
}

declare module '@jetbrains/ring-ui/components/popup/popup' {
  import {Directions} from '@jetbrains/ring-ui/components/popup/position'
  import React, {CSSProperties} from 'react'

  export interface PopupProps {
    className?: string
    'data-test'?: string
    children?: React.ReactNode

    anchorElement?: Node | null
    target?: string
    style?: CSSProperties
    offset?: number
    hidden?: boolean
    onOutsideClick?: (e: MouseEvent) => void
    onEscPress?: Function
    onCloseAttempt?: (e: MouseEvent | KeyboardEvent, isEscape: boolean) => void
    dontCloseOnAnchorClick?: boolean
    shortcuts?: boolean
    keepMounted?: boolean
    client?: boolean
    directions?: Directions[]
    autoPositioning?: boolean
    autoCorrectTopOverflow?: boolean
    left?: number
    top?: number
    maxHeight?: number
    minWidth?: number
    sidePadding?: number

    attached?: boolean

    onMouseDown?: Function
    onMouseUp?: Function
    onMouseOver?: Function
    onMouseOut?: Function
    onContextMenu?: Function
    onDirectionChange?: Function
  }

  export default class Popup extends React.Component<PopupProps> {}
}
