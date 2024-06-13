package com.huanf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }

    /**
     * 背景介绍:
     *
     * 1、登录的用户名是任意的，密码是123。
     * 用户登录成功之后，就会把session数据(也就是用户数据)，其中写死user字符串作为key存入GetHttpSessionConfig类的ServerEndpointConfig集合(是Map集合)里面
     * 然后在ChatEndpoint类，根据user字符串取出该用户对应的session数据，把这个session数据再存入的httpSession变量，
     * 然后从httpSession变量里面再存入onlineUsers变量，此时onlineUsers变量里面有数据的话，就说明有在在线用户，就会通过'系统通知'在线用户列表数据传给前端
     *
     * 2、可以跟在线的用户进行私聊，双方的数据是通过'非系统通知'在线用户列表数据传给前端
     *
     * 3、运行启动类，使用两个不同浏览器分别访问 http://localhost/login.html，密码123，用户名任意，然后就可以聊天了
     *
     * 4、没有数据库，数据是存储在请求内存里的。刷新网页之后数据还在，但是重启SpringBoot之后数据就没了
     */

}
