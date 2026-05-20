package com.hxs.context;

/**
 * 用户上下文（ThreadLocal）
 */
public class UserContext {

    private static final ThreadLocal<Long> TL = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentId(Long id) { TL.set(id); }
    public static Long getCurrentId() { return TL.get(); }
    public static void removeCurrentId() { TL.remove(); }

}
