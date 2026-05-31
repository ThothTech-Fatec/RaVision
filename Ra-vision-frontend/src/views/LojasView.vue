<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import AppNav from '@/components/AppNav.vue'

const router = useRouter()

interface Funcionario {
  id: number
  matricula: string
  codCargo: number
  descrCargo: string
  codLoja: number
  descrLoja: string
  codMarca: number
  descrMarca: string
  dataAdmissao: string
  dataDemissao: string | null
}

interface LojaAgrupada {
  codLoja: number
  descrLoja: string
  codMarca: number
  descrMarca: string
  funcionarios: Funcionario[]
  totalFuncionarios: number
  totalAtivos: number
  totalDesligados: number
}

const mesesDisponiveis = ref<string[]>([])
const selectedMes = ref<string>('')
const lojas = ref<LojaAgrupada[]>([])
const isLoading = ref(false)
const searchQuery = ref('')

const filteredLojas = computed(() => {
  if (!searchQuery.value) return lojas.value
  const query = searchQuery.value.toLowerCase()
  return lojas.value.filter(loja => 
    loja.descrLoja.toLowerCase().includes(query) || 
    loja.codLoja.toString().includes(query) ||
    loja.descrMarca.toLowerCase().includes(query)
  )
})

const getHeaders = () => {
  const token = localStorage.getItem('token')
  return { Authorization: `Bearer ${token}` }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  const [year, month, day] = dateStr.split('-')
  return `${day}/${month}/${year}`
}

const loadMeses = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/rh/meses-disponiveis', { headers: getHeaders() })
    mesesDisponiveis.value = res.data
    if (mesesDisponiveis.value.length > 0) {
      selectedMes.value = mesesDisponiveis.value[0] || ''
      await loadFuncionarios()
    }
  } catch (error) {
    console.error("Erro ao carregar meses", error)
  }
}

const loadFuncionarios = async () => {
  if (!selectedMes.value) return
  isLoading.value = true
  try {
    const res = await axios.get(`http://localhost:8080/api/rh/funcionarios?dateRef=${selectedMes.value}`, { headers: getHeaders() })
    const data: Funcionario[] = res.data

    // Agrupar por loja
    const map = new Map<number, LojaAgrupada>()
    
    data.forEach(func => {
      if (!map.has(func.codLoja)) {
        map.set(func.codLoja, {
          codLoja: func.codLoja,
          descrLoja: func.descrLoja,
          codMarca: func.codMarca,
          descrMarca: func.descrMarca,
          funcionarios: [],
          totalFuncionarios: 0,
          totalAtivos: 0,
          totalDesligados: 0
        })
      }
      const loja = map.get(func.codLoja)!
      loja.funcionarios.push(func)
      loja.totalFuncionarios++
      if (func.dataDemissao) {
        loja.totalDesligados++
      } else {
        loja.totalAtivos++
      }
    })

    lojas.value = Array.from(map.values()).sort((a, b) => a.descrLoja.localeCompare(b.descrLoja))
  } catch (error) {
    console.error("Erro ao carregar funcionários", error)
  } finally {
    isLoading.value = false
  }
}

const onMesChange = () => {
  loadFuncionarios()
}

