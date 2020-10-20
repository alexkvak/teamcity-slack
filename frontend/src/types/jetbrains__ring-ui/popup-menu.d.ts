declare module '@jetbrains/ring-ui/components/popup-menu/popup-menu' {
  import List, {ListProps} from '@jetbrains/ring-ui/components/list/list'
  import {PopupProps} from '@jetbrains/ring-ui/components/popup/popup'
  import React, {CSSProperties} from 'react'

  export interface PopupMenuProps extends PopupProps, ListProps {
    closeOnSelect?: boolean
  }

  export default class Popup extends React.Component<PopupMenuProps> {
    list: List
  }
}
