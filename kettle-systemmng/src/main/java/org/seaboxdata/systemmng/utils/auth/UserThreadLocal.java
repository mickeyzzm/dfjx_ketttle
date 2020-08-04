package org.seaboxdata.systemmng.utils.auth;


import org.seaboxdata.systemmng.auth.vo.OnlineUser;

public class UserThreadLocal {

    private static ThreadLocal<OnlineUser> userThread = new ThreadLocal<>();

    public static void set(OnlineUser user){
        userThread.set(user);
    }

    public static OnlineUser get(){
        return userThread.get();
    }

    //防止内存泄漏
    public static void remove(){
        userThread.remove();
    }
}
