<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import MarkdownIt from 'markdown-it'
import ChatSidebar from '@/components/ChatSidebar.vue'

const sidebarOpen = ref(true)
const router = useRouter()
const md = new MarkdownIt()

interface Message {
  id: number
  text: string
  fromMe: boolean
  time: string
}

const newMessage = ref('')
const isTyping = ref(false)
const messagesEndRef = ref<HTMLElement | null>(null)
const messages = ref<Message[]>([])

// Mês de referência (Competência)
const dateRef = ref('2025-11-01') // Default para o último mês do mock
const availableDates = ref(['2025-09-01', '2025-10-01', '2025-11-01'])

const suggestions = [
  'Qual a comissão do MATRIC-102?',
  'Explique o cálculo do MATRIC-110',
  'Quem é o gerente da LOJA-10?',
  'Por que o bônus da Black Friday foi aplicado?',
]

function getTime() {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

async function sendMessage(text?: string) {
  const content = (text ?? newMessage.value).trim()
  if (!content || isTyping.value) return

  // Adiciona mensagem do usuário
  messages.value.push({ id: Date.now(), text: content, fromMe: true, time: getTime() })
  newMessage.value = ''

  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })

  // Chama a IA real via Backend Java Proxy
  isTyping.value = true
  
  const token = localStorage.getItem('token')

  try {
    const response = await axios.post('http://localhost:8080/api/chat', {
      message: content,
      dateRef: dateRef.value
    }, {
      headers: { Authorization: `Bearer ${token}` }
    })

    const aiText = response.data.response

    messages.value.push({
      id: Date.now() + 1,
      text: aiText,
      fromMe: false,
      time: getTime(),
    })
  } catch (error: any) {
    console.error('Erro ao chamar IA:', error)
    messages.value.push({
      id: Date.now() + 1,
      text: '⚠️ Ocorreu um erro ao conectar com o assistente Ra Vision. Verifique se os serviços backend estão rodando.',
      fromMe: false,
      time: getTime(),
    })
  } finally {
    isTyping.value = false
  }

  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function newChat() {
  messages.value = []
}

function logout() {
  localStorage.clear()
  router.push('/')
}

const isAdmin = localStorage.getItem('role') === 'ADMINISTRADOR'

