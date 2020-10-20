import {MutableRefObject} from 'react'

type Merge<M, N> = Omit<M, Extract<keyof M, keyof N>> & N

declare module '@jetbrains/ring-ui/components/input/input' {
  import React, {Ref, InputHTMLAttributes} from 'react'

  export enum Size {
    AUTO = 'Auto',
    S = 'S',
    M = 'M',
    L = 'L',
    FULL = 'FULL',
  }

  export interface InputProps {
    className?: string
    'data-test'?: string
    children?: React.ReactNode
    value?: string
    theme?: string
    inputClassName?: string
    size?: Size | keyof typeof Size
    label?: string
    active?: boolean
    compact?: boolean
    error?: string | null
    multiline?: boolean
    borderless?: boolean
    onClear?: Function
    onChange?: Function
    inputRef?: Ref<HTMLInputElement> | MutableRefObject<HTMLInputElement | undefined>
    disabled?: boolean
    type?: string
    autoFocus?: boolean
    loading?: boolean
  }

  type NativeInputProps = InputHTMLAttributes<HTMLInputElement>

  export default class Input extends React.Component<Merge<NativeInputProps, InputProps>> {}
}
