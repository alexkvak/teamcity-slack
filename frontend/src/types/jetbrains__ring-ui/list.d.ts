/* eslint-disable no-magic-numbers */
declare module '@jetbrains/ring-ui/components/list/list' {
  import {PopupProps} from '@jetbrains/ring-ui/components/popup/popup'
  import React, {CSSProperties} from 'react'

  export enum ListType {
    SEPARATOR = 0,
    LINK = 1,
    ITEM = 2,
    HINT = 3,
    CUSTOM = 4,
    TITLE = 5,
    MARGIN = 6,
  }

  export interface ListDataItem {
    rgItemType?: ListType
    label?: string | React.ReactNode
    key?: string | number
    className?: string
    avatar?: string
    subavatar?: string
    glyph?: string | Function
    description?: string
    onClick?: Function
    onMouseUp?: Function
    template?: React.ReactNode
    href?: string
    disabled?: boolean
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    [k: string]: any
  }

  export interface ListProps {
    className?: string
    hint?: string
    hintOnSelection?: string
    data?: ListDataItem[]
    maxHeight?: number
    activeIndex?: number
    restoreActiveIndex?: boolean
    activateSingleItem?: boolean
    activateFirstItem?: boolean
    shortcuts?: boolean
    onMouseOut?: Function
    onSelect?: (item: ListDataItem, event: Event) => void
    onScrollToBottom?: Function
    onResize?: Function
    useMouseUp?: boolean
    visible?: boolean
    renderOptimization?: boolean
    disableMoveOverflow?: boolean
    disableMoveDownOverflow?: boolean
    compact?: boolean
  }

  export default class List extends React.Component<ListProps> {
    static ListProps: {
      Type: typeof ListType
    }

    upHandler: (event: Event) => void
    downHandler: (event: Event) => void
    homeHandler: (event: Event) => void
    endHandler: (event: Event) => void
    moveHandler: (event: Event) => void
    enterHandler: (event: Event) => void
  }
}
