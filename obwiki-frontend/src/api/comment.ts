import { CommentSaveReq } from '@/types/comment';
import api from './index';

export interface CommentQueryResp {
    id: number;
    content: string;
    userId: number;
    postId: number;
    parentId?: number;
    voteCount: number;
    createTime: string;
    updateTime: string;
}

// 创建评论
export function createComment(data: CommentSaveReq) {
    return api.post('/api/comment', data);
}

export function getCommentList(postId: number) {
    return api.get(`/api/comment/list/${postId}`);
}

// 评论点赞
export function voteComment(commentId: number, userId: number) {
    return api.post(`/api/comment/vote/${commentId}`, { userId });
} 