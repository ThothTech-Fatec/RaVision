<script setup lang="ts">
import { ref, watch } from 'vue'
import type { WidgetConfig } from '@/data/boardMockData'

const props = defineProps<{
  modelValue: boolean
  config: WidgetConfig
  isProcessing?: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  save: [config: WidgetConfig]
}>()

const localConfig = ref<WidgetConfig>({ ...props.config })

watch(
  () => props.config,
  (val) => { localConfig.value = { ...val } },
  { deep: true },
)

watch(
  () => props.modelValue,
  (open) => { if (open) localConfig.value = { ...props.config } },
)

function close() {
  emit('update:modelValue', false)
}

function save() {
  emit('save', { ...localConfig.value })
  close()
}

const widgetTypeOptions = [
  { value: 'ai', label: 'Gerado por IA' },
  { value: 'manual', label: 'Manual' },
]
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="modelValue"
        class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/30 backdrop-blur-sm"
        @click.self="close"
      >
        <Transition
          enter-active-class="transition-all duration-200 ease-out"
          enter-from-class="opacity-0 scale-95 translate-y-2"
          enter-to-class="opacity-100 scale-100 translate-y-0"
          leave-active-class="transition-all duration-150 ease-in"
          leave-from-class="opacity-100 scale-100 translate-y-0"
          leave-to-class="opacity-0 scale-95 translate-y-2"
          appear
        >
          <div
            v-if="modelValue"
            class="bg-white rounded-2xl shadow-2xl w-full max-w-md border border-slate-200 flex flex-col"
          >
            <!-- Modal header -->
            <div class="flex items-start justify-between px-6 pt-5 pb-4 border-b border-slate-100">
              <div>
                <h2 class="text-sm font-bold text-slate-800">Configurar widget</h2>
                <p class="text-xs text-slate-400 mt-0.5">
                  Defina os ajustes do widget e o prompt que sera executado.
                </p>
              </div>
              <button
                @click="close"
                class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors shrink-0"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <!-- Tab bar (only Geral) -->
            <div class="px-6 pt-3 pb-0 border-b border-slate-100">
              <button
                class="pb-2.5 text-xs font-semibold text-indigo-600 border-b-2 border-indigo-600 -mb-px"
              >
                Geral
              </button>
            </div>

            <!-- Form body -->
            <div class="px-6 py-5 flex flex-col gap-4 overflow-y-auto">

              <!-- Título do widget -->
              <div class="flex flex-col gap-1.5">
                <label class="text-xs font-semibold text-slate-700">Titulo do widget</label>
                <input
                  v-model="localConfig.title"
                  type="text"
                  class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-400 transition-all"
                />
              </div>

              <!-- Exibir título -->
              <label class="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  v-model="localConfig.showTitle"
                  type="checkbox"
                  class="w-4 h-4 rounded accent-indigo-600"
                />
                <span class="text-xs text-slate-700">Exibir titulo no widget</span>
              </label>

              <!-- Prompt -->
              <div class="flex flex-col gap-1.5">
                <div class="flex items-center justify-between">
                  <label class="text-xs font-semibold text-slate-700">Prompt</label>
                  <!-- Mic icon -->
                  <button class="p-1 text-slate-400 hover:text-indigo-500 transition-colors" title="Gravar prompt por voz">
                    <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                      <path stroke-linecap="round" stroke-linejoin="round"
                        d="M12 18.75a6 6 0 006-6v-1.5m-6 7.5a6 6 0 01-6-6v-1.5m6 7.5v3.75m-3.75 0h7.5M12 15.75a3 3 0 01-3-3V4.5a3 3 0 116 0v8.25a3 3 0 01-3 3z" />
                    </svg>
                  </button>
                </div>
                <textarea
                  v-model="localConfig.prompt"
                  rows="4"
                  class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-400 transition-all resize-none"
                  placeholder="Descreva o que esse widget deve exibir..."
                />
              </div>

              <!-- Tipo do widget -->
              <div class="flex flex-col gap-1.5">
                <label class="text-xs font-semibold text-slate-700">Tipo do widget</label>
                <select
                  v-model="localConfig.type"
                  class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-400 transition-all appearance-none cursor-pointer"
                >
                  <option v-for="opt in widgetTypeOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </option>
                </select>
              </div>

              <!-- Exibir análise em card -->
              <label class="flex items-center gap-2.5 cursor-pointer select-none">
                <input
                  v-model="localConfig.showAnalysis"
                  type="checkbox"
                  class="w-4 h-4 rounded accent-indigo-600"
                />
                <span class="text-xs text-slate-700">Exibir análise em card</span>
              </label>
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-end gap-2 px-6 py-4 border-t border-slate-100">
              <button
                @click="close"
                class="px-4 py-2 text-xs font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors"
              >
                Cancelar
              </button>
              <button
                @click="save"
                :disabled="isProcessing"
                class="flex items-center gap-1.5 px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-70 rounded-lg shadow-sm shadow-indigo-200 transition-all hover:-translate-y-0.5 active:translate-y-0"
              >
                <svg
                  v-if="isProcessing"
                  class="animate-spin w-3.5 h-3.5"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                {{ isProcessing ? 'Processando...' : 'Salvar' }}
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
