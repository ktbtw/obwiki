import { CommentQueryResp } from '@/types/comment';
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
    isVoted: boolean;
    createTime: string;
    updateTime: string;
    comments: CommentQueryResp[];
}

// 创建文章
export function createPost(data: PostSaveReq) {
    return api.post('/api/post/save', data);
}

// 获取文章列表
export function getPostList(params?: any) {
    return api.get('/api/post/list', { params });
}

// 获取文章详情
export function getPostDetail(id: string) {
    return api.get(`/api/post/${id}`);
}

// 文章点赞
export function votePost(postId: number, userId: number, cancel: boolean) {
    return api.post(`/api/post/vote/${postId}`, { userId, cancel });
} 