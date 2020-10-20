declare module '@jetbrains/ring-ui/components/shortcuts/shortcuts' {
  import React from 'react'

  export interface ShortcutsMap {
    [k: string]: (e: KeyboardEvent) => void | boolean
  }

  export interface ShortcutsProps {
    children?: React.ReactNode
    disabled?: boolean
    scope?: string
    map?: ShortcutsMap
    options?: {modal?: boolean}
  }

  export default class Shortcuts extends React.Component<ShortcutsProps> {}
}

declare module '@jetbrains/ring-ui/components/shortcuts/shortcut-title' {
  export function getShortcutTitle(shortcut: string): string
}
