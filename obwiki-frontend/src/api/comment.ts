import api from './index';

export function getCommentList(postId: number) {
  return api.get('/api/comment/list', { params: { postId } });
}

export function createComment(data: { postId: number; userId: number; content: string }) {
  return api.post('/api/comment', data);
} 