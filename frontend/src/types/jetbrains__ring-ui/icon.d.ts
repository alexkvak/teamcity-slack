/* eslint-disable no-magic-numbers */

declare module '@jetbrains/ring-ui/components/icon/icon' {
  import React, {HTMLAttributes} from 'react'

  export interface IconProps extends HTMLAttributes<HTMLSpanElement> {
    className?: string
    'data-test'?: string
    color?: string
    glyph?: string | Function
    height?: number
    size?: number
    width?: number
    loading?: boolean
    suppressSizeWarning?: boolean
  }

  interface Size {
    Size14: number
  }

  export default class Icon extends React.Component<IconProps> {
    static Size: Size
  }
}
