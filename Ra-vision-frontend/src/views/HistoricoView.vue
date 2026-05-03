<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

interface HistoricoItem {
  id: number
  username: string
  acaoRealizada: string
  dataHora: string
  detalhesAcao: string
}

const historico = ref<HistoricoItem[]>([])
const isLoading = ref(true)
const errorMessage = ref('')
const deleteError = ref('')
const role = localStorage.getItem('role')
const isAdmin = computed(() => role === 'ADMINISTRADOR')

function getAuthHeaders() {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

async function carregarHistorico() {
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await axios.get('http://localhost:8080/api/historico', {
      headers: getAuthHeaders(),
    })
    historico.value = response.data
  } catch (error: any) {
    if (error.response?.status === 401 || error.response?.status === 403) {
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('username')
      router.push('/')
    } else {
      errorMessage.value = 'Erro ao carregar o histórico. Verifique se o backend está rodando.'
    }
  } finally {
    isLoading.value = false
  }
}

async function excluirRegistro(id: number) {
  if (!confirm('Excluir este registro do histórico?')) return
  deleteError.value = ''
  try {
    await axios.delete(`http://localhost:8080/api/historico/${id}`, { headers: getAuthHeaders() })
    historico.value = historico.value.filter(h => h.id !== id)
  } catch (error: any) {
    deleteError.value = error.response?.data?.message || 'Erro ao excluir registro.'
  }
}

async function limparHistorico() {
  if (!confirm('Tem certeza que deseja apagar TODO o histórico? Esta ação não pode ser desfeita.')) return
  deleteError.value = ''
  try {
    await axios.delete('http://localhost:8080/api/historico', { headers: getAuthHeaders() })
    historico.value = []
  } catch (error: any) {
    deleteError.value = error.response?.data?.message || 'Erro ao limpar histórico.'
  }
}