onMounted(() => {
  loadMeses()
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  localStorage.removeItem('username')
  router.push('/')
}

const expandedLojas = ref<Set<number>>(new Set())

const toggleLoja = (codLoja: number) => {
  if (expandedLojas.value.has(codLoja)) {
    expandedLojas.value.delete(codLoja)
  } else {
    expandedLojas.value.add(codLoja)
  }
}

const filterAtivos = ref(false)

const getIniciais = (nomeOuMatricula: string) => {
  if (!nomeOuMatricula) return 'U'
  const parts = nomeOuMatricula.replace('MATRIC-', '').split('-')
  const p1 = parts[0] || ''
  const p2 = parts[1] || ''
  if (parts.length > 1 && p1 && p2) {
    return p1.charAt(0).toUpperCase() + p2.charAt(0).toUpperCase()
  }
  return p1.substring(0, 2).toUpperCase()
}

const calcularTempoCasa = (dataAdmissao: string, dataDemissao?: string | null) => {
  if (!dataAdmissao) return '-'
  const admissao = new Date(dataAdmissao)
  if (isNaN(admissao.getTime())) return '-'
  
  const fim = dataDemissao ? new Date(dataDemissao) : new Date()
  if (isNaN(fim.getTime())) return '-'
  
  let anos = fim.getFullYear() - admissao.getFullYear()
  let meses = fim.getMonth() - admissao.getMonth()
  
  if (meses < 0 || (meses === 0 && fim.getDate() < admissao.getDate())) {
    anos--
    meses += 12
  }
  
  if (anos > 0) {
    return `${anos} ano${anos > 1 ? 's' : ''}${meses > 0 ? ` e ${meses} mês${meses > 1 ? 'es' : ''}` : ''}`
  } else if (meses > 0) {
    return `${meses} mês${meses > 1 ? 'es' : ''}`
  } else {
    return 'Menos de 1 mês'
  }
}
</script>

<template>
  <div class="min-h-screen bg-slate-50 flex flex-col font-sans">
    <!-- Header -->
    <header class="flex items-center gap-2 px-4 py-3.5 bg-white border-b border-slate-200 shadow-sm shrink-0">
      <div class="flex-1 min-w-0">
        <h1 class="text-sm font-bold text-slate-800 leading-tight truncate">Lojas e Equipes</h1>
        <p class="text-xs text-slate-400 truncate">Visão geral de funcionários por loja</p>
      </div>

      <AppNav />

      <button @click="logout" class="p-2 ml-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-xl transition-colors">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
        </svg>
      </button>
    </header>

    <!-- Main Content -->
    <main class="flex-1 p-6 overflow-y-auto">
      <div class="max-w-5xl mx-auto space-y-6">
        
        <!-- Controls -->
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between bg-white p-4 rounded-xl shadow-sm border border-slate-200 gap-4">
          <div class="flex items-center gap-3">
            <span class="text-sm font-semibold text-slate-700">Mês de Referência:</span>
            <select 
              v-model="selectedMes" 
              @change="onMesChange"
              class="px-3 py-1.5 bg-slate-50 border border-slate-200 text-slate-700 text-sm rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-medium"
            >
              <option v-for="mes in mesesDisponiveis" :key="mes" :value="mes">
                {{ mes.split('-').reverse().join('/') }}
              </option>
            </select>
          </div>

          <!-- Search Bar -->
          <div class="w-full sm:w-auto flex-1 max-w-md flex items-center bg-slate-50 border border-slate-200 rounded-lg px-3 py-1.5 focus-within:ring-2 focus-within:ring-indigo-500/20 focus-within:border-indigo-500 transition-all">
            <svg class="w-4 h-4 text-slate-400 shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
              <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
            <input 
              v-model="searchQuery" 
              type="text" 
              placeholder="Buscar loja por nome ou código..." 
              class="w-full bg-transparent border-none focus:outline-none text-sm text-slate-700 ml-2 placeholder-slate-400"
            />
          </div>
          
          <!-- Toggle Ativos -->
          <label class="flex items-center gap-2 cursor-pointer shrink-0">
            <div class="relative">
              <input type="checkbox" v-model="filterAtivos" class="sr-only peer">
              <div class="w-9 h-5 bg-slate-200 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all peer-checked:bg-indigo-600"></div>
            </div>
            <span class="text-xs font-medium text-slate-600">Ocultar Desligados</span>
          </label>
          
          <div class="text-xs text-slate-500 font-medium shrink-0 ml-auto">
            Total de Lojas: <span class="text-indigo-600 font-bold">{{ filteredLojas.length }}</span>
          </div>
        </div>

        <!-- Loading -->
        <div v-if="isLoading" class="flex flex-col items-center justify-center py-12 gap-3">
          <div class="w-8 h-8 border-3 border-indigo-200 border-t-indigo-600 rounded-full animate-spin"></div>
          <p class="text-sm text-slate-500">Buscando quadro de funcionários...</p>
        </div>

        <!-- Empty State -->
        <div v-else-if="lojas.length === 0" class="text-center py-16 bg-white rounded-xl border border-slate-200 border-dashed">
          <p class="text-slate-500">Nenhum dado encontrado para a competência selecionada.</p>
        </div>

        <!-- Search Empty State -->
        <div v-else-if="filteredLojas.length === 0" class="text-center py-16 bg-white rounded-xl border border-slate-200 border-dashed">
          <p class="text-slate-500">Nenhuma loja encontrada para a busca "{{ searchQuery }}".</p>
        </div>

        <!-- Lojas List -->
        <div v-else class="space-y-4">
          <div v-for="loja in filteredLojas" :key="loja.codLoja" class="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden transition-all duration-200 hover:shadow-md">
            
            <!-- Loja Header (Accordion Toggle) -->
            <button 
              @click="toggleLoja(loja.codLoja)" 
              class="w-full flex items-center justify-between px-6 py-4 bg-white hover:bg-slate-50 transition-colors text-left"
            >
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 bg-indigo-50 text-indigo-600 rounded-lg flex items-center justify-center shrink-0">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
                  </svg>
                </div>
                <div>
                  <h2 class="text-base font-bold text-slate-800">{{ loja.descrLoja }} <span class="text-xs font-normal text-slate-400 ml-1">#{{ loja.codLoja }}</span></h2>
                  <p class="text-xs text-slate-500 mt-0.5">Marca: <span class="font-medium text-slate-700">{{ loja.descrMarca }}</span></p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <div class="flex flex-col items-end mr-4">
                  <div class="flex items-center gap-2 mb-1">
                    <span class="text-xs font-bold text-slate-700">HC: {{ loja.totalFuncionarios }}</span>
                    <span class="text-[10px] px-1.5 py-0.5 rounded bg-emerald-50 text-emerald-600 border border-emerald-100 font-semibold" title="Ativos">
                      {{ loja.totalAtivos }}
                    </span>
                    <span class="text-[10px] px-1.5 py-0.5 rounded bg-red-50 text-red-600 border border-red-100 font-semibold" title="Desligados" v-if="loja.totalDesligados > 0">
                      {{ loja.totalDesligados }}
                    </span>
                  </div>
                  <div class="w-32 h-1.5 bg-slate-100 rounded-full overflow-hidden flex">
                    <div class="h-full bg-emerald-500" :style="`width: ${(loja.totalAtivos / loja.totalFuncionarios) * 100}%`"></div>
                    <div class="h-full bg-red-500" :style="`width: ${(loja.totalDesligados / loja.totalFuncionarios) * 100}%`"></div>
                  </div>
                </div>
                <svg 
                  class="w-5 h-5 text-slate-400 transition-transform duration-200" 
                  :class="{ 'rotate-180': expandedLojas.has(loja.codLoja) }"
                  fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"
                >
                  <path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7" />
                </svg>
              </div>
            </button>

            <!-- Loja Content (Employees Table) -->
            <div v-show="expandedLojas.has(loja.codLoja)" class="border-t border-slate-100 bg-slate-50 p-4">
              <div class="overflow-x-auto bg-white rounded-lg border border-slate-200">
                <table class="w-full text-left text-sm whitespace-nowrap">
                  <thead class="bg-slate-50 text-slate-600 border-b border-slate-200 text-xs uppercase tracking-wider">
                    <tr>
                      <th class="px-4 py-3 font-semibold">Funcionário</th>
                      <th class="px-4 py-3 font-semibold">Cargo</th>
                      <th class="px-4 py-3 font-semibold text-center">Tempo de Empresa</th>
                      <th class="px-4 py-3 font-semibold text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <template v-for="func in loja.funcionarios" :key="func.id">
                      <tr v-if="!filterAtivos || !func.dataDemissao" class="hover:bg-slate-50/50 transition-colors">
                        <td class="px-4 py-3">
                          <div class="flex items-center gap-3">
                            <div class="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center text-xs font-bold shadow-sm border border-indigo-200">
                              {{ getIniciais(func.matricula) }}
                            </div>
                            <div class="font-medium text-slate-800 font-mono text-xs">
                              {{ func.matricula }}
                            </div>
                          </div>
                        </td>
                        <td class="px-4 py-3">
                          <div class="text-slate-800 font-medium text-sm">{{ func.descrCargo }}</div>
                          <div class="text-[10px] font-mono text-slate-400 mt-0.5">Cod: {{ func.codCargo }}</div>
                        </td>
                        <td class="px-4 py-3 text-center">
                          <div class="text-slate-700 font-medium text-sm">{{ calcularTempoCasa(func.dataAdmissao, func.dataDemissao) }}</div>
                          <div class="text-[10px] text-slate-400 mt-0.5">Desde {{ formatDate(func.dataAdmissao) }}</div>
                        </td>
                        <td class="px-4 py-3 text-center">
                          <div v-if="func.dataDemissao" class="inline-flex flex-col items-center">
                            <span class="bg-red-50 text-red-600 text-[10px] font-bold px-2 py-0.5 rounded border border-red-100">DESLIGADO</span>
                            <span class="text-[10px] text-slate-500 mt-1">em {{ formatDate(func.dataDemissao) }}</span>
                          </div>
                          <div v-else class="inline-flex">
                            <span class="bg-emerald-50 text-emerald-600 text-[10px] font-bold px-2 py-0.5 rounded border border-emerald-100">ATIVO</span>
                          </div>
                        </td>
                      </tr>
                    </template>
                  </tbody>
                </table>
              </div>
            </div>
            
          </div>
        </div>

      </div>
    </main>
  </div>
</template>
