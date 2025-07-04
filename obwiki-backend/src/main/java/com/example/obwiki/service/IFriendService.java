package com.example.obwiki.service;

public interface IFriendService {
    void addFriend(Long userId, Long friendId);
    java.util.List<com.example.obwiki.entity.Friend> listFriends(Long userId);
    void deleteFriend(Long userId, Long friendId);
    boolean isFriend(Long userId, Long friendId);
} 