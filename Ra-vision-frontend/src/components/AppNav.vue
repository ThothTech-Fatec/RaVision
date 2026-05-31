<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const isAdmin = localStorage.getItem('role') === 'ADMINISTRADOR'

const isCTO = localStorage.getItem('role') === 'CTO'

interface NavItem {
  label: string
  path: string
  title: string
  adminOnly?: boolean
  monitoramentoAuth?: boolean
}

const navItems: NavItem[] = [
  { label: 'Chat',      path: '/chat',      title: 'Assistente IA'          },
  { label: 'Importar',  path: '/importar',  title: 'Importação de dados'    },
  { label: 'Lojas',     path: '/lojas',     title: 'Lojas e Equipes'        },
  { label: 'Regras',    path: '/regras',    title: 'Regras de negócio'      },
  { label: 'Anomalias', path: '/anomalias', title: 'Auditoria de Anomalias' },
  { label: 'Histórico', path: '/historico', title: 'Histórico de execuções' },
  { label: 'Board',     path: '/board',     title: 'Dashboards'             },
  { label: 'Perfil',    path: '/perfil',    title: 'Meu perfil'             },
  { label: 'Cadastro',  path: '/cadastro',  title: 'Cadastrar usuário', adminOnly: true },
]

const visibleItems = computed(() =>
  navItems.filter(item => {
    if (item.adminOnly) return isAdmin
    if (item.monitoramentoAuth) return isAdmin || isCTO
    return true
  })
)

function isActive(path: string) {
  return route.path === path
}
</script>

<template>
  <div class="flex items-center gap-1">
    <button
      v-for="item in visibleItems"
      :key="item.path"
      @click="router.push(item.path)"
      :title="item.title"
      :class="[
        'flex items-center gap-1.5 px-2.5 py-1.5 text-xs font-semibold rounded-lg transition-colors border shrink-0',
        isActive(item.path)
          ? 'bg-indigo-50 text-indigo-600 border-indigo-200'
          : 'bg-slate-50 text-slate-500 border-slate-200 hover:bg-slate-100 hover:text-slate-700',
      ]"
    >
      <!-- Chat -->
      <svg v-if="item.path === '/chat'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
      </svg>
      <!-- Importar -->
      <svg v-else-if="item.path === '/importar'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5" />
      </svg>
      <!-- Lojas -->
      <svg v-else-if="item.path === '/lojas'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
      </svg>
      <!-- Regras -->
      <svg v-else-if="item.path === '/regras'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
      <!-- Anomalias -->
      <svg v-else-if="item.path === '/anomalias'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
      </svg>
      <!-- Histórico -->
      <svg v-else-if="item.path === '/historico'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <!-- Board -->
      <svg v-else-if="item.path === '/board'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
      </svg>
      <!-- Perfil -->
      <svg v-else-if="item.path === '/perfil'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
      </svg>
      <!-- Cadastro -->
      <svg v-else-if="item.path === '/cadastro'" class="w-3.5 h-3.5 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
      </svg>

      <span class="hidden sm:inline">{{ item.label }}</span>
    </button>
  </div>
</template>
