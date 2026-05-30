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

export interface ChartDataset {
  label: string
  data: number[]
  backgroundColor: string | string[]
}

export interface ChartData {
  labels: string[]
  datasets: ChartDataset[]
}

export interface DataTableColumn {
  key: string
  label: string
}

export type DataTableRow = Record<string, string | number>

// --- Visão Executiva ---
export const faturamentoVsComissoesData: ChartData = {
  labels: ['Jun', 'Jul', 'Ago', 'Set', 'Out', 'Nov'],
  datasets: [
    {
      label: 'Faturamento (R$ Milhares)',
      data: [850, 920, 880, 1050, 1200, 1350],
      backgroundColor: '#3b82f6', // blue
    },
    {
      label: 'Comissões Pagas (R$ Milhares)',
      data: [42, 46, 44, 52, 60, 67],
      backgroundColor: '#10b981', // emerald
    },
  ],
}

export const rankingMarcasData: ClassificationTable[] = [
  {
    id: 'marcas_faturamento',
    title: 'Top Marcas por Faturamento',
    rows: [
      { classificacao: 'Marca A', total: 450000, isClickable: true },
      { classificacao: 'Marca B', total: 320000, isClickable: true },
      { classificacao: 'Marca C', total: 180000 },
      { classificacao: 'Marca D', total: 95000 },
    ],
  },
]

export const produtosMaisVendidosData: ClassificationTable[] = [
  {
    id: 'produtos_vendidos',
    title: 'Top 5 Produtos/Serviços',
    rows: [
      { classificacao: 'Plano Anual Premium', total: 1250 },
      { classificacao: 'Plano Semestral', total: 890 },
      { classificacao: 'Taxa Adesão', total: 640 },
      { classificacao: 'Mensalidade Básica', total: 512 },
      { classificacao: 'Upgrade VIP', total: 320 },
    ],
  },
]

// --- Gestão de Lojas ---
export const rankingVendedoresColumns: DataTableColumn[] = [
  { key: 'matricula', label: 'Matrícula' },
  { key: 'nome', label: 'Nome' },
  { key: 'loja', label: 'Loja' },
  { key: 'vendas', label: 'Vendas (R$)' },
  { key: 'comissao', label: 'Comissão (R$)' },
]

export const rankingVendedoresRows: DataTableRow[] = [
  { matricula: 'MAT-101', nome: 'João Silva', loja: 'Loja Centro', vendas: 'R$ 85.000', comissao: 'R$ 4.250' },
  { matricula: 'MAT-205', nome: 'Maria Santos', loja: 'Loja Shopping', vendas: 'R$ 78.500', comissao: 'R$ 3.925' },
  { matricula: 'MAT-089', nome: 'Pedro Costa', loja: 'Loja Norte', vendas: 'R$ 72.000', comissao: 'R$ 3.600' },
  { matricula: 'MAT-312', nome: 'Ana Souza', loja: 'Loja Sul', vendas: 'R$ 65.400', comissao: 'R$ 3.270' },
  { matricula: 'MAT-156', nome: 'Lucas Lima', loja: 'Loja Centro', vendas: 'R$ 59.800', comissao: 'R$ 2.990' },
  { matricula: 'MAT-420', nome: 'Julia Alves', loja: 'Loja Leste', vendas: 'R$ 55.000', comissao: 'R$ 2.750' },
]

export const comissaoPorLojaData: ChartData = {
  labels: ['Loja Centro', 'Loja Sul', 'Loja Norte', 'Loja Leste', 'Loja Shopping', 'Loja Matriz'],
  datasets: [
    {
      label: 'Comissão Paga (R$)',
      data: [18500, 14200, 11800, 9500, 22400, 31000],
      backgroundColor: '#6366f1', // indigo
    },
  ],
}

export const comissaoPorRegiaoData: ChartData = {
  labels: ['Sudeste', 'Sul', 'Nordeste', 'Centro-Oeste', 'Norte'],
  datasets: [
    {
      label: 'Distribuição (R$)',
      data: [85000, 32000, 28000, 15000, 8000],
      backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899'],
    },
  ],
}

// --- Operacional RH ---
export const motivosProporcionalidadeData: ChartData = {
  labels: ['Admissão', 'Demissão', 'Férias', 'Afastamento INSS', 'Faltas Injustificadas'],
  datasets: [
    {
      label: 'Casos no Mês',
      data: [15, 8, 22, 5, 45],
      backgroundColor: ['#8b5cf6', '#ef4444', '#f59e0b', '#64748b', '#f97316'],
    },
  ],
}

// --- Métricas IA ---
export const metricasIaData: ChartData = {
  labels: ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'],
  datasets: [
    {
      label: 'Interações com IA',
      data: [120, 145, 180, 165, 210, 85, 45],
      backgroundColor: '#8b5cf6', // violet
    },
  ],
}

// --- Configurações de widgets padrão ---
export interface WidgetConfig {
  id: string
  title: string
  showTitle: boolean
  prompt: string
  type: string
  showAnalysis: boolean
}

export const defaultWidgetConfigs: Record<string, WidgetConfig> = {}

