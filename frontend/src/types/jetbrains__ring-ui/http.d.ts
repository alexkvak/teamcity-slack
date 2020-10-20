declare module '@jetbrains/ring-ui/components/http/http' {
  import Auth from '@jetbrains/ring-ui/components/auth/auth__core'

  type QueryValue = string | number | boolean | null | undefined

  export class HTTPError extends Error {
    constructor(response: any, data: any)
    public data: any
    public status: number
  }

  interface AdditionalParams {
    headers?: {[k: string]: string | null} | null
    query?: {[k: string]: QueryValue | QueryValue[]} | null
    body?: {[k: string]: any} | null | FormData
    sendRawBody?: boolean
  }

  type HTTPParams = RequestInit | AdditionalParams

  export default class HTTP {
    public constructor(auth: Auth, baseUrl?: string, fetchConfig?: RequestInit)

    baseUrl: string

    fetchConfig: RequestInit

    request<T>(url: string, params?: HTTPParams): Promise<T>
    get<T>(url: string, params?: HTTPParams): Promise<T>
    post<T>(url: string, params?: HTTPParams): Promise<T>
  }
}

declare module '@jetbrains/ring-ui/components/http/http.mock' {
  import HTTP from '@jetbrains/ring-ui/components/http/http'

  export default class HTTPMock extends HTTP {
    public constructor()
    respondDefault(response: {}): void
    respondForUrl(url: string, response: {}): void
  }
}
