<script setup lang="ts">
// TODO: receber lista de atividades via prop quando o backend estiver pronto
// Exemplo de estrutura futura:
// interface Activity {
//   id: number
//   description: string
//   date: string
//   type: 'create' | 'edit' | 'delete'
// }
// defineProps<{ activities: Activity[] }>()

// Por enquanto, a lista está sempre vazia
const activities: never[] = []
</script>

<template>
  <div class="bg-white rounded-2xl border border-slate-200 shadow-xs overflow-hidden">
    <!-- Cabeçalho da seção -->
    <div class="flex items-center gap-3 px-6 py-4 border-b border-slate-100">
      <div class="w-8 h-8 bg-slate-100 rounded-xl flex items-center justify-center shrink-0">
        <svg class="w-4 h-4 text-slate-500" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <div>
        <p class="text-sm font-bold text-slate-800">Histórico de Alterações</p>
        <p class="text-xs text-slate-400">Ações realizadas por você no sistema</p>
      </div>
    </div>

    <!-- Lista de atividades (futura) -->
    <div v-if="activities.length > 0" class="divide-y divide-slate-100">
      <!-- TODO: renderizar activities quando o backend for implementado -->
    </div>

    <!-- Estado vazio -->
    <div v-else class="flex flex-col items-center justify-center py-14 text-center px-4">
      <div class="w-12 h-12 bg-slate-100 rounded-2xl flex items-center justify-center mb-3">
        <svg class="w-6 h-6 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      </div>
      <p class="text-slate-600 font-medium text-sm">Nenhuma alteração registrada</p>
      <p class="text-slate-400 text-xs mt-1 max-w-xs leading-relaxed">
        Suas ações no sistema aparecerão aqui assim que o histórico for ativado.
      </p>
    </div>
  </div>
</template>
