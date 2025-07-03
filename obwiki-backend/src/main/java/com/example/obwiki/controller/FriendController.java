package com.example.obwiki.controller;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.obwiki.entity.Friend;
import com.example.obwiki.entity.User;
import com.example.obwiki.resp.CommonResp;
import com.example.obwiki.service.IFriendService;
import com.example.obwiki.service.IUserService;

@RestController
@RequestMapping("/obwiki/friend")
public class FriendController {
    @Autowired
    private IFriendService friendService;
    @Autowired
    private IUserService userService;

    @GetMapping("/list")
    public CommonResp<List<FriendInfo>> list(@RequestParam Long userId) {
        List<Friend> friends = friendService.listFriends(userId);
        // 查找好友用户信息
        List<FriendInfo> result = friends.stream().map(f -> {
            User u = userService.getById(f.getFriendId());
            FriendInfo info = new FriendInfo();
            info.setId(u.getId());
            info.setName(u.getName());
            return info;
        }).collect(Collectors.toList());
        CommonResp<List<FriendInfo>> resp = new CommonResp<>();
        resp.setContent(result);
        return resp;
    }

    public static class FriendInfo {
        private Long id;
        private String name;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
} 