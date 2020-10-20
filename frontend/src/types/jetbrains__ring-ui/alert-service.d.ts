declare module '@jetbrains/ring-ui/components/alert-service/alert-service' {
  type Alert = {
    key: string
    message: string
    type: 'error' | 'message' | 'success' | 'warning' | 'loading'
  }

  export interface AlertService {
    setDefaultTimeout: (timeout: number) => void
    error: (message: string, timeout?: number) => string
    message: (message: string, timeout?: number) => string
    warning: (message: string, timeout?: number) => string
    successMessage: (message: string, timeout?: number) => string
    loadingMessage: (message: string, timeout?: number) => string
    removeWithoutAnimation: (message: string) => void
    showingAlerts: Alert[]
  }

  const alertService: AlertService
  export default alertService
}
