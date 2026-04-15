<script setup lang="ts">
import { reactive, computed, watch } from 'vue'
import type { BusinessRule } from '@/types/businessRule'

const props = defineProps<{
  open: boolean
  isEditing: boolean
  initialData: Omit<BusinessRule, 'id'>
}>()

const emit = defineEmits<{
  close: []
  save: [data: Omit<BusinessRule, 'id'>]
}>()

const form = reactive<Omit<BusinessRule, 'id'>>({
  name: '',
  description: '',
  validFrom: '',
  validTo: '',
})

watch(
  () => props.open,
  (val) => {
    if (val) Object.assign(form, { ...props.initialData })
  },
)

const formValid = computed(
  () => form.name.trim() && form.description.trim() && form.validFrom && form.validTo,
)
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 bg-black/30 z-50 flex items-center justify-center p-4"
        @click.self="emit('close')"
      >
        <Transition
          enter-active-class="transition duration-200 ease-out"
          enter-from-class="opacity-0 scale-95"
          enter-to-class="opacity-100 scale-100"
          leave-active-class="transition duration-150 ease-in"
          leave-from-class="opacity-100 scale-100"
          leave-to-class="opacity-0 scale-95"
        >
          <div v-if="open" class="w-full max-w-lg bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden">
            <!-- Header -->
            <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100">
              <div class="w-9 h-9 bg-indigo-50 rounded-xl flex items-center justify-center shrink-0">
                <svg class="w-4.5 h-4.5 text-indigo-600" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <div class="flex-1">
                <p class="text-sm font-bold text-slate-800">{{ isEditing ? 'Editar Regra' : 'Nova Regra de Negócio' }}</p>
                <p class="text-xs text-slate-400">{{ isEditing ? 'Altere os campos desejados' : 'Preencha os dados da nova regra' }}</p>
              </div>
              <button
                @click="emit('close')"
                class="p-1.5 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <!-- Body -->
            <form @submit.prevent="emit('save', { ...form })" class="px-6 py-5 space-y-4">
              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Nome da Regra</label>
                <input
                  v-model="form.name"
                  type="text"
                  placeholder="Ex: Bônus Black Friday"
                  required
                  class="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all"
                />
              </div>

              <div>
                <label class="block text-xs font-semibold text-slate-700 mb-1.5">Descrição</label>
                <textarea
                  v-model="form.description"
                  placeholder="Descreva como esta regra funciona e quais condições ela aplica…"
                  rows="3"
                  required
                  class="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 placeholder-slate-400 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all resize-none"
                ></textarea>
              </div>

              <div class="grid grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5">Válido De</label>
                  <input
                    v-model="form.validFrom"
                    type="date"
                    required
                    class="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all"
                  />
                </div>
                <div>
                  <label class="block text-xs font-semibold text-slate-700 mb-1.5">Válido Até</label>
                  <input
                    v-model="form.validTo"
                    type="date"
                    required
                    :min="form.validFrom"
                    class="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-800 focus:outline-none focus:border-indigo-400 focus:ring-3 focus:ring-indigo-100 transition-all"
                  />
                </div>
              </div>

              <div class="flex gap-3 pt-1">
                <button
                  type="button"
                  @click="emit('close')"
                  class="flex-1 py-2.5 border border-slate-200 text-slate-600 hover:bg-slate-50 text-sm font-semibold rounded-xl transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  :disabled="!formValid"
                  class="flex-1 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:bg-slate-300 disabled:cursor-not-allowed text-white text-sm font-semibold rounded-xl transition-all shadow-md hover:shadow-lg active:shadow-sm"
                >
                  {{ isEditing ? 'Salvar Alterações' : 'Criar Regra' }}
                </button>
              </div>
            </form>
          </div>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
