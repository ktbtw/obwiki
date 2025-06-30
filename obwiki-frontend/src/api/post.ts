import api from './index';

export function getPostList() {
  return api.get('/api/post/list');
}

export function getPostDetail(id: number) {
  return api.get(`/api/post/${id}`);
}

export function createPost(data: { userId: number; title: string; content: string }) {
  return api.post('/api/post', data);
}

export function votePost(id: number) {
  return api.post(`/api/post/${id}/vote`);
} 