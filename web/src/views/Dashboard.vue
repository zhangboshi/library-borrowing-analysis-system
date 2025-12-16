<template>
  <LayoutShell>
    <div class="toolbar">
      <el-radio-group v-model="rangeType" size="small" @change="onRangePreset">
        <el-radio-button label="7d">近7天</el-radio-button>
        <el-radio-button label="30d">近30天</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-if="rangeType === 'custom'"
        v-model="customRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        size="small"
        @change="loadAll"
      />
    </div>

    <el-row :gutter="16">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <el-card class="kpi-card" shadow="hover">
          <div class="card-label">{{ card.label }}</div>
          <div class="card-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover" header="借阅趋势">
          <div v-loading="loading" class="chart-wrapper">
            <BaseChart :option="trendOption" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" header="分类占比">
          <div v-loading="loading" class="chart-wrapper">
            <BaseChart :option="categoryOption" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover" header="读者借阅频次分布">
          <div v-loading="loading" class="chart-wrapper">
            <BaseChart :option="frequencyOption" />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" header="状态分布">
          <div v-loading="loading" class="chart-wrapper">
            <BaseChart :option="statusOption" />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card shadow="hover" header="Top 10 热门图书">
          <el-table :data="topBooks" size="small" border v-loading="loading">
            <el-table-column prop="title" label="书名" />
            <el-table-column prop="count" label="借阅次数" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" header="近7天借阅量（概览）">
          <div class="highlight-box" v-loading="loading">
            <div class="highlight-number">{{ overview.recent7Days }}</div>
            <div class="highlight-desc">近7天新增借阅</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </LayoutShell>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import dayjs from 'dayjs';
import LayoutShell from '@/components/LayoutShell.vue';
import BaseChart from '@/components/BaseChart.vue';
import {
  fetchOverview,
  fetchBorrowTrend,
  fetchCategoryShare,
  fetchTopBooks,
  fetchStatusDistribution,
  fetchReaderFrequency,
} from '@/api/stats';

const loading = ref(false);
const overview = ref({
  totalBorrow: 0,
  totalReaders: 0,
  totalBooks: 0,
  unreturned: 0,
  recent7Days: 0,
});
const trend = ref([]);
const categoryShare = ref([]);
const topBooks = ref([]);
const statusDistribution = ref([]);
const readerFrequency = ref([]);

const rangeType = ref('30d');
const customRange = ref([]);

const computedRange = () => {
  const today = dayjs().endOf('day');
  if (rangeType.value === '7d') {
    return {
      start: today.subtract(6, 'day').format('YYYY-MM-DD'),
      end: today.format('YYYY-MM-DD'),
    };
  }
  if (rangeType.value === 'month') {
    return {
      start: today.startOf('month').format('YYYY-MM-DD'),
      end: today.format('YYYY-MM-DD'),
    };
  }
  if (rangeType.value === 'custom' && customRange.value?.length === 2) {
    return { start: customRange.value[0], end: customRange.value[1] };
  }
  // default 30d
  return {
    start: today.subtract(29, 'day').format('YYYY-MM-DD'),
    end: today.format('YYYY-MM-DD'),
  };
};

const statCards = computed(() => [
  { label: '总借阅量', value: overview.value.totalBorrow },
  { label: '读者数', value: overview.value.totalReaders },
  { label: '图书数', value: overview.value.totalBooks },
  { label: '未归还', value: overview.value.unreturned },
  { label: '近7天借阅', value: overview.value.recent7Days },
]);

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: trend.value.map((i) => i.date) },
  yAxis: { type: 'value' },
  series: [{ type: 'line', data: trend.value.map((i) => i.count), smooth: true, areaStyle: {} }],
}));

const categoryOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: '70%',
      data: categoryShare.value.map((i) => ({ name: i.category, value: i.count })),
      label: { formatter: '{b}: {d}%' },
    },
  ],
}));

const statusOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['40%', '70%'],
      data: statusDistribution.value.map((i) => ({ name: i.status, value: i.count })),
      label: { formatter: '{b}: {d}%' },
    },
  ],
}));

const frequencyOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: readerFrequency.value.map((i) => i.bucket) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: readerFrequency.value.map((i) => i.count), barWidth: 30 }],
}));

const loadAll = async () => {
  const { start, end } = computedRange();
  loading.value = true;
  try {
    const [o, t, c, rf, top, status] = await Promise.all([
      fetchOverview(),
      fetchBorrowTrend({ granularity: 'day', start, end }),
      fetchCategoryShare({ start, end }),
      fetchReaderFrequency({ start, end }),
      fetchTopBooks({ start, end, limit: 10 }),
      fetchStatusDistribution({ start, end }),
    ]);
    overview.value = o.data;
    trend.value = t.data;
    categoryShare.value = c.data;
    readerFrequency.value = rf.data;
    topBooks.value = top.data;
    statusDistribution.value = status.data;
  } finally {
    loading.value = false;
  }
};

const onRangePreset = () => {
  if (rangeType.value !== 'custom') {
    customRange.value = [];
    loadAll();
  }
};

onMounted(loadAll);
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.kpi-card {
  background: linear-gradient(135deg, #f5f7ff, #ffffff);
}
.card-label {
  color: #888;
  font-size: 13px;
}
.card-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 8px;
}
.chart-wrapper {
  min-height: 340px;
}
.highlight-box {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  flex-direction: column;
}
.highlight-number {
  font-size: 42px;
  font-weight: 800;
  color: #409eff;
}
.highlight-desc {
  color: #888;
  margin-top: 6px;
}
</style>
