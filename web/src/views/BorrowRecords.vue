<template>
  <LayoutShell>
    <el-card>
      <div class="toolbar">
        <el-date-picker
          v-model="filters.range"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          size="small"
        />
        <el-select v-model="filters.category" placeholder="分类" clearable size="small" style="width: 140px">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="filters.status" placeholder="状态" clearable size="small" style="width: 140px">
          <el-option v-for="s in statusOptions" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="filters.readerType" placeholder="读者类型" clearable size="small" style="width: 140px">
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
          <el-option label="馆员" value="STAFF" />
        </el-select>
        <el-button type="primary" size="small" @click="loadData">查询</el-button>
        <el-button size="small" @click="resetFilters">重置</el-button>
        <el-button type="success" size="small" @click="openCreate" v-if="isStaff()">新增借阅</el-button>
      </div>

      <el-table :data="records" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="readerName" label="读者" />
        <el-table-column prop="bookTitle" label="书名" />
        <el-table-column prop="borrowTime" label="借阅时间" />
        <el-table-column prop="dueTime" label="应还时间" />
        <el-table-column prop="returnTime" label="归还时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.status !== 'RETURNED' && isStaff()"
              type="primary"
              size="small"
              @click="handleReturn(row)"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="prev, pager, next"
          :page-size="pageSize"
          :current-page="page"
          :total="total"
          @current-change="handlePage"
        />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增借阅" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="读者" prop="readerId">
          <el-select v-model="form.readerId" filterable placeholder="选择读者">
            <el-option v-for="r in readerOptions" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="图书" prop="bookId">
          <el-select v-model="form.bookId" filterable placeholder="选择图书">
            <el-option v-for="b in bookOptions" :key="b.id" :label="b.title" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅时间" prop="borrowTime">
          <el-date-picker v-model="form.borrowTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="应还时间" prop="dueTime">
          <el-date-picker v-model="form.dueTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dialogLoading" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>
  </LayoutShell>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import LayoutShell from '@/components/LayoutShell.vue';
import {
  fetchBorrowRecords,
  returnBorrowRecord,
  createBorrowRecord,
  fetchReaders,
  fetchBooks,
} from '@/api/borrow';
import dayjs from 'dayjs';
import { useUserStore } from '@/stores/user';

const page = ref(1);
const pageSize = 10;
const total = ref(0);
const records = ref([]);
const loading = ref(false);

const filters = ref({
  range: [],
  category: '',
  status: '',
  readerType: '',
});

const statusOptions = ['BORROWED', 'RETURNED', 'OVERDUE', 'LOST'];

const categories = ref([]);
const readerOptions = ref([]);
const bookOptions = ref([]);

const dialogVisible = ref(false);
const dialogLoading = ref(false);
const formRef = ref();
const form = ref({
  readerId: '',
  bookId: '',
  borrowTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
  dueTime: dayjs().add(14, 'day').format('YYYY-MM-DDTHH:mm:ss'),
});
const rules = {
  readerId: [{ required: true, message: '请选择读者', trigger: 'change' }],
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }],
  borrowTime: [{ required: true, message: '请选择借阅时间', trigger: 'change' }],
  dueTime: [{ required: true, message: '请选择应还时间', trigger: 'change' }],
};

const statusTag = (status) => {
  const map = {
    BORROWED: 'warning',
    OVERDUE: 'danger',
    RETURNED: 'success',
    LOST: 'info',
  };
  return map[status] || 'info';
};

const userStore = useUserStore();
const isStaff = () => ['ADMIN', 'STAFF'].includes(userStore.user?.role);

const buildParams = () => {
  const params = { page: page.value, pageSize };
  if (filters.value.range?.length === 2) {
    params.startTime = filters.value.range[0];
    params.endTime = filters.value.range[1];
  }
  if (filters.value.category) params.category = filters.value.category;
  if (filters.value.status) params.status = filters.value.status;
  if (filters.value.readerType) params.readerType = filters.value.readerType;
  return params;
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await fetchBorrowRecords(buildParams());
    records.value = res.data.records || [];
    total.value = res.data.total || 0;
  } finally {
    loading.value = false;
  }
};

const handlePage = (p) => {
  page.value = p;
  loadData();
};

const resetFilters = () => {
  filters.value = { range: [], category: '', status: '', readerType: '' };
  page.value = 1;
  loadData();
};

const handleReturn = async (row) => {
  await ElMessageBox.confirm(`确认将【${row.bookTitle}】标记为归还吗？`, '提示', {
    type: 'warning',
  });
  await returnBorrowRecord(row.id, { returnTime: dayjs().format('YYYY-MM-DDTHH:mm:ss') });
  ElMessage.success('归还成功');
  loadData();
};

const openCreate = () => {
  dialogVisible.value = true;
};

const submitCreate = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    dialogLoading.value = true;
    try {
      await createBorrowRecord(form.value);
      ElMessage.success('新增借阅成功');
      dialogVisible.value = false;
      loadData();
    } finally {
      dialogLoading.value = false;
    }
  });
};

const loadOptions = async () => {
  const [readersRes, booksRes] = await Promise.all([
    fetchReaders({ page: 1, pageSize: 200 }),
    fetchBooks({ page: 1, pageSize: 200 }),
  ]);
  readerOptions.value = readersRes.data.records || [];
  bookOptions.value = booksRes.data.records || [];
  categories.value = Array.from(new Set((booksRes.data.records || []).map((b) => b.category))).filter(Boolean);
};

onMounted(async () => {
  await loadOptions();
  await loadData();
});
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: center;
  flex-wrap: wrap;
}
.pager {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
