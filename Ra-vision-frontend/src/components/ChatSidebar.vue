<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

defineProps<{ open: boolean }>()
const emit = defineEmits<{ toggle: []; 'new-chat': [] }>()

interface Conversation {
  id: number
  title: string
  time: string
  group: 'hoje' | 'ontem' | 'anterior'
}

const activeId = ref(1)

const conversations: Conversation[] = [
  { id: 1, title: 'Análise de risco da operação', time: '14:32', group: 'hoje' },
  { id: 2, title: 'Regras de aprovação de crédito', time: '11:20', group: 'hoje' },
  { id: 3, title: 'Decisão sobre fornecedor X', time: 'Ontem', group: 'ontem' },
  { id: 4, title: 'Critérios de conformidade', time: 'Ontem', group: 'ontem' },
  { id: 5, title: 'Revisão de processo interno', time: '20/03', group: 'anterior' },
  { id: 6, title: 'Auditoria de fluxo financeiro', time: '19/03', group: 'anterior' },
]

const byGroup = (group: Conversation['group']) => conversations.filter((c) => c.group === group)

function selectChat(id: number) {
  activeId.value = id
}
</script>

<template>
  <!-- Backdrop mobile -->
  <div
    v-if="open"
    class="fixed inset-0 bg-black/20 z-40 md:hidden"
    @click="emit('toggle')"
  />

  <!-- Sidebar -->
  <aside
    :class="[
      'fixed md:relative inset-y-0 left-0 z-50 md:z-auto h-full',
      'bg-white border-r border-slate-200 flex flex-col',
      'transition-all duration-300 ease-in-out overflow-hidden shrink-0',
      open ? 'w-64 shadow-2xl md:shadow-none' : 'w-0 md:w-14',
    ]"
  >
    <!-- Header -->
    <div class="flex items-center gap-3 px-3 border-b border-slate-200 shrink-0 py-3.5">

      <!-- Quando aberta -->
      <template v-if="open">
        <div class="w-9 h-9 bg-slate-100 rounded-xl flex items-center justify-center shrink-0">
          <svg class="w-5 h-5 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-bold text-slate-800 truncate leading-tight">Conversas</p>
          <p class="text-xs text-slate-400">Histórico de chats</p>
        </div>
        <button
          @click="emit('toggle')"
          class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors shrink-0"
          title="Recolher sidebar"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
          </svg>
        </button>
      </template>

      <!-- Quando recolhida -->
      <template v-else>
        <button
          @click="emit('toggle')"
          class="w-9 h-9 flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors mx-auto"
          title="Expandir sidebar"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M13 5l7 7-7 7M5 5l7 7-7 7" />
          </svg>
        </button>
      </template>
    </div>

    <!-- Nova conversa -->
    <div :class="['py-3 shrink-0 space-y-2', open ? 'px-3' : 'px-2']">
      <button
        @click="emit('new-chat')"
        :class="[
          'w-full flex items-center gap-2.5 rounded-xl transition-colors text-sm font-medium',
          'bg-indigo-50 hover:bg-indigo-100 text-indigo-600',
          open ? 'px-3 py-2.5' : 'justify-center px-2 py-2.5',
        ]"
        title="Nova conversa"
      >
        <svg class="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        <span v-if="open">Nova conversa</span>
      </button>

      <button
        @click="router.push('/importar')"
        :class="[
          'w-full flex items-center gap-2.5 rounded-xl transition-colors text-sm font-medium',
          'bg-slate-50 hover:bg-slate-100 text-slate-700 border border-slate-200',
          open ? 'px-3 py-2.5' : 'justify-center px-2 py-2.5',
        ]"
        title="Importar Dados"
      >
        <svg class="w-4 h-4 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
        </svg>
        <span v-if="open">Importar Dados</span>
      </button>
    </div>

    <!-- Lista de conversas (só quando aberta) -->
    <div v-if="open" class="flex-1 overflow-y-auto px-2 pb-2">
      <!-- Hoje -->
      <template v-if="byGroup('hoje').length">
        <p class="text-xs font-semibold text-slate-400 px-2 py-1.5 uppercase tracking-wider">Hoje</p>
        <button
          v-for="conv in byGroup('hoje')"
          :key="conv.id"
          @click="selectChat(conv.id)"
          :class="[
            'w-full flex flex-col items-start px-3 py-2.5 rounded-xl text-left transition-colors mb-0.5',
            activeId === conv.id
              ? 'bg-indigo-50 text-indigo-700'
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-800',
          ]"
        >
          <span class="text-sm font-medium truncate w-full">{{ conv.title }}</span>
          <span class="text-xs text-slate-400 mt-0.5">{{ conv.time }}</span>
        </button>
      </template>

      <!-- Ontem -->
      <template v-if="byGroup('ontem').length">
        <p class="text-xs font-semibold text-slate-400 px-2 pt-3 pb-1.5 uppercase tracking-wider">Ontem</p>
        <button
          v-for="conv in byGroup('ontem')"
          :key="conv.id"
          @click="selectChat(conv.id)"
          :class="[
            'w-full flex flex-col items-start px-3 py-2.5 rounded-xl text-left transition-colors mb-0.5',
            activeId === conv.id
              ? 'bg-indigo-50 text-indigo-700'
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-800',
          ]"
        >
          <span class="text-sm font-medium truncate w-full">{{ conv.title }}</span>
          <span class="text-xs text-slate-400 mt-0.5">{{ conv.time }}</span>
        </button>
      </template>

      <!-- Anteriores -->
      <template v-if="byGroup('anterior').length">
        <p class="text-xs font-semibold text-slate-400 px-2 pt-3 pb-1.5 uppercase tracking-wider">Anteriores</p>
        <button
          v-for="conv in byGroup('anterior')"
          :key="conv.id"
          @click="selectChat(conv.id)"
          :class="[
            'w-full flex flex-col items-start px-3 py-2.5 rounded-xl text-left transition-colors mb-0.5',
            activeId === conv.id
              ? 'bg-indigo-50 text-indigo-700'
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-800',
          ]"
        >
          <span class="text-sm font-medium truncate w-full">{{ conv.title }}</span>
          <span class="text-xs text-slate-400 mt-0.5">{{ conv.time }}</span>
        </button>
      </template>
    </div>

    <!-- Ícones de conversas (collapsed) -->
    <div v-else class="flex-1 flex flex-col items-center py-1 gap-0.5 overflow-hidden">
      <button
        v-for="conv in conversations.slice(0, 6)"
        :key="conv.id"
        @click="selectChat(conv.id)"
        :class="[
          'w-9 h-9 rounded-xl flex items-center justify-center transition-colors shrink-0',
          activeId === conv.id
            ? 'bg-indigo-50 text-indigo-600'
            : 'text-slate-400 hover:bg-slate-100 hover:text-slate-600',
        ]"
        :title="conv.title"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
      </button>
    </div>
  </aside>
</template>
