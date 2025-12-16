<template>
  <LayoutShell>
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in statCards" :key="card.label">
        <el-card>
          <div class="card-label">{{ card.label }}</div>
          <div class="card-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card header="借阅趋势">
          <BaseChart :option="trendOption" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="分类占比">
          <BaseChart :option="categoryOption" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card header="Top 图书">
          <el-table :data="topBooks" size="small" border>
            <el-table-column prop="title" label="书名" />
            <el-table-column prop="count" label="借阅次数" width="120" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card header="状态分布">
          <BaseChart :option="statusOption" />
        </el-card>
      </el-col>
    </el-row>
  </LayoutShell>
</template>

<script setup>
import { onMounted, ref, computed } from 'vue';
import LayoutShell from '@/components/LayoutShell.vue';
import BaseChart from '@/components/BaseChart.vue';
import {
  fetchOverview,
  fetchBorrowTrend,
  fetchCategoryShare,
  fetchTopBooks,
  fetchStatusDistribution,
} from '@/api/stats';
import dayjs from 'dayjs';

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

const statCards = computed(() => [
  { label: '总借阅量', value: overview.value.totalBorrow },
  { label: '读者数', value: overview.value.totalReaders },
  { label: '图书数', value: overview.value.totalBooks },
  { label: '未归还', value: overview.value.unreturned },
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
    },
  ],
}));

const loadData = async () => {
  const [o, t, c, top, status] = await Promise.all([
    fetchOverview(),
    fetchBorrowTrend({ granularity: 'day' }),
    fetchCategoryShare({}),
    fetchTopBooks({ limit: 5 }),
    fetchStatusDistribution({}),
  ]);
  overview.value = o.data;
  trend.value = t.data;
  categoryShare.value = c.data;
  topBooks.value = top.data;
  statusDistribution.value = status.data;
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.card-label {
  color: #888;
  font-size: 13px;
}
.card-value {
  font-size: 22px;
  font-weight: 700;
  margin-top: 8px;
}
</style>
