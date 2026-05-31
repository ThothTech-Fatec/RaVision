<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppNav from '@/components/AppNav.vue'
import { erroImportacaoService, type ErroImportacao } from '@/services/erroImportacaoService'

const router = useRouter()
const role = localStorage.getItem('role')
const isGestor = role === 'GESTOR_RH' || role === 'ADMINISTRADOR'

// Se não for gestor/admin, redirecionar
onMounted(() => {
  if (!isGestor) {
    router.push('/chat')
  }
})

// Tipos
type StatusErro = 'PENDENTE' | 'RESOLVIDO'

// Estados
const erros = ref<ErroImportacao[]>([])
const isLoading = ref(true)

// Filtros e Paginação
const dateInicioFiltro = ref('')
const dateFimFiltro = ref('')
const nomeArquivoFiltro = ref('')
const statusFiltro = ref<StatusErro | ''>('PENDENTE')

const page = ref(0)
const totalPages = ref(0)
const totalElements = ref(0)

// Toast
type ToastStatus = 'processing' | 'success' | 'error' | null
const toast = ref<{ status: ToastStatus; message: string }>({ status: null, message: '' })

function showToast(status: ToastStatus, message: string) {
  toast.value = { status, message }
  if (status === 'success' || status === 'error') {
    setTimeout(() => { toast.value = { status: null, message: '' } }, 5000)
  }
}

const carregarErros = async () => {
  isLoading.value = true
  try {
    const res = await erroImportacaoService.listarErros(
      statusFiltro.value || undefined,
      nomeArquivoFiltro.value || undefined,
      dateInicioFiltro.value || undefined,
      dateFimFiltro.value || undefined,
      page.value,
      10 // 10 itens por página
    )
    erros.value = res.content || []
    totalPages.value = res.totalPages
    totalElements.value = res.totalElements
  } catch (error) {
    console.error("Erro ao carregar erros:", error)
    showToast('error', 'Erro ao carregar lista de erros de importação.')
  } finally {
    isLoading.value = false
  }
}

const prevPage = () => {
  if (page.value > 0) {
    page.value--
    carregarErros()
  }
}

const nextPage = () => {
  if (page.value < totalPages.value - 1) {
    page.value++
    carregarErros()
  }
}

const aplicarFiltros = () => {
  page.value = 0
  carregarErros()
}

const resolverErro = async (id: number) => {
  try {
    showToast('processing', 'Resolvendo...')
    await erroImportacaoService.resolverErro(id)
    showToast('success', 'Erro marcado como resolvido.')
    await carregarErros()
    // Atualiza o contador no AppNav via evento ou apenas atualiza a tela
    window.dispatchEvent(new Event('import-error-resolved'))
  } catch (error) {
    showToast('error', 'Erro ao resolver o problema.')
  }
}

const getStatusColor = (status: string) => {
  switch (status) {
    case 'RESOLVIDO': return 'bg-green-100 text-green-800 border-green-200'
    case 'PENDENTE': default: return 'bg-yellow-100 text-yellow-800 border-yellow-200'
  }
}

const getTipoLabel = (tipo: string) => {
  switch (tipo) {
    case 'MISSING_DATA': return 'Dado Ausente'
    case 'FORMAT_ERROR': return 'Formato Inválido'
    case 'INVALID_VALUE': return 'Valor Inválido'
    case 'STRUCTURAL_ERROR': return 'Erro Estrutural'
    default: return tipo
  }
}

const formatDateTime = (val: string) => {
  if (!val) return '-'
  const d = new Date(val)
  return d.toLocaleString('pt-BR')
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}

