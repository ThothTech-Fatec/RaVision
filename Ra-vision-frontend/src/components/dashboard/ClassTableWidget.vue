<script setup lang="ts">
import type { ClassificationRow } from '@/types/dashboard'

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
          :key="row.classLabel"
          class="border-b border-slate-50 hover:bg-slate-50 transition-colors"
        >
          <td class="py-2 pr-4 text-slate-700">
            <span v-if="row.indicatorColor" class="inline-block w-2 h-2 rounded-full mr-2" :style="{ backgroundColor: row.indicatorColor }"></span>
            {{ row.classLabel }}
          </td>
          <td class="py-2 text-right">
            <span class="text-slate-700 font-medium">
              {{ row.value }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
