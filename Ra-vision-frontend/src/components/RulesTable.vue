<script setup lang="ts">
import type { BusinessRule } from '@/types/businessRule'

defineProps<{ rules: BusinessRule[] }>()

const emit = defineEmits<{
  edit: [rule: BusinessRule]
  delete: [id: number]
}>()

function formatDate(dateStr: string) {
  if (!dateStr) return '—'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

function ruleStatus(rule: BusinessRule): 'active' | 'upcoming' | 'expired' {
  const today = new Date().toISOString().slice(0, 10)
  if (rule.validTo < today) return 'expired'
  if (rule.validFrom > today) return 'upcoming'
  return 'active'
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">

    <!-- Estado vazio -->
    <div v-if="rules.length === 0" class="flex flex-col items-center justify-center py-20 text-center px-4">
      <div class="w-14 h-14 bg-slate-100 rounded-2xl flex items-center justify-center mb-4">
        <svg class="w-7 h-7 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>
      <p class="text-slate-600 font-medium text-sm">Nenhuma regra cadastrada</p>
      <p class="text-slate-400 text-xs mt-1">Clique em "Nova Regra" para começar.</p>
    </div>

    <!-- Tabela com dados -->
    <table v-else class="w-full text-sm">
      <thead>
        <tr class="border-b border-slate-100 bg-slate-50">
          <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider w-1/4">Nome</th>
          <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider">Descrição</th>
          <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider whitespace-nowrap">Válido De</th>
          <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider whitespace-nowrap">Válido Até</th>
          <th class="text-left px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider">Status</th>
          <th class="px-5 py-3 text-xs font-bold text-slate-500 uppercase tracking-wider text-right">Ações</th>
        </tr>
      </thead>
      <tbody class="divide-y divide-slate-100">
        <tr
          v-for="rule in rules"
          :key="rule.id"
          class="hover:bg-slate-50 transition-colors"
        >
          <td class="px-5 py-4">
            <span class="font-semibold text-slate-800">{{ rule.name }}</span>
          </td>
          <td class="px-5 py-4">
            <span class="text-slate-600 line-clamp-2 leading-relaxed">{{ rule.description }}</span>
          </td>
          <td class="px-5 py-4 whitespace-nowrap text-slate-600">{{ formatDate(rule.validFrom) }}</td>
          <td class="px-5 py-4 whitespace-nowrap text-slate-600">{{ formatDate(rule.validTo) }}</td>
          <td class="px-5 py-4">
            <span
              :class="[
                'inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold',
                ruleStatus(rule) === 'active'   ? 'bg-green-50 text-green-700' :
                ruleStatus(rule) === 'upcoming' ? 'bg-indigo-50 text-indigo-700' :
                                                  'bg-slate-100 text-slate-500',
              ]"
            >
              <span
                :class="[
                  'w-1.5 h-1.5 rounded-full',
                  ruleStatus(rule) === 'active'   ? 'bg-green-500' :
                  ruleStatus(rule) === 'upcoming' ? 'bg-indigo-400' :
                                                    'bg-slate-400',
                ]"
              ></span>
              {{ ruleStatus(rule) === 'active' ? 'Ativa' : ruleStatus(rule) === 'upcoming' ? 'Futura' : 'Expirada' }}
            </span>
          </td>
          <td class="px-5 py-4">
            <div class="flex items-center justify-end gap-1">
              <button
                @click="emit('edit', rule)"
                class="p-2 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition-colors"
                title="Editar regra"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
              </button>
              <button
                @click="emit('delete', rule.id)"
                class="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="Excluir regra"
              >
                <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
