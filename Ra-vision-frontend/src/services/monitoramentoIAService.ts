import axios from 'axios'

const API_URL = 'http://localhost:8080/api/monitoramento/ia'

export interface MonitoramentoIADTO {
  timestamp: string
  perguntaUsuario: string
  respostaIA: string
  tempoRespostaMs: number
  ferramentaUtilizada: string
  sucessoFerramenta: boolean
  usuarioLogado: string
}

export interface EstatisticaDiariaDTO {
  data: string
  quantidadePerguntas: number
  tempoMedioMs: number
}

export interface MonitoramentoAgregadoDTO {
  totalRequisicoes: number
  tempoMedioGeralMs: number
  estatisticasDiarias: EstatisticaDiariaDTO[]
  logsRecentes: MonitoramentoIADTO[]
}

const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
}

export const buscarMetricasIA = async (dataInicio: string, dataFim: string): Promise<MonitoramentoAgregadoDTO> => {
  const response = await axios.get(API_URL, {
    params: { dataInicio, dataFim },
    ...getAuthHeaders()
  })
  return response.data
}
