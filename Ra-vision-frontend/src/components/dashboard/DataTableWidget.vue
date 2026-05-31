<script setup lang="ts">
import type { DataTableColumn, DataTableRow } from '@/types/dashboard'

defineProps<{
  columns: DataTableColumn[]
  rows: DataTableRow[]
}>()

function cellColor(value: string | number): string {
  if (typeof value !== 'string') return 'text-slate-700'
  const raw = value.replace(',', '.').replace('%', '')
  const num = parseFloat(raw)
  if (isNaN(num) || num === 0) return 'text-slate-400'
  return num > 0 ? 'text-emerald-600' : 'text-red-500'
}
</script>

<template>
  <div class="w-full overflow-x-auto">
    <table class="min-w-max w-full text-xs border-separate border-spacing-0">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="[
              'py-2 px-3 font-semibold text-slate-600 bg-white border-b border-slate-200 whitespace-nowrap',
              col.key === 'sku'
                ? 'text-left sticky left-0 z-10 border-r border-slate-100'
                : 'text-right',
            ]"
          >
            {{ col.label }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="(row, i) in rows"
          :key="i"
          class="hover:bg-slate-50 transition-colors"
        >
          <td
            v-for="col in columns"
            :key="col.key"
            :class="[
              'py-2 px-3 border-b border-slate-100 whitespace-nowrap',
              col.key === 'sku'
                ? 'text-left font-medium text-slate-700 sticky left-0 bg-white z-10 border-r border-slate-100'
                : `text-right ${cellColor(row[col.key])}`,
            ]"
          >
            {{ row[col.key] }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
