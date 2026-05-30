<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import AppNav from '@/components/AppNav.vue'

const router = useRouter()
const token = () => localStorage.getItem('token')

// --- Import State ---
const fileRH = ref<File | null>(null)
const fileVendas = ref<File | null>(null)
const fileComissao = ref<File | null>(null)
const isUploading = ref(false)
const isDragging = ref(false)
const uploadMessage = ref<{ type: 'success' | 'error' | null; text: string }>({ type: null, text: '' })

const filesSelected = computed(() => [fileRH.value, fileVendas.value, fileComissao.value].filter(Boolean).length)

function detectAndAssignFile(file: File) {
  const name = file.name.toLowerCase()
  if (name.includes('rh') || name.includes('hr') || name.includes('base_hc') || name.includes('base rh')) {
    fileRH.value = file
  } else if (name.includes('venda') || name.includes('sales')) {
    fileVendas.value = file
  } else if (name.includes('comiss') || name.includes('commission')) {
    fileComissao.value = file
  } else {
    // assign to first empty slot
    if (!fileRH.value) fileRH.value = file
    else if (!fileVendas.value) fileVendas.value = file
    else if (!fileComissao.value) fileComissao.value = file
  }
}

function onDrop(e: DragEvent) {
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files) {
    Array.from(files).forEach(detectAndAssignFile)
  }
}

function onFileInput(e: Event) {
  const files = (e.target as HTMLInputElement).files
  if (files) Array.from(files).forEach(detectAndAssignFile)
}

async function uploadFiles() {
  if (filesSelected.value < 3) {
    uploadMessage.value = { type: 'error', text: 'Selecione os 3 arquivos antes de enviar.' }
    return
  }
  isUploading.value = true
  uploadMessage.value = { type: null, text: '' }
  const form = new FormData()
  form.append('rh', fileRH.value!)
  form.append('vendas', fileVendas.value!)
  form.append('comissao', fileComissao.value!)
  try {
    const res = await fetch('http://localhost:8080/api/upload', {
      method: 'POST', body: form, headers: { Authorization: `Bearer ${token()}` }
    })
    const text = await res.text()
    if (res.ok) {
      uploadMessage.value = { type: 'success', text: text || 'Arquivos importados e cruzados com sucesso.' }
      fileRH.value = null; fileVendas.value = null; fileComissao.value = null
    } else {
      uploadMessage.value = { type: 'error', text: text || 'Falha ao validar os arquivos.' }
    }
  } catch {
    uploadMessage.value = { type: 'error', text: 'Erro ao conectar com o servidor.' }
  } finally {
    isUploading.value = false
  }
}

// --- Payroll State ---
const dateRef = ref('')
const isProcessing = ref(false)
const processMessage = ref<{ type: 'success' | 'error' | null; text: string }>({ type: null, text: '' })
const reportOpen = ref(false)
const reportData = ref<any[]>([])
const isLoadingReport = ref(false)

const totalComissao = computed(() =>
  reportData.value.reduce((acc, r) => acc + (r.valorComissao || 0), 0)
)

function formatBRL(val: number) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(val)
}

async function processarFolha() {
  if (!dateRef.value) {
    processMessage.value = { type: 'error', text: 'Selecione o mês de competência.' }
    return
  }
  isProcessing.value = true
  processMessage.value = { type: null, text: '' }
  reportOpen.value = false
  reportData.value = []
  const formatted = `${dateRef.value}-01`
  try {
    const res = await fetch(`http://localhost:8080/api/calculos/processar-folha?dateRef=${formatted}`, {
      method: 'POST', headers: { Authorization: `Bearer ${token()}` }
    })
    const text = await res.text()
    processMessage.value = { type: res.ok ? 'success' : 'error', text: text || (res.ok ? 'Processado com sucesso!' : 'Falha no processamento.') }
  } catch {
    processMessage.value = { type: 'error', text: 'Erro ao conectar com o servidor.' }
  } finally {
    isProcessing.value = false
  }
}

