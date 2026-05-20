import React, { useState, useEffect } from 'react';
import { Card, List, Skeleton, Layout, Button, message, Avatar, Collapse, Modal } from 'antd';
import { useHistory } from 'react-router-dom';
import './UserProfile.css';
import { Statistic, Row, Col, Progress } from 'antd';
import { API_PATHS } from '../constants/api';
import BottomNav from '../components/BottomNav';

const { Header } = Layout;

export default function UserProfile() {
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const history = useHistory();
  const [studySituation, setStudySituation] = useState(null);
  const [studyLoading, setStudyLoading] = useState(true);
  const [bindingModalVisible, setBindingModalVisible] = useState(false);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        // 在fetch请求中添加CORS模式配置
        const response = await fetch(API_PATHS.STUDENT_INFO, {
          // 显式声明CORS模式
          headers: {
            'token': `${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        });
        const result = await response.json();
        setUserInfo(result.data);
      } catch (error) {
        console.error('获取用户信息失败:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchUserInfo();

    const fetchStudySituation = async () => {
      try {
        setStudyLoading(true);
        const response = await fetch(API_PATHS.UPDATE_GPA, {
          method: 'GET',
          headers: {
            'token': `${localStorage.getItem('token')}`,
            'Content-Type': 'application/json'
          }
        });
        const result = await response.json();
        setStudySituation(result.data);
      } catch (error) {
        setStudySituation(null);
      } finally {
        setStudyLoading(false);
      }
    };
    fetchStudySituation();
  }, []);

  console.log('userInfo:', userInfo); // 调试用，查看userInfo内容
  const infoItems = [
    {
      type: 'combined',
      name: userInfo?.name,
      sid: userInfo?.sid
    },
    { label: '学院', value: userInfo?.collegeName },
    { label: '专业', value: userInfo?.majorName },
    { label: '班级', value: userInfo?.className },
    { label: '学籍状态', value: userInfo?.status },
    { label: '政治面貌', value: userInfo?.politicsStatus },
    { label: '民族', value: userInfo?.nationality },
    { label: '出生日期', value: userInfo?.birthday },
    { label: '户籍地址', value: userInfo?.domicile }
  ];

  return (
    <Layout className="dashboard-container">
      <Header>
        <div className="header-content" style={{
          display: 'flex',
          justifyContent: 'center',
          position: 'relative',
          padding: '0 16px'
        }}>
          <h1 style={{ margin: 0 }}>个人信息</h1>
        </div>
      </Header>
      <div className="profile-container">
        {/* 学号信息卡片 */}
        <Card bordered={false} style={{ marginBottom: 24 }}>
          <Skeleton loading={loading} active>
            <List>
              <List.Item>
                <div className="info-item">
                  <div style={{ display: 'flex', alignItems: 'center', gap: 16, height: 1 }}>
                    <Avatar
                      size={48}
                      style={{
                        backgroundColor: '#1890ff',
                        flexShrink: 0,
                        fontSize: 24,
                        lineHeight: '48px'
                      }}>
                      {infoItems[0].name?.charAt(0) || '未'}
                    </Avatar>
                    <div>
                      <div style={{ fontSize: 22, fontWeight: 500, marginLeft: 45 }}>{infoItems[0].sid}</div>
                    </div>
                  </div>
                </div>
              </List.Item>
            </List>
            <div style={{ marginTop: 16, textAlign: 'center' }}>
              <Button 
                type="primary" 
                onClick={() => setBindingModalVisible(true)}
                style={{ backgroundColor: '#1890ff', borderColor: '#1890ff' }}
              >
                查看绑定码
              </Button>
            </div>
          </Skeleton>
        </Card>

        {/* 学习情况卡片 */}
        <Card
          title="学习情况"
          bordered={false}
          style={{ marginBottom: 24 }}
        >
          <Skeleton loading={studyLoading} active>
            {studySituation && (
              <>
                <Row gutter={[16, 16]}>
                  <Col span={8}>
                    <Statistic
                      title="GPA"
                      value={Number(studySituation.gpa)}
                      precision={2}
                      valueStyle={{
                        color: '#3f8600',
                        fontSize: '16px'  // 调整数值大小
                      }}
                      titleStyle={{ fontSize: '14px' }}  // 调整标题大小
                    />
                  </Col>
                  <Col span={8}>
                    <Statistic
                      title="计划课程数"
                      value={Number(studySituation.planCourse)}
                      valueStyle={{ fontSize: '16px' }}
                      titleStyle={{ fontSize: '14px' }}
                    />
                  </Col>
                  <Col span={8}>
                    <Statistic
                      title="已通过课程"
                      value={Number(studySituation.passPlanCourse)}
                      valueStyle={{
                        color: '#3f8600',
                        fontSize: '16px'
                      }}
                      titleStyle={{ fontSize: '14px' }}
                    />
                  </Col>
                </Row>
                <div style={{ marginTop: 24 }}>
                  <div style={{
                    marginBottom: 8,
                    fontSize: '14px'  // 调整进度条标题大小
                  }}>
                    河北师大副本闯关进度
                  </div>
                  <Progress
                    percent={
                      studySituation.planCourse && studySituation.passPlanCourse
                        ? Math.round(Number(studySituation.passPlanCourse) / Number(studySituation.planCourse) * 100)
                        : 0
                    }
                    status="active"
                    strokeColor={{
                      '0%': '#108ee9',
                      '100%': '#87d068',
                    }}
                    strokeWidth={8}  // 调整进度条粗细
                  />
                </div>
                <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
                  <Col span={24}>
                    <Card size="small" title="未通过课程" headStyle={{ fontSize: '14px' }}>
                      <Statistic
                        value={Number(studySituation.failPlanCourse)}
                        valueStyle={{
                          color: '#cf1322',
                          fontSize: '14px'  // 从16px调整为14px
                        }}
                      />
                    </Card>
                  </Col>
                  <Col span={24}>
                    <Card size="small" title="未修课程" titleStyle={{ fontSize: '1px' }}>
                      <Statistic
                        value={Number(studySituation.unstudyPlanCourse)}
                        valueStyle={{
                          color: '#faad14',
                          fontSize: '16px'
                        }}
                      />
                    </Card>
                  </Col>
                  <Col span={24}>
                    <Card size="small" title="修读中课程" headStyle={{ fontSize: '14px' }}>
                      <Statistic
                        value={Number(studySituation.studyingPlanCourse)}
                        valueStyle={{
                          color: '#1890ff',
                          fontSize: '16px'
                        }}
                      />
                    </Card>
                  </Col>
                  <Col span={24}>
                    <Card size="small" title="计划外通过课程" titleStyle={{ fontSize: '1px' }}>
                      <Statistic
                        value={Number(studySituation.outPlanPassCourse)}
                        valueStyle={{
                          color: '#faad14',
                          fontSize: '16px'
                        }}
                      />
                    </Card>
                  </Col>
                  <Col span={24}>
                    <Card size="small" title="计划外未通过" titleStyle={{ fontSize: '1px' }}>
                      <Statistic
                        value={Number(studySituation.outPlanFailCourse)}
                        valueStyle={{
                          color: '#faad14',
                          fontSize: '16px'
                        }}
                      />
                    </Card>
                  </Col>
                </Row>
              </>
            )}
          </Skeleton>
        </Card>

       

        {/* 新增更新GPA按钮 */}
        <div style={{ margin: '16px 0', textAlign: 'center' }}>
          <Button
            type="link"
            onClick={async () => {
              try {
                message.info('该功能请求极慢，建议使用成绩计算器计算GPA...', 5); // 添加提示信息，5秒后自动消失
                setStudyLoading(true);
                const response = await fetch(API_PATHS.UPDATE_GPA, {
                  method: 'PUT',
                  headers: {
                    'token': localStorage.getItem('token'),
                    'Content-Type': 'application/json'
                  }
                });
                const result = await response.json();
                if (result.code === 1) {
                  message.success('学习情况更新成功');
                  // 刷新学习情况数据
                  const refreshResponse = await fetch(API_PATHS.UPDATE_GPA, {
                    method: 'GET',
                    headers: {
                      'token': localStorage.getItem('token'),
                      'Content-Type': 'application/json'
                    }
                  });
                  const refreshResult = await refreshResponse.json();
                  setStudySituation(refreshResult.data);
                } else {
                  message.error(result.msg || '更新失败');
                }
              } catch (error) {
                message.error('请求失败');
              } finally {
                setStudyLoading(false);
              }
            }}
            style={{ color: '#1890ff' }}
            loading={studyLoading}
            disabled={studyLoading}
          >
            更新学习情况
          </Button>
        </div>

        <div style={{ marginTop: 24, textAlign: 'center' }}>
          <Button
            type="primary"
            danger
            onClick={() => {
              localStorage.removeItem('token');
              localStorage.removeItem('studentInfo');
              message.success('已退出登录');
              history.push('/');
            }}
          >
            退出登录
          </Button>
        </div>
      </div>
      <BottomNav />
      
      <Modal
        title="绑定码管理"
        visible={bindingModalVisible}
        onCancel={() => setBindingModalVisible(false)}
        footer={null}
        centered
        width={400}
      >
        <div style={{ textAlign: 'center', padding: '20px 0' }}>
          <div style={{ marginBottom: 16 }}>
            <strong>您的绑定码：</strong>
          </div>
          <div style={{ 
            backgroundColor: '#f5f5f5', 
            padding: '16px', 
            borderRadius: '8px',
            fontSize: '20px',
            fontWeight: 'bold',
            fontFamily: 'monospace',
            marginBottom: 24,
            wordBreak: 'break-all'
          }}>
            {userInfo?.bindingKey || '绑定码为空，请重新登录后查看'}
          </div>
          
          <div style={{ display: 'flex', gap: 12, justifyContent: 'center' }}>
            <Button
              type="primary"
              onClick={() => {
                if (userInfo?.bindingKey) {
                  // 创建一个临时的textarea元素
                  const textarea = document.createElement('textarea');
                  textarea.value = '绑定 ' + userInfo.bindingKey;
                  textarea.style.position = 'fixed';
                  textarea.style.opacity = '0';
                  document.body.appendChild(textarea);
                  textarea.select();
                  
                  try {
                    // 尝试使用现代的clipboard API
                    navigator.clipboard.writeText('绑定 ' + userInfo.bindingKey)
                      .then(() => message.success('绑定码已复制到剪贴板'))
                      .catch(() => {
                        // 如果现代API失败，回退到document.execCommand
                        const successful = document.execCommand('copy');
                        if (successful) {
                          message.success('绑定码已复制到剪贴板');
                        } else {
                          message.error('复制失败，请手动复制');
                        }
                      });
                  } catch (err) {
                    // 如果navigator.clipboard.writeText抛出异常，也回退到document.execCommand
                    try {
                      const successful = document.execCommand('copy');
                      if (successful) {
                        message.success('绑定码已复制到剪贴板');
                      } else {
                        message.error('复制失败，请手动复制');
                      }
                    } catch (execErr) {
                      message.error('复制失败，请手动复制');
                    }
                  } finally {
                    // 清理临时元素
                    document.body.removeChild(textarea);
                  }
                } else {
                  message.warning('暂无绑定码可复制');
                }
              }}
            >
              一键复制
            </Button>
            <Button
              type="default"
              danger
              onClick={() => {
                Modal.confirm({
                  title: '确认解绑',
                  content: '确定要解绑当前账号吗？解绑后需要重新绑定才能使用相关功能。',
                  okText: '确认解绑',
                  cancelText: '取消',
                  okType: 'danger',
                  centered: true,
                  async onOk() {
                    try {
                      const response = await fetch(API_PATHS.UNBIND, {
                        method: 'PUT',
                        headers: {
                          'token': `${localStorage.getItem('token')}`,
                          'Content-Type': 'application/json'
                        }
                      });
                      const result = await response.json();
                      
                      if (result.code === 1) {
                        message.success('解绑成功');
                        // 刷新用户信息
                        const userResponse = await fetch(API_PATHS.STUDENT_INFO, {
                          headers: {
                            'token': `${localStorage.getItem('token')}`,
                            'Content-Type': 'application/json'
                          }
                        });
                        const userResult = await userResponse.json();
                        setUserInfo(userResult.data);
                      } else {
                        message.error(result.message || '解绑失败');
                      }
                    } catch (error) {
                      console.error('解绑失败:', error);
                      message.error('解绑失败，请重试');
                    }
                    setBindingModalVisible(false);
                  },
                  onCancel() {
                    // 取消解绑，不做任何操作
                  },
                });
              }}
            >
              解绑
            </Button>
          </div>
        </div>
      </Modal>
    </Layout>
  );
}