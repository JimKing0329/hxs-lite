import React, { useState, useEffect } from 'react';
import { Layout, Card, List, Skeleton, Typography, Modal, Divider, Collapse, message, Button } from 'antd';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { authFetch } from '../utils/request';
import BottomNav from '../components/BottomNav';
import './ExecutePlanPage.css';

const { Header, Content } = Layout;
const { Title, Text } = Typography;
const { Panel } = Collapse;

export default function ExecutePlanPage() {
    const history = useHistory();
    const [executePlan, setExecutePlan] = useState([]);
  const [currentTermKey, setCurrentTermKey] = useState('');
    const [loading, setLoading] = useState(true);
    const [courseDetail, setCourseDetail] = useState(null);
    const [detailVisible, setDetailVisible] = useState(false);

    // 获取执行计划数据
    useEffect(() => {
        const fetchExecutePlan = async () => {
            try {
                const response = await authFetch(API_PATHS.GET_EXECUTE_PLAN);

                const result = await response.json();
                if (result.code === 1 && result.data) {
                    // 按学年学期分组数据
                    const groupedData = groupByTerm(result.data.executeCourseList);
                    // 使用后端返回的year和term字段确定当前学期
                    const currentTerm = `${result.data.year}-${result.data.year + 1}/${result.data.term}`;
                    setCurrentTermKey(currentTerm);
                    setExecutePlan(groupedData);
                } else {
                    message.error(result.msg);
                }
            } catch (error) {
                console.error('获取执行计划出错:', error);
                message.error('获取执行计划出错');
            } finally {
                setLoading(false);
            }
        };

        fetchExecutePlan();
    }, []);

    // 按学年学期分组数据
    const groupByTerm = (data) => {
        const grouped = {};

        data.forEach(item => {
            // 检查recommendTerm是否存在且不为null
            if (item.recommendTerm) {
                // 将推荐学期按逗号分割成数组
                const terms = item.recommendTerm.split(',').map(term => term.trim());
                
                terms.forEach(term => {
                    if (!grouped[term]) {
                        grouped[term] = [];
                    }
                    grouped[term].push(item);
                });
            }
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
            .map(term => ({
                term,
                courses: grouped[term].sort((a, b) => {
                    // 将必修课程排在前面
                    if (a.courseType === '必修' && b.courseType !== '必修') {
                        return -1;
                    }
                    if (a.courseType !== '必修' && b.courseType === '必修') {
                        return 1;
                    }
                    return 0;
                })
            }));
    };

    // 显示课程详情
    const showCourseDetail = (course) => {
        setCourseDetail(course);
        setDetailVisible(true);
    };

    // 关闭课程详情
    const handleCloseDetail = () => {
        setDetailVisible(false);
    };

    // 更新专业信息
    const [updateMajorLoading, setUpdateMajorLoading] = useState(false);
    
    const handleUpdateMajor = async () => {
        setUpdateMajorLoading(true);
        try {
            const response = await authFetch(API_PATHS.UPDATE_MAJOR, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' }
            });

            const result = await response.json();
            if (response.ok && result.code === 1) {
                message.success('专业信息更新成功，页面将自动刷新');
                // 刷新页面以获取最新信息
                window.location.reload();
            } else {
                message.error(result.msg || '更新专业信息失败');
            }
        } catch (error) {
            console.error('更新专业信息失败:', error);
            message.error('网络错误，无法更新专业信息');
        } finally {
            setUpdateMajorLoading(false);
        }
    };

    return (
        <Layout className="execute-plan-layout">
            <Header>
                <div className="header-content" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    position: 'relative',
                    padding: '0 16px'
                }}>
                    <h1 style={{ margin: 0 }}>执行计划</h1>
                </div>
            </Header>

            <Content className="execute-plan-content">
                {loading ? (
                    <div className="loading-container">
                        <Skeleton active paragraph={{ rows: 10 }} />
                    </div>
                ) : (
                    <div className="term-cards">
                        <Collapse defaultActiveKey={[currentTermKey]} className="term-collapse">
                            {executePlan.map((termGroup) => (
                                <Panel
                                    key={termGroup.term}
                                    header={(
              <>
                {formatTermTitle(termGroup.term)}
                {termGroup.term === currentTermKey && 
                  <Text type="success" style={{ marginLeft: 8, fontSize: 12 }}>(当前学期)</Text> }
              </>
            )}
                                    className="term-panel"
                                >
                                    <List
                                        itemLayout="horizontal"
                                        dataSource={termGroup.courses}
                                        renderItem={(course) => (
                                            <List.Item
                                                onClick={() => showCourseDetail(course)}
                                                className="course-item"
                                            >
                                                <div className="course-basic-info">
                                                    <Text strong className="course-name">{course.courseName}</Text>
                                                    <Text className="course-credit">{course.courseType} ({course.coursePoint} 学分)</Text>
                                                </div>
                                            </List.Item>
                                        )}
                                    />
                                </Panel>
                            ))}
                        </Collapse>
                    </div>
                )}
            </Content>


            {/* 课程详情弹窗 */}
            <Modal
                title="课程详情"
                open={detailVisible}
                onCancel={handleCloseDetail}
                footer={null}
                destroyOnClose
            >
                {courseDetail && (
                    <div className="course-detail">
                        <Title level={4}>{courseDetail.courseName}</Title>
                        <Divider />
                        <div className="detail-item">
                            <Text type="secondary">学分：</Text>
                            <Text strong>{courseDetail.coursePoint}</Text>
                        </div>
                        <div className="detail-item">
                            <Text type="secondary">开课周次：</Text>
                            <Text>{courseDetail.courseWeek}</Text>
                        </div>
                        <div className="detail-item">
                            <Text type="secondary">课时：</Text>
                            <Text>{courseDetail.courseTime}</Text>
                        </div>
                        <div className="detail-item">
                            <Text type="secondary">开课学院：</Text>
                            <Text>{courseDetail.collegeName}</Text>
                        </div>
                        <div className="detail-item">
                            <Text type="secondary">建议修读学期：</Text>
                            <Text>{formatTermTitle(courseDetail.recommendTerm)}</Text>
                        </div>
                    </div>
                )}
            </Modal>

            <div style={{ textAlign: 'center', paddingBottom: '20px', fontSize: '14px', color: '#979B9B' }}>
               tips: 点击课程可查看详情
            </div>
            <div style={{ textAlign: 'center', paddingBottom: '60px', fontSize: '14px', color: '#979B9B' }}>
                转专业了？<Button type="link" loading={updateMajorLoading} onClick={handleUpdateMajor}>更新专业信息</Button>
            </div>
            <BottomNav />
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