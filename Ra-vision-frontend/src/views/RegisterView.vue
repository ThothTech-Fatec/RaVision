<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const name = ref('')
const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const acceptTerms = ref(false)
const isLoading = ref(false)

const passwordStrength = computed(() => {
  const p = password.value
  if (!p) return 0
  let score = 0
  if (p.length >= 8) score++
  if (/[A-Z]/.test(p)) score++
  if (/[0-9]/.test(p)) score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  const labels = ['', 'Fraca', 'Razoável', 'Boa', 'Forte']
  return labels[passwordStrength.value]
})

const strengthColor = computed(() => {
  const colors = ['', 'bg-red-400', 'bg-amber-400', 'bg-blue-400', 'bg-emerald-400']
  return colors[passwordStrength.value]
})

const passwordsMatch = computed(() => {
  return !confirmPassword.value || password.value === confirmPassword.value
})

async function handleRegister() {
  if (!name.value || !email.value || !password.value || !acceptTerms.value) return
  if (!passwordsMatch.value) return
  isLoading.value = true
  // TODO: conectar com backend
  await new Promise((resolve) => setTimeout(resolve, 1000))
  isLoading.value = false
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen flex bg-linear-to-br from-slate-50 via-indigo-50 to-purple-50">
    <!-- Painel decorativo esquerdo -->
    <div
      class="hidden lg:flex lg:w-1/2 xl:w-2/5 flex-col justify-center bg-linear-to-br from-purple-600 to-indigo-700 p-12 relative overflow-hidden"
    >
      <div class="absolute -top-20 -right-20 w-80 h-80 bg-white/10 rounded-full blur-3xl"></div>
      <div class="absolute bottom-10 -left-16 w-72 h-72 bg-indigo-500/20 rounded-full blur-2xl"></div>

      <div class="absolute top-12 left-12 z-10 flex items-center gap-3">
        <div class="w-9 h-9 bg-white/20 rounded-xl flex items-center justify-center">
          <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <span class="text-white font-bold text-xl tracking-tight">Ra Vision</span>
      </div>

      <div class="relative z-10">
        <h2 class="text-4xl font-bold text-white leading-tight mb-4">
          Regras de negócio<br />que a IA entende
        </h2>
        <p class="text-purple-200 text-lg leading-relaxed">
          Centralize o conhecimento da sua empresa e deixe a IA apoiar cada decisão com clareza.
        </p>
        <div class="mt-8 space-y-3">
          <div v-for="item in ['Regras organizadas e acessíveis', 'Explicações para cada decisão', 'Apoio à gestão e operações']" :key="item" class="flex items-center gap-3">
            <div class="w-5 h-5 bg-white/20 rounded-full flex items-center justify-center shrink-0">
              <svg class="w-3 h-3 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="3">
                <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <span class="text-purple-100 text-sm">{{ item }}</span>
          </div>
        </div>
      </div>

    </div>

    <!-- Formulário de cadastro -->
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

        <div class="mb-7">
          <h1 class="text-3xl font-bold text-slate-800 mb-2">Criar conta</h1>
          <p class="text-slate-500">Preencha os dados abaixo para começar</p>
        </div>

        <form @submit.prevent="handleRegister" class="space-y-4">
          <!-- Nome -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Nome completo</label>
            <div class="relative">
              <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
                <svg class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </span>
              <input
                v-model="name"
                type="text"
                placeholder="Seu nome"
                required
                class="w-full pl-10 pr-4 py-3 bg-white border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all text-sm"
              />
            </div>
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">E-mail</label>
            <div class="relative">
              <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
                <svg class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
              </span>
              <input
                v-model="email"
                type="email"
                placeholder="seu@email.com"
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
                placeholder="Mínimo 8 caracteres"
                required
                class="w-full pl-10 pr-11 py-3 bg-white border border-slate-200 rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all text-sm"
              />
              <button type="button" @click="showPassword = !showPassword" class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors">
                <svg v-if="!showPassword" class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <!-- Força da senha -->
            <div v-if="password" class="mt-2">
              <div class="flex gap-1 mb-1">
                <div v-for="i in 4" :key="i" class="h-1 flex-1 rounded-full transition-all duration-300" :class="i <= passwordStrength ? strengthColor : 'bg-slate-200'"></div>
              </div>
              <p class="text-xs" :class="passwordStrength <= 1 ? 'text-red-400' : passwordStrength === 2 ? 'text-amber-500' : passwordStrength === 3 ? 'text-blue-500' : 'text-emerald-500'">
                Força da senha: {{ strengthLabel }}
              </p>
            </div>
          </div>

          <!-- Confirmar senha -->
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1.5">Confirmar senha</label>
            <div class="relative">
              <span class="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
                <svg class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
              </span>
              <input
                v-model="confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                placeholder="Repita a senha"
                required
                :class="[
                  'w-full pl-10 pr-11 py-3 bg-white border rounded-xl text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-3 transition-all text-sm',
                  !passwordsMatch ? 'border-red-300 focus:border-red-400 focus:ring-red-100' : 'border-slate-200 focus:border-indigo-400 focus:ring-indigo-100'
                ]"
              />
              <button type="button" @click="showConfirmPassword = !showConfirmPassword" class="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 transition-colors">
                <svg v-if="!showConfirmPassword" class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
                <svg v-else class="w-4.5 h-4.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                </svg>
              </button>
            </div>
            <p v-if="!passwordsMatch" class="mt-1 text-xs text-red-400">As senhas não coincidem</p>
          </div>

          <!-- Termos -->
          <label class="flex items-start gap-2.5 cursor-pointer">
            <input v-model="acceptTerms" type="checkbox" class="w-4 h-4 mt-0.5 rounded border-slate-300 text-indigo-600 focus:ring-indigo-400 shrink-0" />
            <span class="text-sm text-slate-600 leading-relaxed">
              Concordo com os
              <a href="#" class="text-indigo-600 hover:underline font-medium">Termos de Uso</a>
              e a
              <a href="#" class="text-indigo-600 hover:underline font-medium">Política de Privacidade</a>
            </span>
          </label>

          <!-- Botão cadastrar -->
          <button
            type="submit"
            :disabled="isLoading || !acceptTerms || !passwordsMatch"
            class="w-full py-3 bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-400 disabled:cursor-not-allowed text-white font-semibold rounded-xl transition-all duration-200 flex items-center justify-center gap-2 shadow-lg shadow-indigo-200 hover:shadow-indigo-300 hover:-translate-y-0.5 active:translate-y-0"
          >
            <svg v-if="isLoading" class="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
            </svg>
            {{ isLoading ? 'Criando conta...' : 'Criar conta' }}
          </button>
        </form>

        <p class="text-center text-sm text-slate-500 mt-6">
          Já tem uma conta?
          <router-link to="/" class="text-indigo-600 hover:text-indigo-700 font-semibold transition-colors">
            Entrar
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
