<script setup lang="ts">
import type { WidgetConfig } from '@/data/boardMockData'

defineProps<{
  config: WidgetConfig
}>()

const typeLabel: Record<string, string> = {
  ai: 'Gerado por IA',
  manual: 'Manual',
}
</script>

<template>
  <div class="flex flex-col items-center justify-center gap-3 py-8 px-4 text-center min-h-[180px]">
    <!-- Icon -->
    <div class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center shrink-0">
      <svg class="w-5 h-5 text-indigo-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round"
          d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5M9 11.25v1.5M12 9v3.75m3-6v6" />
      </svg>
    </div>

    <!-- Label -->
    <div>
      <p class="text-xs font-semibold text-slate-600">Widget em desenvolvimento</p>
      <p class="text-xs text-slate-400 mt-1 max-w-[240px]">
        Será exibido quando o back-end estiver conectado.
      </p>
    </div>

    <!-- Prompt preview -->
    <div v-if="config.prompt" class="w-full max-w-xs bg-slate-50 border border-slate-100 rounded-lg px-3 py-2 text-left">
      <p class="text-[10px] text-slate-400 font-medium uppercase tracking-wide mb-1">Prompt</p>
      <p class="text-xs text-slate-600 line-clamp-2">{{ config.prompt }}</p>
    </div>

    <!-- Type badge -->
    <span class="text-[10px] font-medium text-indigo-600 bg-indigo-50 border border-indigo-100 px-2 py-0.5 rounded-full">
      {{ typeLabel[config.type] ?? config.type }}
    </span>
  </div>
</template>
