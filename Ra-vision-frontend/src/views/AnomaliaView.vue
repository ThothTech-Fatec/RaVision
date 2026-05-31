<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import AppNav from '@/components/AppNav.vue'

const router = useRouter()
const role = localStorage.getItem('role')
const username = localStorage.getItem('username')
const isGestor = role === 'GESTOR_RH' || role === 'ADMINISTRADOR'

// Se não for gestor/admin, redirecionar
onMounted(() => {
  if (!isGestor) {
    router.push('/chat')
  }
})

// Tipos
type TipoAlgoritmo = 'IQR' | 'Z_SCORE'
type TipoAnomalia = 'MUITO_ACIMA_DA_MEDIA' | 'MUITO_ABAIXO_DA_MEDIA'
type StatusAnalise = 'PENDENTE' | 'JUSTIFICADA' | 'FALSO_POSITIVO'

interface ConfiguracaoAnomalia {
  algoritmo: TipoAlgoritmo
  limiteMultiplicador: number
  agruparPorCargo: boolean
}

interface Anomalia {
  id: number
  dateRef: string
  matricula: string
  codCargo: number
  valorComissao: number
  limiteEsperado: number
  valorMedio: number
  tipoAnomalia: TipoAnomalia
  statusAnalise: StatusAnalise
  justificativa: string
}

// Estados
const configuracao = ref<ConfiguracaoAnomalia>({
  algoritmo: 'IQR',
  limiteMultiplicador: 1.5,
  agruparPorCargo: true
})

const anomalias = ref<Anomalia[]>([])
const isLoading = ref(true)
const isConfigOpen = ref(false)
const isAuditing = ref(false)
const auditingId = ref<number | null>(null)

// Filtros
const dateRefFiltro = ref('')
const matriculaFiltro = ref('')
const statusFiltro = ref<StatusAnalise | ''>('')

// Auditoria
const auditoriaForm = ref({
  statusAnalise: 'JUSTIFICADA' as StatusAnalise,
  justificativa: ''
})

// Toast
type ToastStatus = 'processing' | 'success' | 'error' | null
const toast = ref<{ status: ToastStatus; message: string }>({ status: null, message: '' })

function showToast(status: ToastStatus, message: string) {
  toast.value = { status, message }
  if (status === 'success' || status === 'error') {
    setTimeout(() => { toast.value = { status: null, message: '' } }, 5000)
  }
}

const getHeaders = () => {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

const formatCurrency = (value: number) => {
  if (value === undefined || value === null) return '-'
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}

const carregarConfiguracao = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/anomalias/configuracao', { headers: getHeaders() })
    if (res.data) {
      configuracao.value = res.data
    }
  } catch (error) {
    console.error('Erro ao carregar configuração', error)
  }
}

const salvarConfiguracao = async () => {
  try {
    await axios.post('http://localhost:8080/api/anomalias/configuracao', configuracao.value, { headers: getHeaders() })
    isConfigOpen.value = false
    showToast('success', 'Configuração de anomalias salva com sucesso!')
  } catch (error) {
    showToast('error', 'Erro ao salvar configuração.')
  }
}

const carregarAnomalias = async () => {
  isLoading.value = true
  try {
    const params = new URLSearchParams()
    if (dateRefFiltro.value) params.append('dateRef', `${dateRefFiltro.value}-01`) // converte de YYYY-MM para YYYY-MM-DD
    if (matriculaFiltro.value) params.append('matricula', matriculaFiltro.value)
    if (statusFiltro.value) params.append('statusAnalise', statusFiltro.value)
    params.append('size', '100') // Paginação fixa por agora para simplificar

    const res = await axios.get(`http://localhost:8080/api/anomalias?${params.toString()}`, { headers: getHeaders() })
    anomalias.value = res.data.content || []
  } catch (error) {
    console.error("Erro ao carregar anomalias:", error)
    showToast('error', 'Erro ao carregar lista de anomalias.')
  } finally {
    isLoading.value = false
  }
}

