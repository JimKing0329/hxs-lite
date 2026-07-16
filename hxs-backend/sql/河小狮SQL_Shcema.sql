-- 统一使用 utf8mb4_general_ci，避免不同 MySQL 版本默认 collation 不一致导致 JOIN 报错
SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

create table classroom_availability
(
    id            bigint auto_increment
        primary key,
    classroom_id  bigint                              not null comment '关联主表ID',
    empty_session tinyint                             not null comment '节次 (1-12等)',
    create_time   timestamp default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '教室空闲节次表';

create index idx_classroom_session
    on classroom_availability (classroom_id, empty_session);

create table course
(
    id              int auto_increment comment '自增id'
        primary key,
    course_id       varchar(255)                 not null comment '课程唯一标识',
    sid             varchar(20)                  null comment '学生id',
    title           varchar(255)                 not null comment '课程名称',
    teacher         varchar(100)                 null comment '授课教师（可扩展为外键）',
    class_name      varchar(100)                 null comment '班级名称',
    credit          decimal(3, 1)                null comment '学分',
    weekday         tinyint                      null comment '星期几(1-7)',
    start_session   tinyint                      null comment '开始节次',
    end_session     tinyint                      null comment '结束节次',
    evaluation_mode varchar(50) default '未安排' null comment '考核方式',
    campus          varchar(50)                  null comment '校区名称',
    place           varchar(100)                 null comment '上课地点',
    theory_hours    int                          null comment '理论学时',
    lab_hours       int                          null comment '实验学时',
    weekly_hours    tinyint                      null comment '周学时',
    total_hours     int                          null comment '总学时',
    week_number     tinyint                      null comment '教学周(1-20)'
)
comment '课程表'
;

create table empty_classroom
(
    id             bigint auto_increment comment '自增主键'
        primary key,
    class_id       varchar(50)                         null comment '教室唯一ID (cd_id)',
    class_name     varchar(100)                        null comment '教室全称 (cdmc)',
    campus_name    varchar(50)                         null comment '所属校区 (xqmc)',
    class_category varchar(50)                         null comment '教室类型 (cdlbmc)',
    week_number    tinyint                             null comment '周次 (1-52)',
    weekday        tinyint                             null comment '星期几 (1-7, 1=周一)',
    create_time    timestamp default CURRENT_TIMESTAMP null,
    building       varchar(50)                         null comment '教学楼号'
)
    comment '空闲教室主表';

create index idx_week_weekday
    on empty_classroom (week_number, weekday);

create table exam_info
(
    id          bigint auto_increment comment '自增主键'
        primary key,
    course_id   varchar(128) not null comment '课程代码',
    title       varchar(128) not null comment '课程名称',
    exam_time   varchar(128) null comment '考试时间（格式示例：2023-12-25 14:30）',
    location    varchar(128) null comment '考试地点',
    campus      varchar(128) null comment '考试校区',
    seat        varchar(128) null comment '考试座号',
    retake      varchar(128) null comment '重修标记（Y/ N）',
    exam_name   varchar(128) null comment '考试批次名',
    teacher     varchar(128) null comment '教师信息（ID_姓名 格式示例：T1001_张三）',
    class_name  varchar(128) null comment '教学班名称',
    college     varchar(128) null comment '开课学院',
    credit      float        null comment '学分数',
    exam_method varchar(128) null comment '考试方式（多个用&分隔）',
    paper_id    varchar(128) null comment '试卷编号',
    remark      text         null comment '备注',
    sid         varchar(128) null
)
    comment '考试信息表';

create index idx_course
    on exam_info (course_id);

create index idx_exam_time
    on exam_info (exam_time(10));

create index idx_paper
    on exam_info (paper_id);

create index idx_teacher
    on exam_info (teacher(20));

create table execute_course
(
    id             bigint auto_increment
        primary key,
    course_name    varchar(100) not null,
    course_point   varchar(20)  null,
    course_week    varchar(50)  null,
    college_name   varchar(100) null,
    course_time    varchar(100) null,
    major_code     varchar(36)  null,
    recommend_term varchar(128) null comment '推荐学年学期',
    course_type    varchar(10)  null comment '课程性质  选/必修'
)
comment '执行计划表';

create index index_major_code
    on execute_course (major_code);

create table major_info
(
    grade      int         not null comment '年级',
    major_id   varchar(10) not null comment '专业ID',
    college_id varchar(10) not null comment '学院ID',
    major_name varchar(50) not null comment '专业名称',
    major_code varchar(32) not null comment '专业代码',
    primary key (grade, college_id, major_name, major_id)
)
    comment '专业信息表';

create table score
(
    id            bigint unsigned auto_increment
        primary key,
    sid           varchar(20)                         not null comment '学生ID',
    grade         varchar(10)                         null comment '成绩等级（如A/B/C）',
    grade_point   varchar(10)                         null comment '绩点（如3.5/4.0）',
    category_name varchar(50)                         null comment '课程类别名称',
    college_name  varchar(50)                         null comment '学院名称',
    teacher_name  varchar(50)                         null comment '教师姓名',
    class_id      varchar(128)                        null comment '班级ID',
    major         varchar(50)                         null comment '专业名称',
    created_time  timestamp default CURRENT_TIMESTAMP null comment '创建时间',
    course_name   varchar(128)                        not null comment '课程名',
    year          int unsigned                        null comment '学年',
    term          tinyint unsigned                    null comment '学期',
    credit        varchar(10)                         null comment '学分',
    course_type   varchar(10)                         null comment '课程性质'
);

create index idx_class_id
    on score (class_id);

create index idx_sid
    on score (sid);

create index idx_teacher
    on score (teacher_name);

create index year_term_index
    on score (year, term);

create table score_detail
(
    id           bigint unsigned auto_increment
        primary key,
    sid          varchar(64) null comment '学号',
    course_name  varchar(64) null comment '课程名称',
    class_id     varchar(64) null comment '班级id',
    grade_column varchar(64) null comment '成绩列名',
    grade_ratio  varchar(64) null comment '成绩占比',
    grade        varchar(64) null comment '成绩分数',
    year         int         null,
    term         int         null
)
comment '成绩明细表';

create index idx_sid_course_class
    on score_detail (sid, course_name, class_id);

create table study_situation
(
    id                   bigint auto_increment comment '主键ID'
        primary key,
    sid                  bigint       not null comment '学生ID',
    gpa                  varchar(50)  null comment '平均学分绩点（GPA）描述',
    plan_course          varchar(100) null comment '计划总课程描述',
    pass_plan_course     varchar(100) null comment '计划内通过课程描述',
    fail_plan_course     varchar(100) null comment '计划内未通过课程描述',
    unstudy_plan_course  varchar(100) null comment '计划内未修课程描述',
    studying_plan_course varchar(100) null comment '计划内在读课程描述',
    out_plan_pass_course varchar(100) null comment '计划外通过课程描述',
    out_plan_fail_course varchar(100) null comment '计划外未通过课程描述'
)
    comment '学生学习情况记录表';

create index idx_sid
    on study_situation (sid)
    comment '学生ID索引';

create table system_config
(
    config_key   varchar(64)                        not null comment '配置键'
        primary key,
    config_value varchar(256)                       not null comment '配置值',
    remark       varchar(128)                       null comment '备注',
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '系统配置表';

create table system_dates
(
    id              int auto_increment
        primary key,
    term_start_date date        null,
    remark          varchar(50) null comment '日期备注',
    year            int         null comment '学年',
    term            int         null comment '学期'
)
comment '系统日期表';

create table user
(
    sid                 varchar(64)  not null comment '学号'
        primary key,
    password            varchar(64)  null comment '密码',
    name                varchar(128) null comment '姓名',
    college_name        varchar(128) null comment '学院名称',
    major_name          varchar(128) null comment '专业名称',
    class_name          varchar(128) null comment '班级名称',
    status              varchar(32)  null comment '学籍状态',
    enrollment_date     date         null comment '入学日期',
    candidate_number    varchar(64)  null comment '考生号',
    graduation_school   varchar(128) null comment '毕业学校',
    domicile            varchar(128) null comment '籍贯',
    postal_code         varchar(6)   null comment '邮政编码',
    politics_status     varchar(32)  null comment '政治面貌',
    nationality         varchar(32)  null comment '民族',
    education           varchar(32)  null comment '培养层次（学历）',
    phone_number        varchar(32)  null comment '手机号码',
    parents_number      varchar(32)  null comment '家长电话',
    email               varchar(128) null comment '电子邮箱',
    birthday            date         null comment '出生日期',
    registration_number varchar(64)  null comment '报到号',
    weight              varchar(16)  null comment '体重（kg）',
    height              varchar(16)  null comment '身高（cm）',
    gender              varchar(16)  null comment '性别',
    jw                  varchar(128) null comment 'token',
    jsession_id         varchar(128) null comment 'session_id',
    major_code          varchar(16)  null comment '专业代码',
    last_login          datetime     null comment '上次登录时间',
    open_id             varchar(64)  null comment '用户绑定微信openid',
    binding_key         varchar(64)  null comment '用户的唯一key，用于验证绑定'
)
    comment '用户信息表';

create table wechat_article
(
    id  int unsigned auto_increment comment '主键ID'
        primary key,
    url varchar(128) null comment '文章短链接（可选，如自己生成的短链）'
)
    comment '公众号文章链接表';

create table classes
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    grade       varchar(10)  null comment '年级代码 (njdm)',
    class_id    varchar(50)  null comment '班级ID (bh_id)',
    major_id    varchar(50)  null comment '专业ID (zyh_id)',
    college     varchar(100) null comment '学院名称 (jgmc)',
    class_name  varchar(100) null comment '班级名称 (bjmc)',
    campus_name varchar(50)  null comment '校区名称 (xqmc)',
    major_name  varchar(100) null comment '专业名称 (zymc)'
)
    comment '班级信息表';

create table main_course
(
    id            bigint auto_increment comment '主键ID'
        primary key,
    course_name   varchar(100) null comment '课程名称',
    weeks         varchar(50)  null comment '周次',
    week_day      varchar(20)  null comment '星期',
    start_session tinyint      null comment '开始节次',
    end_session   tinyint      null comment '结束节次',
    class_id      varchar(50)  null comment '班级ID'
)
    comment '主修课程表';

create index idx_main_course_class_id
    on main_course (class_id);

create table other_course
(
    id          bigint auto_increment comment '主键ID'
        primary key,
    course_name varchar(100) null comment '课程名称',
    weeks       varchar(50)  null comment '周次',
    class_id    varchar(50)  null comment '班级ID'
)
    comment '其他课程表';

create index idx_other_course_class_id
    on other_course (class_id);

