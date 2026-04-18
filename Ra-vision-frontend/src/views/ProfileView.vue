<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ChangePasswordModal from '@/components/ChangePasswordModal.vue'
import UserActivityList from '@/components/UserActivityList.vue'

const router = useRouter()

// Dados do usuário autenticado via localStorage
const user = ref({
  name: localStorage.getItem('username') || 'Usuário',
  role: localStorage.getItem('role') || '',
})

const passwordModalOpen = ref(false)

const feedbackMessage = ref<{ type: 'success' | 'error'; text: string } | null>(null)

function handlePasswordSave(_currentPassword: string, _newPassword: string) {
  // TODO: PUT /api/users/me/password  { currentPassword, newPassword }
  passwordModalOpen.value = false
  feedbackMessage.value = { type: 'success', text: 'Senha alterada com sucesso!' }
  setTimeout(() => (feedbackMessage.value = null), 4000)
}

function logout() {
  localStorage.clear()
  router.push('/')
}

function getInitials(name: string) {
  return name
    .split(' ')
    .slice(0, 2)
    .map((n) => n[0])
    .join('')
    .toUpperCase()
}
</script>

<template>
  <div class="h-screen flex flex-col bg-slate-50 overflow-hidden">

    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight">Meu Perfil</h1>
        <p class="text-xs text-slate-400">Informações da sua conta</p>
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
          @click="router.push('/historico')"
          class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200 shrink-0"
          title="Histórico de Execuções"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <span class="hidden sm:inline">Histórico</span>
        </button>
      </div>

      <!-- Sair -->
      <button
        @click="logout"
        class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
        title="Sair"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Conteúdo -->
    <div class="flex-1 overflow-y-auto p-6">
      <div class="max-w-5xl mx-auto space-y-6">

        <!-- Feedback de ação (largura total, acima dos cards) -->
        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 -translate-y-2"
          enter-to-class="opacity-100 translate-y-0"
          leave-active-class="transition duration-200 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="opacity-0"
        >
          <div
            v-if="feedbackMessage"
            :class="[
              'flex items-center gap-3 p-4 rounded-xl text-sm font-medium',
              feedbackMessage.type === 'success' ? 'bg-green-50 text-green-800' : 'bg-red-50 text-red-800',
            ]"
          >
            <svg v-if="feedbackMessage.type === 'success'" class="w-5 h-5 text-green-500 shrink-0" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.857-9.809a.75.75 0 00-1.214-.882l-3.483 4.79-1.88-1.88a.75.75 0 10-1.06 1.061l2.5 2.5a.75.75 0 001.137-.089l4-5.5z" clip-rule="evenodd" />
            </svg>
            {{ feedbackMessage.text }}
          </div>
        </Transition>

        <!-- Cards lado a lado -->
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 items-start">

        <!-- Card de informações do usuário -->
        <div class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
          <!-- Cabeçalho do card -->
          <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100">
            <div class="w-8 h-8 bg-indigo-50 rounded-xl flex items-center justify-center shrink-0">
              <svg class="w-4 h-4 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
            </div>
            <div>
              <p class="text-sm font-bold text-slate-800">Informações da Conta</p>
              <p class="text-xs text-slate-400">Seus dados cadastrados no sistema</p>
            </div>
          </div>

          <div class="px-6 py-6 space-y-6">
            <!-- Avatar + nome -->
            <div class="flex items-center gap-4">
              <div class="w-16 h-16 bg-linear-to-br from-indigo-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-200 shrink-0">
                <span class="text-white font-bold text-xl tracking-tight">{{ getInitials(user.name) }}</span>
              </div>
              <div>
                <p class="text-base font-bold text-slate-800">{{ user.name }}</p>
                <p class="text-xs text-slate-400 mt-0.5">Membro do Ra Vision</p>
              </div>
            </div>

            <div class="h-px bg-slate-100"></div>

            <!-- Campos de informação -->
            <div class="space-y-4">
              <!-- Nome -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Nome</label>
                <div class="flex items-center gap-3 px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl">
                  <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  <span class="text-sm text-slate-700 font-medium">{{ user.name }}</span>
                </div>
              </div>

              <!-- Perfil (Role) -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Perfil</label>
                <div class="flex items-center gap-3 px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl">
                  <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                  </svg>
                  <span class="text-sm text-slate-700 font-medium">{{ user.role }}</span>
                </div>
              </div>

              <!-- Senha -->
              <div>
                <label class="block text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5">Senha</label>
                <div class="flex items-center gap-3">
                  <div class="flex-1 flex items-center gap-3 px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl">
                    <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    <span class="text-sm text-slate-400 tracking-widest">••••••••</span>
                  </div>
                  <button
                    @click="passwordModalOpen = true"
                    class="flex items-center gap-2 px-4 py-2.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 text-xs font-semibold rounded-xl transition-colors border border-indigo-100 shrink-0 whitespace-nowrap"
                  >
                    <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                    </svg>
                    Alterar Senha
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

          <!-- Lista de atividades -->
          <UserActivityList />

        </div><!-- fim grid -->
      </div>
    </div>
  </div>

  <ChangePasswordModal
    :open="passwordModalOpen"
    @close="passwordModalOpen = false"
    @save="handlePasswordSave"
  />
</template>
