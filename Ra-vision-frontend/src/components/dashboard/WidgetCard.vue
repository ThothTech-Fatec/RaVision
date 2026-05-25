<script setup lang="ts">
defineProps<{
  title?: string
  showTitle?: boolean
  deletable?: boolean
}>()

const emit = defineEmits<{
  delete: []
}>()
</script>

<template>
  <div class="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col overflow-hidden">
    <!-- Card header: only rendered when there is a title or a delete button -->
    <div
      v-if="(showTitle !== false && title) || deletable"
      class="flex items-center justify-between px-4 pt-3 pb-1 shrink-0"
    >
      <span v-if="showTitle !== false && title" class="text-xs font-semibold text-slate-700 truncate pr-2">
        {{ title }}
      </span>
      <span v-else class="flex-1" />

      <button
        v-if="deletable"
        @click="emit('delete')"
        class="p-1.5 text-slate-300 hover:text-red-400 hover:bg-red-50 rounded-lg transition-colors shrink-0"
        title="Remover widget"
      >
        <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
    </div>

    <!-- Slot content -->
    <div class="flex-1 min-h-0 p-3">
      <slot />
    </div>
  </div>
</template>
