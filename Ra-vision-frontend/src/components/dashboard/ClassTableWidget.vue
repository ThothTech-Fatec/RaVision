<script setup lang="ts">
import type { ClassificationRow } from '@/data/boardMockData'

defineProps<{
  rows: ClassificationRow[]
}>()
</script>

<template>
  <div class="w-full overflow-hidden">
    <table class="w-full text-xs">
      <thead>
        <tr class="border-b border-slate-100">
          <th class="text-left py-2 pr-4 font-semibold text-slate-600">Classificação</th>
          <th class="text-right py-2 font-semibold text-slate-600">Total de produtos</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="row in rows"
          :key="row.classificacao"
          class="border-b border-slate-50 hover:bg-slate-50 transition-colors"
        >
          <td class="py-2 pr-4 text-slate-700">{{ row.classificacao }}</td>
          <td class="py-2 text-right">
            <span
              :class="row.isClickable ? 'text-indigo-600 font-medium cursor-pointer hover:underline' : 'text-slate-700'"
            >
              {{ row.total }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
