import api from './index';

export interface CommentSaveReq {
    postId: number;
    userId: number;
    content: string;
    parentId?: number;
}

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

export function createComment(data: CommentSaveReq) {
    return api.post('/api/comment/save', data);
}

export function getCommentList(postId: number) {
    return api.get(`/api/comment/list/${postId}`);
}

export function voteComment(commentId: number, userId: number) {
    return api.post(`/api/comment/${commentId}/vote?userId=${userId}`);
} 