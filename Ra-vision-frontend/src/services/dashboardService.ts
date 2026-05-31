import axios from 'axios'

const API_URL = 'http://localhost:8080/api/dashboards/comissoes'

export interface DashboardAgregacaoDTO {
  chave: string
  valor: number
}

export interface DashboardTotalDTO {
  total: number
}

export interface ExecutivaKPIsDTO {
  faturamentoAtual: number
  comissoesAtuais: number
  custoComissaoPercentual: number
}

export interface HistoricoEvolucaoDTO {
  mes: string
  faturamento: number
  comissoes: number
}

export interface RankingVendedorDTO {
  matricula: string
  nome: string
  loja: string
  vendas: number
  comissao: number
}

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
}

export const getComissoesPorLoja = async (dateRef: string): Promise<DashboardAgregacaoDTO[]> => {
  const response = await axios.get(`${API_URL}/loja`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getComissoesPorMarca = async (dateRef: string): Promise<DashboardAgregacaoDTO[]> => {
  const response = await axios.get(`${API_URL}/marca`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getTotalComissaoGeral = async (dateRef: string): Promise<DashboardTotalDTO> => {
  const response = await axios.get(`${API_URL}/geral`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getKPIsExecutiva = async (dateRef: string): Promise<ExecutivaKPIsDTO> => {
  const response = await axios.get(`${API_URL}/executiva/kpis`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getHistoricoExecutiva = async (dateRef: string): Promise<HistoricoEvolucaoDTO[]> => {
  const response = await axios.get(`${API_URL}/executiva/historico`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getTopMarcas = async (dateRef: string): Promise<DashboardAgregacaoDTO[]> => {
  const response = await axios.get(`${API_URL}/executiva/top-marcas`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getRankingVendedores = async (dateRef: string): Promise<RankingVendedorDTO[]> => {
  const response = await axios.get(`${API_URL}/lojas/ranking-vendedores`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getProporcionalidadeRH = async (dateRef: string): Promise<DashboardAgregacaoDTO[]> => {
  const response = await axios.get(`${API_URL}/rh/proporcionalidade`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}

export const getAnomaliasCadastroRH = async (dateRef: string): Promise<number> => {
  const response = await axios.get(`${API_URL}/rh/anomalias-cadastro`, {
    params: { dateRef },
    ...getAuthHeaders()
  })
  return response.data
}
