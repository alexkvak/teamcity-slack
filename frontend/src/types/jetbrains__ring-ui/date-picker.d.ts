declare module '@jetbrains/ring-ui/components/date-picker/date-picker' {
  import React from 'react'

  type DateType = null | string | number | Date

  export interface DatePickerProps {
    className?: string
    popupClassName?: string
    date?: DateType
    range?: boolean
    from?: DateType
    to?: DateType
    clear?: boolean
    displayFormat?: string
    displayMonthFormat?: string
    displayDayFormat?: string
    inputFormat?: string
    datePlaceholder?: string
    rangePlaceholder?: string
    onChange?: (date?: {toDate: () => Date}) => void
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    dropdownProps?: any
  }

  export default class DatePicker extends React.Component<DatePickerProps> {}
}
