import React, { useState, useEffect } from 'react';
import { Layout, Card, Row, Col, Table, Spin, message, Modal, Form, DatePicker, Input, Button, Select, Switch } from 'antd';
import {
  UserOutlined, LoginOutlined, TeamOutlined, CalendarOutlined,
  SyncOutlined, HomeOutlined, WechatOutlined, LogoutOutlined,
  DashboardOutlined, LinkOutlined, BookOutlined, HeartOutlined
} from '@ant-design/icons';
import { API_PATHS } from '../constants/api';
import { authFetch } from '../utils/request';
import moment from 'moment';
import './AdminDashboard.css';


const { Header, Content } = Layout;

export default function AdminDashboard() {
  const [loading, setLoading] = useState(true);
  const [userDistribution, setUserDistribution] = useState([]);
  const [userCount, setUserCount] = useState(0);
  const [todayLoginCount, setTodayLoginCount] = useState(0);
  const [sevenDayLoginCount, setSevenDayLoginCount] = useState(0);
  const [visible, setVisible] = useState(false);
  const [modalLoading, setModalLoading] = useState(false);
  const [form] = Form.useForm();
  const [updateWeek, setUpdateWeek] = useState('');
  const [updateWeekLoading, setUpdateWeekLoading] = useState(false);
  const [termDateType, setTermDateType] = useState('1'); // 1: 开学日期, 2: 课表日期，默认开学日期
  const [updateClassesLoading, setUpdateClassesLoading] = useState(false);
  const [updateCourseTableLoading, setUpdateCourseTableLoading] = useState(false);
  const [courseTableTaskEnabled, setCourseTableTaskEnabled] = useState(false);
  const [courseTableTaskSwitchLoading, setCourseTableTaskSwitchLoading] = useState(false);

  // 微信菜单状态 & URL 配置
  const [wechatMenuState, setWechatMenuState] = useState('');
  const [menuStateLoading, setMenuStateLoading] = useState(false);
  const [menuUrls, setMenuUrls] = useState({ calender: '', map_hq: '', map_yh: '' });
  const [menuUrlSaving, setMenuUrlSaving] = useState({});
  const [supportClickCount, setSupportClickCount] = useState(0);
  const [courseTableClickCount, setCourseTableClickCount] = useState(0);

  // 菜单 URL 配置项
  const menuUrlTypes = [
    { type: 'calender', title: '校历链接', desc: '微信菜单-校历页面 URL' },
    { type: 'map_hq',  title: '红旗校区地图链接', desc: '微信菜单-红旗地图 URL' },
    { type: 'map_yh',  title: '裕华校区地图链接', desc: '微信菜单-裕华地图 URL' },
  ];

  // 获取菜单 URL
  const fetchMenuUrls = async () => {
    for (const { type } of menuUrlTypes) {
      try {
        const res = await authFetch(`${API_PATHS.GET_MENU_URL}?type=${type}`);
        const result = await res.json();
        if (result.code === 1) {
          setMenuUrls(prev => ({ ...prev, [type]: result.data || '' }));
        }
      } catch (e) {
        console.error(`获取菜单URL失败 type=${type}:`, e);
      }
    }
  };

  // 更新菜单 URL
  const handleMenuUrlUpdate = async (type) => {
    setMenuUrlSaving(prev => ({ ...prev, [type]: true }));
    try {
      const res = await authFetch(
        `${API_PATHS.UPDATE_MENU_URL}?type=${type}&url=${encodeURIComponent(menuUrls[type])}`,
        { method: 'PUT' }
      );
      const result = await res.json();
      if (result.code === 1) {
        message.success('链接更新成功');
      } else {
        message.error(result.msg || '更新失败');
      }
    } catch (e) {
      console.error('更新菜单URL失败:', e);
      message.error('网络错误');
    } finally {
      setMenuUrlSaving(prev => ({ ...prev, [type]: false }));
    }
  };

  // 更新微信菜单状态（自动推送菜单）
  const handleMenuStateChange = (newState) => {
    Modal.confirm({
      title: '确认切换菜单',
      content: `确定要将微信菜单切换为「${newState}」吗？切换后将自动推送至微信。`,
      okText: '确认切换',
      cancelText: '取消',
      onOk: async () => {
        setMenuStateLoading(true);
        try {
          const res = await authFetch(
            `${API_PATHS.UPDATE_WECHAT_MENU_STATE}?state=${encodeURIComponent(newState)}`,
            { method: 'PUT' }
          );
          const result = await res.json();
          if (result.code === 1) {
            setWechatMenuState(newState);
            message.success(`菜单已切换为「${newState}」并推送至微信`);
          } else {
            message.error(result.msg || '更新失败');
          }
        } catch (e) {
          console.error('更新菜单状态失败:', e);
          message.error('网络错误');
        } finally {
          setMenuStateLoading(false);
        }
      },
    });
  };

  useEffect(() => {
    // 检查是否已登录
    const token = localStorage.getItem('Authorization');
    if (!token) {
      window.location.href = '/admin/login';
      return;
    }

    // 获取所有统计数据
    const fetchAllData = async () => {
      try {
        setLoading(true);
        // 并行请求接口
        const [distributionRes, userCountRes, loginCountRes, sevenDayLoginCountRes, courseTableTaskEnabledRes, menuStateRes, clickCountsRes] = await Promise.all([
          authFetch(API_PATHS.GET_USER_DISTRIBUTION),
          authFetch(API_PATHS.GET_USER_COUNT),
          authFetch(API_PATHS.GET_TODAY_LOGIN_COUNT),
          authFetch(API_PATHS.GET_SEVEN_DAY_LOGIN_COUNT),
          authFetch(API_PATHS.GET_COURSE_TABLE_TASK_ENABLED),
          authFetch(API_PATHS.GET_WECHAT_MENU_STATE),
          authFetch(API_PATHS.GET_CLICK_COUNTS)
        ]);

        // 解析响应数据
        const distributionData = await distributionRes.json();
        const userCountData = await userCountRes.json();
        const loginCountData = await loginCountRes.json();
        const sevenDayLoginCountData = await sevenDayLoginCountRes.json();
        const courseTableTaskEnabledData = await courseTableTaskEnabledRes.json();
        const menuStateData = await menuStateRes.json();
        const clickCountsData = await clickCountsRes.json();

        // 处理数据
        if (distributionData.code === 1) {
          setUserDistribution(distributionData.data || []);
        } else {
          message.error('获取用户分布失败');
        }

        if (userCountData.code === 1) {
          setUserCount(userCountData.data || 0);
        } else {
          message.error('获取用户总数失败');
        }

        if (loginCountData.code === 1) {
          setTodayLoginCount(loginCountData.data || 0);
        } else {
          message.error('获取今日登录数失败');
        }

        if (sevenDayLoginCountData.code === 1) {
          setSevenDayLoginCount(sevenDayLoginCountData.data || 0);
        } else {
          message.error('获取最近7天登录数失败');
        }

        if (courseTableTaskEnabledData.code === 1) {
          setCourseTableTaskEnabled(courseTableTaskEnabledData.data || false);
        } else {
          message.error('获取课表定时任务状态失败');
        }

        if (menuStateData.code === 1) {
          setWechatMenuState(menuStateData.data || '开学');
        }

        if (clickCountsData.code === 1 && clickCountsData.data) {
          setSupportClickCount(clickCountsData.data.supportClickCount || 0);
          setCourseTableClickCount(clickCountsData.data.courseTableClickCount || 0);
        }
      } catch (error) {
        console.error('获取统计数据失败:', error);
        message.error('网络错误，无法获取数据');
      } finally {
        setLoading(false);
      }
    };

    fetchAllData();
    fetchMenuUrls();
  }, []);

  const handleOpenModal = async (dateType = '1') => {
    setModalLoading(true);
    try {
      const token = localStorage.getItem('Authorization');
      // 添加时间戳参数避免缓存
      const timestamp = new Date().getTime();
      const response = await authFetch(`${API_PATHS.GET_CURRENT_TERM_START_DATE}/${dateType}?_t=${timestamp}`);
      const result = await response.json();
      if (result.code === 1 && result.data) {
        console.log(`获取到日期类型 ${dateType} 的数据:`, result.data);
        form.setFieldsValue({
          year: result.data.year,
          term: result.data.term,
          termStartDate: moment(result.data.termStartDate),
          remark: result.data.remark
        });
        setVisible(true);
      } else {
        message.error('获取当前开学时间失败');
      }
    } catch (error) {
      console.error('获取数据失败:', error);
      message.error('网络错误，无法获取当前开学时间');
    } finally {
      setModalLoading(false);
    }
  };

  const handleCancel = () => {
    setVisible(false);
    form.resetFields();
  };

  const handleUpdateMajorInfo = async () => {
    try {
      const response = await authFetch(API_PATHS.UPDATE_MAJOR_INFO, {
        method: 'PUT',
      });

      const result = await response.json();
      if (response.ok && result.code === 1) {
        message.success(`更新成功，当前共${result.data}个专业`);
      } else {
        message.error(result.msg || '更新专业信息失败');
      }
    } catch (error) {
      console.error('更新专业信息失败:', error);
      message.error('网络错误，无法更新专业信息');
    }
  };

  const handleUpdateMajorInfoWithConfirm = () => {
    Modal.confirm({
      title: '确认更新专业信息',
      content: '确定要更新专业信息吗？此操作可能会覆盖现有数据。',
      okText: '确认',
      cancelText: '取消',
      onOk: () => {
        handleUpdateMajorInfo();
      },
      onCancel: () => {
        // 用户取消操作
      },
    });
  };

  const handleUpdateClasses = () => {
    Modal.confirm({
      title: '确认更新班级信息',
      content: '确定要更新班级信息吗？此操作可能会覆盖现有数据。',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        setUpdateClassesLoading(true);
        try {
          const response = await authFetch(API_PATHS.ADMIN_UPDATE_CLASSES, { method: 'PUT' });
          const result = await response.json();
          if (response.ok && result.code === 1) {
            message.success('班级信息更新成功');
          } else {
            message.error(result.msg || '更新班级信息失败');
          }
        } catch (error) {
          console.error('更新班级信息失败:', error);
          message.error('网络错误，无法更新班级信息');
        } finally {
          setUpdateClassesLoading(false);
        }
      },
    });
  };

  const handleUpdateCourseTable = () => {
    Modal.confirm({
      title: '确认更新课程表',
      content: '确定要更新所有课程表吗？此操作耗时较长，请耐心等待。',
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        setUpdateCourseTableLoading(true);
        try {
          const response = await authFetch(API_PATHS.ADMIN_UPDATE_COURSE_TABLE, { method: 'PUT' });
          const result = await response.json();
          if (response.ok && result.code === 1) {
            message.success('课程表更新成功');
          } else {
            message.error(result.msg || '更新课程表失败');
          }
        } catch (error) {
          console.error('更新课程表失败:', error);
          message.error('网络错误，无法更新课程表');
        } finally {
          setUpdateCourseTableLoading(false);
        }
      },
    });
  };

  const handleUpdateEmptyClassroom = async () => {
    if (!updateWeek || updateWeek <= 0) {
      message.error('请输入有效的周数');
      return;
    }

    setUpdateWeekLoading(true);
    try {
      const response = await authFetch(`${API_PATHS.UPDATE_EMPTY_CLASSROOM}?week=${updateWeek}`, {
        method: 'POST',
      });

      const result = await response.json();
      if (response.ok && result.code === 1) {
        message.success('空教室信息更新成功');
        setUpdateWeek('');
      } else {
        message.error(result.msg || '更新空教室信息失败');
      }
    } catch (error) {
      console.error('更新空教室信息失败:', error);
      message.error('网络错误，无法更新空教室信息');
    } finally {
      setUpdateWeekLoading(false);
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      const response = await authFetch(API_PATHS.UPDATE_TERM_START_DATE, {
        method: 'PUT',
        body: JSON.stringify({
          year: values.year,
          term: values.term,
          date: values.termStartDate.format('YYYY-MM-DD'),
          remark: values.remark,
          id: parseInt(termDateType)
        })
      });

      const result = await response.json();
      if (response.ok && result.code === 1) {
        message.success('开学日期更新成功');
        setVisible(false);
        form.resetFields();
      } else {
        message.error(result.msg || '更新失败，请重试');
      }
    } catch (error) {
      console.error('提交失败:', error);
      message.error('网络错误，提交失败');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('Authorization');
    window.location.href = '/admin/login';
  };

  const handleCourseTableTaskToggle = async (checked) => {
    setCourseTableTaskSwitchLoading(true);
    try {
      const response = await authFetch(`${API_PATHS.UPDATE_COURSE_TABLE_TASK_ENABLED}?enabled=${checked}`, {
        method: 'PUT',
      });
      const result = await response.json();
      if (response.ok && result.code === 1) {
        setCourseTableTaskEnabled(checked);
        message.success(checked ? '课表定时任务已启用' : '课表定时任务已关闭');
      } else {
        message.error(result.msg || '更新状态失败');
      }
    } catch (error) {
      console.error('更新课表定时任务状态失败:', error);
      message.error('网络错误，无法更新状态');
    } finally {
      setCourseTableTaskSwitchLoading(false);
    }
  };

  // 表格列定义
  const columns = [
    {
      title: '学院',
      dataIndex: 'college',
      key: 'college',
      render: (college) => college || '未分配',
    },
    {
      title: '用户数量',
      dataIndex: 'count',
      key: 'count',
      sorter: (a, b) => a.count - b.count,
      render: (count) => {
        const percentage = maxDistributionCount > 0 ? (count / maxDistributionCount) * 100 : 0;
        return (
          <div className="distribution-cell">
            <span className="distribution-count">{count}</span>
            <div className="distribution-bar" style={{ width: `${percentage}%`, flex: 1 }} />
          </div>
        );
      },
    },
  ];

  // 统计卡片数据
  const statsCards = [
    { title: '总用户数', value: userCount, icon: <UserOutlined />, color: 'blue' },
    { title: '24H内登录数', value: todayLoginCount, icon: <LoginOutlined />, color: 'green' },
    { title: '最近7天登录数', value: sevenDayLoginCount, icon: <LoginOutlined />, color: 'cyan' },
    { title: '学院数量', value: userDistribution.filter(item => item.college).length, icon: <TeamOutlined />, color: 'orange' },
    { title: '支持作者点击', value: supportClickCount, icon: <HeartOutlined />, color: 'red' },
    { title: '课表查询次数', value: courseTableClickCount, icon: <CalendarOutlined />, color: 'purple' },
  ];

  // 计算分布最大值（用于进度条）
  const maxDistributionCount = userDistribution.length > 0
    ? Math.max(...userDistribution.map(item => item.count || 0))
    : 1;

  return (
    <Layout className="admin-dashboard">
      <Header className="admin-header">
        <div className="header-left">
          <DashboardOutlined className="header-icon" />
          <span className="header-title">河小狮 Lite 管理系统</span>
        </div>
        <Button icon={<LogoutOutlined />} onClick={handleLogout} className="logout-btn">退出</Button>
      </Header>

      <Content className="admin-content">
        <Spin spinning={loading} tip="加载中...">
          {/* 统计卡片区域 */}
          <div className="stats-section">
            <Row gutter={[16, 16]}>
              {statsCards.map((card, index) => (
                <Col xs={12} sm={12} lg={6} key={index}>
                  <div className={`stat-card stat-card-${card.color}`}>
                    <div className="stat-content">
                      <div className="stat-info">
                        <span className="stat-label">{card.title}</span>
                        <span className="stat-value">{card.value}</span>
                      </div>
                      <div className="stat-icon">
                        {card.icon}
                      </div>
                    </div>
                    <div className="stat-accent" />
                  </div>
                </Col>
              ))}
            </Row>
          </div>

          {/* 操作面板区域 */}
          <div className="action-section">
            <Row gutter={[16, 16]}>
              {/* 📅 学期管理 */}
              <Col xs={24} lg={12}>
                <Card className="action-panel" title={<span><CalendarOutlined style={{ marginRight: 8 }} />学期管理</span>}>
                  <div className="action-list">
                    <div className="action-item">
                      <div className="action-icon action-icon-blue">
                        <CalendarOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">修改开学日期</span>
                        <span className="action-desc">设置新学期开学时间</span>
                      </div>
                      <div className="action-control">
                        <Button type="primary" onClick={() => handleOpenModal(termDateType)} size="small">
                          去设置
                        </Button>
                      </div>
                    </div>
                  </div>
                </Card>
              </Col>

              {/* 📚 教务数据 */}
              <Col xs={24} lg={12}>
                <Card className="action-panel" title={<span><BookOutlined style={{ marginRight: 8 }} />教务数据</span>}>
                  <div className="action-list">
                    <div className="action-item">
                      <div className="action-icon action-icon-green">
                        <SyncOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">更新专业信息</span>
                        <span className="action-desc">同步最新专业数据</span>
                      </div>
                      <div className="action-control">
                        <Button type="primary" onClick={handleUpdateMajorInfoWithConfirm} size="small">
                          立即更新
                        </Button>
                      </div>
                    </div>

                    <div className="action-item">
                      <div className="action-icon action-icon-cyan">
                        <TeamOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">更新班级信息</span>
                        <span className="action-desc">同步最新班级数据</span>
                      </div>
                      <div className="action-control">
                        <Button type="primary" onClick={handleUpdateClasses} loading={updateClassesLoading} size="small">
                          立即更新
                        </Button>
                      </div>
                    </div>

                    <div className="action-item">
                      <div className="action-icon action-icon-geekblue">
                        <CalendarOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">更新新学期课程表</span>
                        <span className="action-desc">刷新全部课程表数据</span>
                      </div>
                      <div className="action-control">
                        <Button type="primary" onClick={handleUpdateCourseTable} loading={updateCourseTableLoading} size="small">
                          立即更新
                        </Button>
                      </div>
                    </div>

                    <div className="action-item">
                      <div className="action-icon action-icon-orange">
                        <SyncOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">新学期课表课表定时任务</span>
                        <span className="action-desc">每小时自动更新课表数据</span>
                      </div>
                      <div className="action-control">
                        <Switch
                          checked={courseTableTaskEnabled}
                          onChange={handleCourseTableTaskToggle}
                          loading={courseTableTaskSwitchLoading}
                          checkedChildren="启用"
                          unCheckedChildren="关闭"
                        />
                      </div>
                    </div>
                  </div>
                </Card>
              </Col>

              {/* 🏫 教室管理 */}
              <Col xs={24} lg={12}>
                <Card className="action-panel" title={<span><HomeOutlined style={{ marginRight: 8 }} />教室管理</span>}>
                  <div className="action-list">
                    <div className="action-item" style={{ flexWrap: 'wrap' }}>
                      <div className="action-icon action-icon-purple">
                        <HomeOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">更新空教室</span>
                        <span className="action-desc">按周次刷新空教室信息</span>
                      </div>
                      <div className="action-control">
                        <div className="week-input-group">
                          <Input
                            type="number"
                            placeholder="第几周"
                            value={updateWeek}
                            onChange={(e) => setUpdateWeek(e.target.value)}
                            min="1"
                            size="small"
                          />
                          <Button
                            type="primary"
                            onClick={handleUpdateEmptyClassroom}
                            loading={updateWeekLoading}
                            size="small"
                          >
                            更新
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </Card>
              </Col>

              {/* 💬 微信管理 */}
              <Col xs={24} lg={12}>
                <Card className="action-panel" title={<span><WechatOutlined style={{ marginRight: 8 }} />微信管理</span>}>
                  <div className="action-list">
                    {/* 菜单状态切换 */}
                    <div className="action-item">
                      <div className="action-icon action-icon-orange">
                        <WechatOutlined />
                      </div>
                      <div className="action-info">
                        <span className="action-title">当前菜单状态</span>
                        <span className="action-desc">切换后自动推送至微信</span>
                      </div>
                      <div className="action-control">
                        <Select
                          value={wechatMenuState}
                          onChange={handleMenuStateChange}
                          loading={menuStateLoading}
                          size="small"
                          style={{ width: 100 }}
                        >
                          <Select.Option value="开学">开学</Select.Option>
                          <Select.Option value="假期">假期</Select.Option>
                          <Select.Option value="迎新">迎新</Select.Option>
                        </Select>
                      </div>
                    </div>

                    {/* 菜单链接配置 */}
                    {menuUrlTypes.map(({ type, title, desc }) => (
                      <div className="action-item" key={type}>
                        <div className="action-icon action-icon-blue">
                          <LinkOutlined />
                        </div>
                        <div className="action-info" style={{ flex: 1 }}>
                          <span className="action-title">{title}</span>
                          <span className="action-desc">{desc}</span>
                          <Input
                            size="small"
                            placeholder="请输入链接 URL"
                            value={menuUrls[type]}
                            onChange={(e) => setMenuUrls(prev => ({ ...prev, [type]: e.target.value }))}
                            style={{ marginTop: 4 }}
                          />
                        </div>
                        <div className="action-control">
                          <Button
                            type="primary"
                            onClick={() => handleMenuUrlUpdate(type)}
                            loading={menuUrlSaving[type]}
                            size="small"
                          >
                            保存
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </Card>
              </Col>
            </Row>
          </div>

          {/* 用户分布表格 */}
          <div className="distribution-section">
            <Card className="distribution-card" title="用户学院分布">
              <Table
                columns={columns}
                dataSource={userDistribution.map((item, index) => ({
                  ...item,
                  key: index,
                }))}
                pagination={false}
                locale={{ emptyText: '暂无用户分布数据' }}
              />
            </Card>
          </div>
          <Modal
            title="修改开学日期"
            visible={visible}
            onCancel={handleCancel}
            footer={[
              <Button key="cancel" onClick={handleCancel}>取消</Button>,
              <Button key="submit" type="primary" onClick={handleSubmit} loading={modalLoading}>提交</Button>
            ]}
          >
            <Form form={form} layout="vertical" initialValues={{ termStartDate: null, remark: '', year: '', term: '' }}>
              <Form.Item
                label="日期类型"
                rules={[{ required: true, message: '请选择日期类型' }]}
              >
                <Select
                  placeholder="请选择日期类型"
                  value={termDateType}
                  onChange={(value) => {
                    setTermDateType(value);
                    // 当选择改变时，重新获取数据
                    handleOpenModal(value);
                  }}
                >
                  <Select.Option value="1">开学日期</Select.Option>
                  <Select.Option value="2">课表日期</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item
                name="year"
                label="学年"
                rules={[{ required: true, message: '请输入学年' }]}
              >
                <Input placeholder="例如：2023-2024" />
              </Form.Item>
              <Form.Item
                name="term"
                label="学期"
                rules={[{ required: true, message: '请选择学期' }]}
              >
                <Select placeholder="请选择学期">
                  <Select.Option value="1">第一学期</Select.Option>
                  <Select.Option value="2">第二学期</Select.Option>
                </Select>
              </Form.Item>
              <Form.Item
                name="termStartDate"
                label="开学日期"
                rules={[{ required: true, message: '请选择开学日期' }]}
              >
                <DatePicker format="YYYY-MM-DD" style={{ width: '100%' }} />
              </Form.Item>
              <Form.Item
                name="remark"
                label="备注"
                rules={[{ required: true, message: '请输入备注' }]}
              >
                <Input.TextArea rows={4} placeholder="请输入备注信息" />
              </Form.Item>
            </Form>
          </Modal>
        </Spin>
      </Content>
    </Layout>
  );
}