const dispararAnalise = async () => {
  if (!dateRefFiltro.value) {
    showToast('error', 'Selecione um mês de competência no filtro para rodar a análise.')
    return
  }
  
  showToast('processing', '⏳ Analisando comissões...')
  try {
    const dateRefFull = `${dateRefFiltro.value}-01`
    const res = await axios.post(`http://localhost:8080/api/anomalias/disparar?dateRef=${dateRefFull}`, {}, { headers: getHeaders() })
    
    showToast('success', `✅ Análise concluída: ${res.data.anomaliasDetectadas} anomalia(s) encontrada(s) em ${res.data.totalAnalisados} registros.`)
    await carregarAnomalias()
  } catch (error: any) {
    const msg = error.response?.data?.erro || 'Erro interno.'
    showToast('error', `❌ Falha na análise: ${msg}`)
  }
}

const anomaliaSendoAuditada = ref<Anomalia | null>(null)

const abrirAuditoria = (anomalia: Anomalia) => {
  auditingId.value = anomalia.id
  anomaliaSendoAuditada.value = anomalia
  auditoriaForm.value = {
    statusAnalise: anomalia.statusAnalise === 'PENDENTE' ? 'JUSTIFICADA' : anomalia.statusAnalise,
    justificativa: anomalia.justificativa || ''
  }
  isAuditing.value = true
}

const salvarAuditoria = async () => {
  if (!auditingId.value) return
  
  try {
    await axios.put(`http://localhost:8080/api/anomalias/${auditingId.value}/auditar`, auditoriaForm.value, { headers: getHeaders() })
    isAuditing.value = false
    showToast('success', 'Anomalia auditada com sucesso.')
    await carregarAnomalias()
  } catch (error) {
    showToast('error', 'Erro ao auditar anomalia.')
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'JUSTIFICADA': return 'bg-green-100 text-green-800 border-green-200'
    case 'FALSO_POSITIVO': return 'bg-slate-100 text-slate-800 border-slate-200'
    case 'PENDENTE': default: return 'bg-yellow-100 text-yellow-800 border-yellow-200'
  }
}

const getTipoColor = (tipo: string) => {
  return tipo === 'MUITO_ACIMA_DA_MEDIA' 
    ? 'text-red-600 bg-red-50' 
    : 'text-orange-600 bg-orange-50'
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}

