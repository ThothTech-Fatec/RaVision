export interface WidgetConfig {
  id: string
  title: string
  showTitle: boolean
  prompt: string
  type: 'ai' | 'data' | 'chart'
  showAnalysis: boolean
}

export interface ChartData {
  labels: string[]
  datasets: any[]
}

export interface ClassificationRow {
  classLabel: string
  value: string | number
  indicatorColor?: string
}

export interface DataTableColumn {
  key: string
  label: string
}

export interface DataTableRow {
  [key: string]: any
}
