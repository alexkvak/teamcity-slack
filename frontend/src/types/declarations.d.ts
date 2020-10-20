declare module '@jetbrains/logos/*'
declare module '@jetbrains/icons/*'

declare module '*.css' {
  const content: {readonly [className: string]: string}
  export default content
}

declare module '*.svg'
