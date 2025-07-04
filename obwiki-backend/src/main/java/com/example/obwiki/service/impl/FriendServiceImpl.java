package com.example.obwiki.service.impl;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.obwiki.entity.Friend;
import com.example.obwiki.mapper.FriendMapper;
import com.example.obwiki.service.IFriendService;

@Service
public class FriendServiceImpl implements IFriendService {
    @Autowired
    private FriendMapper friendMapper;

    @Override
    public void addFriend(Long userId, Long friendId) {
        // 不能加自己为好友
        if (userId != null && userId.equals(friendId)) {
            return;
        }
        // 检查是否已存在
        if (friendMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend>()
                .eq("user_id", userId).eq("friend_id", friendId)) == 0) {
            Friend f1 = new Friend();
            f1.setUserId(userId);
            f1.setFriendId(friendId);
            f1.setCreateTime(new Date());
            friendMapper.insert(f1);
        }
        if (friendMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend>()
                .eq("user_id", friendId).eq("friend_id", userId)) == 0) {
            Friend f2 = new Friend();
            f2.setUserId(friendId);
            f2.setFriendId(userId);
            f2.setCreateTime(new Date());
            friendMapper.insert(f2);
        }
    }
    
    public void deleteFriend(Long userId, Long friendId) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend> wrapper1 = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper1.eq("user_id", userId).eq("friend_id", friendId);
        friendMapper.delete(wrapper1);
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend> wrapper2 = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        wrapper2.eq("user_id", friendId).eq("friend_id", userId);
        friendMapper.delete(wrapper2);
    }

    @Override
    public java.util.List<Friend> listFriends(Long userId) {
        return friendMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend>()
                .eq("user_id", userId));
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return friendMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Friend>()
                .eq("user_id", userId).eq("friend_id", friendId)) > 0;
    }
} 