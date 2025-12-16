import request from './request';

export function login(data) {
  return request.post('/api/auth/login', data);
}

export function fetchMe() {
  return request.get('/api/auth/me');
}

export function logoutApi() {
  return request.post('/api/auth/logout');
}
