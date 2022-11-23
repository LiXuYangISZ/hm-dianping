package com.hmdp.utils;

import com.hmdp.entity.User;

/**
 * @author lxy
 * @version 1.0
 * @Description 保存User到ThreadLocal的工具类
 * @date 2022/11/24 1:22
 */
public class UserHolder {
    private static final ThreadLocal<User> tl = new ThreadLocal<>();

    public static void saveUser(User user){
        tl.set(user);
    }

    public static User getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
