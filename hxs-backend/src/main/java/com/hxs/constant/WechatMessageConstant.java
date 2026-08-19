package com.hxs.constant;

import org.springframework.beans.factory.annotation.Value;

/**
 * 微信公众号回复文案常量
 */
public final class WechatMessageConstant {


    public static final String DORMITORY_REPLY = """
            <a href=\"http://202.206.100.146/yxxt/web/xsLogin/login.zf\">点我查询宿舍</a>
            
            提示：默认账号为身份证号，密码为身份证后六位~
            """;
    private String baseUrl;

    @Value("${hxs.base-url}")
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public static final String SUBSCRIBE_REPLY = """
            感谢关注河小狮lite，如出现反应慢、无法正常使用等问题，可直接在公众号留言！
            
            常见问题解答请看<a href="https://mp.weixin.qq.com/s/_VNcQx3YpF_NSKZUqwuYvg">精选推文</a>
            
            研究生课表查询<a href="http://211.82.255.223">点这里</a>
            新生课表查询请点击右下角快捷查询 - 新学期课表
            右上角置顶服务号，查询更便捷！！
            
            本公众号由软件学院同学独立开发，如果您觉得好用就分享给同学吧！！
            """;

    public static final String USER_NOT_EXISTS = "未查询到用户信息，请先登录系统！！\n<a href=\"%s/dashboard\">点我登录系统</a>";
    public static final String FAIL_BIND = "绑定失败,请检查您的key或者绑定格式是否正确！！\n注意：绑定与绑定码之间存在空格！！";
    public static final String USER_ALREADY_BIND = "该用户已被绑定！！请登录系统解绑后重试！！";
    public static final String SUCCESS_BIND = "绑定成功！！\n回复 \"课表\" 可获取今日课表\n回复 \"成绩\" 获取本学期成绩";
    public static final String WECHAT_ALREADY_BIND = "该微信已绑定其他用户！！请解绑后重试！！";
    public static final String USER_NOT_BIND = "未绑定教务账号，请 <a href=\"https://mp.weixin.qq.com/s/-erOPmRBxs_zpFe7-WLatw\">查看教程</a> 进行绑定！！";
    public static final String UPDATE_GRADE_FAIL = "成绩更新失败，可能是登录过期或未绑定，请重新登录河小狮lite后重试！！\n<a href=\"https://mp.weixin.qq.com/s/-erOPmRBxs_zpFe7-WLatw\">查看绑定教程</a>";
    public static final String RANKING_REPLY = """
            <a href="http://82.156.49.70/all-scores">点我查看排名</a>
            
            说明：排名仅统计使用河小狮lite且更新了成绩的同学，排名范围为同专业，排名仅供参考，有误差敬请谅解！！
            邀请同学使用河小狮lite，获取更准确的数据！！
            """;
    public static final String SUPPORT = "<a href=\"http://82.156.49.70/support\">点这里支持作者</a>";
    public static final String NOTICE_REPLY = """
            本公众号不是官方“狮小伴”小程序，请勿混淆！
            
            录取材料和入学电子材料在以下任一网站下载即可
            
            <a href="https://zsjyc.hebtu.edu.cn/zsw/a/tzgg/">河北师范大学招生信息网</a>
            <a href="https://jwc.hebtu.edu.cn/a/newcyxz/index.html">河北师范大学教务处</a>
            <a href="https://yingxin.hebtu.edu.cn/">河北师范大学迎新网</a>
            
            易班使用手册如下
            <a href="https://mp.weixin.qq.com/s/lYG7ddDTsTfb3whR6MCswQ">易班新生使用手册</a>
            欢迎2026级新同学~~
            您可以持续关注本公众号，获取课表查询、成绩查询等服务！
            """;

    private WechatMessageConstant() {}
}