onMounted(() => {
  carregarErros()
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
        <span class="truncate">{{ toast.message }}</span>
      </div>
    </Transition>

    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight truncate">Erros de Importação</h1>
        <p class="text-xs text-slate-400 truncate">Gerenciamento de falhas na carga de planilhas</p>
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
      
      <!-- Toolbar Filtros -->
      <div class="bg-white p-4 rounded-xl shadow-sm border border-slate-200 flex flex-wrap items-end gap-4">
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Data Início</label>
          <input v-model="dateInicioFiltro" type="date" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Data Fim</label>
          <input v-model="dateFimFiltro" type="date" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Nome Arquivo</label>
          <input v-model="nomeArquivoFiltro" type="text" placeholder="Ex: vendas.csv" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20 w-40" />
        </div>
        <div>
          <label class="block text-xs font-medium text-slate-500 mb-1">Status</label>
          <select v-model="statusFiltro" class="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500/20">
            <option value="">Todos</option>
            <option value="PENDENTE">Pendentes</option>
            <option value="RESOLVIDO">Resolvidos</option>
          </select>
        </div>
        <button @click="aplicarFiltros" class="px-4 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm shadow-indigo-200 transition-colors">
          Filtrar
        </button>
      </div>

      <!-- Tabela -->
      <div class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden flex-1">
        <div v-if="isLoading" class="text-center py-10 text-slate-500">
          Carregando erros...
        </div>
        
        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm whitespace-nowrap">
            <thead class="bg-slate-50 text-slate-600 border-b border-slate-200">
              <tr>
                <th class="px-4 py-3 font-semibold">Data/Hora</th>
                <th class="px-4 py-3 font-semibold">Arquivo</th>
                <th class="px-4 py-3 font-semibold">Posição</th>
                <th class="px-4 py-3 font-semibold">Tipo</th>
                <th class="px-4 py-3 font-semibold w-1/3">Detalhe do Erro</th>
                <th class="px-4 py-3 font-semibold text-center">Status</th>
                <th class="px-4 py-3 font-semibold text-right">Ação</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-if="erros.length === 0">
                <td colspan="7" class="px-6 py-8 text-center text-slate-500 text-sm">
                  Nenhum erro de importação encontrado com estes filtros. 🎉
                </td>
              </tr>
              <tr v-for="erro in erros" :key="erro.id" class="hover:bg-slate-50 transition-colors">
                <td class="px-4 py-3 text-slate-500 text-xs">{{ formatDateTime(erro.timestamp) }}</td>
                <td class="px-4 py-3 font-medium text-slate-800">{{ erro.nomeArquivo }}</td>
                <td class="px-4 py-3 text-slate-500 text-xs">
                  Linha: <span class="font-bold text-slate-700">{{ erro.linha || '-' }}</span>
                </td>
                <td class="px-4 py-3 text-slate-600 text-xs font-semibold">{{ getTipoLabel(erro.tipoErro) }}</td>
                <td class="px-4 py-3 text-slate-600 whitespace-normal break-words max-w-md text-xs">{{ erro.mensagemErro }}</td>
                <td class="px-4 py-3 text-center">
                  <span :class="['px-2 py-0.5 text-xs font-semibold rounded-full border', getStatusColor(erro.status)]">
                    {{ erro.status }}
                  </span>
                </td>
                <td class="px-4 py-3 text-right">
                  <button 
                    v-if="erro.status === 'PENDENTE'"
                    @click="resolverErro(erro.id)" 
                    class="px-3 py-1 bg-white border border-slate-200 hover:bg-green-50 text-green-600 hover:border-green-200 font-medium rounded text-xs transition-colors"
                  >
                    Resolver
                  </button>
                  <span v-else class="text-xs text-slate-400">Resolvido</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Paginação -->
        <div v-if="totalPages > 1" class="px-4 py-3 border-t border-slate-200 bg-slate-50 flex items-center justify-between sm:px-6">
          <div class="flex flex-1 justify-between sm:hidden">
            <button @click="prevPage" :disabled="page === 0" class="relative inline-flex items-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50">Anterior</button>
            <button @click="nextPage" :disabled="page >= totalPages - 1" class="relative ml-3 inline-flex items-center rounded-md border border-slate-300 bg-white px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-50">Próxima</button>
          </div>
          <div class="hidden sm:flex sm:flex-1 sm:items-center sm:justify-between">
            <div>
              <p class="text-sm text-slate-700">
                Página <span class="font-medium">{{ page + 1 }}</span> de <span class="font-medium">{{ totalPages }}</span> ({{ totalElements }} resultados)
              </p>
            </div>
            <div>
              <nav class="isolate inline-flex -space-x-px rounded-md shadow-sm" aria-label="Pagination">
                <button @click="prevPage" :disabled="page === 0" class="relative inline-flex items-center rounded-l-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50">
                  <span class="sr-only">Anterior</span>
                  <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true"><path fill-rule="evenodd" d="M12.79 5.23a.75.75 0 01-.02 1.06L8.832 10l3.938 3.71a.75.75 0 11-1.04 1.08l-4.5-4.25a.75.75 0 010-1.08l4.5-4.25a.75.75 0 011.06.02z" clip-rule="evenodd" /></svg>
                </button>
                <button @click="nextPage" :disabled="page >= totalPages - 1" class="relative inline-flex items-center rounded-r-md px-2 py-2 text-slate-400 ring-1 ring-inset ring-slate-300 hover:bg-slate-50 focus:z-20 focus:outline-offset-0 disabled:opacity-50">
                  <span class="sr-only">Próxima</span>
                  <svg class="h-5 w-5" viewBox="0 0 20 20" fill="currentColor" aria-hidden="true"><path fill-rule="evenodd" d="M7.21 14.77a.75.75 0 01.02-1.06L11.168 10 7.23 6.29a.75.75 0 111.04-1.08l4.5 4.25a.75.75 0 010 1.08l-4.5 4.25a.75.75 0 01-1.06-.02z" clip-rule="evenodd" /></svg>
                </button>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
