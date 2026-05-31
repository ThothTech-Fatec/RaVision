<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
} from 'chart.js'
import type { ChartData } from '@/types/dashboard'

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip, Legend)

const props = defineProps<{
  chartTitle: string
  data: ChartData
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
let chartInstance: Chart | null = null

function buildChart() {
  if (!canvasRef.value) return
  if (chartInstance) {
    chartInstance.destroy()
    chartInstance = null
  }

  chartInstance = new Chart(canvasRef.value, {
    type: 'bar',
    data: {
      labels: props.data.labels,
      datasets: props.data.datasets.map(ds => ({
        label: ds.label,
        data: ds.data,
        backgroundColor: ds.backgroundColor,
        borderRadius: 2,
        borderSkipped: false,
      })),
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'top',
          align: 'start',
          labels: {
            boxWidth: 12,
            boxHeight: 12,
            borderRadius: 3,
            useBorderRadius: true,
            font: { size: 11 },
            color: '#475569',
            padding: 12,
          },
        },
        tooltip: {
          callbacks: {
            title: (items) => items[0]?.label ?? '',
          },
        },
        title: {
          display: true,
          text: props.chartTitle,
          font: { size: 12, weight: 'bold' },
          color: '#334155',
          padding: { bottom: 8 },
        },
      },
      scales: {
        x: {
          stacked: true,
          grid: { display: false },
          ticks: { font: { size: 10 }, color: '#94a3b8' },
          title: {
            display: true,
            text: 'Mês',
            font: { size: 10 },
            color: '#94a3b8',
          },
        },
        y: {
          stacked: true,
          beginAtZero: true,
          grid: { color: '#f1f5f9' },
          ticks: { font: { size: 10 }, color: '#94a3b8', stepSize: 20 },
          title: {
            display: true,
            text: 'Quantidade de SKUs',
            font: { size: 10 },
            color: '#94a3b8',
          },
        },
      },
    },
  })
}

onMounted(() => buildChart())

watch(() => props.data, () => buildChart(), { deep: true })

onUnmounted(() => {
  chartInstance?.destroy()
  chartInstance = null
})
</script>

<template>
  <div class="relative w-full h-full min-h-[300px]">
    <canvas ref="canvasRef" />
  </div>
</template>
