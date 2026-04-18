<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ChatSidebar from '@/components/ChatSidebar.vue'

const sidebarOpen = ref(true)
const router = useRouter()

const fileRH = ref<File | null>(null)
const fileVendas = ref<File | null>(null)
const fileComissao = ref<File | null>(null)

const isUploading = ref(false)
const statusMessage = ref<{ type: 'success' | 'error' | null; text: string }>({
  type: null,
  text: '',
})

function handleFileChange(event: Event, type: 'rh' | 'vendas' | 'comissao') {
  const target = event.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    if (type === 'rh') fileRH.value = target.files[0]
    if (type === 'vendas') fileVendas.value = target.files[0]
    if (type === 'comissao') fileComissao.value = target.files[0]
  }
}

async function uploadFiles() {
  if (!fileRH.value || !fileVendas.value || !fileComissao.value) {
    statusMessage.value = {
      type: 'error',
      text: 'Por favor, selecione os três arquivos antes de enviar.',
    }
    return
  }

  isUploading.value = true
  statusMessage.value = { type: null, text: '' }

  const formData = new FormData()
  formData.append('rh', fileRH.value)
  formData.append('vendas', fileVendas.value)
  formData.append('comissao', fileComissao.value)

  const token = localStorage.getItem('token')

  try {
    const response = await fetch('http://localhost:8080/api/upload', {
      method: 'POST',
      body: formData,
      headers: { Authorization: `Bearer ${token}` },
    })

    const result = await response.text()

    if (response.ok) {
      statusMessage.value = { type: 'success', text: result || 'Upload realizado com sucesso!' }
      // Limpar formulário se desejar
      fileRH.value = null
      fileVendas.value = null
      fileComissao.value = null
    } else {
      statusMessage.value = { type: 'error', text: result || 'Falha na validação do arquivo.' }
    }
  } catch (error: any) {
    statusMessage.value = {
      type: 'error',
      text: 'Erro ao conectar-se com o servidor. Verifique se o Back-end está rodando.',
    }
  } finally {
    isUploading.value = false
  }
}

function newChat() {
  router.push('/chat')
}

function logout() {
  localStorage.clear()
  router.push('/')
}
</script>

<template>
  <div class="h-screen flex flex-row bg-slate-50 overflow-hidden">
    <!-- Sidebar -->
    <ChatSidebar :open="sidebarOpen" @toggle="sidebarOpen = !sidebarOpen" @new-chat="newChat" />

    <!-- Área principal -->
    <div class="flex-1 flex flex-col overflow-hidden min-w-0">
      <!-- Header -->
      <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
        <!-- Toggle sidebar (mobile) -->
        <button
          @click="sidebarOpen = !sidebarOpen"
          class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors shrink-0 md:hidden"
          title="Menu"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>

        <!-- Título -->
        <div class="flex-1 min-w-0">
          <h1 class="text-sm font-bold text-slate-800 leading-tight">Importação de Dados</h1>
          <p class="text-xs text-slate-400">Envio de planilhas de competência RH, Vendas e Comissão</p>
        </div>

        <!-- Ações -->
        <div class="flex items-center gap-1">
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

      <!-- Área de Importação (Formulário) -->
      <div class="flex-1 overflow-y-auto flex flex-col items-center justify-start p-6">
        <div class="w-full max-w-2xl bg-white rounded-3xl shadow-xs border border-slate-200 p-8 mt-4">
          
          <div class="flex flex-col items-center justify-center text-center mb-8">
            <div class="w-16 h-16 bg-linear-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-xl shadow-indigo-200 mb-5">
              <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
              </svg>
            </div>
            <h2 class="text-2xl font-bold text-slate-800 mb-2">Importar Planilhas de Competência</h2>
            <p class="text-slate-500 text-sm max-w-md leading-relaxed">
              Realize o upload dos relatórios base gerados pelo seu ERP. O <strong>Ra Vision</strong> fará o cruzamento e processamento dos cálculos automaticamente.
            </p>
          </div>

          <form @submit.prevent="uploadFiles" class="space-y-6">
            
            <!-- Campo RH -->
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">1. Planilha de RH</label>
              <div class="relative group">
                <div class="absolute inset-0 bg-indigo-50/50 hidden group-hover:block rounded-xl border-2 border-dashed border-indigo-300 pointer-events-none transition-all"></div>
                <input
                  type="file"
                  accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                  @change="(e) => handleFileChange(e, 'rh')"
                  class="block w-full text-sm text-slate-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 bg-slate-50 rounded-xl border border-slate-200 cursor-pointer shadow-xs focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>

            <!-- Campo Vendas -->
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">2. Planilha de Vendas</label>
              <div class="relative group">
                <div class="absolute inset-0 bg-indigo-50/50 hidden group-hover:block rounded-xl border-2 border-dashed border-indigo-300 pointer-events-none transition-all"></div>
                <input
                  type="file"
                  accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                  @change="(e) => handleFileChange(e, 'vendas')"
                  class="block w-full text-sm text-slate-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 bg-slate-50 rounded-xl border border-slate-200 cursor-pointer shadow-xs focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>

            <!-- Campo Comissão -->
            <div>
              <label class="block text-sm font-semibold text-slate-700 mb-2">3. Planilha de Comissão Base</label>
              <div class="relative group">
                <div class="absolute inset-0 bg-indigo-50/50 hidden group-hover:block rounded-xl border-2 border-dashed border-indigo-300 pointer-events-none transition-all"></div>
                <input
                  type="file"
                  accept=".csv, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet, application/vnd.ms-excel"
                  @change="(e) => handleFileChange(e, 'comissao')"
                  class="block w-full text-sm text-slate-500 file:mr-4 file:py-2.5 file:px-4 file:rounded-xl file:border-0 file:text-sm file:font-semibold file:bg-indigo-50 file:text-indigo-700 hover:file:bg-indigo-100 bg-slate-50 rounded-xl border border-slate-200 cursor-pointer shadow-xs focus:ring-2 focus:ring-indigo-500/50"
                />
              </div>
            </div>

            <!-- Feedbacks Visuais -->
            <div
              v-if="statusMessage.type"
              :class="[
                'p-4 rounded-xl text-sm flex gap-3',
                statusMessage.type === 'success' ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800'
              ]"
            >
              <div v-if="statusMessage.type === 'success'" class="hidden sm:block">
                <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
                </svg>
              </div>
              <div v-else class="hidden sm:block">
                <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd" />
                </svg>
              </div>
              <div class="flex-1 font-medium whitespace-pre-wrap">{{ statusMessage.text }}</div>
            </div>

            <!-- Botão de Upload -->
            <button
              type="submit"
              :disabled="isUploading || !fileRH || !fileVendas || !fileComissao"
              class="w-full mt-4 flex justify-center items-center py-3.5 px-4 border border-transparent rounded-xl shadow-sm md:shadow-md text-sm font-semibold text-white bg-indigo-600 hover:bg-indigo-700 hover:-translate-y-0.5 active:translate-y-0 disabled:bg-slate-300 disabled:-translate-y-0 disabled:cursor-not-allowed transition-all"
            >
              <svg v-if="isUploading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              <span v-if="!isUploading">Enviar para Processamento</span>
              <span v-else>Enviando planilhas...</span>
            </button>
            <p class="text-xs text-slate-400 text-center mt-3">São permitidos apenas arquivos .csv ou Excel .xlsx.</p>

          </form>
        </div>
      </div>
      
    </div><!-- fim área principal -->
  </div>
</template>
