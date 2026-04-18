<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// Tipos
type TipoRegra = 'OVERRIDE_PERCENTUAL' | 'BONUS_FIXO' | 'FAIXA_VENDAS' | 'BLACK_FRIDAY'
type StatusAprovacao = 'PENDENTE' | 'APROVADA' | 'RECUSADA'

interface RegraNegocio {
  id: number
  descricaoRegra: string
  tipoRegra: TipoRegra
  mesCompetencia: string
  condicoesAplicacao: string
  valorModificador: number
  statusAprovacao: StatusAprovacao
  criadoPor: string
  dataCriacao: string
}

// Estado
const regras = ref<RegraNegocio[]>([])
const isLoading = ref(true)
const modalOpen = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)
const role = localStorage.getItem('role')
const username = localStorage.getItem('username')
const isGestor = role === 'GESTOR_RH'

// Form
const form = ref({
  descricaoRegra: '',
  tipoRegra: 'OVERRIDE_PERCENTUAL' as TipoRegra,
  mesCompetencia: '',
  condicoesAplicacao: '{}',
  valorModificador: 0
})

// Header para requisições
const getHeaders = () => {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

// Carregar regras
const carregarRegras = async () => {
  isLoading.value = true
  try {
    const res = await axios.get('http://localhost:8080/api/regras', { headers: getHeaders() })
    regras.value = res.data
  } catch (error) {
    console.error("Erro ao carregar regras:", error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  carregarRegras()
})

const openCreate = () => {
  isEditing.value = false
  editingId.value = null
  form.value = {
    descricaoRegra: '',
    tipoRegra: 'OVERRIDE_PERCENTUAL',
    mesCompetencia: '',
    condicoesAplicacao: '{}',
    valorModificador: 0
  }
  modalOpen.value = true
}

const openEdit = (regra: RegraNegocio) => {
  isEditing.value = true
  editingId.value = regra.id
  form.value = {
    descricaoRegra: regra.descricaoRegra,
    tipoRegra: regra.tipoRegra,
    mesCompetencia: regra.mesCompetencia,
    condicoesAplicacao: regra.condicoesAplicacao || '{}',
    valorModificador: regra.valorModificador
  }
  modalOpen.value = true
}

const saveRule = async () => {
  try {
    if (isEditing.value && editingId.value) {
      await axios.put(`http://localhost:8080/api/regras/${editingId.value}`, form.value, { headers: getHeaders() })
    } else {
      await axios.post('http://localhost:8080/api/regras', form.value, { headers: getHeaders() })
    }
    modalOpen.value = false
    await carregarRegras()
  } catch (error) {
    console.error("Erro ao salvar regra", error)
    alert("Falha ao salvar a regra. Verifique os dados.")
  }
}

const deleteRule = async (id: number) => {
  if (confirm('Tem certeza que deseja excluir esta regra?')) {
    try {
      await axios.delete(`http://localhost:8080/api/regras/${id}`, { headers: getHeaders() })
      await carregarRegras()
    } catch (error) {
      console.error("Erro ao excluir", error)
    }
  }
}

const aprovarRegra = async (id: number) => {
  try {
    await axios.put(`http://localhost:8080/api/regras/${id}/aprovar`, {}, { headers: getHeaders() })
    await carregarRegras()
  } catch (error) {
    console.error("Erro ao aprovar", error)
  }
}

const recusarRegra = async (id: number) => {
  try {
    await axios.put(`http://localhost:8080/api/regras/${id}/recusar`, {}, { headers: getHeaders() })
    await carregarRegras()
  } catch (error) {
    console.error("Erro ao recusar", error)
  }
}

const formatTipo = (tipo: string) => {
  const map: Record<string, string> = {
    'OVERRIDE_PERCENTUAL': 'Substituição %',
    'BONUS_FIXO': 'Bônus Fixo',
    'FAIXA_VENDAS': 'Faixa de Vendas',
    'BLACK_FRIDAY': 'Black Friday'
  }
  return map[tipo] || tipo
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'APROVADA': return 'bg-green-100 text-green-800 border-green-200'
    case 'RECUSADA': return 'bg-red-100 text-red-800 border-red-200'
    case 'PENDENTE': default: return 'bg-yellow-100 text-yellow-800 border-yellow-200'
  }
}

function logout() {
  localStorage.clear()
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col font-sans">
    
    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight truncate">Gerenciar Regras Dinâmicas</h1>
        <p class="text-xs text-slate-400 truncate">Configuração de comissões, metas e bônus</p>
      </div>

      <!-- Ações Header -->
      <div class="flex items-center gap-2">
        <button
          @click="openCreate"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg shadow-sm shadow-indigo-200 transition-all hover:-translate-y-0.5 active:translate-y-0"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          <span class="hidden sm:inline">Nova Regra</span>
        </button>
      </div>

      <!-- Navegação Header -->
      <div class="flex items-center gap-2 ml-2 pl-2 border-l border-slate-200">
        <button @click="router.push('/chat')" class="px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200">Chat</button>
        <button @click="router.push('/historico')" class="px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200">Histórico</button>
        <button @click="router.push('/perfil')" class="px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200">Perfil</button>
      </div>

      <!-- Sair -->
      <button @click="logout" class="p-2 ml-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Main -->
    <main class="flex-1 p-6 overflow-y-auto">
      <div v-if="isLoading" class="text-center py-10 text-slate-500">
        Carregando regras...
      </div>
      
      <div v-else class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden">
        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="bg-slate-50 text-slate-600 border-b border-slate-200">
              <tr>
                <th class="px-6 py-4 font-semibold">Descrição</th>
                <th class="px-6 py-4 font-semibold">Tipo</th>
                <th class="px-6 py-4 font-semibold">Competência</th>
                <th class="px-6 py-4 font-semibold">Valor</th>
                <th class="px-6 py-4 font-semibold">Criador</th>
                <th class="px-6 py-4 font-semibold text-center">Status</th>
                <th class="px-6 py-4 font-semibold text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="regras.length === 0">
                <td colspan="7" class="px-6 py-8 text-center text-slate-500 text-sm">
                  Nenhuma regra encontrada.
                </td>
              </tr>
              <tr v-for="regra in regras" :key="regra.id" class="hover:bg-slate-50 transition-colors">
                <td class="px-6 py-4">
                  <div class="font-medium text-slate-800">{{ regra.descricaoRegra }}</div>
                  <div class="text-xs text-slate-400 mt-1">ID: {{ regra.id }} | {{ regra.condicoesAplicacao }}</div>
                </td>
                <td class="px-6 py-4 text-slate-600">
                  <span class="px-2 py-1 bg-slate-100 text-slate-600 rounded-md text-xs font-medium border border-slate-200">{{ formatTipo(regra.tipoRegra) }}</span>
                </td>
                <td class="px-6 py-4 text-slate-600">{{ regra.mesCompetencia }}</td>
                <td class="px-6 py-4 font-semibold text-slate-700">{{ regra.valorModificador }}</td>
                <td class="px-6 py-4 text-slate-600">{{ regra.criadoPor }}</td>
                <td class="px-6 py-4 text-center">
                  <span :class="['px-2.5 py-1 text-xs font-semibold rounded-full border', getStatusColor(regra.statusAprovacao)]">
                    {{ regra.statusAprovacao }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <div class="flex items-center justify-end gap-2">
                    
                    <!-- Ações de Edição/Exclusão para o criador (ou admin) -->
                    <template v-if="regra.criadoPor === username || role === 'ADMINISTRADOR'">
                      <button @click="openEdit(regra)" class="p-1.5 text-blue-500 hover:bg-blue-50 rounded-lg transition-colors" title="Editar">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" /></svg>
                      </button>
                      <button @click="deleteRule(regra.id)" class="p-1.5 text-red-500 hover:bg-red-50 rounded-lg transition-colors" title="Excluir">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                      </button>
                    </template>
                    
                    <!-- Ações de Aprovação para GESTOR_RH -->
                    <template v-if="isGestor && regra.statusAprovacao === 'PENDENTE'">
                      <div class="w-px h-4 bg-slate-200 mx-1"></div>
                      <button @click="aprovarRegra(regra.id)" class="p-1.5 text-green-600 hover:bg-green-50 rounded-lg transition-colors" title="Aprovar Regra">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
                      </button>
                      <button @click="recusarRegra(regra.id)" class="p-1.5 text-orange-600 hover:bg-orange-50 rounded-lg transition-colors" title="Recusar Regra">
                        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg>
                      </button>
                    </template>
                    
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>

    <!-- Modal Form -->
    <div v-if="modalOpen" class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div class="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden border border-slate-200 flex flex-col max-h-[90vh]">
        <div class="px-6 py-4 border-b border-slate-100 flex items-center justify-between bg-slate-50">
          <h2 class="text-lg font-bold text-slate-800">{{ isEditing ? 'Editar Regra' : 'Nova Regra Dinâmica' }}</h2>
          <button @click="modalOpen = false" class="text-slate-400 hover:text-slate-600 transition-colors p-1"><svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" /></svg></button>
        </div>
        
        <div class="p-6 overflow-y-auto space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Descrição</label>
            <input v-model="form.descricaoRegra" type="text" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" placeholder="Ex: Bônus de fim de ano" />
          </div>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Tipo da Regra</label>
              <select v-model="form.tipoRegra" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500">
                <option value="OVERRIDE_PERCENTUAL">Substituição Percentual</option>
                <option value="BONUS_FIXO">Bônus Fixo</option>
                <option value="FAIXA_VENDAS">Faixa de Vendas</option>
                <option value="BLACK_FRIDAY">Black Friday</option>
              </select>
            </div>
            <div>
              <label class="block text-sm font-medium text-slate-700 mb-1">Mês Competência</label>
              <input v-model="form.mesCompetencia" type="month" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Valor Modificador</label>
            <input v-model.number="form.valorModificador" type="number" step="0.01" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" placeholder="0.00" />
          </div>
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Condições (JSON)</label>
            <textarea v-model="form.condicoesAplicacao" rows="3" class="w-full px-3 py-2 bg-white border border-slate-200 rounded-lg text-sm font-mono focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500" placeholder='{"matricula": "123"}'></textarea>
          </div>
        </div>
        
        <div class="px-6 py-4 border-t border-slate-100 flex justify-end gap-3 bg-slate-50">
          <button @click="modalOpen = false" class="px-4 py-2 text-sm font-medium text-slate-600 bg-white border border-slate-200 rounded-lg hover:bg-slate-50 transition-colors shadow-sm">Cancelar</button>
          <button @click="saveRule" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 transition-colors shadow-sm shadow-indigo-200">Salvar Regra</button>
        </div>
      </div>
    </div>
    
  </div>
</template>
