<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import WidgetCard from '@/components/dashboard/WidgetCard.vue'
import BarChartWidget from '@/components/dashboard/BarChartWidget.vue'
import ClassTableWidget from '@/components/dashboard/ClassTableWidget.vue'
import DataTableWidget from '@/components/dashboard/DataTableWidget.vue'
import WidgetConfigModal from '@/components/dashboard/WidgetConfigModal.vue'
import WidgetPlaceholder from '@/components/dashboard/WidgetPlaceholder.vue'
import NewTabModal from '@/components/dashboard/NewTabModal.vue'
import AppNav from '@/components/AppNav.vue'
import {
  skusCnlChartData,
  classificationTables,
  assertividadeTableColumns,
  assertividadeTableRows,
} from '@/data/boardMockData'
import type { WidgetConfig } from '@/data/boardMockData'

const router = useRouter()

// --- Tab structure ---
interface UserWidget {
  id: string
  config: WidgetConfig
}

interface BoardTab {
  id: string
  label: string
  isDefault?: boolean
  widgets: UserWidget[]
}

const tabs = reactive<BoardTab[]>([
  { id: 'cnl', label: 'CNL', isDefault: true, widgets: [] },
  { id: 'estoque', label: 'Estoque', widgets: [] },
  { id: 'planejamento', label: 'Planejamento Produção', widgets: [] },
  { id: 'faturamento', label: 'Faturamento', widgets: [] },
])

const activeTabId = ref('cnl')

const activeTab = computed(() => tabs.find(t => t.id === activeTabId.value) ?? tabs[0])

// --- New tab modal ---
const newTabModalOpen = ref(false)

function onCreateTab(label: string) {
  const id = `tab_${Date.now()}`
  tabs.push({ id, label, widgets: [] })
  activeTabId.value = id
}

// --- Widget config modal (creation only) ---
const configModalOpen = ref(false)
const processingWidget = ref(false)

const blankConfig = (): WidgetConfig => ({
  id: '',
  title: 'Novo widget',
  showTitle: true,
  prompt: '',
  type: 'ai',
  showAnalysis: false,
})

const newWidgetConfig = ref<WidgetConfig>(blankConfig())

function openAddWidget() {
  newWidgetConfig.value = blankConfig()
  configModalOpen.value = true
}

function onSaveWidget(config: WidgetConfig) {
  const id = `widget_${Date.now()}`
  activeTab.value.widgets.push({ id, config: { ...config, id } })
}

function deleteWidget(widgetId: string) {
  const tab = activeTab.value
  tab.widgets = tab.widgets.filter(w => w.id !== widgetId)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col font-sans">

    <!-- Top bar -->
    <header class="flex items-center gap-3 px-4 py-3 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <button class="p-2 text-slate-500 hover:text-slate-700 hover:bg-slate-100 rounded-xl transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
        </svg>
      </button>

      <h1 class="text-sm font-bold text-slate-800 flex-1">Board CSN</h1>

      <AppNav />

      <button class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors ml-1">
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
          <circle cx="5" cy="12" r="1.5" />
          <circle cx="12" cy="12" r="1.5" />
          <circle cx="19" cy="12" r="1.5" />
        </svg>
      </button>

      <button
        @click="logout"
        class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
        title="Sair"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round"
            d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Tab bar -->
    <div class="flex items-center gap-1 px-4 py-2.5 bg-white border-b border-slate-200 shrink-0">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        @click="activeTabId = tab.id"
        :class="[
          'px-4 py-1.5 text-xs font-semibold rounded-full transition-all',
          activeTabId === tab.id
            ? 'bg-indigo-600 text-white shadow-sm shadow-indigo-200'
            : 'text-slate-600 hover:bg-slate-100 border border-slate-200',
        ]"
      >
        {{ tab.label }}
      </button>

      <!-- Add tab -->
      <button
        @click="newTabModalOpen = true"
        class="w-7 h-7 flex items-center justify-center text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-full transition-colors border border-slate-200 ml-1"
        title="Nova aba"
      >
        <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
      </button>
    </div>

    <!-- Main content -->
    <main class="flex-1 p-4 overflow-y-auto flex flex-col gap-4">

      <!-- Content toolbar: add widget button -->
      <div class="flex items-center justify-end">
        <button
          @click="openAddWidget"
          class="flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg shadow-sm shadow-indigo-200 transition-all hover:-translate-y-0.5 active:translate-y-0"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          Adicionar widget
        </button>
      </div>

      <!-- CNL default tab: pre-built demo widgets -->
      <template v-if="activeTab.isDefault">

        <!-- Top grid: bar chart + 2×2 classification tables -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div class="lg:col-span-1">
            <WidgetCard title="Widget" class="h-full">
              <BarChartWidget
                chartTitle="SKUs por CNL (Empilhado) - por mês"
                :data="skusCnlChartData"
              />
            </WidgetCard>
          </div>

          <div class="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 gap-4">
            <WidgetCard
              v-for="table in classificationTables"
              :key="table.id"
              :title="table.title"
            >
              <ClassTableWidget :rows="table.rows" />
            </WidgetCard>
          </div>
        </div>

        <!-- Data table widget -->
        <WidgetCard title="assertividade_pa_m1">
          <DataTableWidget
            :columns="assertividadeTableColumns"
            :rows="assertividadeTableRows"
          />
        </WidgetCard>

      </template>

      <!-- User-created placeholder widgets -->
      <div
        v-if="activeTab.widgets.length > 0"
        class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4"
      >
        <WidgetCard
          v-for="widget in activeTab.widgets"
          :key="widget.id"
          :title="widget.config.showTitle ? widget.config.title : undefined"
          :showTitle="widget.config.showTitle"
          :deletable="true"
          @delete="deleteWidget(widget.id)"
        >
          <WidgetPlaceholder :config="widget.config" />
        </WidgetCard>
      </div>

      <!-- Empty state for non-default tabs with no widgets -->
      <div
        v-if="!activeTab.isDefault && activeTab.widgets.length === 0"
        class="flex-1 flex flex-col items-center justify-center py-20 gap-4 text-center"
      >
        <div class="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center">
          <svg class="w-7 h-7 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round"
              d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z" />
          </svg>
        </div>
        <div>
          <p class="text-sm font-semibold text-slate-600">Nenhum widget ainda</p>
          <p class="text-xs text-slate-400 mt-1">Clique em "Adicionar widget" para criar o primeiro widget desta aba.</p>
        </div>
        <button
          @click="openAddWidget"
          class="flex items-center gap-1.5 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold rounded-lg shadow-sm shadow-indigo-200 transition-all hover:-translate-y-0.5"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
          </svg>
          Adicionar widget
        </button>
      </div>

    </main>

    <!-- Widget creation modal -->
    <WidgetConfigModal
      v-model="configModalOpen"
      :config="newWidgetConfig"
      :isProcessing="processingWidget"
      @save="onSaveWidget"
    />

    <!-- New tab modal -->
    <NewTabModal
      v-model="newTabModalOpen"
      @create="onCreateTab"
    />

  </div>
</template>
