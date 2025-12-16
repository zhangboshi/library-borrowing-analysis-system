import request from './request';

export function fetchOverview() {
  return request.get('/api/stats/overview');
}

export function fetchBorrowTrend(params) {
  return request.get('/api/stats/borrow-trend', { params });
}

export function fetchCategoryShare(params) {
  return request.get('/api/stats/category-share', { params });
}

export function fetchTopBooks(params) {
  return request.get('/api/stats/top-books', { params });
}

export function fetchReaderFrequency(params) {
  return request.get('/api/stats/reader-frequency', { params });
}

export function fetchStatusDistribution(params) {
  return request.get('/api/stats/status-distribution', { params });
}
