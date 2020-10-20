declare module '@jetbrains/ring-ui/components/auth/auth__core' {
  import HTTP from '@jetbrains/ring-ui/components/http/http'

  export class LoginFlow {
    public constructor(requestBuilder: Function, storage: WindowLocalStorage)

    public authorize(): Promise<void>
  }

  interface AuthUser {
    guest: boolean
    id: string
    name: string
    login: string
    profile: {
      avatar: {
        url: string
      }
    }
  }

  export interface AuthConfig {
    reloadOnUserChange?: boolean
    embeddedLogin?: boolean
    EmbeddedLoginFlow?: LoginFlow | Function
    serverUri: string
    clientId: string
    redirectUri: string
    redirect?: boolean
    requestCredentials?: string
    backgroundRefreshTimeout?: number
    scope?: string[]
    optionalScopes?: string[]
    userFields?: string[]
    cleanHash?: boolean
    onLogout?: Function
    onPostponeChangedUser?: () => {}
    onPostponeLogout?: () => {}
    enableBackendStatusCheck?: boolean
    backendCheckTimeout?: boolean
    checkBackendIsUp?: () => Promise<void>
    onBackendDown?: () => {}

    defaultExpiresIn?: number
  }

  export default class Auth {
    public constructor(config: AuthConfig)

    public init(): Promise<string>
    public requestToken(): Promise<string>
    public login(): Promise<void>
    public user: undefined | AuthUser
    public requestUser(): Promise<AuthUser> | AuthUser
    public http: HTTP
    public config: AuthConfig
  }
}