function formatMarkdown(text: string) {
  return md.render(text)
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
        <h1 class="text-sm font-bold text-slate-800 leading-tight">Ra Vision IA</h1>
        <p class="text-xs text-slate-400">Assistente de Operações</p>
      </div>

      <!-- Seletor de Competência -->
      <div class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-lg">
        <span class="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Mês:</span>
        <select 
          v-model="dateRef" 
          class="text-xs font-semibold text-indigo-600 bg-transparent border-none focus:outline-none cursor-pointer"
        >
          <option v-for="date in availableDates" :key="date" :value="date">
            {{ date.split('-').reverse().join('/') }}
          </option>
        </select>
      </div>

      <!-- Navegação -->
      <div class="flex items-center gap-2">
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
          @click="router.push('/historico')"
          class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200 shrink-0"
          title="Histórico de Execuções"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span class="hidden sm:inline">Histórico</span>
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
        <button
          v-if="isAdmin"
          @click="router.push('/cadastro')"
          class="flex items-center gap-2 px-3 py-1.5 bg-purple-50 hover:bg-purple-100 text-purple-600 text-xs font-semibold rounded-lg transition-colors border border-purple-100 shrink-0"
          title="Cadastrar Usuário"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
          </svg>
          <span class="hidden sm:inline">Cadastrar</span>
        </button>
      </div>

      <!-- Ações -->
      <div class="flex items-center gap-1 ml-2">
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

    <!-- Área de mensagens -->
    <div class="flex-1 overflow-y-auto flex flex-col">
      <div class="flex-1 max-w-3xl w-full mx-auto px-4 py-6 space-y-5 flex flex-col">

        <!-- Estado vazio: boas-vindas -->
        <div v-if="messages.length === 0" class="flex flex-col items-center justify-center flex-1 text-center px-4">
          <div class="w-16 h-16 bg-linear-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-xl shadow-indigo-200 mb-5">
            <svg class="w-8 h-8 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
            </svg>
          </div>
          <h2 class="text-2xl font-bold text-slate-800 mb-2">Olá! Sou a Ra Vision IA</h2>
          <p class="text-slate-500 text-sm max-w-sm leading-relaxed mb-8">
            Explico cálculos de comissão, identifico anomalias e apoio na gestão de regras de negócio.
          </p>

          <!-- Sugestões -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-2.5 w-full max-w-md">
            <button
              v-for="s in suggestions"
              :key="s"
              @click="sendMessage(s)"
              class="flex items-center gap-2.5 px-4 py-3 bg-white border border-slate-200 hover:border-indigo-300 hover:bg-indigo-50 rounded-xl text-left text-sm text-slate-700 hover:text-indigo-700 transition-all duration-150 shadow-sm hover:shadow-md hover:-translate-y-0.5"
            >
              <svg class="w-4 h-4 text-indigo-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              {{ s }}
            </button>
          </div>
        </div>

        <!-- Separador de data -->
        <div v-if="messages.length > 0" class="flex items-center gap-3">
          <div class="flex-1 h-px bg-slate-200"></div>
          <span class="text-xs text-slate-400 font-medium">Hoje</span>
          <div class="flex-1 h-px bg-slate-200"></div>
        </div>

        <!-- Mensagens -->
        <template v-for="msg in messages" :key="msg.id">
          <div :class="['flex gap-3', msg.fromMe ? 'justify-end' : 'justify-start w-full']">
            <!-- Avatar da IA -->
            <div
              v-if="!msg.fromMe"
              class="w-7 h-7 bg-linear-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center shrink-0 mt-2 shadow-sm"
            >
              <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
              </svg>
            </div>

            <div :class="['flex flex-col', msg.fromMe ? 'max-w-[85%] items-end' : 'flex-1 min-w-0']">
              <div
                :class="[
                  'px-4 py-3 rounded-2xl text-sm leading-relaxed prose prose-slate max-w-none',
                  msg.fromMe
                    ? 'bg-indigo-600 text-white rounded-br-sm'
                    : 'bg-white text-slate-800 shadow-sm border border-slate-100 rounded-bl-sm',
                ]"
              >
                <div v-if="!msg.fromMe" class="markdown-container" v-html="formatMarkdown(msg.text)"></div>
                <div v-else>{{ msg.text }}</div>
              </div>
              <p :class="['text-[10px] mt-1 text-slate-400 uppercase font-bold tracking-tight', msg.fromMe ? 'text-right' : '']">
                {{ msg.fromMe ? 'Operações' : 'Ra Vision IA' }} · {{ msg.time }}
              </p>
            </div>
          </div>
        </template>

        <!-- Indicador de digitação da IA -->
        <div v-if="isTyping" class="flex items-end gap-3">
          <div class="w-7 h-7 bg-linear-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center shrink-0 shadow-sm">
            <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
            </svg>
          </div>
          <div class="bg-white border border-slate-100 shadow-sm px-4 py-3.5 rounded-2xl rounded-bl-sm flex items-center gap-1.5">
            <span class="w-2 h-2 bg-indigo-400 rounded-full animate-bounce [animation-delay:0ms]"></span>
            <span class="w-2 h-2 bg-indigo-400 rounded-full animate-bounce [animation-delay:150ms]"></span>
            <span class="w-2 h-2 bg-indigo-400 rounded-full animate-bounce [animation-delay:300ms]"></span>
          </div>
        </div>

        <div ref="messagesEndRef"></div>
      </div>
    </div>

    <!-- Input de mensagem -->
    <div class="shrink-0 px-4 py-3 bg-white border-t border-slate-200">
      <div class="max-w-3xl mx-auto">
        <div class="flex items-end gap-2 bg-slate-100 rounded-2xl px-4 py-2.5 shadow-inner">
          <textarea
            v-model="newMessage"
            @keydown="handleKeydown"
            placeholder="Ex: Qual a comissão do MATRIC-102?"
            rows="1"
            :disabled="isTyping"
            class="flex-1 bg-transparent text-sm text-slate-800 placeholder-slate-400 resize-none focus:outline-none py-0.5 max-h-32 disabled:opacity-50"
          ></textarea>

          <button
            @click="sendMessage()"
            :disabled="!newMessage.trim() || isTyping"
            class="p-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white rounded-xl transition-all duration-150 shrink-0 disabled:cursor-not-allowed shadow-md hover:shadow-lg active:shadow-sm"
            title="Enviar"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </button>
        </div>
        <p class="text-[10px] text-slate-400 text-center mt-2 flex items-center justify-center gap-1">
          <svg class="w-3 h-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          A IA pode cometer erros. Verifique os dados importantes.
        </p>
      </div>
    </div>
    </div><!-- fim área principal -->
  </div>
</template>

<style scoped>
@reference "../assets/main.css";

.markdown-container :deep(table) {
  @apply w-full border-collapse my-3 text-xs;
}
.markdown-container :deep(th), .markdown-container :deep(td) {
  @apply border border-slate-200 px-3 py-1.5 text-left;
}
.markdown-container :deep(th) {
  @apply bg-slate-50 font-bold text-slate-600;
}
.markdown-container :deep(p) {
  @apply my-2 first:mt-0 last:mb-0;
}
.markdown-container :deep(code) {
  @apply bg-slate-100 px-1 rounded font-mono text-indigo-600;
}
</style>
