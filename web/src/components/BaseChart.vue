<template>
  <div ref="chartRef" class="chart-container"></div>
</template>

<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue';
import * as echarts from 'echarts';

const props = defineProps({
  option: { type: Object, required: true },
});

const chartRef = ref(null);
let chart;

const render = () => {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  chart.setOption(props.option);
};

onMounted(() => {
  render();
  window.addEventListener('resize', resizeChart);
});

watch(() => props.option, render, { deep: true });

const resizeChart = () => {
  if (chart) chart.resize();
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart);
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style scoped>
.chart-container {
  width: 100%;
  height: 320px;
}
</style>
