import React, { useState, useEffect, useRef } from 'react';
import { Layout, Card, Row, Col, Table, Spin, message, Modal, Form, DatePicker, Input, Button, Select } from 'antd';
import {
  UserOutlined, LoginOutlined, TeamOutlined, CalendarOutlined,
  SyncOutlined, HomeOutlined, WechatOutlined, LogoutOutlined,
  DashboardOutlined, PictureOutlined
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
  const [wechatMenuModalVisible, setWechatMenuModalVisible] = useState(false);
  const [selectedMenu, setSelectedMenu] = useState('');
  const [termDateType, setTermDateType] = useState('1'); // 1: 开学日期, 2: 课表日期，默认开学日期

  // 微信素材上传状态（校历/红旗地图/裕华地图）
  const [uploadStates, setUploadStates] = useState({
    calender: { uploading: false, preview: '' },
    map_hq:   { uploading: false, preview: '' },
    map_yh:   { uploading: false, preview: '' },
  });
  const uploadRefs = {
    calender: useRef(null),
    map_hq:   useRef(null),
    map_yh:   useRef(null),
  };

  // 素材类型配置
  const mediaUploadTypes = [
    { type: 'calender', title: '更新校历图片', desc: '上传新校历图片到微信公众号', iconColor: 'blue' },
    { type: 'map_hq',  title: '更新红旗校区地图', desc: '上传红旗校区地图图片', iconColor: 'purple' },
    { type: 'map_yh',  title: '更新裕华校区地图', desc: '上传裕华校区地图图片', iconColor: 'green' },
  ];

  const handleMediaUpload = async (type, e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    if (!allowedTypes.includes(file.type)) {
      message.error('仅支持 jpg/png 格式的图片');
      e.target.value = '';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      message.error('图片大小不能超过 10MB');
      e.target.value = '';
      return;
    }

    // 预览
    const reader = new FileReader();
    reader.onload = (ev) => setUploadStates(prev => ({
      ...prev,
      [type]: { ...prev[type], preview: ev.target.result },
    }));
    reader.readAsDataURL(file);

    // 上传
    setUploadStates(prev => ({ ...prev, [type]: { ...prev[type], uploading: true } }));
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await authFetch(`${API_PATHS.UPLOAD_MEDIA_ID}?type=${type}`, {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();
      if (response.ok && result.code === 1) {
        message.success('上传成功');
      } else {
        message.error(result.msg || '上传失败');
        setUploadStates(prev => ({ ...prev, [type]: { ...prev[type], preview: '' } }));
      }
    } catch (error) {
      console.error('上传素材失败:', error);
      message.error('网络错误，上传失败');
      setUploadStates(prev => ({ ...prev, [type]: { ...prev[type], preview: '' } }));
    } finally {
      setUploadStates(prev => ({ ...prev, [type]: { ...prev[type], uploading: false } }));
      e.target.value = '';
    }
  };

  const triggerMediaUpload = (type) => {
    uploadRefs[type]?.current?.click();
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
        // 并行请求三个接口
        const [distributionRes, userCountRes, loginCountRes, sevenDayLoginCountRes] = await Promise.all([
          authFetch(API_PATHS.GET_USER_DISTRIBUTION),
          authFetch(API_PATHS.GET_USER_COUNT),
          authFetch(API_PATHS.GET_TODAY_LOGIN_COUNT),
          authFetch(API_PATHS.GET_SEVEN_DAY_LOGIN_COUNT)
        ]);

        // 解析响应数据
        const distributionData = await distributionRes.json();
        const userCountData = await userCountRes.json();
        const loginCountData = await loginCountRes.json();
        const sevenDayLoginCountData = await sevenDayLoginCountRes.json();

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
      } catch (error) {
        console.error('获取统计数据失败:', error);
        message.error('网络错误，无法获取数据');
      } finally {
        setLoading(false);
      }
    };

    fetchAllData();
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

  const handleOpenWechatMenuModal = () => {
    setWechatMenuModalVisible(true);
  };

  const handleWechatMenuCancel = () => {
    setWechatMenuModalVisible(false);
    setSelectedMenu('');
  };

  const handleWechatMenuSubmit = async () => {
    if (!selectedMenu) {
      message.error('请选择菜单类型');
      return;
    }

    try {
      const response = await authFetch(`${API_PATHS.UPDATE_WECHAT_MENU}?type=${selectedMenu}`, {
        method: 'PUT',
      });

      const result = await response.json();
      if (response.ok && result.code === 1) {
        message.success('微信公众号菜单更新成功');
        setWechatMenuModalVisible(false);
        setSelectedMenu('');
      } else {
        message.error(result.msg || '更新微信公众号菜单失败');
      }
    } catch (error) {
      console.error('更新微信公众号菜单失败:', error);
      message.error('网络错误，无法更新微信公众号菜单');
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('Authorization');
    window.location.href = '/admin/login';
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
            <Card className="action-panel" title="系统操作">
              <div className="action-list">
                {/* 修改开学日期 */}
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

                {/* 更新专业信息 */}
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

                {/* 更新空教室 */}
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

                {/* 切换微信公众号菜单 */}
                <div className="action-item">
                  <div className="action-icon action-icon-orange">
                    <WechatOutlined />
                  </div>
                  <div className="action-info">
                    <span className="action-title">切换微信公众号菜单</span>
                    <span className="action-desc">管理公众号底部菜单</span>
                  </div>
                  <div className="action-control">
                    <Button type="primary" onClick={handleOpenWechatMenuModal} size="small">
                      去切换
                    </Button>
                  </div>
                </div>

                {/* 微信素材上传（校历/红旗地图/裕华地图） */}
                {mediaUploadTypes.map(({ type, title, desc, iconColor }) => (
                  <div className="action-item" key={type} style={{ flexWrap: 'wrap' }}>
                    <div className={`action-icon action-icon-${iconColor}`}>
                      <PictureOutlined />
                    </div>
                    <div className="action-info">
                      <span className="action-title">{title}</span>
                      <span className="action-desc">{desc}</span>
                    </div>
                    <div className="action-control">
                      <div className="calender-upload-group">
                        {uploadStates[type].preview && (
                          <img src={uploadStates[type].preview} alt="预览" className="calender-preview" />
                        )}
                        <input
                          ref={uploadRefs[type]}
                          type="file"
                          accept="image/jpeg,image/jpg,image/png"
                          onChange={(e) => handleMediaUpload(type, e)}
                          style={{ display: 'none' }}
                        />
                        <Button
                          type="primary"
                          onClick={() => triggerMediaUpload(type)}
                          loading={uploadStates[type].uploading}
                          size="small"
                        >
                          {uploadStates[type].preview ? '重新上传' : '选择图片'}
                        </Button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
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
          <Modal
            title="切换微信公众号菜单"
            visible={wechatMenuModalVisible}
            onCancel={handleWechatMenuCancel}
            footer={[
              <Button key="cancel" onClick={handleWechatMenuCancel}>取消</Button>,
              <Button key="submit" type="primary" onClick={handleWechatMenuSubmit}>
                确定
              </Button>
            ]}
          >
            <Form layout="vertical">
              <Form.Item
                label="请选择菜单类型"
                rules={[{ required: true, message: '请选择菜单类型' }]}
              >
                <Select
                  placeholder="请选择菜单类型"
                  value={selectedMenu}
                  onChange={(value) => setSelectedMenu(value)}
                >
                  <Select.Option value="开学">开学</Select.Option>
                  <Select.Option value="假期">假期</Select.Option>
                  <Select.Option value="迎新">迎新</Select.Option>
                </Select>
              </Form.Item>
            </Form>
          </Modal>
        </Spin>
      </Content>
    </Layout>
  );
}