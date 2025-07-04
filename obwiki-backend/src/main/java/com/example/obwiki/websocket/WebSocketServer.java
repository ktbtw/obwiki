package com.example.obwiki.websocket;

import java.io.IOException;
import java.util.HashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson.JSONObject;
import com.example.obwiki.entity.ChatMessage;
import com.example.obwiki.service.IChatMessageService;
import com.example.obwiki.service.IFriendService;

@Component
@ServerEndpoint("/ws/{token}")//客户端连接地址
public class WebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);

    //每个客户端一个token
    private String token = "";
    //放置所有连接
    private static HashMap<String, Session> map = new HashMap<>();

    // 假设有FriendService实现加好友
    private static IFriendService friendService;
    @Autowired
    public void setFriendService(IFriendService friendService) {
        WebSocketServer.friendService = friendService;
    }

    private static IChatMessageService chatMessageService;
    @Autowired
    public void setChatMessageService(IChatMessageService chatMessageService) {
        WebSocketServer.chatMessageService = chatMessageService;
    }

    //连接成功
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        map.put(token, session);
        this.token = token;
        LOG.info("有新连接：token：{}，session id：{}，当前连接数：{}", token, session.getId(), map.size());
        System.out.println("WebSocket连接建立：token=" + token + ", sessionId=" + session.getId() + ", 当前连接数=" + map.size());
    }

    //连接关闭
    @OnClose
    public void onClose(Session session) {
        map.remove(this.token);
        LOG.info("连接关闭，token：{}，session id：{}！当前连接数：{}", this.token, session.getId(), map.size());
        System.out.println("WebSocket连接关闭：token=" + this.token + ", sessionId=" + session.getId() + ", 当前连接数=" + map.size());
    }

    //收到消息
    @OnMessage
    public void onMessage(String message, Session session) {
        LOG.info("收到消息：{}，内容：{}", token, message);
        System.out.println("WebSocket收到消息：token=" + token + ", 内容=" + message);
        JSONObject json = JSONObject.parseObject(message);
        String type = json.getString("type");
        if ("friendRequest".equals(type)) {
            // 好友请求，转发给目标用户
            String toUserId = json.getString("toUserId");
            Session toSession = map.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                try { toSession.getBasicRemote().sendText(message); } catch (IOException ignored) {}
            }
        } else if ("friendResponse".equals(type)) {
            // 同意好友请求，转发给发起方
            String toUserId = json.getString("toUserId");
            Session toSession = map.get(toUserId);
            if (toSession != null && toSession.isOpen()) {
                try { toSession.getBasicRemote().sendText(message); } catch (IOException ignored) {}
            }
            // 数据库加好友
            Boolean accepted = json.getBoolean("accepted");
            if (accepted != null && accepted && friendService != null) {
                Long fromUserId = json.getLong("fromUserId");
                Long toUid = json.getLong("toUserId");
                friendService.addFriend(fromUserId, toUid);
            }
        } else if ("chat".equals(type)) {
            // 聊天消息，先验证好友关系
            Long fromUserId = json.getLong("fromUserId");
            Long toUserId = json.getLong("toUserId");
            
            // 检查是否为好友关系
            if (friendService != null && !friendService.isFriend(fromUserId, toUserId)) {
                // 不是好友关系，发送拒绝消息给发送方
                JSONObject rejectMsg = new JSONObject();
                rejectMsg.put("type", "friendDeleted");
                rejectMsg.put("message", "对方已删除你为好友，无法发送消息");
                rejectMsg.put("fromUserId", fromUserId);
                rejectMsg.put("toUserId", toUserId);
                
                Session fromSession = map.get(String.valueOf(fromUserId));
                if (fromSession != null && fromSession.isOpen()) {
                    try { 
                        fromSession.getBasicRemote().sendText(rejectMsg.toJSONString()); 
                    } catch (IOException ignored) {}
                }
                return; // 不处理此消息
            }
            
            // 是好友关系，正常转发消息
            Session toSession = map.get(String.valueOf(toUserId));
            if (toSession != null && toSession.isOpen()) {
                try { toSession.getBasicRemote().sendText(message); } catch (IOException ignored) {}
            }
            // 保存到数据库
            if (chatMessageService != null) {
                ChatMessage chatMsg = new ChatMessage();
                chatMsg.setFromUserId(fromUserId);
                chatMsg.setToUserId(toUserId);
                chatMsg.setContent(json.getString("content"));
                chatMsg.setTime(json.getDate("time"));
                chatMessageService.saveMessage(chatMsg);
            }
        }
    }

    //连接错误
    @OnError
    public void onError(Session session, Throwable error) {
        LOG.error("发生错误", error);
        System.out.println("WebSocket发生错误：token=" + token + ", error=" + error.getMessage());
    }

    //群发消息
    public void sendInfo(String message) {
        System.out.println("WebSocket推送内容：" + message + ", 当前连接数=" + map.size());
        for (String token : map.keySet()) {
            Session session = map.get(token);
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("WebSocket已推送到token=" + token);
            } catch (IOException e) {
                LOG.error("推送消息失败：{}，内容：{}", token, message);
                System.out.println("WebSocket推送失败：token=" + token + ", error=" + e.getMessage());
            }
            LOG.info("推送消息：{}，内容：{}", token, message);
        }
    }
}