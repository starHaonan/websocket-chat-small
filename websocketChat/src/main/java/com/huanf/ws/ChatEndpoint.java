package com.huanf.ws;

import com.alibaba.fastjson.JSON;
import com.huanf.config.GetHttpSessionConfig;
import com.huanf.utils.MessageUtils;
import com.huanf.ws.pojo.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//configurator属性表示使用我们写的GetHttpSessionConfig配置类
@Slf4j
@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfig.class)
@Component
public class ChatEndpoint {

    //保存在线的用户。ConcurrentHashMap线程安全的集合。下面那行的String，其实就是user
    private static final Map<String, Session> onlineUsers = new ConcurrentHashMap<>();

    private HttpSession httpSession;

    /**
     * 建立websocket连接后，被调用
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        //获取我们写的GetHttpSessionConfig类里面保存的session，由于当时存的时候，key存的是HttpSession.class.getName()，
        //所以下一个取的时候，也需要使用这个key才能取出来。取出来的数据赋值给最上面定义的httpSession变量
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        //由于在UserController类的login方法，往session存入的key是user，所以下一行取的时候，也需要使用这个key才能取出来
        String user = (String) this.httpSession.getAttribute("user");
        //1，将session进行保存到最上面定义的onlineUsers对象。注意onlineUsers对象的唯一数据来源就是下一行
        onlineUsers.put(user, session);
        //2，广播消息。需要将登陆的所有的用户推送给所有的用户。也就是获取在线的好友列表。MessageUtils是我们写的工具类
        String message = MessageUtils.getMessage(true, null, getFriends());
        broadcastAllUsers(message);
    }


    public Set getFriends() {
        //拿到该Map集合(onlineUsers是最上面定义的Map集合)的所有key，且key的返回值刚好用Set类型接收。这个Map的所有key，其实就是写死的user字符串
        Set<String> set = onlineUsers.keySet();
        return set;
    }

    //广播消息的具体逻辑
    private void broadcastAllUsers(String message) {
        try {
            //遍历最上面定义的onlineUsers对象的数据
            Set<Map.Entry<String, Session>> entries = onlineUsers.entrySet();
            for (Map.Entry<String, Session> entry : entries) {
                //获取到所有用户对应的session对象
                Session session = entry.getValue();
                //发送消息，getBasicRemote().sendText()方法是官方写好的。getBasicRemote表示同步的消息
                session.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 浏览器发送消息到服务端，该方法被调用。也就是私聊
     * <p>
     * 张三  -->  李四
     *
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        try {
            //将消息推送给指定的用户
            Message msg = JSON.parseObject(message, Message.class);
            //获取 消息接收方的用户名
            String toName = msg.getToName();
            String messageTemp = msg.getMessage();
            //获取消息接收方用户对象的session对象
            Session session = onlineUsers.get(toName);
            String user = (String) this.httpSession.getAttribute("user");
            String msg1 = MessageUtils.getMessage(false, user, messageTemp);
            // 发送消息，getBasicRemote().sendText()方法是官方写好的。getBasicRemote表示同步的消息
            session.getBasicRemote().sendText(msg1);
        } catch (IOException e) {
            log.error("发送消息失败:{}", e.getMessage());
        }
    }

    /**
     * 断开 websocket 连接时被调用
     *
     * @param session
     */
    @OnClose
    public void onClose(Session session) {
        //1,从onlineUsers中删除当前用户的session对象，表示当前用户下线了
        String user = (String) this.httpSession.getAttribute("user");
        onlineUsers.remove(user);
        //2,通知其他所有的用户，当前用户下线了
        String message = MessageUtils.getMessage(true, null, getFriends());
        broadcastAllUsers(message);
    }
}
