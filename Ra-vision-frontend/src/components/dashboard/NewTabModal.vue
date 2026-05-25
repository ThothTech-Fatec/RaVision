<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  create: [label: string]
}>()

const label = ref('')

watch(
  () => props.modelValue,
  (open) => { if (open) label.value = '' },
)

function close() {
  emit('update:modelValue', false)
}

function create() {
  const name = label.value.trim()
  if (!name) return
  emit('create', name)
  close()
}
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
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
          appear
        >
          <div
            v-if="modelValue"
            class="bg-white rounded-2xl shadow-2xl w-full max-w-sm border border-slate-200"
          >
            <!-- Header -->
            <div class="flex items-center justify-between px-6 pt-5 pb-4 border-b border-slate-100">
              <div>
                <h2 class="text-sm font-bold text-slate-800">Nova aba</h2>
                <p class="text-xs text-slate-400 mt-0.5">Defina o nome da nova aba do board.</p>
              </div>
              <button
                @click="close"
                class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="px-6 py-5">
              <label class="block text-xs font-semibold text-slate-700 mb-1.5">Nome da aba</label>
              <input
                v-model="label"
                type="text"
                placeholder="Ex: Logística, Compras..."
                class="w-full px-3 py-2 text-sm rounded-lg border border-slate-200 bg-slate-50 text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 focus:border-indigo-400 transition-all"
                @keydown.enter="create"
                autofocus
              />
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
                @click="create"
                :disabled="!label.trim()"
                class="px-4 py-2 text-xs font-semibold text-white bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg shadow-sm shadow-indigo-200 transition-all hover:-translate-y-0.5 active:translate-y-0"
              >
                Criar aba
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
