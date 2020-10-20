declare module '@jetbrains/ring-ui/components/footer/footer' {
  import * as React from 'react'

  interface LinkItem {
    url?: string
    label: string
    copyright?: number
    title?: string
    target?: string
  }

  export type FooterItem = React.ReactNode | LinkItem | string

  interface FooterProps {
    className?: string
    floating?: boolean
    left?: FooterItem[]
    center?: FooterItem[]
    right?: FooterItem[]
  }

  export default class Footer extends React.Component<FooterProps> {}
}
