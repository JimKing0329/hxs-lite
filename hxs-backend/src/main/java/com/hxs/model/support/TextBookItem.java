package com.hxs.model.support;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

@Data
public class TextBookItem {
    @JSONField(name = "kcmc") private String courseName;
    @JSONField(name = "jcmc") private String bookName;
    @JSONField(name = "jcbb") private String edition;
    @JSONField(name = "zz") private String author;
    @JSONField(name = "cbs") private String press;
    @JSONField(name = "isbn") private String isbn;
}
