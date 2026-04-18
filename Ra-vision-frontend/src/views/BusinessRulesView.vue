<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import type { BusinessRule } from '@/types/businessRule'
import RulesTable from '@/components/RulesTable.vue'
import RuleModal from '@/components/RuleModal.vue'
import DeleteConfirmModal from '@/components/DeleteConfirmModal.vue'

const router = useRouter()

// ─── Mock data (substitua por chamadas à API quando o backend estiver pronto) ──

const rules = ref<BusinessRule[]>([
  {
    id: 1,
    name: 'Bônus Black Friday',
    description: 'Acréscimo de 15% na comissão de vendas realizadas durante o período da Black Friday.',
    validFrom: '2025-11-24',
    validTo: '2025-11-30',
  },
  {
    id: 2,
    name: 'Meta Trimestral Q1',
    description: 'Vendedores que atingirem 110% da meta trimestral recebem adicional de R$ 500,00.',
    validFrom: '2025-01-01',
    validTo: '2025-03-31',
  },
  {
    id: 3,
    name: 'Regra de Crédito Especial',
    description: 'Aprovação automática para clientes com score acima de 800 e histórico limpo nos últimos 12 meses.',
    validFrom: '2025-06-01',
    validTo: '2025-12-31',
  },
  {
    id: 4,
    name: 'Desconto Fidelidade',
    description: 'Clientes com mais de 3 anos de relacionamento recebem 5% de desconto em todas as operações.',
    validFrom: '2025-01-01',
    validTo: '2025-12-31',
  },
])

let nextId = 5

// ─── Modal criar / editar ─────────────────────────────────────────────────────

const modalOpen = ref(false)
const isEditing = ref(false)
const editingId = ref<number | null>(null)

const emptyForm = (): Omit<BusinessRule, 'id'> => ({
  name: '',
  description: '',
  validFrom: '',
  validTo: '',
})

const modalInitialData = ref(emptyForm())

function openCreate() {
  isEditing.value = false
  editingId.value = null
  modalInitialData.value = emptyForm()
  modalOpen.value = true
}

function openEdit(rule: BusinessRule) {
  isEditing.value = true
  editingId.value = rule.id
  modalInitialData.value = { name: rule.name, description: rule.description, validFrom: rule.validFrom, validTo: rule.validTo }
  modalOpen.value = true
}

function saveRule(data: Omit<BusinessRule, 'id'>) {
  if (isEditing.value && editingId.value !== null) {
    // TODO: PUT /api/rules/:id
    const idx = rules.value.findIndex((r) => r.id === editingId.value)
    if (idx !== -1) rules.value[idx] = { id: editingId.value, ...data }
  } else {
    // TODO: POST /api/rules
    rules.value.push({ id: nextId++, ...data })
  }
  modalOpen.value = false
}

// ─── Confirmação de exclusão ──────────────────────────────────────────────────

const deleteConfirmId = ref<number | null>(null)

function deleteRule() {
  if (deleteConfirmId.value === null) return
  // TODO: DELETE /api/rules/:id
  rules.value = rules.value.filter((r) => r.id !== deleteConfirmId.value)
  deleteConfirmId.value = null
}
</script>

<template>
  <div class="h-screen flex flex-col bg-slate-50 overflow-hidden">

    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight">Regras de Negócio</h1>
        <p class="text-xs text-slate-400">Gerencie as regras que orientam os cálculos e decisões</p>
      </div>

      <!-- Navegação -->
      <div class="flex items-center gap-2">
        <button
          @click="router.push('/chat')"
          class="flex items-center gap-2 px-3 py-1.5 bg-indigo-50 hover:bg-indigo-100 text-indigo-600 text-xs font-semibold rounded-lg transition-colors border border-indigo-100 shrink-0"
          title="Ir para o Chat"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <span class="hidden sm:inline">Chat</span>
        </button>
        <button
          @click="router.push('/perfil')"
          class="flex items-center gap-2 px-3 py-1.5 bg-slate-50 hover:bg-slate-100 text-slate-600 text-xs font-semibold rounded-lg transition-colors border border-slate-200 shrink-0"
          title="Meu Perfil"
        >
          <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
          </svg>
          <span class="hidden sm:inline">Perfil</span>
        </button>
      </div>

      <!-- Sair -->
      <button
        @click="router.push('/')"
        class="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
        title="Sair"
      >
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Conteúdo -->
    <div class="flex-1 overflow-y-auto p-6">
      <div class="max-w-5xl mx-auto">

        <!-- Topo: contagem + botão nova regra -->
        <div class="flex items-center justify-between mb-6">
          <p class="text-xs text-slate-400">
            {{ rules.length }} regra{{ rules.length !== 1 ? 's' : '' }} cadastrada{{ rules.length !== 1 ? 's' : '' }}
          </p>
          <button
            @click="openCreate"
            class="flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-xl transition-all duration-150 shadow-md hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
            </svg>
            Nova Regra
          </button>
        </div>

        <RulesTable
          :rules="rules"
          @edit="openEdit"
          @delete="(id) => (deleteConfirmId = id)"
        />
      </div>
    </div>

  </div>

  <RuleModal
    :open="modalOpen"
    :is-editing="isEditing"
    :initial-data="modalInitialData"
    @close="modalOpen = false"
    @save="saveRule"
  />

  <DeleteConfirmModal
    :open="deleteConfirmId !== null"
    @confirm="deleteRule"
    @cancel="deleteConfirmId = null"
  />
</template>
