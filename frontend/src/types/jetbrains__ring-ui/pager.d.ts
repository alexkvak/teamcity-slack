declare module '@jetbrains/ring-ui/components/pager/pager' {
  import React from 'react'

  export interface PagerProps {
    total: number
    currentPage?: number
    pageSize?: number
    pageSizes?: number[]
    visiblePagesLimit?: number
    disablePageSizeSelector?: boolean
    openTotal?: boolean
    canLoadLastPageWithOpenTotal?: boolean
    onPageChange?: Function
    onPageSizeChange?: Function
    onLoadPage?: Function
    className?: string
    translations?: {
      string: string
    }
    loader?: boolean
    hrefFunc?: Function
  }

  export default class Input extends React.Component<PagerProps> {}
}
