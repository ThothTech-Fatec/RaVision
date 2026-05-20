export interface ClassificationRow {
  classificacao: string
  total: number
  isClickable?: boolean
}

export interface ClassificationTable {
  id: string
  title: string
  rows: ClassificationRow[]
}

export interface SkuCnlDataset {
  label: string
  data: number[]
  backgroundColor: string
}

export interface SkuCnlChartData {
  labels: string[]
  datasets: SkuCnlDataset[]
}

export interface DataTableColumn {
  key: string
  label: string
}

export type DataTableRow = Record<string, string | number>

// --- SKUs por CNL ---
export const skusCnlChartData: SkuCnlChartData = {
  labels: [
    '2025-01', '2025-02', '2025-03', '2025-04', '2025-05', '2025-06',
    '2025-07', '2025-08', '2025-09', '2025-10', '2025-11', '2025-12', '2026-01',
  ],
  datasets: [
    {
      label: 'CNL Positivo',
      data: [52, 50, 45, 47, 48, 50, 38, 26, 50, 52, 60, 62, 62],
      backgroundColor: '#22c55e',
    },
    {
      label: 'CNL Zerado',
      data: [2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2],
      backgroundColor: '#eab308',
    },
    {
      label: 'CNL Negativo',
      data: [18, 20, 15, 22, 20, 18, 25, 35, 20, 16, 12, 10, 18],
      backgroundColor: '#ef4444',
    },
  ],
}

// --- Tabelas de Classificação ---
export const classificationTables: ClassificationTable[] = [
  {
    id: 'pa_m1',
    title: 'Classificação - classificacao_assertividade_pa_m1 último mês disponível',
    rows: [
      { classificacao: 'critica', total: 43 },
      { classificacao: 'boa', total: 10, isClickable: true },
      { classificacao: 'sem classificacao - sem venda', total: 10, isClickable: true },
      { classificacao: 'razoavel', total: 8 },
      { classificacao: 'excelente', total: 7 },
      { classificacao: 'baixa', total: 5 },
    ],
  },
  {
    id: 'pa_m2',
    title: 'Classificação - classificacao_assertividade_pa_m2 último mês disponível',
    rows: [
      { classificacao: 'critica', total: 45 },
      { classificacao: 'sem classificacao - sem venda', total: 10 },
      { classificacao: 'excelente', total: 8 },
      { classificacao: 'boa', total: 7 },
      { classificacao: 'razoavel', total: 7 },
      { classificacao: 'baixa', total: 6 },
    ],
  },
  {
    id: 'bobina_m1',
    title: 'Classificação - classificacao_assertividade_bobina_m1 último mês disponível',
    rows: [
      { classificacao: 'critica', total: 43 },
      { classificacao: 'sem classificacao - sem venda', total: 23 },
      { classificacao: 'baixa', total: 8 },
      { classificacao: 'excelente', total: 6 },
      { classificacao: 'boa', total: 2 },
      { classificacao: 'razoavel', total: 1, isClickable: true },
    ],
  },
  {
    id: 'bobina_m2',
    title: 'Classificação - classificacao_assertividade_bobina_m2 último mês disponível',
    rows: [
      { classificacao: 'critica', total: 45 },
      { classificacao: 'sem classificacao - sem venda', total: 23 },
      { classificacao: 'razoavel', total: 6 },
      { classificacao: 'boa', total: 4 },
      { classificacao: 'excelente', total: 3 },
      { classificacao: 'baixa', total: 2 },
    ],
  },
]

// --- Tabela de Assertividade ---
export const assertividadeTableColumns: DataTableColumn[] = [
  { key: 'sku', label: 'SKU' },
  { key: '2025-01', label: '2025-01' },
  { key: '2025-02', label: '2025-02' },
  { key: '2025-03', label: '2025-03' },
  { key: '2025-04', label: '2025-04' },
  { key: '2025-05', label: '2025-05' },
  { key: '2025-06', label: '2025-06' },
  { key: '2025-07', label: '2025-07' },
  { key: '2025-08', label: '2025-08' },
  { key: '2025-09', label: '2025-09' },
  { key: '2025-10', label: '2025-10' },
  { key: '2025-11', label: '2025-11' },
  { key: '2025-12', label: '2025-12' },
  { key: '2026-01', label: '2026-01' },
  { key: '2026-02', label: '2026-02' },
]

