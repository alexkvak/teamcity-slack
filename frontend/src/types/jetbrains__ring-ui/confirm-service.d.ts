declare module '@jetbrains/ring-ui/components/confirm-service/confirm-service' {
  interface ConfirmOptions {
    text: string
    description?: string | React.ReactNode
    confirmLabel?: string
    rejectLabel?: string
    cancelIsDefault?: boolean
    onBeforeConfirm?: Function
  }

  const confirm: (options: ConfirmOptions) => Promise<void>
  export default confirm
}
