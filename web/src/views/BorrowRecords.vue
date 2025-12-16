<template>
  <LayoutShell>
    <el-card>
      <div class="filter-row">
        <el-date-picker
          v-model="range"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
        <el-button type="primary" @click="loadData">查询</el-button>
      </div>
      <el-table :data="records" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="readerName" label="读者" />
        <el-table-column prop="bookTitle" label="书名" />
        <el-table-column prop="borrowTime" label="借阅时间" />
        <el-table-column prop="dueTime" label="应还时间" />
        <el-table-column prop="returnTime" label="归还时间" />
        <el-table-column prop="status" label="状态" />
      </el-table>
      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePage"
        />
      </div>
    </el-card>
  </LayoutShell>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import LayoutShell from '@/components/LayoutShell.vue';
import { fetchBorrowRecords } from '@/api/borrow';

const page = ref(1);
const pageSize = 10;
const total = ref(0);
const records = ref([]);
const range = ref([]);

const loadData = async () => {
  const params = { page: page.value, pageSize };
  if (range.value && range.value.length === 2) {
    params.startTime = range.value[0];
    params.endTime = range.value[1];
  }
  const res = await fetchBorrowRecords(params);
  records.value = res.data.records || [];
  total.value = res.data.total || 0;
};

const handlePage = (p) => {
  page.value = p;
  loadData();
};

onMounted(loadData);
</script>

<style scoped>
.filter-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: center;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
