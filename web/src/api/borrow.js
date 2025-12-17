import request from './request';

export function fetchBorrowRecords(params) {
  return request.get('/api/borrow-records', { params });
}

export function createBorrowRecord(data) {
  return request.post('/api/borrow-records', data);
}

export function returnBorrowRecord(id, data = {}) {
  return request.post(`/api/borrow-records/${id}/return`, data);
}

export function fetchReaders(params) {
  return request.get('/api/readers', { params });
}

export function fetchBooks(params) {
  return request.get('/api/books', { params });
}
