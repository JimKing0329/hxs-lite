import React from 'react';
import { Layout, Card, List, Skeleton, Button, message, Modal, Table, Typography } from 'antd';
import { EllipsisOutlined } from '@ant-design/icons';
import { Dropdown, Menu } from 'antd';
import './Dashboard.css';
import { Collapse } from 'antd';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { authFetch, isAuthenticated } from '../utils/request';
import BottomNav from '../components/BottomNav';

const { Header, Content } = Layout;
const { Panel } = Collapse;

import { useState, useEffect } from 'react';
import { Spin } from 'antd';

export default function Dashboard() {
  const history = useHistory();
  const [courses, setCourses] = useState([]);
  const [tomorrowCourses, setTomorrowCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tomorrowLoading, setTomorrowLoading] = useState(true);
  const [updating, setUpdating] = useState(false);
  const [updateScoreLoading, setUpdateScoreLoading] = useState(false);
  const [updateExamLoading, setUpdateExamLoading] = useState(false);
  const [exams, setExams] = useState([]);
  const [examsLoading, setExamsLoading] = useState(true);
  const [scoreDetailVisible, setScoreDetailVisible] = useState(false);
  const [scoreDetailLoading, setScoreDetailLoading] = useState(false);
  const [scoreDetail, setScoreDetail] = useState(null);
  const [grades, setGrades] = useState([]);
  const [gradesLoading, setGradesLoading] = useState(true);
  // 导航到GPA计算器页面
  const navigateToGPACalculator = () => {
    history.push('/gpa-calculator');
  };

  // Add back the fetchCourses effect
  useEffect(() => {
    if (!isAuthenticated()) return;

    const fetchCourses = async () => {
      try {
        const [todayRes, tomorrowRes] = await Promise.all([
          authFetch(API_PATHS.TODAY_COURSE),
          authFetch(API_PATHS.TOMORROW_COURSE)
        ]);

        const todayData = await todayRes.json();
        const tomorrowData = await tomorrowRes.json();

        if (todayData.code === 1) setCourses(todayData.data);
        if (tomorrowData.code === 1) setTomorrowCourses(tomorrowData.data);
      } catch (error) {
        console.error('获取课表失败:', error);
      } finally {
        setLoading(false);
        setTomorrowLoading(false);
      }
    };

    fetchCourses();
  }, [isAuthenticated]);

  // 获取成绩对应的颜色
  const getGradeColor = (grade) => {
    const num = parseInt(grade);
    if (isNaN(num)) return '#45A3F5'; // 非数字成绩（如优秀）显示为蓝色
    if (num >= 60) return '#45A3F5'; // 及格 - 橙色
    return '#f5222d'; // 不及格 - 红色
  };



  // 获取分项成绩详情
  const fetchScoreDetail = async (courseName, classId, year, term) => {
    // 先设置课程基本信息并打开弹窗，显示加载状态
    const courseInfo = grades.find(g =>
      g.course === courseName &&
      g.classId === classId &&
      g.year === year &&
      g.term === term
    );

    setScoreDetail({
      courseName: courseName,
      teacherName: courseInfo?.teacherName,
      credit: courseInfo?.credit,
      gradePoint: courseInfo?.gradePoint,
      scoreDetails: null
    });
    setScoreDetailVisible(true);
    setScoreDetailLoading(true);

    // 等待弹窗渲染完成
    await new Promise(resolve => setTimeout(resolve, 50));

    try {
      const params = new URLSearchParams({ courseName, classId, year, term });
      const response = await authFetch(`${API_PATHS.SCORE.DETAIL}?${params}`);
      const result = await response.json();
      if (result.code === 1) {
        setScoreDetail(prev => ({
          ...prev,
          scoreDetails: result.data.items
        }));
      } else {
        message.error(result.msg || '获取成绩详情失败');
        setScoreDetailVisible(false);
      }
    } catch (error) {
      console.error('获取成绩详情失败:', error);
      message.error('获取成绩详情失败');
      setScoreDetailVisible(false);
    } finally {
      setScoreDetailLoading(false);
    }
  };

  // Add back the grades effect
  useEffect(() => {
    if (!isAuthenticated()) return;

    const fetchGrades = async () => {
      try {

        // 在请求URL中添加参数
        const response = await authFetch(`${API_PATHS.SCORE.LIST}?year=-114&term=-514`);
        const result = await response.json();
        if (result.code === 1) {
          setGrades(result.data.map(item => ({
            course: item.courseName,
            score: item.grade,
            classId: item.classId,
            year: item.year,
            term: item.term,
            teacherName: item.teacherName,
            credit: item.credit,
            gradePoint: item.gradePoint
          })));
        }
      } catch (error) {
        console.error('获取成绩失败:', error);
      } finally {
        setGradesLoading(false);
      }
    };

    fetchGrades();
  }, [isAuthenticated]);

  // Add back the exams effect
  useEffect(() => {
    if (!isAuthenticated()) return;

    const fetchExams = async () => {
      try {
        const response = await authFetch(API_PATHS.EXAM.LIST);
        const result = await response.json();
        if (result.code === 1) {
          // 适配新的字段名
          setExams(result.data.map(item => ({
            title: item.courseName,
            examTime: item.examTime,
            examMethod: item.examForm,
            campus: item.examPlace,  // 考试地点
            location: '',
            seat: item.seatNo,
            examName: ''
          })));
        }
      } catch (error) {
        console.error('获取考试信息失败:', error);
      } finally {
        setExamsLoading(false);
      }
    };

    fetchExams();
  }, [isAuthenticated]);

  if (!isAuthenticated()) {
    return null;
  }

  // 添加加载状态优化
  const [initialLoading, setInitialLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated()) return;

    const fetchInitialData = async () => {
      try {
        // 并行请求改为分批请求或优先级请求
        const todayRes = await authFetch(API_PATHS.TODAY_COURSE);
        
        const todayData = await todayRes.json();
        if (todayData.code === 1) setCourses(todayData.data);
        
        // 设置初始加载完成
        setInitialLoading(false);
        
        // 后续请求可以异步进行
        fetchRemainingData();
      } catch (error) {
        console.error('获取初始数据失败:', error);
        setInitialLoading(false);
      }
    };

    const fetchRemainingData = async () => {
      // 获取其他数据的函数
      // ...
    };

    fetchInitialData();
  }, [isAuthenticated]);

  // 在渲染部分添加
  if (initialLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return (
    <Layout className="dashboard-container">
                  <Header>
                <div className="header-content" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    position: 'relative',
                    padding: '0 16px'
                }}>
                    <h1 style={{ margin: 0 }}>河小狮lite</h1>
                    <Dropdown
                      overlay={
                        <Menu>
                          <Menu.Item key="gpa-calculator" onClick={navigateToGPACalculator}>
                            成绩计算器
                          </Menu.Item>
                          <Menu.Item key="failed-courses" onClick={() => history.push('/fail-rate-rank')}>
                            挂科排行
                          </Menu.Item>
                          <Menu.Item key="textbook-query" onClick={() => history.push('/textbook-query')}>
                            教材查询
                          </Menu.Item>
                        </Menu>
                      }
                    >
                      <Button icon={<EllipsisOutlined />} size="small" style={{ position: 'absolute', right: '-25px', top: '20px' }} />
                    </Dropdown>
                </div>
                
            </Header>
      <Content>
        <div style={{ marginBottom: 24 }}>
          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>今日课表</span>
                <div>
                  <Button
                    type="link"
                    size="small"
                    onClick={() => history.push('/course-table')}
                    style={{ marginRight: 8 }}
                  >
                    全部课表
                  </Button>
                  <Button
                    type="link"
                    size="small"
                    loading={updating}
                    onClick={async () => {
                      try {
                        setUpdating(true);
                        const response = await authFetch(API_PATHS.UPDATE_COURSE_TABLE, {
                          method: 'PUT',
                          headers: { 'Content-Type': 'application/json' }
                        });
                        const result = await response.json();
                        if (result.code === 1) {
                          message.success('课表更新成功');
                          // 更新后重新获取课表
                          const [todayRes, tomorrowRes] = await Promise.all([
                            authFetch(API_PATHS.TODAY_COURSE),
                            authFetch(API_PATHS.TOMORROW_COURSE)
                          ]);

                          const todayData = await todayRes.json();
                          const tomorrowData = await tomorrowRes.json();

                          if (todayData.code === 1) setCourses(todayData.data);
                          if (tomorrowData.code === 1) setTomorrowCourses(tomorrowData.data);
                        } else {
                          message.error(result.msg || '更新失败');
                        }
                      } catch (error) {
                        console.error('更新课表失败:', error);
                        message.error('更新课表失败');
                      } finally {
                        setUpdating(false);
                      }
                    }}
                  >
                    更新课表
                  </Button>
                </div>
              </div>
            }
            bordered={false}
            className="collapse-panel"
          >
            <Skeleton loading={loading} active>
              {courses.length === 0 && !loading ? (
                <div style={{ textAlign: 'center', padding: 16, color: 'rgba(0,0,0,0.45)' }}>
                  今天没有课程喔~
                </div>
                
              ) : (
                <List
                  dataSource={courses}
                  renderItem={item => (
                    <List.Item>
                      <List.Item.Meta
                        title={`${item.title} - ${item.teacher}`}
                        description={
                          <div>
                            <div>{`星期${['日', '一', '二', '三', '四', '五', '六'][item.weekday]} 第${item.startSession}-${item.endSession}节`}</div>
                            <div>{`${item.campus} · ${item.place}`}</div>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              )}
            </Skeleton>
          </Card>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          <Card
            title="明日课表"
            bordered={false}
            className="collapse-panel"
          >
            <Skeleton loading={tomorrowLoading} active>
              {tomorrowCourses.length === 0 && !tomorrowLoading ? (
                <div style={{ textAlign: 'center', padding: 16, color: 'rgba(0,0,0,0.45)' }}>
                  明天没有课程啦~
                </div>
              ) : (
                <List
                  dataSource={tomorrowCourses}
                  renderItem={item => (
                    <List.Item>
                      <List.Item.Meta
                        title={`${item.title} - ${item.teacher}`}
                        description={
                          <div>
                            <div>{`星期${['日', '一', '二', '三', '四', '五', '六'][item.weekday]} 第${item.startSession}-${item.endSession}节`}</div>
                            <div>{`${item.campus} · ${item.place}`}</div>
                          </div>
                        }
                      />
                    </List.Item>
                  )}
                />
              )}
            </Skeleton>
          </Card>

          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>最新成绩<br /><Typography.Text type="secondary" style={{ fontSize: 12 }}>(点击成绩查看详情)</Typography.Text></span>
                <div>
                  <Button
                    type="link"
                    size="small"
                    onClick={() => history.push('/all-scores')}
                  >
                    全部成绩
                  </Button>
                  <Button
                    type="link"
                    size="small"
                    loading={updateScoreLoading}
                    onClick={async () => {
                      try {
                        setUpdateScoreLoading(true);
                        const response = await authFetch(API_PATHS.SCORE.REFRESH, {
                          method: 'PUT',
                          headers: { 'Content-Type': 'application/json' }
                        });
                        const result = await response.json();
                        if (result.code === 1) {
                          message.success('成绩更新成功');
                          // 更新后重新获取成绩
                          const gradesRes = await authFetch(`${API_PATHS.SCORE.LIST}?year=-114&term=-514`);
                          const gradesData = await gradesRes.json();
                          if (gradesData.code === 1) {
                            setGrades(gradesData.data.map(item => ({
                              course: item.courseName,
                              score: item.grade,
                              classId: item.classId,
                              year: item.year,
                              term: item.term,
                              teacherName: item.teacherName,
                              credit: item.credit,
                              gradePoint: item.gradePoint
                            })));
                          }
                        } else {
                          message.error(result.msg || '更新失败');
                        }
                      } catch (error) {
                        message.error('请求失败');
                      } finally {
                        setUpdateScoreLoading(false);
                      }
                    }}
                  >
                    更新成绩
                  </Button>
                </div>
              </div>
            }
            bordered={false}
            className="collapse-panel"
          >
            <Skeleton loading={gradesLoading} active>
              {grades.length === 0 && !gradesLoading ? (
                <div style={{ textAlign: 'center', padding: 16, color: 'rgba(0,0,0,0.45)' }}>
                  本学期成绩暂无，看看全部成绩吧！
                </div>
              ) : (
                <List
                  dataSource={grades}
                  renderItem={item => (
                    <List.Item
                      style={{ display: 'flex', justifyContent: 'space-between', cursor: 'pointer' }}
                      onClick={() => fetchScoreDetail(item.course, item.classId, item.year, item.term)}
                    >
                      <span style={{
                        maxWidth: '60%',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {item.course}
                      </span>
                      <span className="score-badge" style={{
                        flexShrink: 0,
                        padding: '0 8px',
                        borderRadius: 4,
                        backgroundColor: getGradeColor(item.score)
                      }}>
                        {item.score}
                      </span>
                    </List.Item>
                  )}
                />
              )}
            </Skeleton>
          </Card>

          <Card
            title={
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span>考试安排</span>
                <Button
                  type="link"
                  size="small"
                  loading={updateExamLoading}
                  onClick={async () => {
                    try {
                      setUpdateExamLoading(true);
                      const response = await authFetch(API_PATHS.EXAM.REFRESH, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json' }
                      });
                      const result = await response.json();
                      if (result.code === 1) {
                        message.success('考试信息更新成功');
                        // 更新后重新获取考试信息
                        const examsRes = await authFetch(API_PATHS.EXAM.LIST);
                        const examsData = await examsRes.json();
                        if (examsData.code === 1) {
                          setExams(examsData.data.map(item => ({
                            title: item.courseName,
                            examTime: item.examTime,
                            examMethod: item.examForm,
                            campus: item.examPlace,
                            location: '',
                            seat: item.seatNo,
                            examName: ''
                          })));
                        }
                      } else {
                        message.error(result.msg || '更新失败');
                      }
                    } catch (error) {
                      message.error('请求失败');
                    } finally {
                      setUpdateExamLoading(false);
                    }
                  }}
                >
                  更新考试安排
                </Button>
              </div>
            }
            variant="outlined"  // 替换 bordered={false}或bordered
            className="collapse-panel"
          >
            <Skeleton loading={examsLoading} active>
              {exams.length === 0 && !examsLoading ? (
                <div style={{ textAlign: 'center', padding: 16, color: 'rgba(0,0,0,0.45)' }}>
                  暂无考试安排
                </div>
              ) : (
                <List
                  dataSource={exams}
                  renderItem={item => (
                    <List.Item>
                      <div style={{ width: '100%', lineHeight: '1.6' }}>
                        <div style={{ fontWeight: 'bold', marginBottom: 8 }}>{item.title}</div>
                        <div style={{ marginBottom: 6 }}>考试时间: {item.examTime}</div>
                        <div style={{ marginBottom: 6 }}>考试方式: {item.examMethod}</div>
                        <div style={{ marginBottom: 6 }}>地点: {item.campus} {item.location}</div>
                        <div style={{ marginBottom: 6 }}>座位号: {item.seat}</div>
                        <div style={{ color: '#888', marginTop: 8 }}>{item.examName}</div>
                      </div>
                    </List.Item>
                  )}
                />
              )}
            </Skeleton>
          </Card>
        </div>
      </Content>

      {/* 添加成绩详情模态框 */}
      <Modal
        title={scoreDetail?.courseName || '成绩详情'}
        open={scoreDetailVisible}
        onCancel={() => setScoreDetailVisible(false)}
        footer={null}
        width={600}
        className="score-detail-modal"
        maskStyle={{
          backdropFilter: 'blur(8px)',
          backgroundColor: 'rgba(0, 0, 0, 0.45)'
        }}
        bodyStyle={{
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderRadius: '8px',
          minHeight: '200px'
        }}
      >
        <Spin
          spinning={scoreDetailLoading}
          tip="加载中..."
          style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '150px'
          }}
        >
          {scoreDetail && scoreDetail.scoreDetails && (
            <div className="score-detail-content">
              <div style={{ marginBottom: 16 }}>
                <div>
                  <Typography.Text strong>教师: </Typography.Text>
                  <Typography.Text>{scoreDetail.teacherName || '未知'}</Typography.Text>
                </div>
                <div>
                  <Typography.Text strong>学分: </Typography.Text>
                  <Typography.Text>{scoreDetail.credit || '未知'}</Typography.Text>
                </div>

                <div>
                  <Typography.Text strong>绩点: </Typography.Text>
                  <Typography.Text>{scoreDetail.gradePoint || '未知'}</Typography.Text>
                </div>

              </div>
              <Table
                dataSource={scoreDetail.scoreDetails}
                pagination={false}
                rowKey="scoreColumn"
                columns={[
                  {
                    title: '成绩分项',
                    dataIndex: 'scoreColumn',
                    key: 'scoreColumn',
                    width: '50%'
                  },
                  {
                    title: '比例',
                    dataIndex: 'scoreRatio',
                    key: 'scoreRatio',
                    width: '20%'
                  },
                  {
                    title: '成绩',
                    dataIndex: 'score',
                    key: 'score',
                    width: '30%',
                    render: (text) => (
                      <span style={{ color: '#1890ff', fontWeight: 'bold' }}>{text}</span>
                    )
                  }
                ]}
              />
            </div>
          )}
        </Spin>
      </Modal>

      <BottomNav />
    </Layout>
  );
}
