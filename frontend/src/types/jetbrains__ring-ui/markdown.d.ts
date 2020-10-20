declare module '@jetbrains/ring-ui/components/markdown/markdown' {
  import React from 'react'

  export interface Props {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
    inline?: boolean
    source: string
    renderers?: {[k: string]: React.ComponentClass}
  }

  export default class Markdown extends React.Component<Props> {}
}
