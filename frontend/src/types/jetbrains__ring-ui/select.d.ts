/* eslint-disable no-magic-numbers */

declare module '@jetbrains/ring-ui/components/select/select' {
  import {ListDataItem, ListType} from '@jetbrains/ring-ui/components/list/list'
  import * as React from 'react'

  export enum SelectType {
    BUTTON = 'BUTTON',
    INPUT = 'INPUT',
    CUSTOM = 'CUSTOM',
    INLINE = 'INLINE',
    MATERIAL = 'MATERIAL',
    INPUT_WITHOUT_CONTROLS = 'INPUT_WITHOUT_CONTROLS',
  }

  export type SelectOption = ListDataItem

  // eslint-disable-next-line no-empty-pattern
  type CustomAnchor = ({}: {
    wrapperProps: {}
    buttonProps: {}
    popup: () => React.ReactNode
  }) => React.ReactNode

  interface MultipleConfig {
    selectAll?: boolean
    limit?: number
  }

  type MultipleValue = true | MultipleConfig

  interface SelectProps<Multiple extends MultipleValue | false = false> {
    className?: string
    multiple?: Multiple | MultipleConfig
    allowAny?: boolean
    filter?: boolean | object

    getInitial?: Function
    onClose?: Function
    onOpen?: Function
    onDone?: Function
    onFilter?: (query: string) => void
    onChange?: Multiple extends MultipleValue
      ? (option: SelectOption[]) => void
      : (option: SelectOption | null) => void
    onReset?: Function
    onLoadMore?: Function
    onAdd?: Function
    onBeforeOpen?: Function
    onSelect?: (option: SelectOption) => void
    onDeselect?: Function
    onFocus?: Function
    onBlur?: Function
    onKeyDown?: Function

    selected?: Multiple extends MultipleValue ? SelectOption[] : SelectOption | null
    data?: SelectOption[]
    loading?: boolean
    loadingMessage?: string
    notFoundMessage?: string
    maxHeight?: number
    minWidth?: number
    directions?: string[]
    popupClassName?: string
    popupStyle?: {[k: string]: string}
    top?: number
    left?: number
    renderOptimization?: boolean
    ringPopupTarget?: string
    hint?: string
    add?: {[k: string]: any}
    tags?: {
      reset?:
        | boolean
        | {
            label?: string
            separator?: boolean
            glyph?: string
          }
    }
    type?: SelectType
    disabled?: boolean
    hideSelected?: boolean
    label?: string
    selectedLabel?: string
    inputPlaceholder?: string
    clear?: boolean
    hideArrow?: boolean
    compact?: boolean
    size?: string
    theme?: Theme
    //eslint-disable-next-line no-empty-pattern, max-len
    customAnchor?: CustomAnchor
    disableMoveOverflow?: boolean
  }

  export default class Select<
    Multiple extends MultipleValue | false = false
  > extends React.Component<SelectProps<Multiple>> {
    static Type: typeof SelectType
  }
}
