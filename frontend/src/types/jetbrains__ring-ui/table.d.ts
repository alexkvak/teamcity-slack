declare module '@jetbrains/ring-ui/components/table/selection' {
  interface CloneWith<T> {
    data?: T[]
    selected?: Set<T>
    focused?: T
  }

  type Options = {
    data?: T[]
    selected?: typeof Set
    focused?: boolean | null
    getKey?: () => string
    getChildren?: () => T[]
    isItemSelectable?: () => boolean
  }

  export default class TableSelection<T> {
    constructor(Options?)

    cloneWith: ({data, selected, focused}: CloneWith<T>) => TableSelection<T>
    focus: (value: T | null) => TableSelection<T>
    moveUp: () => TableSelection<T> | undefined
    moveDown: () => TableSelection<T> | undefined
    moveStart: () => TableSelection<T> | undefined
    moveEnd: () => TableSelection<T> | undefined
    select: () => TableSelection<T>
    deselect: () => TableSelection<T>
    toggleSelection: () => TableSelection<T>
    selectAll: () => TableSelection<T>
    resetFocus: () => TableSelection<T>
    resetSelection: () => TableSelection<T>
    reset: () => TableSelection<T>
    isFocused: (value: T) => boolean
    isSelected: (value: T) => boolean
    getFocused: () => T
    getSelected: () => Set<T>
    getActiveSelected: () => Set<T>
  }
}

declare module '@jetbrains/ring-ui/components/table/table' {
  import TableSelection from '@jetbrains/ring-ui/components/table/selection'
  import TableSelection from '@jetbrains/ring-ui/components/table/selection'
  import {TableProps} from '@jetbrains/ring-ui/components/table/table'
  import React from 'react'

  export interface Column<T> {
    id: keyof T | string
    title: string
    sortable?: boolean
    rightAlign?: boolean
    getValue?: (item: T) => React.ReactNode
    className?: string
  }

  export interface TableProps<T> {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
    loaderClassName?: string
    data: T[]
    columns: Column<T>[]
    caption?: string
    isItemSelectable?: (item: T) => boolean
    stickyHeader?: boolean
    stickyHeaderOffset?: string
    loading?: boolean
    getItemKey?: (item: T) => string
    onSort?: (value: {column: Column<T>; order: boolean}) => void
    onReorder?: () => number
    sortKey?: string | null
    sortOrder?: boolean
    draggable?: boolean
    alwaysShowDragHandle?: boolean
    getItemLevel?: (item: T) => number
    isItemCollapsible?: (item: T) => void
    isParentCollapsible?: (item: T) => void
    isItemCollapsed?: (item: T) => void
    onItemCollapse?: (item: T) => void
    onItemExpand?: (item: T) => void
    isDisabledSelectionVisible?: Function
    getCheckboxTooltip?: () => string
    // focusSensorHOC
    focused?: boolean
    onFocusRestore?: Function

    // selectionShortcutsHOC
    selection?: TableSelection<T>
    selectable?: boolean
    onSelect?: (selected: TableSelection<T>) => void
    shortcutsMap?: {}

    // disableHoverHOC
    disabledHover?: boolean

    remoteSelection?: boolean
  }

  export default class Table<T> extends React.Component<TableProps<T>> {}
}

declare module '@jetbrains/ring-ui/components/table/smart-table' {
  import React from 'react'

  interface SmartTableProps<T> extends TableProps<T> {
    onSelectionChange: (selection: TableSelection<T>) => void
  }

  // eslint-disable-next-line react/no-multi-comp
  export default class SmartTable<T> extends React.Component<SmartTableProps<T>> {}
}