export const assertividadeTableRows: DataTableRow[] = [
  { sku: 'SKU-001', '2025-01': '0,00%', '2025-02': '-18,40%', '2025-03': '74,50%', '2025-04': '0,00%', '2025-05': '0,00%', '2025-06': '85,20%', '2025-07': '0,00%', '2025-08': '-5,30%', '2025-09': '92,10%', '2025-10': '0,00%', '2025-11': '0,00%', '2025-12': '67,80%', '2026-01': '0,00%', '2026-02': '0,00%' },
  { sku: 'SKU-002', '2025-01': '45,20%', '2025-02': '0,00%', '2025-03': '0,00%', '2025-04': '-12,60%', '2025-05': '88,40%', '2025-06': '0,00%', '2025-07': '72,30%', '2025-08': '0,00%', '2025-09': '0,00%', '2025-10': '-8,90%', '2025-11': '91,20%', '2025-12': '0,00%', '2026-01': '0,00%', '2026-02': '78,50%' },
  { sku: 'SKU-003', '2025-01': '0,00%', '2025-02': '63,70%', '2025-03': '0,00%', '2025-04': '0,00%', '2025-05': '-22,10%', '2025-06': '79,80%', '2025-07': '0,00%', '2025-08': '55,40%', '2025-09': '0,00%', '2025-10': '0,00%', '2025-11': '-14,70%', '2025-12': '83,60%', '2026-01': '0,00%', '2026-02': '0,00%' },
  { sku: 'SKU-004', '2025-01': '71,30%', '2025-02': '0,00%', '2025-03': '-9,80%', '2025-04': '94,50%', '2025-05': '0,00%', '2025-06': '0,00%', '2025-07': '-31,20%', '2025-08': '68,90%', '2025-09': '0,00%', '2025-10': '87,60%', '2025-11': '0,00%', '2025-12': '0,00%', '2026-01': '-7,40%', '2026-02': '95,30%' },
  { sku: 'SKU-005', '2025-01': '0,00%', '2025-02': '0,00%', '2025-03': '58,20%', '2025-04': '0,00%', '2025-05': '77,90%', '2025-06': '0,00%', '2025-07': '0,00%', '2025-08': '-16,50%', '2025-09': '89,70%', '2025-10': '0,00%', '2025-11': '73,40%', '2025-12': '0,00%', '2026-01': '0,00%', '2026-02': '-3,20%' },
  { sku: 'SKU-006', '2025-01': '-25,60%', '2025-02': '82,10%', '2025-03': '0,00%', '2025-04': '66,80%', '2025-05': '0,00%', '2025-06': '0,00%', '2025-07': '96,40%', '2025-08': '0,00%', '2025-09': '-19,30%', '2025-10': '71,90%', '2025-11': '0,00%', '2025-12': '84,70%', '2026-01': '0,00%', '2026-02': '0,00%' },
  { sku: 'SKU-007', '2025-01': '0,00%', '2025-02': '0,00%', '2025-03': '69,40%', '2025-04': '-7,20%', '2025-05': '0,00%', '2025-06': '91,80%', '2025-07': '0,00%', '2025-08': '0,00%', '2025-09': '76,50%', '2025-10': '-11,40%', '2025-11': '0,00%', '2025-12': '0,00%', '2026-01': '88,20%', '2026-02': '0,00%' },
  { sku: 'SKU-008', '2025-01': '54,30%', '2025-02': '0,00%', '2025-03': '0,00%', '2025-04': '79,60%', '2025-05': '-28,90%', '2025-06': '0,00%', '2025-07': '62,70%', '2025-08': '0,00%', '2025-09': '0,00%', '2025-10': '93,80%', '2025-11': '-6,10%', '2025-12': '0,00%', '2026-01': '75,40%', '2026-02': '0,00%' },
  { sku: 'SKU-009', '2025-01': '0,00%', '2025-02': '-4,70%', '2025-03': '81,30%', '2025-04': '0,00%', '2025-05': '0,00%', '2025-06': '-33,80%', '2025-07': '57,90%', '2025-08': '0,00%', '2025-09': '84,60%', '2025-10': '0,00%', '2025-11': '0,00%', '2025-12': '-10,20%', '2026-01': '90,40%', '2026-02': '0,00%' },
  { sku: 'SKU-010', '2025-01': '66,80%', '2025-02': '0,00%', '2025-03': '0,00%', '2025-04': '-15,30%', '2025-05': '72,60%', '2025-06': '0,00%', '2025-07': '0,00%', '2025-08': '48,70%', '2025-09': '0,00%', '2025-10': '0,00%', '2025-11': '-9,50%', '2025-12': '86,90%', '2026-01': '0,00%', '2026-02': '61,40%' },
]

// --- Configurações de widgets padrão ---
export interface WidgetConfig {
  id: string
  title: string
  showTitle: boolean
  prompt: string
  type: string
  showAnalysis: boolean
}

export const defaultWidgetConfigs: Record<string, WidgetConfig> = {
  skuCnl: {
    id: 'skuCnl',
    title: 'Widget',
    showTitle: true,
    prompt: 'crie um gráfico de barras empilhadas de SKUs por CNL por mês',
    type: 'ai',
    showAnalysis: false,
  },
  classPA_M1: {
    id: 'classPA_M1',
    title: 'Classificação PA M1',
    showTitle: true,
    prompt: 'tabela de classificação assertividade pa m1 último mês disponível',
    type: 'ai',
    showAnalysis: false,
  },
  classPA_M2: {
    id: 'classPA_M2',
    title: 'Classificação PA M2',
    showTitle: true,
    prompt: 'tabela de classificação assertividade pa m2 último mês disponível',
    type: 'ai',
    showAnalysis: false,
  },
  classBobina_M1: {
    id: 'classBobina_M1',
    title: 'Classificação Bobina M1',
    showTitle: true,
    prompt: 'tabela de classificação assertividade bobina m1 último mês disponível',
    type: 'ai',
    showAnalysis: false,
  },
  classBobina_M2: {
    id: 'classBobina_M2',
    title: 'Classificação Bobina M2',
    showTitle: true,
    prompt: 'tabela de classificação assertividade bobina m2 último mês disponível',
    type: 'ai',
    showAnalysis: false,
  },
  assertividadeTable: {
    id: 'assertividadeTable',
    title: 'assertividade_pa_m1',
    showTitle: true,
    prompt: 'tabela de assertividade pa m1 por SKU e mês',
    type: 'ai',
    showAnalysis: false,
  },
}