async function toggleReport() {
  if (!dateRef.value) return
  reportOpen.value = !reportOpen.value
  if (reportOpen.value && reportData.value.length === 0) {
    isLoadingReport.value = true
    try {
      const formatted = `${dateRef.value}-01`
      const res = await fetch(`http://localhost:8080/api/calculos/relatorio?dateRef=${formatted}`, {
        headers: { Authorization: `Bearer ${token()}` }
      })
      reportData.value = res.ok ? await res.json() : []
    } catch {
      reportData.value = []
    } finally {
      isLoadingReport.value = false
    }
  }
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}
</script>

<template>
  <div class="h-screen bg-slate-50 flex flex-col overflow-hidden">
    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1">
        <h1 class="text-sm font-bold text-slate-800">Importação de Dados</h1>
        <p class="text-xs text-slate-400">Envio de planilhas de RH, Vendas e Comissão</p>
      </div>
      <AppNav />
      <button @click="logout" class="p-2 ml-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Main 2-column layout -->
    <div class="flex-1 flex flex-col xl:flex-row gap-6 p-6 overflow-hidden">

      <!-- LEFT CARD: Importar Planilhas -->
      <div class="w-full xl:w-[45%] bg-white rounded-2xl border border-slate-200 shadow-sm p-8 flex flex-col overflow-hidden">
        <!-- Header -->
        <div class="flex flex-col items-center text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-br from-violet-500 to-indigo-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-200 mb-4">
            <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-slate-800">Importar Planilhas</h2>
          <p class="text-sm text-slate-500 mt-1.5 max-w-xs leading-relaxed">
            Selecione ou arraste os 3 arquivos necessários. O Ra Vision irá detectar cada um automaticamente.
          </p>
        </div>

        <!-- Dropzone -->
        <label
          for="fileInput"
          @dragover.prevent="isDragging = true"
          @dragleave="isDragging = false"
          @drop.prevent="onDrop"
          :class="['flex flex-col items-center justify-center gap-2 border-2 border-dashed rounded-xl py-10 cursor-pointer transition-all',
            isDragging ? 'border-indigo-400 bg-indigo-50' : 'border-slate-200 bg-slate-50 hover:border-indigo-300 hover:bg-indigo-50/50']"
        >
          <svg class="w-8 h-8 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 8.25H7.5a2.25 2.25 0 00-2.25 2.25v9a2.25 2.25 0 002.25 2.25h9a2.25 2.25 0 002.25-2.25v-9a2.25 2.25 0 00-2.25-2.25H15M9 12l3 3m0 0l3-3m-3 3V2.25" />
          </svg>
          <span class="text-sm font-medium text-slate-600">Clique para selecionar ou arraste os arquivos</span>
          <span class="text-xs text-slate-400">RH · Vendas · Comissão | .csv ou .xlsx</span>
          <span class="text-xs font-semibold text-indigo-500 mt-1">{{ filesSelected }}/3 arquivos selecionados</span>
        </label>
        <input id="fileInput" type="file" multiple accept=".csv,.xlsx,.xls" class="hidden" @change="onFileInput" />

        <!-- Expected File Names -->
        <div class="mt-6 bg-slate-50 rounded-xl border border-slate-200 p-4 space-y-2.5">
          <p class="text-[10px] uppercase font-bold text-slate-400 tracking-widest mb-3">Nomes Esperados dos Arquivos</p>
          <div class="flex items-center gap-2.5">
            <span class="px-2 py-0.5 bg-blue-500 text-white text-[10px] font-bold rounded shrink-0">HR</span>
            <span class="text-xs text-slate-500 font-mono">HR BASE_NOV25 - BASE HC.csv</span>
            <span v-if="fileRH" class="ml-auto text-emerald-500">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
            </span>
          </div>
          <div class="flex items-center gap-2.5">
            <span class="px-2 py-0.5 bg-emerald-500 text-white text-[10px] font-bold rounded shrink-0">SALES</span>
            <span class="text-xs text-slate-500 font-mono">SALES BASE_NOV25 - SALES.csv</span>
            <span v-if="fileVendas" class="ml-auto text-emerald-500">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
            </span>
          </div>
          <div class="flex items-center gap-2.5">
            <span class="px-2 py-0.5 bg-violet-500 text-white text-[10px] font-bold rounded shrink-0">COMMISSION</span>
            <span class="text-xs text-slate-500 font-mono">BASE_COMPISS_FINAL - Commission.csv</span>
            <span v-if="fileComissao" class="ml-auto text-emerald-500">
              <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" /></svg>
            </span>
          </div>
        </div>

        <!-- Upload status message -->
        <div v-if="uploadMessage.type" class="mt-4 flex items-center gap-2.5 p-3.5 rounded-xl text-sm"
          :class="uploadMessage.type === 'success' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'">
          <svg v-if="uploadMessage.type === 'success'" class="w-5 h-5 shrink-0 text-emerald-500" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
          </svg>
          <svg v-else class="w-5 h-5 shrink-0 text-red-400" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd" />
          </svg>
          <span class="font-medium">{{ uploadMessage.text }}</span>
        </div>

        <!-- Spacer -->
        <div class="flex-1" />

        <!-- Upload Button -->
        <button
          @click="uploadFiles"
          :disabled="isUploading || filesSelected < 3"
          class="mt-6 w-full flex items-center justify-center gap-2 py-3.5 rounded-xl text-sm font-semibold text-white transition-all
            bg-slate-400 disabled:cursor-not-allowed
            enabled:bg-indigo-600 enabled:hover:bg-indigo-700 enabled:hover:-translate-y-0.5 enabled:shadow-md enabled:hover:shadow-lg"
        >
          <svg v-if="isUploading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
          <svg v-else class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" /></svg>
          {{ isUploading ? 'Enviando...' : 'Enviar Planilhas' }}
        </button>
      </div>

      <!-- RIGHT CARD: Fechamento de Folha -->
      <div class="flex-1 bg-white rounded-2xl border border-slate-200 shadow-sm p-8 flex flex-col overflow-hidden">
        
        <!-- Controles fixos (nao crescem) -->
        <div class="shrink-0">
          <!-- Header -->
          <div class="flex flex-col items-center text-center mb-8">
            <div class="w-16 h-16 bg-gradient-to-br from-emerald-400 to-green-600 rounded-2xl flex items-center justify-center shadow-lg shadow-green-200 mb-4">
              <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h2 class="text-2xl font-bold text-slate-800">Fechamento de Folha</h2>
            <p class="text-sm text-slate-500 mt-1.5 max-w-xs leading-relaxed">
              Após importar as planilhas e cadastrar as regras, selecione o mês e rode o motor para calcular as comissões finais.
            </p>
          </div>

          <!-- Mês de Competência -->
          <div class="mb-4">
            <label class="block text-sm font-semibold text-slate-700 mb-1.5">Mês de Competência</label>
            <input type="month" v-model="dateRef"
              class="w-full px-3 py-2.5 text-sm text-slate-700 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500/30 focus:border-green-500 transition-all" />
          </div>

          <!-- Mensagem Processamento -->
          <div v-if="processMessage.type" class="mb-4 flex items-center gap-2.5 p-3.5 rounded-xl text-sm"
            :class="processMessage.type === 'success' ? 'bg-emerald-50 text-emerald-700' : 'bg-red-50 text-red-700'">
            <svg v-if="processMessage.type === 'success'" class="w-5 h-5 shrink-0 text-emerald-500" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
            </svg>
            <span class="font-medium">{{ processMessage.text }}</span>
          </div>

          <!-- Botão Rodar Folha -->
          <button
            @click="processarFolha"
            :disabled="isProcessing || !dateRef"
            class="w-full flex items-center justify-center gap-2 py-3.5 rounded-xl text-sm font-bold text-white transition-all
              disabled:bg-slate-300 disabled:cursor-not-allowed
              enabled:bg-green-600 enabled:hover:bg-green-700 enabled:hover:-translate-y-0.5 enabled:shadow-md enabled:hover:shadow-lg"
          >
            <svg v-if="isProcessing" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
            {{ isProcessing ? 'Processando...' : 'Rodar Processamento Completo da Folha' }}
          </button>

          <!-- Toggle Relatório -->
          <button
            v-if="dateRef"
            @click="toggleReport"
            class="mt-4 w-full flex items-center justify-between px-4 py-3 rounded-xl border border-slate-200 hover:bg-slate-50 transition-colors"
          >
            <div class="flex items-center gap-2 text-sm font-semibold text-slate-700">
              <svg class="w-4 h-4 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
              Visualizar Relatório de Folha
              <span v-if="reportData.length" class="text-xs text-slate-400 font-normal">{{ reportData.length }} funcionários</span>
            </div>
            <div class="flex items-center gap-3">
              <span v-if="reportData.length" class="text-sm font-bold text-emerald-600">{{ formatBRL(totalComissao) }}</span>
              <svg class="w-4 h-4 text-slate-400 transition-transform duration-200" :class="{ 'rotate-180': reportOpen }" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
              </svg>
            </div>
          </button>
        </div><!-- fim shrink-0 -->

        <!-- Tabela do Relatório: cresce até o fim do card -->
        <div v-if="reportOpen" class="mt-2 flex-1 min-h-0 rounded-xl border border-slate-200 flex flex-col overflow-hidden">
          <!-- Cabeçalho do Relatório -->
          <div class="flex items-center justify-between px-4 py-3 bg-white border-b border-slate-100 shrink-0">
            <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">
              Relatório de Folha — {{ dateRef }}
            </span>
            <span class="text-sm font-bold text-emerald-600">{{ formatBRL(totalComissao) }}</span>
          </div>

          <!-- Loading -->
          <div v-if="isLoadingReport" class="flex items-center justify-center py-10 gap-2">
            <svg class="w-5 h-5 animate-spin text-indigo-500" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
            <span class="text-sm text-slate-500">Buscando relatório...</span>
          </div>

          <!-- Vazio -->
          <div v-else-if="!reportData.length" class="text-center py-10 text-sm text-slate-400">
            Nenhum dado encontrado. Execute o processamento da folha primeiro para este mês.
          </div>

          <!-- Tabela com scroll interno -->
          <div v-else class="flex-1 min-h-0 overflow-y-auto">
            <table class="w-full text-xs text-left">
              <thead class="sticky top-0 bg-slate-50 border-b border-slate-200">
                <tr>
                  <th class="px-4 py-2.5 font-bold text-slate-500 uppercase tracking-wider">Matrícula</th>
                  <th class="px-4 py-2.5 font-bold text-slate-500 uppercase tracking-wider">Loja</th>
                  <th class="px-4 py-2.5 font-bold text-slate-500 uppercase tracking-wider text-center">Trabalhados</th>
                  <th class="px-4 py-2.5 font-bold text-slate-500 uppercase tracking-wider">Motivo</th>
                  <th class="px-4 py-2.5 font-bold text-slate-500 uppercase tracking-wider text-right">Comissão</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                <tr v-for="row in reportData" :key="row.matricula" class="hover:bg-slate-50/70">
                  <td class="px-4 py-2.5 font-mono font-semibold text-indigo-600">{{ row.matricula }}</td>
                  <td class="px-4 py-2.5 text-slate-600">{{ row.loja || '—' }}</td>
                  <td class="px-4 py-2.5 text-slate-600 text-center">{{ row.diasTrabalhados }}<span class="text-slate-400">/{{ row.diasDoMes }}</span></td>
                  <td class="px-4 py-2.5 text-slate-600">{{ row.motivo || 'INTEGRAL' }}</td>
                  <td class="px-4 py-2.5 font-semibold text-slate-800 text-right">{{ formatBRL(row.valorComissao) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

      </div><!-- fim right card -->

    </div>
  </div>
</template>