function formatDate(dateStr: string) {
  if (!dateStr) return '—'
  const d = new Date(dateStr)
  return d.toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

function getActionIcon(acao: string) {
  if (acao.toLowerCase().includes('login')) return 'login'
  if (acao.toLowerCase().includes('registro') || acao.toLowerCase().includes('registro')) return 'register'
  if (acao.toLowerCase().includes('cálculo') || acao.toLowerCase().includes('calculo')) return 'calc'
  if (acao.toLowerCase().includes('regra')) return 'rule'
  return 'default'
}

function getActionColor(acao: string) {
  const type = getActionIcon(acao)
  switch (type) {
    case 'login': return 'bg-green-50 text-green-700'
    case 'register': return 'bg-indigo-50 text-indigo-700'
    case 'calc': return 'bg-amber-50 text-amber-700'
    case 'rule': return 'bg-purple-50 text-purple-700'
    default: return 'bg-slate-100 text-slate-600'
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}

onMounted(carregarHistorico)
</script>

<template>
  <div class="h-screen flex flex-col bg-slate-50 overflow-hidden">

    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight">Histórico de Execuções</h1>
        <p class="text-xs text-slate-400">Rastreabilidade e auditoria de ações do sistema</p>
      </div>

      <!-- Navegação -->
      <div class="flex items-center gap-2">
        <button
          @click="router.push('/chat')"
          class="flex items-center gap-2 px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 text-xs font-semibold rounded-lg transition-colors border border-indigo-100 shrink-0"
          title="Ir para o Chat"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <span class="hidden sm:inline">Chat</span>
        </button>
        <button
          @click="router.push('/regras')"
          class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200 shrink-0"
          title="Regras de Negócio"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <span class="hidden sm:inline">Regras</span>
        </button>
        <button
          @click="router.push('/perfil')"
          class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200 shrink-0"
          title="Meu Perfil"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span class="hidden sm:inline">Perfil</span>
        </button>
      </div>

      <!-- Ações -->
      <div class="flex items-center gap-1 ml-2">
        <!-- Limpar todo o histórico (admin only) -->
        <button
          v-if="isAdmin"
          @click="limparHistorico"
          class="flex items-center gap-1.5 px-3 py-1.5 text-red-500 hover:text-red-700 hover:bg-red-50 rounded-xl transition-colors text-xs font-semibold border border-red-100"
          title="Limpar todo o histórico"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
          <span class="hidden sm:inline">Limpar Tudo</span>
        </button>
        <button
          @click="carregarHistorico"
          class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors"
          title="Atualizar"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
          </svg>
        </button>
        <button
          @click="logout"
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
          title="Sair"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
        </button>
      </div>
    </header>

    <!-- Conteúdo -->
    <div class="flex-1 overflow-y-auto p-6">
      <div class="max-w-5xl mx-auto">

        <!-- Header de contagem -->
        <div class="flex items-center justify-between mb-6">
          <p class="text-xs text-slate-400">
            {{ historico.length }} registro{{ historico.length !== 1 ? 's' : '' }} encontrado{{ historico.length !== 1 ? 's' : '' }}
          </p>
        </div>

        <!-- Erro -->
        <div v-if="errorMessage || deleteError" class="flex items-center gap-3 p-4 mb-6 bg-red-50 text-red-700 rounded-xl text-sm font-medium">
          <svg class="w-5 h-5 text-red-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd" />
          </svg>
          {{ errorMessage || deleteError }}
        </div>

        <!-- Loading -->
        <div v-if="isLoading" class="flex flex-col items-center justify-center py-20">
          <svg class="w-8 h-8 animate-spin text-indigo-500 mb-4" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
          <p class="text-sm text-slate-500">Carregando histórico...</p>
        </div>

        <!-- Tabela -->
        <div v-else-if="historico.length > 0" class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <table class="w-full text-sm" id="historico-table">
            <thead>
              <tr class="border-b border-slate-100 bg-slate-50">
                <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider">Usuário</th>
                <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider">Ação</th>
                <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider whitespace-nowrap">Data / Hora</th>
                <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider">Detalhes</th>
                <th v-if="isAdmin" class="px-5 py-3"></th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr
                v-for="item in historico"
                :key="item.id"
                class="hover:bg-slate-50 transition-colors"
              >
                <td class="px-5 py-4">
                  <div class="flex items-center gap-2.5">
                    <div class="w-7 h-7 bg-linear-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center shrink-0">
                      <span class="text-white font-bold text-[10px]">{{ item.username.charAt(0).toUpperCase() }}</span>
                    </div>
                    <span class="font-semibold text-slate-800">{{ item.username }}</span>
                  </div>
                </td>
                <td class="px-5 py-4">
                  <span :class="['inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold', getActionColor(item.acaoRealizada)]">
                    {{ item.acaoRealizada }}
                  </span>
                </td>
                <td class="px-5 py-4 whitespace-nowrap text-slate-600">
                  {{ formatDate(item.dataHora) }}
                </td>
                <td class="px-5 py-4">
                  <span class="text-slate-600 line-clamp-2 leading-relaxed">{{ item.detalhesAcao || '—' }}</span>
                </td>
                <!-- Botão excluir (admin only) -->
                <td v-if="isAdmin" class="px-3 py-4 text-right">
                  <button
                    @click="excluirRegistro(item.id)"
                    class="p-1.5 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Excluir registro"
                  >
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Estado vazio -->
        <div v-else class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <div class="flex flex-col items-center justify-center py-20 text-center px-4">
            <div class="w-16 h-16 bg-slate-100 rounded-2xl flex items-center justify-center mb-4">
              <svg class="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <p class="text-slate-600 font-medium text-sm">Nenhum registro de execução encontrado</p>
            <p class="text-slate-400 text-xs mt-1 max-w-xs leading-relaxed">
              As ações realizadas no sistema aparecerão aqui conforme os usuários interagirem.
            </p>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>
