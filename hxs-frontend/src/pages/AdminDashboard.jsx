import React, { useState, useEffect } from 'react';
import { Layout, Card, Row, Col, Table, Statistic, Spin, message, Typography, Modal, Form, DatePicker, Input, Button, Select } from 'antd';
import { UserOutlined, LoginOutlined, TeamOutlined } from '@ant-design/icons';
import { API_PATHS } from '../constants/api';
import { authFetch, saveToken } from '../utils/request';
import moment from 'moment';
import './AdminDashboard.css';


const { Header, Content } = Layout;
const { Title } = Typography;

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
      const response = await authFetch(`${API_PATHS.GET_CURRENT_TERM_START_DATE}?id=${dateType}&_t=${timestamp}`);
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
        method: 'PUT',
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
    },
  ];

  return (
    <Layout className="admin-dashboard">
      <Header style={{ background: '#fff', padding: '0 24px', boxShadow: '0 2px 8px rgba(0,0,0,0.06)' }}>
        <div style={{ display: 'flex', alignItems: 'center', height: '100%' }}>
          <Title level={4} style={{ margin: 0 }}>管理系统仪表盘</Title>
        </div>
      </Header>

      <Content style={{ padding: '24px' }}>
        <Spin spinning={loading} tip="加载中...">
          {/* 统计卡片区域 */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Statistic
                  title="总用户数"
                  value={userCount}
                  prefix={<UserOutlined style={{ color: '#1890ff' }} />
                  }
                  valueStyle={{ fontSize: 32 }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Statistic
                  title="24H内登录数"
                  value={todayLoginCount}
                  prefix={<LoginOutlined style={{ color: '#52c41a' }} />
                  }
                  valueStyle={{ fontSize: 32 }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Statistic
                  title="最近7天登录数"
                  value={sevenDayLoginCount}
                  prefix={<LoginOutlined style={{ color: '#52c41a' }} />
                  }
                  valueStyle={{ fontSize: 32 }}
                />
              </Card>
            </Col>

            <Col xs={24} sm={24} lg={8}>
              <Card>
                <Statistic
                  title="学院数量"
                  value={userDistribution.filter(item => item.college).length}
                  prefix={<TeamOutlined style={{ color: '#fa8c16' }} />
                  }
                  valueStyle={{ fontSize: 32 }}
                />
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Button
                  type="primary"
                  onClick={() => handleOpenModal(termDateType)}
                  style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  修改开学日期
                </Button>
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Button
                  type="primary"
                  onClick={handleUpdateMajorInfoWithConfirm}
                  style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  更新专业信息
                </Button>
              </Card>
            </Col>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', alignItems: 'center' }}>
                  <Input
                    type="number"
                    placeholder="请输入第几周"
                    value={updateWeek}
                    onChange={(e) => setUpdateWeek(e.target.value)}
                    min="1"
                    style={{ width: '100%' }}
                  />
                  <Button
                    type="primary"
                    onClick={handleUpdateEmptyClassroom}
                    loading={updateWeekLoading}
                    style={{ width: '100%' }}
                  >
                    更新空教室
                  </Button>
                </div>
              </Card>
            </Col>
          </Row>

          {/* 微信公众号菜单切换卡片 */}
          <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
            <Col xs={24} sm={12} lg={8}>
              <Card>
                <Button
                  type="primary"
                  onClick={handleOpenWechatMenuModal}
                  style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}
                >
                  切换微信公众号菜单
                </Button>
              </Card>
            </Col>
          </Row>

          {/* 用户分布表格 */}
          <Card title="用户学院分布">
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