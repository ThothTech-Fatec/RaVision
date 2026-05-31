import axios from 'axios';

const API_URL = 'http://localhost:8080/api/erros-importacao';

const getHeaders = () => {
  const token = localStorage.getItem('token');
  return { Authorization: `Bearer ${token}` };
};

export interface ErroImportacao {
  id: number;
  timestamp: string;
  nomeArquivo: string;
  linha: number | null;
  coluna: string | null;
  mensagemErro: string;
  tipoErro: 'MISSING_DATA' | 'FORMAT_ERROR' | 'INVALID_VALUE' | 'STRUCTURAL_ERROR';
  status: 'PENDENTE' | 'RESOLVIDO';
  usuarioUpload: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export const erroImportacaoService = {
  async listarErros(
    status?: string,
    nomeArquivo?: string,
    dataInicio?: string,
    dataFim?: string,
    page: number = 0,
    size: number = 10
  ): Promise<PageResponse<ErroImportacao>> {
    const params = new URLSearchParams();
    if (status) params.append('status', status);
    if (nomeArquivo) params.append('nomeArquivo', nomeArquivo);
    if (dataInicio) params.append('dataInicio', `${dataInicio}T00:00:00`);
    if (dataFim) params.append('dataFim', `${dataFim}T23:59:59`);
    params.append('page', page.toString());
    params.append('size', size.toString());
    params.append('sort', 'timestamp,desc');

    const response = await axios.get(`${API_URL}?${params.toString()}`, { headers: getHeaders() });
    return response.data;
  },

  async resolverErro(id: number): Promise<ErroImportacao> {
    const response = await axios.put(`${API_URL}/${id}/resolver`, {}, { headers: getHeaders() });
    return response.data;
  },

  async contarErrosPendentes(): Promise<number> {
    const response = await axios.get(`${API_URL}/pendentes/count`, { headers: getHeaders() });
    return response.data;
  }
};
