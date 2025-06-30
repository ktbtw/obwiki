export interface CommentQueryResp {
    id: number;
    content: string;
    userId: number;
    postId: number;
    voteCount: number;
    createTime: string;
    updateTime: string;
}

export interface CommentSaveReq {
    content: string;
    userId: number;
    postId: number;
    parentId?: number;
} 