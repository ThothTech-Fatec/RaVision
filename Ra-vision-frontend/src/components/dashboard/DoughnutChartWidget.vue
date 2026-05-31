<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import {
  Chart,
  DoughnutController,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js'
import type { ChartData } from '@/types/dashboard'

Chart.register(DoughnutController, ArcElement, Tooltip, Legend)

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
    type: 'doughnut',
    data: {
      labels: props.data.labels,
      datasets: props.data.datasets.map(ds => ({
        label: ds.label,
        data: ds.data,
        backgroundColor: ds.backgroundColor,
        borderWidth: 1,
      })),
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
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
            label: (item) => ` ${item.label}: ${item.raw}`,
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
      cutout: '65%',
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
