declare module '@jetbrains/ring-ui/components/global/url' {
  export interface QueryParams {
    [k: string]: string
  }

  export function getBaseURI(): string | undefined
  export function getAbsoluteBaseURL(): string
  export function joinBaseURLAndPath(baseUrl: string, path: string): string
  export function parseQueryString(queryString: string): QueryParams
  export function isDataURI(uri: string): boolean
  export function getOrigin(uri: string): string | undefined
}

declare module '@jetbrains/ring-ui/components/global/get-uid' {
  export default function getUID(name: string): string
}

declare module '@jetbrains/ring-ui/components/global/sniffer' {
  //https://github.com/antivanov/sniffr/blob/master/src/sniffr.js
  enum BROWSER_NAME {
    firefox = 'firefox',
    chrome = 'chrome',
    ie = 'ie',
    safari = 'safari',
    edge = 'edge',
  }

  enum OS {
    linux = 'linux',
    macos = 'macos',
    windows = 'windows',
    ios = 'ios',
    openbsd = 'openbsd',
    android = 'android',
  }

  interface SnifferProperty<T extends string> {
    name: T
    version: string[]
    versionString: string
  }

  interface Sniffer {
    os: SnifferProperty<OS>
    device: SnifferProperty<string>
    browser: SnifferProperty<BROWSER_NAME>
  }

  const sniffer: Sniffer

  export default sniffer
}
