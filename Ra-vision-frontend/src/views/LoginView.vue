<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

async function handleLogin() {
  if (!username.value || !password.value) return
  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await axios.post('http://localhost:8080/api/auth/login', {
      username: username.value,
      password: password.value,
    })

    const { token, username: user, role } = response.data

    localStorage.setItem('token', token)
    localStorage.setItem('username', user)
    localStorage.setItem('role', role)

    router.push('/chat')
  } catch (error: any) {
    if (error.response?.status === 401) {
      errorMessage.value = 'Usuário ou senha inválidos.'
    } else if (error.response?.data?.error) {
      errorMessage.value = error.response.data.error
    } else {
      errorMessage.value = 'Erro ao conectar com o servidor. Verifique se o backend está rodando.'
    }
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex bg-linear-to-br from-slate-50 via-indigo-50 to-purple-50">
    <!-- Painel decorativo esquerdo -->
    <div
      class="hidden lg:flex lg:w-1/2 xl:w-2/5 flex-col justify-between bg-linear-to-br from-indigo-600 to-purple-700 p-12 relative overflow-hidden"
    >
      <!-- Círculos decorativos -->
      <div
        class="absolute -top-24 -left-24 w-96 h-96 bg-white/10 rounded-full blur-3xl"
      ></div>
      <div
        class="absolute bottom-0 right-0 w-80 h-80 bg-purple-500/20 rounded-full blur-2xl"
      ></div>

      <div class="relative z-10">
        <div class="flex items-center gap-3 mb-2">
          <div class="w-9 h-9 bg-white/20 rounded-xl flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-white font-bold text-xl tracking-tight">Ra Vision</span>
        </div>
      </div>

      <div class="relative z-10">
        <h2 class="text-4xl font-bold text-white leading-tight mb-4">
          Decisões mais<br />inteligentes com IA
        </h2>
        <p class="text-indigo-200 text-lg leading-relaxed">
          Organize suas regras de negócio e entenda o raciocínio por trás de cada decisão.
        </p>
      </div>

      <div class="relative z-10 flex gap-4">
        <div class="bg-white/10 backdrop-blur rounded-2xl p-4 flex-1">
          <div class="text-2xl font-bold text-white mb-1">100%</div>
          <div class="text-indigo-200 text-sm">Decisões explicadas</div>
        </div>
        <div class="bg-white/10 backdrop-blur rounded-2xl p-4 flex-1">
          <div class="text-2xl font-bold text-white mb-1">24/7</div>
          <div class="text-indigo-200 text-sm">Sempre disponível</div>
        </div>
      </div>
    </div>

    <!-- Formulário de login -->
    <div class="flex-1 flex items-center justify-center p-6 sm:p-10">
      <div class="w-full max-w-md">
        <!-- Logo mobile -->
        <div class="flex items-center gap-2 mb-8 lg:hidden">
          <div class="w-8 h-8 bg-indigo-600 rounded-xl flex items-center justify-center">
            <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
          </div>
          <span class="text-indigo-600 font-bold text-lg">Ra Vision</span>
        </div>

        <div class="mb-8">
          <h1 class="text-3xl font-bold text-slate-800 mb-2">Bem-vindo de volta</h1>
          <p class="text-slate-500">Entre com suas credenciais para continuar</p>
        </div>

        <!-- Erro -->
        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 -translate-y-2"
          enter-to-class="opacity-100 translate-y-0"
          leave-active-class="transition duration-200 ease-in"
          leave-from-class="opacity-100"
          leave-to-class="opacity-0"
        >
          <div
            v-if="errorMessage"
            class="flex items-center gap-3 p-4 mb-5 bg-red-50 text-red-700 rounded-xl text-sm font-medium"
          >
            <svg class="w-5 h-5 text-red-400 shrink-0" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.28 7.22a.75.75 0 00-1.06 1.06L8.94 10l-1.72 1.72a.75.75 0 101.06 1.06L10 11.06l1.72 1.72a.75.75 0 101.06-1.06L11.06 10l1.72-1.72a.75.75 0 00-1.06-1.06L10 8.94 8.28 7.22z" clip-rule="evenodd" />
            </svg>
            {{ errorMessage }}
          </div>
        </Transition>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Usuário -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Usuário</label>
            <div class="relative">
              <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
                <svg class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </span>
              <input
                v-model="username"
                type="text"
                id="login-username"
                placeholder="seu.usuario"
                required
                class="w-full pl-10 pr-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all text-sm"
              />
            </div>
          </div>

          <!-- Senha -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Senha</label>
            <div class="relative">
              <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
                <svg class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                </svg>
              </span>
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                id="login-password"
                placeholder="••••••••"
                required
                class="w-full pl-10 pr-11 py-3 bg-white border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all text-sm"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors"
              >
                <svg v-if="!showPassword" class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
          </div>

          <!-- Botão entrar -->
          <button
            type="submit"
            id="login-submit"
            :disabled="isLoading"
            class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 text-white font-semibold rounded-xl transition-all duration-200 flex items-center justify-center gap-2 shadow-lg shadow-indigo-200 hover:shadow-indigo-300 hover:-translate-y-0.5 active:translate-y-0"
          >
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            {{ isLoading ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
