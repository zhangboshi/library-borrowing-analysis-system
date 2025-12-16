import request from './request';

export function fetchBorrowRecords(params) {
  return request.get('/api/borrow-records', { params });
}
