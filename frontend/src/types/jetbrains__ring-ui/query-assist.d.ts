declare module '@jetbrains/ring-ui/components/query-assist/query-assist' {
  import * as React from 'react'

  export interface QueryAssistProps {
    theme?: string
    className?: string
    popupClassName?: string
    clear?: boolean
    glass?: boolean
    hint?: string
    hintOnSelection?: string
    placeholder?: string
    loader?: boolean
    dataSource: (params: {query: string; caret: number}) => void
    onApply?: (params: {
      query: string
      caret: number
      focus: boolean
      suggestionsQuery: string
      dirty: boolean
    }) => void
    query?: string
    onFocusChange?: (state: {focus: boolean}) => void
  }

  export default class QueryAssist extends React.Component<QueryAssistProps> {
    handleApply: () => {}
  }
}
