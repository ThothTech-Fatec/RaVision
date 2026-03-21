<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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

const suggestions = [
  'Aplique essa regra de negócio pra mim',
  'Por que essa decisão foi tomada?',
  'Analise o risco desta operação',
  'Sugira critérios para aprovar este processo',
]

function getTime() {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

async function sendMessage(text?: string) {
  const content = (text ?? newMessage.value).trim()
  if (!content) return

  messages.value.push({ id: Date.now(), text: content, fromMe: true, time: getTime() })
  newMessage.value = ''

  await nextTick()
  messagesEndRef.value?.scrollIntoView({ behavior: 'smooth' })

  // Simula IA digitando
  isTyping.value = true
  await new Promise((resolve) => setTimeout(resolve, 1200 + Math.random() * 800))
  isTyping.value = false

  messages.value.push({
    id: Date.now() + 1,
    text: 'Entendi sua mensagem! Quando o backend estiver conectado, responderei com inteligência artificial. Por enquanto, estou apenas simulando uma resposta.',
    fromMe: false,
    time: getTime(),
  })

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
  router.push('/')
}
</script>

<template>
  <div class="h-screen flex flex-col bg-slate-50 overflow-hidden">
    <!-- Header -->
    <header class="flex items-center gap-3 px-5 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <!-- Logo + nome da IA -->
      <div class="flex items-center gap-3 flex-1 min-w-0">
        <div class="relative shrink-0">
          <div class="w-9 h-9 bg-linear-to-br from-indigo-500 to-purple-600 rounded-xl flex items-center justify-center shadow-md shadow-indigo-200">
            <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
            </svg>
          </div>
        </div>
        <div class="min-w-0">
          <h1 class="text-sm font-bold text-slate-800 leading-tight">Ra Vision IA</h1>
          <p class="text-xs text-slate-400">Assistente de negócios</p>
        </div>
      </div>

      <!-- Ações -->
      <div class="flex items-center gap-1">
        <button
          @click="newChat"
          class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-600 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-colors"
          title="Nova conversa"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          <span class="hidden sm:inline">Nova conversa</span>
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
            Organizo regras de negócio, explico decisões e apoio sua equipe a tomar escolhas mais inteligentes.
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
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['flex gap-3', msg.fromMe ? 'justify-end' : 'justify-start']"
        >
          <!-- Avatar da IA -->
          <div
            v-if="!msg.fromMe"
            class="w-7 h-7 bg-linear-to-br from-indigo-500 to-purple-600 rounded-lg flex items-center justify-center shrink-0 mt-auto shadow-sm"
          >
            <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09z" />
            </svg>
          </div>

          <div class="max-w-xs sm:max-w-sm md:max-w-md lg:max-w-lg xl:max-w-xl">
            <div
              :class="[
                'px-4 py-3 rounded-2xl text-sm leading-relaxed',
                msg.fromMe
                  ? 'bg-indigo-600 text-white rounded-br-sm'
                  : 'bg-white text-slate-800 shadow-sm border border-slate-100 rounded-bl-sm',
              ]"
            >
              {{ msg.text }}
            </div>
            <p :class="['text-xs mt-1 text-slate-400', msg.fromMe ? 'text-right' : '']">
              {{ msg.fromMe ? 'Você' : 'Ra Vision IA' }} · {{ msg.time }}
            </p>
          </div>
        </div>

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
        <div class="flex items-end gap-2 bg-slate-100 rounded-2xl px-4 py-2.5">
          <textarea
            v-model="newMessage"
            @keydown="handleKeydown"
            placeholder="Pergunte qualquer coisa..."
            rows="1"
            :disabled="isTyping"
            class="flex-1 bg-transparent text-sm text-slate-800 placeholder-slate-400 resize-none focus:outline-none py-0.5 max-h-32 disabled:opacity-50"
          ></textarea>

          <button
            @click="sendMessage()"
            :disabled="!newMessage.trim() || isTyping"
            class="p-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 text-white rounded-xl transition-all duration-150 shrink-0 disabled:cursor-not-allowed hover:-translate-y-0.5 active:translate-y-0"
            title="Enviar"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
            </svg>
          </button>
        </div>
        <p class="text-xs text-slate-400 text-center mt-1.5">Enter para enviar · Shift+Enter para nova linha</p>
      </div>
    </div>
  </div>
</template>
