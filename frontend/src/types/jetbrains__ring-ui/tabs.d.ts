/* eslint-disable react/no-multi-comp */
declare module '@jetbrains/ring-ui/components/tabs/tabs' {
  import React from 'react'

  export interface TabProps {
    className?: string
    children: React.ReactNode
    'data-test'?: string
    title: Function | React.ReactNode
    id?: string
    href?: string
    disabled?: boolean
  }

  export class Tab extends React.Component<TabProps> {}

  export interface TabsProps {
    className?: string
    children: React.ReactNode[]
    'data-test'?: string
    selected?: string
    onSelect?: (key: string) => void
    theme?: Theme
  }

  export class Tabs extends React.Component<TabsProps> {}

  export interface SmartTabsProps extends TabsProps {
    initSelected?: string
  }

  export class SmartTabs extends React.Component<SmartTabsProps> {}

  export const CustomItem: React.FC<{children: React.ReactNode}>
}