onMounted(() => {
  // Inicializa com 01/09/2025 (padrão)
  dateRefFiltro.value = '2025-09'
  
  carregarConfiguracao()
  carregarAnomalias()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col font-sans">

    <!-- Toast -->
    <Transition
      enter-active-class="transition-all duration-300 ease-out"
      enter-from-class="opacity-0 -translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition-all duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-4"
    >
      <div
        v-if="toast.status"
        :class="[
          'fixed top-4 left-1/2 -translate-x-1/2 z-[100] flex items-center gap-3 px-5 py-3 rounded-2xl shadow-xl border text-sm font-medium max-w-lg w-full mx-4',
          toast.status === 'processing' ? 'bg-indigo-600 text-white border-indigo-500' :
          toast.status === 'success'    ? 'bg-emerald-600 text-white border-emerald-500' :
                                          'bg-red-600 text-white border-red-500'
        ]"
      >
        <svg v-if="toast.status === 'processing'" class="animate-spin shrink-0 w-4 h-4" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        <span class="truncate">{{ toast.message }}</span>
      </div>
    </Transition>

    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight truncate">Auditoria de Anomalias</h1>
        <p class="text-xs text-slate-400 truncate">Detecção estatística de desvios no comissionamento</p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="isConfigOpen = true"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-white border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-semibold rounded-lg shadow-sm transition-all"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <span class="hidden sm:inline">Configurações</span>
        </button>
      </div>

      <AppNav />

      <button @click="logout" class="p-2 ml-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Main -->
    <main class="flex-1 p-6 flex flex-col gap-4 overflow-y-auto">
      
      <!-- Toolbar Filtros & Disparo -->
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-wrap items-end gap-4">
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Mês Competência</label>
          <input v-model="dateRefFiltro" type="month" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Matrícula</label>
          <input v-model="matriculaFiltro" type="text" placeholder="Ex: 12345" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 w-32" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Status</label>
          <select v-model="statusFiltro" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
            <option value="">Todos</option>
            <option value="PENDENTE">Pendentes</option>
            <option value="JUSTIFICADA">Justificadas</option>
            <option value="FALSO_POSITIVO">Falsos Positivos</option>
          </select>
        </div>
        <button @click="carregarAnomalias" class="px-4 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold rounded-lg transition-colors">
          Filtrar
        </button>
        <div class="flex-1"></div>
        <button @click="dispararAnalise" class="flex items-center gap-2 px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm shadow-indigo-200 transition-all">
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" />
            <path stroke-linecap="round" stroke-linejoin="round" d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          Rodar Análise
        </button>
      </div>

      <!-- Tabela -->
      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden flex-1">
        <div v-if="isLoading" class="text-center py-10 text-slate-500">
          Carregando anomalias...
        </div>
        
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="bg-slate-50 text-slate-600 border-b border-slate-200">
              <tr>
                <th class="px-4 py-3 font-semibold">Competência</th>
                <th class="px-4 py-3 font-semibold">Matrícula</th>
                <th class="px-4 py-3 font-semibold">Cargo</th>
                <th class="px-4 py-3 font-semibold text-right">Comissão Real</th>
                <th class="px-4 py-3 font-semibold text-right">Limite Esperado</th>
                <th class="px-4 py-3 font-semibold text-center">Tipo</th>
                <th class="px-4 py-3 font-semibold text-center">Status</th>
                <th class="px-4 py-3 font-semibold text-right">Ação</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="anomalias.length === 0">
                <td colspan="8" class="px-6 py-8 text-center text-slate-500 text-sm">
                  Nenhuma anomalia detectada para este filtro.
                </td>
              </tr>
              <tr v-for="anomalia in anomalias" :key="anomalia.id" class="hover:bg-slate-50 transition-colors">
                <td class="px-4 py-3 text-slate-600">{{ anomalia.dateRef }}</td>
                <td class="px-4 py-3 font-medium text-slate-800">{{ anomalia.matricula }}</td>
                <td class="px-4 py-3 text-slate-500 text-xs">Cód: {{ anomalia.codCargo }}</td>
                <td class="px-4 py-3 font-bold text-slate-800 text-right">{{ formatCurrency(anomalia.valorComissao) }}</td>
                <td class="px-4 py-3 text-slate-500 text-right">{{ formatCurrency(anomalia.limiteEsperado) }}</td>
                <td class="px-4 py-3 text-center">
                  <span :class="['px-2 py-0.5 rounded text-xs font-semibold border', getTipoColor(anomalia.tipoAnomalia)]">
                    {{ anomalia.tipoAnomalia === 'MUITO_ACIMA_DA_MEDIA' ? 'Acima' : 'Abaixo' }}
                  </span>
                </td>
                <td class="px-4 py-3 text-center">
                  <span :class="['px-2.5 py-1 text-xs font-semibold rounded-full border', getStatusColor(anomalia.statusAnalise)]">
                    {{ anomalia.statusAnalise }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <button @click="abrirAuditoria(anomalia)" class="px-3 py-1 bg-white border border-slate-200 hover:bg-slate-50 text-indigo-600 font-medium rounded text-xs transition-colors">
                    Auditar
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- Modal Configuração -->
    <div v-if="isConfigOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-sm overflow-hidden border border-slate-200 flex flex-col">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
          <h2 class="text-base font-bold text-slate-800">Parâmetros de Detecção</h2>
          <button @click="isConfigOpen = false" class="text-slate-400 hover:text-slate-600 transition-colors"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
        
        <div class="p-6 space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Algoritmo Estatístico</label>
            <select v-model="configuracao.algoritmo" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="IQR">IQR (Quartis - Recomendado)</option>
              <option value="Z_SCORE">Z-Score (Média/Desvio Padrão)</option>
            </select>
            <p class="text-[10px] text-slate-500 mt-1 leading-tight">IQR é mais robusto para distorções. Z-Score pode dar falsos positivos se houver outliers extremos.</p>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Sensibilidade (Multiplicador)</label>
            <input v-model.number="configuracao.limiteMultiplicador" type="number" step="0.1" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <p class="text-[10px] text-slate-500 mt-1 leading-tight">Padrão IQR: 1.5. Padrão Z-Score: 2.5 ou 3.0.</p>
          </div>
          <div class="flex items-center gap-2 mt-2">
            <input v-model="configuracao.agruparPorCargo" type="checkbox" id="agrupar" class="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500" />
            <label for="agrupar" class="text-sm text-slate-700">Agrupar por Cargo (Ignorar base salarial baseada em hierarquia)</label>
          </div>
        </div>
        
        <div class="px-6 py-4 border-t border-slate-100 flex justify-end gap-2 bg-slate-50">
          <button @click="isConfigOpen = false" class="px-4 py-2 text-sm font-medium text-slate-600 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors">Cancelar</button>
          <button @click="salvarConfiguracao" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm">Salvar Parâmetros</button>
        </div>
      </div>
    </div>

    <!-- Modal Auditoria -->
    <div v-if="isAuditing" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-md overflow-hidden border border-slate-200 flex flex-col">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-gradient-to-r from-indigo-50 to-white">
          <h2 class="text-base font-bold text-slate-800">Auditar Anomalia</h2>
          <button @click="isAuditing = false" class="text-slate-400 hover:text-slate-600 transition-colors"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
        
        <div class="p-6 space-y-4">
          
          <!-- Box de Explicação Inteligente para o Auditor -->
          <div v-if="anomaliaSendoAuditada" class="bg-indigo-50 border border-indigo-100 rounded-lg p-4 mb-2">
            <h3 class="text-xs font-bold text-indigo-800 uppercase tracking-wider mb-2 flex items-center gap-1">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
              Entenda esta anomalia
            </h3>
            <p class="text-sm text-indigo-900 leading-relaxed">
              A comissão real deste funcionário foi de <strong>{{ formatCurrency(anomaliaSendoAuditada.valorComissao) }}</strong>. 
              No entanto, o sistema calculou que o limite matematicamente seguro (o teto/piso padrão da equipe) era <strong>{{ formatCurrency(anomaliaSendoAuditada.limiteEsperado) }}</strong>. 
              Por isso, o valor foi sinalizado como <span class="font-bold underline decoration-indigo-300 decoration-2">{{ anomaliaSendoAuditada.tipoAnomalia === 'MUITO_ACIMA_DA_MEDIA' ? 'anormalmente alto' : 'anormalmente baixo' }}</span> e exige sua revisão manual.
            </p>
            <div class="mt-2 text-xs text-indigo-700 bg-indigo-100/50 p-2 rounded border border-indigo-100">
              <strong>Origem do Limite:</strong> O limite esperado foi gerado automaticamente usando o algoritmo <strong>{{ configuracao.algoritmo === 'IQR' ? 'IQR (Intervalos de Quartis)' : 'Z-Score (Desvio Padrão)' }}</strong> 
              com sensibilidade de <strong>{{ configuracao.limiteMultiplicador }}x</strong>. A comparação foi feita 
              <strong>{{ configuracao.agruparPorCargo ? 'exclusivamente entre funcionários do mesmo cargo' : 'contra toda a folha de pagamentos misturada' }}</strong>.
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Ação de Auditoria</label>
            <select v-model="auditoriaForm.statusAnalise" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="JUSTIFICADA">Aprovar (Justificada)</option>
              <option value="FALSO_POSITIVO">Ignorar (Falso Positivo)</option>
              <option value="PENDENTE">Manter Pendente</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Justificativa / Observação</label>
            <textarea v-model="auditoriaForm.justificativa" rows="4" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" placeholder="Insira o motivo da anomalia (ex: Venda corporativa gigante aprovada)"></textarea>
          </div>
        </div>
        
        <div class="px-6 py-4 border-t border-slate-100 flex justify-end gap-2 bg-slate-50">
          <button @click="isAuditing = false" class="px-4 py-2 text-sm font-medium text-slate-600 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors">Cancelar</button>
          <button @click="salvarAuditoria" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm">Salvar Auditoria</button>
        </div>
      </div>
    </div>

  </div>
</template>
