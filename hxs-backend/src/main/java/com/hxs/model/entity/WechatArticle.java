package com.hxs.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 微信公众号广告文章实体 — 映射 wechat_article 表
 */
@Data
@TableName("wechat_article")
public class WechatArticle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String url;
}
