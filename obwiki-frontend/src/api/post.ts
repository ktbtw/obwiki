import api from './index';

export interface PostSaveReq {
    title: string;
    content: string;
    userId: number;
}

export interface PostQueryResp {
    id: number;
    title: string;
    content: string;
    userId: number;
    viewCount: number;
    voteCount: number;
    createTime: string;
    updateTime: string;
    comments: CommentQueryResp[];
}

export function createPost(data: PostSaveReq) {
    return api.post('/api/post/save', data);
}

export function getPostList() {
    return api.get('/api/post/list');
}

export function getPostDetail(id: string | number) {
    return api.get(`/api/post/${id}`);
}

export function votePost(id: number, userId: number) {
    return api.post(`/api/post/${id}/vote?userId=${userId}`);
} 