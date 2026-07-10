package com.hxs.constant;

/**
 * 微信公众号回复文案常量
 */
public final class WechatMessageConstant {

    public static final String SUBSCRIBE_REPLY = "感谢关注河小狮lite，如出现反应慢、无法正常使用等问题，可直接在公众号留言！\n"
            + "\n首次登录需点击更新课表方可获取相关信息\n"
            + "\n常见问题解答请看<a href=\"https://mp.weixin.qq.com/s/_VNcQx3YpF_NSKZUqwuYvg\">精选推文</a>\n"
            + "\n研究生课表查询<a href=\"http://211.82.255.223\">点这里</a>\n"
            + "\n右上角置顶服务号，查询更便捷！！\n"
            + "\n\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\ud83c\udf39\n"
            + "本公众号由软件学院同学独立制作，如果您觉得好用就分享给同学吧！！";

    public static final String USER_NOT_EXISTS = "未查询到用户信息，请先登录系统！！\n<a href=\"http://115.190.9.5/dashboard\">点我登录系统</a>";
    public static final String FAIL_BIND = "绑定失败,请检查您的key或者绑定格式是否正确！！\n注意：绑定与绑定码之间存在空格！！";
    public static final String USER_ALREADY_BIND = "该用户已被绑定！！请登录系统解绑后重试！！";
    public static final String SUCCESS_BIND = "绑定成功！！\n回复 \"课表\" 可获取今日课表\n回复 \"成绩\" 获取本学期成绩";
    public static final String WECHAT_ALREADY_BIND = "该微信已绑定其他用户！！请解绑后重试！！";
    public static final String USER_NOT_BIND = "未绑定教务账号，请 <a href=\"https://mp.weixin.qq.com/s/-erOPmRBxs_zpFe7-WLatw\">查看教程</a> 进行绑定！！";
    public static final String UPDATE_GRADE_FAIL = "成绩更新失败，可能是登录过期或未绑定，请重新登录河小狮lite后重试！！\n<a href=\"https://mp.weixin.qq.com/s/-erOPmRBxs_zpFe7-WLatw\">查看绑定教程</a>";

    private WechatMessageConstant() {}
}
