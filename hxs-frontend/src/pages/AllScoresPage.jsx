import React, { useState, useEffect } from 'react';
import { Layout, Card, List, Skeleton, Typography, Modal, Divider, Collapse, message, Tag, Table, Button, Tooltip, Spin } from 'antd';
import { ArrowLeftOutlined, QuestionCircleOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { authFetch, isAuthenticated as checkAuth } from '../utils/request';
import './AllScoresPage.css';

const { Header, Content } = Layout;
const { Title, Text } = Typography;
const { Panel } = Collapse;

export default function ScorePage() {
  const history = useHistory();
  const [scores, setScores] = useState([]);
  const [currentTermKey, setCurrentTermKey] = useState('');
  const [loading, setLoading] = useState(true);
  const [scoreDetailLoading, setScoreDetailLoading] = useState(false);
  const [scoreDetailVisible, setScoreDetailVisible] = useState(false);
  const [scoreDetail, setScoreDetail] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(true);
  const [ranking, setRanking] = useState({ rank: 0, total: 0, items: [] });
  const [rankingLoading, setRankingLoading] = useState(false);
  const [rankingModalVisible, setRankingModalVisible] = useState(false);
  const [mySid, setMySid] = useState('');
  const [rankingFlag, setRankingFlag] = useState(false);

  // 获取当前学期的函数
  const getCurrentTerm = () => {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;

    if (month <= 7) {
      return `${year - 1}-${year}/2`; // 第二学期
    } else {
      return `${year}-${year + 1}/1`; // 第一学期
    }
  };

  // 认证检查
  useEffect(() => {
    if (!checkAuth()) {
      setIsAuthenticated(false);
      history.replace('/');
    }
  }, [history]);

  // 获取成绩数据
  useEffect(() => {
    if (!isAuthenticated) return;

    const fetchScores = async () => {
      try {
        const response = await authFetch(API_PATHS.SCORE.LIST);

        const result = await response.json();
        if (result.code === 1 && result.data) {
          // 按学年学期分组数据
          const groupedData = groupByTerm(result.data);
          const currentTerm = getCurrentTerm();
          setCurrentTermKey(currentTerm);
          setScores(groupedData);
          // 获取学号
          if (result.data.length > 0) {
            setMySid(result.data[0].sid);
          }
          // 获取排名数据
          fetchRanking(rankingFlag);
        } else {
          message.error(result.msg || '获取成绩失败');
        }
      } catch (error) {
        console.error('获取成绩出错:', error);
        message.error('获取成绩出错');
      } finally {
        setLoading(false);
      }
    };

    fetchScores();
  }, [isAuthenticated]);

  // 获取排名数据
  const fetchRanking = async (flag = false) => {
    try {
      setRankingLoading(true);
      const response = await authFetch(`${API_PATHS.SCORE.RANKING}?flag=${flag}`);

      const result = await response.json();
      if (result.code === 1 && result.data) {
        setRanking(result.data);
      } else {
        message.error(result.msg || '获取排名失败');
      }
    } catch (error) {
      console.error('获取排名出错:', error);
      message.error('获取排名出错');
    } finally {
      setRankingLoading(false);
    }
  };

  // 按学年学期分组数据
  const groupByTerm = (data) => {
    const grouped = {};

    data.forEach(item => {
      // 构建学期键: "2023-2024/1"
      const termKey = `${item.year}-${item.year + 1}/${item.term}`;

      if (!grouped[termKey]) {
        grouped[termKey] = [];
      }
      grouped[termKey].push(item);
    });

    // 转换为数组并按学期排序
    return Object.keys(grouped)
      .sort((a, b) => {
        // 解析学年学期，例如 "2023-2024/1"
        const [yearA, termA] = a.split('/');
        const [yearB, termB] = b.split('/');

        // 先按学年排序，再按学期排序
        return yearA === yearB ? termB - termA : yearB.localeCompare(yearA);
      })
      .map(term => {
        const courses = grouped[term];
        let totalCredit = 0;
        let totalGradePointProduct = 0;

        courses.forEach(course => {
          // 确保 credit 和 gradePoint 是数字
          const credit = parseFloat(course.credit);
          const gradePoint = parseFloat(course.gradePoint);

          if (!isNaN(credit) && !isNaN(gradePoint)) {
            totalCredit += credit;
            totalGradePointProduct += (credit * gradePoint);
          }
        });

        const gpa = totalCredit > 0 ? (totalGradePointProduct / totalCredit).toFixed(2) : 'N/A';

        return {
          term,
          courses,
          gpa
        };
      });
  };

  // 获取成绩对应的颜色
  const getGradeColor = (grade) => {
    const num = parseInt(grade);
    if (isNaN(num)) return '#45A3F5'; // 非数字成绩（如优秀）显示为蓝色
    if (num >= 60) return '#45A3F5'; // 及格 - 蓝色
    return '#f5222d'; // 不及格 - 红色
  };

  // 获取成绩详情
  const fetchScoreDetail = async (course) => {
    // 先设置课程基本信息并打开弹窗，显示加载状态
    setScoreDetail({
      courseName: course.courseName,
      year: course.year,
      term: course.term,
      teacherName: course.teacherName,
      credit: course.credit,
      gradePoint: course.gradePoint,
      details: null
    });
    setScoreDetailVisible(true);
    setScoreDetailLoading(true);

    // 等待弹窗渲染完成
    await new Promise(resolve => setTimeout(resolve, 50));

    try {
      const params = new URLSearchParams({
        courseName: course.courseName,
        classId: course.classId,
        year: course.year,
        term: course.term
      });
      const response = await authFetch(`${API_PATHS.SCORE.DETAIL}?${params}`);

      const result = await response.json();
      if (result.code === 1) {
        setScoreDetail(prev => ({
          ...prev,
          details: result.data.items
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

  // 关闭详情弹窗
  const handleCloseDetail = () => {
    setScoreDetailVisible(false);
    setScoreDetail(null);
  };

  return (
    <Layout className="score-layout">
      <Header className="course-table-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined style={{ fontSize: 22 }} />}
          onClick={() => history.goBack()}
          style={{ color: '#fff', position: 'absolute', left: 8, top: 20 }}
        />
        <h2 style={{ margin: 0 }}>全部成绩</h2>
      </Header>

      <Content className="score-content">
        {loading ? (
          <div className="loading-container">
            <Skeleton active paragraph={{ rows: 10 }} />
          </div>
        ) : scores.length === 0 ? (
          <div className="no-data">
            <Text type="secondary">暂无成绩数据</Text>
          </div>
        ) : (
          <>
            <Card
              style={{ marginBottom: 16, borderRadius: 8 }}
              bodyStyle={{ padding: '16px 20px' }}
            >
              <div style={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center', marginBottom: 12 }}>
                <Button.Group>
                  <Button 
                    type={!rankingFlag ? 'primary' : 'default'}
                    size="small"
                    onClick={() => {
                      setRankingFlag(false);
                      fetchRanking(false);
                    }}
                  >
                    全部学年
                  </Button>
                  <Button 
                    type={rankingFlag ? 'primary' : 'default'}
                    size="small"
                    onClick={() => {
                      setRankingFlag(true);
                      fetchRanking(true);
                    }}
                  >
                    当前学年
                  </Button>
                </Button.Group>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <Text style={{ fontSize: 14, color: '#666' }}>我的排名：</Text>
                  <Tooltip title="成绩只计算必修课，该成绩排名基于系统已更新成绩统计，可能与实际相差较大，仅供参考！" placement="right">
                    <span style={{ 
                      display: 'inline-block', 
                      width: 14, 
                      height: 14, 
                      borderRadius: '50%', 
                      backgroundColor: '#999', 
                      color: '#fff', 
                      fontSize: 12, 
                      textAlign: 'center', 
                      lineHeight: '14px', 
                      marginLeft: 4, 
                      cursor: 'pointer' 
                    }}>
                      ?
                    </span>
                  </Tooltip>
                  <Text strong style={{ fontSize: 24, color: '#1890ff', marginLeft: 8 }}>
                    {ranking.rank}/{ranking.total}
                  </Text>
                </div>
                <Button
                  type="primary"
                  size="small"
                  onClick={() => setRankingModalVisible(true)}
                >
                  查看完整排名
                </Button>
              </div>
            </Card>
            <Collapse
              defaultActiveKey={scores.length > 0 ? [scores[0].term] : []} // 只展开当前学期
              className="score-collapse"
            >
            {scores.map(termGroup => (
              <Panel
                header={
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Text strong>{termGroup.term}学期</Text>
                    <Tag color="blue">GPA: {termGroup.gpa}</Tag>
                  </div>
                }
                key={termGroup.term}
              >
                <List
                  dataSource={termGroup.courses}
                  renderItem={item => (
                    <List.Item
                      style={{ display: 'flex', justifyContent: 'space-between', cursor: 'pointer' }}
                      onClick={() => fetchScoreDetail(item)}
                    >
                      <span style={{
                        maxWidth: '70%',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        whiteSpace: 'nowrap'
                      }}>
                        {item.courseName}
                      </span>
                      <span className="score-badge" style={{
                        flexShrink: 0,
                        padding: '0 8px',
                        borderRadius: 4,
                        backgroundColor: getGradeColor(item.grade)
                      }}>
                        {item.grade}
                      </span>
                    </List.Item>
                  )}
                />
              </Panel>
            ))}
          </Collapse>
          </>
        )}
      </Content>

      {/* 排名弹窗 */}
      <Modal
        title="完整排名"
        open={rankingModalVisible}
        onCancel={() => setRankingModalVisible(false)}
        footer={null}
        width={600}
        maskStyle={{
          backdropFilter: 'blur(8px)',
          backgroundColor: 'rgba(0, 0, 0, 0.45)'
        }}
        bodyStyle={{
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderRadius: '8px',
          maxHeight: '60vh',
          overflow: 'auto'
        }}
      >
        <Skeleton loading={rankingLoading} active>
          {ranking.items.length > 0 ? (
            <>
              <div style={{ 
                display: 'flex', 
                padding: '12px 16px', 
                backgroundColor: '#f5f5f5', 
                borderRadius: 4,
                marginBottom: 8,
                fontWeight: 'bold',
                color: '#666',
                position: 'sticky',
                top: 0,
                zIndex: 1
              }}>
                <div style={{ width: '50px', textAlign: 'center' }}>排名</div>
                <div style={{ flex: 1 }}>姓名</div>
                <div style={{ width: '80px', textAlign: 'right' }}>加权平均分</div>
              </div>
              <List
                dataSource={ranking.items}
                renderItem={(item, index) => {
                  const isCurrentUser = item.sid === mySid;
                  return (
                    <List.Item
                      style={{
                        backgroundColor: isCurrentUser ? '#1890ff' : 'transparent',
                        borderRadius: 4,
                        padding: '12px 16px',
                        marginBottom: 8,
                        border: isCurrentUser ? '2px solid #096dd9' : 'none',
                        boxShadow: isCurrentUser ? '0 2px 8px rgba(24, 144, 255, 0.3)' : 'none'
                      }}
                    >
                      <div style={{ display: 'flex', width: '100%', alignItems: 'center' }}>
                        <div style={{ width: '50px', textAlign: 'center', fontWeight: 'bold', color: isCurrentUser ? '#fff' : '#666' }}>
                          {index + 1}
                        </div>
                        <div style={{ flex: 1 }}>
                          <div style={{ fontSize: 16, fontWeight: isCurrentUser ? 'bold' : 'normal', color: isCurrentUser ? '#fff' : 'inherit' }}>
                            {item.name}
                            {isCurrentUser && <Tag color="#fff" style={{ marginLeft: 8, color: '#1890ff', fontWeight: 'bold' }}>我</Tag>}
                          </div>
                        </div>
                        <div style={{ width: '80px', textAlign: 'right', fontWeight: 'bold', color: isCurrentUser ? '#fff' : '#1890ff' }}>
                          {item.score}
                        </div>
                      </div>
                    </List.Item>
                  );
                }}
              />
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: 24, color: '#999' }}>
              暂无排名数据
            </div>
          )}
        </Skeleton>
      </Modal>

      {/* 成绩详情弹窗 - 使用表格展示分项成绩 */}
      <Modal
        title={scoreDetail?.courseName || '成绩详情'}
        open={scoreDetailVisible}
        onCancel={handleCloseDetail}
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
          {scoreDetail && scoreDetail.details && (
            <div className="score-detail-content">
              <div className="score-meta">
                <Text strong>学年学期：</Text>
                <Text>{formatTermTitle(`${scoreDetail.year}-${scoreDetail.year + 1}/${scoreDetail.term}`)}</Text>
              </div>
              <div className="score-meta">
                <Text strong>教师：</Text>
                <Text>{scoreDetail.teacherName || '暂无'}</Text>
              </div>
              <div className="score-meta">
                <Text strong>学分：</Text>
                <Text>{scoreDetail.credit}</Text>
              </div>
              <div className="score-meta">
                <Text strong>绩点：</Text>
                <Text>{scoreDetail.gradePoint}</Text>
              </div>

              {scoreDetail.details.length > 0 ? (
                <Table
                  dataSource={scoreDetail.details}
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
              ) : (
                <div className="no-detail-info">
                  <Text type="secondary">无详细分项成绩信息</Text>
                </div>
              )}
            </div>
          )}
        </Spin>
      </Modal>

      <div style={{ textAlign: 'center', paddingBottom: '130px', fontSize: '14px', color: '#979B9B' }}>
        tips: 点击成绩可查看详情
      </div>
    </Layout>
  );
}

// 格式化学期标题
function formatTermTitle(term) {
  if (!term) return '';

  const [year, semester] = term.split('/');
  const semesterText = semester === '1' ? '第一学期' : '第二学期';

  return `${year}学年 ${semesterText}`;
}