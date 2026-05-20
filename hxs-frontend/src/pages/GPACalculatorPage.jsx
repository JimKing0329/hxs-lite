import React, { useState, useEffect } from 'react';
import { Layout, Button, Checkbox, Radio, Table, Typography, message, Spin, Tag } from 'antd';

import { API_PATHS } from '../constants/api';
import { authFetch } from '../utils/request';
import './GPACalculatorPage.css';
import { useHistory } from 'react-router-dom';
import { ArrowLeftOutlined } from '@ant-design/icons';

const { Header, Content } = Layout;
const { Title, Text } = Typography;

const GPACalculatorPage = () => {
    const history = useHistory();
  const [scores, setScores] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCourses, setSelectedCourses] = useState([]);
  const [calculationType, setCalculationType] = useState('weighted'); // 'gpa' or 'weighted'
  const [result, setResult] = useState(null);
  const [groupedScores, setGroupedScores] = useState({});

  useEffect(() => {
    fetchScores();
  }, []);

  // 从API获取成绩数据
  const fetchScores = async () => {
    try {
      setLoading(true);
      const response = await authFetch(API_PATHS.GET_SCORES);
      const data = await response.json();
      
      if (data.code === 1 && data.data) {
        setScores(data.data);
        // 默认不选择
        setSelectedCourses([]);
        // 按学年学期分组
        groupScoresBySemester(data.data);
      } else {
        message.error(data.msg || '获取成绩数据失败');
      }
    } catch (error) {
      console.error('获取成绩数据异常:', error);
      message.error('获取成绩数据异常，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 按学年学期分组成绩
  const groupScoresBySemester = (data) => {
    const groups = {};
    data.forEach(item => {
      const key = `${item.year}学年 第${item.term}学期`;
      if (!groups[key]) {
        groups[key] = [];
      }
      groups[key].push(item);
    });
    setGroupedScores(groups);
  };

  // 检查学期是否全选
  const isSemesterSelected = (semester) => {
    const courses = groupedScores[semester] || [];
    const requiredCourses = courses.filter(course => course.courseType === '必修');
    return requiredCourses.length === 0 || requiredCourses.every(course => selectedCourses.includes(course.classId));
  };

  // 处理学期选择
  const handleSemesterSelect = (semester, checked) => {
    const courses = groupedScores[semester] || [];
    // 只选择必修课程
    const courseIds = courses.filter(course => course.courseType === '必修').map(course => course.classId);
    
    if (checked) {
      // 添加该学期所有课程
      const newSelected = [...selectedCourses];
      courseIds.forEach(id => {
        if (!newSelected.includes(id)) newSelected.push(id);
      });
      setSelectedCourses(newSelected);
    } else {
      // 移除该学期所有课程
      setSelectedCourses(selectedCourses.filter(id => !courseIds.includes(id)));
    }
  };

  // 处理课程选择变化
  const handleCourseSelect = (classId, checked) => {
    if (checked) {
      setSelectedCourses([...selectedCourses, classId]);
    } else {
      setSelectedCourses(selectedCourses.filter(id => id !== classId));
    }
  };

  // 全选
  const selectAll = () => {
  const allIds = scores.map(item => item.classId);
  setSelectedCourses(allIds);
  message.success('已全选所有课程');
};

  // 反选
  const selectInverse = () => {
  const allIds = scores.map(item => item.classId);
  const inverseIds = allIds.filter(id => !selectedCourses.includes(id));
  setSelectedCourses(inverseIds);
  message.success('已反选课程');
};

  // 按课程类型选择
  const selectByType = (type) => {
  const filteredIds = scores
    .filter(item => item.courseType === type)
    .map(item => item.classId);
  setSelectedCourses(filteredIds);
  const typeMap = { '必修': '必修课', '选修': '选修课', '素质类': '素质类课程' };
  message.success(`已选择所有${typeMap[type]}`);
};

  // 计算GPA和加权平均分
  const calculateGPA = () => {
    if (selectedCourses.length === 0) {
      message.warning('请至少选择一门课程');
      return;
    }

    let totalCredits = 0;
    let totalGradePoints = 0;
    let totalWeightedScore = 0;

    scores.forEach(item => {
      if (selectedCourses.includes(item.classId)) {
        const credit = parseFloat(item.credit) || 0;
        const gradePoint = parseFloat(item.gradePoint) || 0;
        const score = isNaN(parseFloat(item.grade)) ? 75 : parseFloat(item.grade);

        totalCredits += credit;
        totalGradePoints += credit * gradePoint;
        totalWeightedScore += credit * score;
      }
    });

    if (totalCredits === 0) {
      message.warning('所选课程学分总和为0，无法计算');
      return;
    }

    const gpa = (totalGradePoints / totalCredits).toFixed(4);
    const weightedAvg = (totalWeightedScore / totalCredits).toFixed(4);

    setResult({
      gpa,
      weightedAvg,
      credits: totalCredits.toFixed(1)
    });
  };

  // 课程类型标签样式
  const getCourseTypeTag = (type) => {
    let color = 'blue';
    if (type === '必修') color = 'red';
    if (type === '选修') color = 'green';
    if (type === '素质类') color = 'purple';

    return (
      <Tag color={color}>{type}</Tag>
    );
  };

  // 渲染学期分组表格
  const renderSemesterTables = () => {
  const sortedSemesters = Object.entries(groupedScores).sort((a, b) => {
    const parseSemester = (semester) => {
      const [yearStr, termStr] = semester.split(' ');
      const year = parseInt(yearStr);
      const term = parseInt(termStr.replace(/[^\d]/g, ''));
      return term === 1 ? year * 100 + 9 : (year + 1) * 100 + 3;
    };
    return parseSemester(b[0]) - parseSemester(a[0]);
  });

  return (
    <div>
      {sortedSemesters.map(([semester, courses]) => (
        <div key={semester} style={{ marginBottom: '24px', border: '1px solid #e8e8e8', borderRadius: '4px', overflow: 'hidden' }}>
          <div style={{ display: 'flex', alignItems: 'center', padding: '16px', background: '#f5f5f5' }}>
            <Checkbox 
              checked={isSemesterSelected(semester)} 
              onChange={(e) => handleSemesterSelect(semester, e.target.checked)}
            />
            <Title level={5} style={{ margin: '0 0 0 8px' }}>{semester}</Title>
          </div>
          <Table
            dataSource={courses}
            pagination={false}
            rowKey="classId"
            columns={[
              {
                title: '选择',
                key: 'select',
                render: (_, record) => (
                  <Checkbox
                    checked={selectedCourses.includes(record.classId)}
                    onChange={(e) => handleCourseSelect(record.classId, e.target.checked)}
                  />
                )
              },
              {
                title: '课程名称',
                dataIndex: 'courseName',
                key: 'courseName',
              },
              {
                title: '学分',
                dataIndex: 'credit',
                key: 'credit',
              },
              {
                title: calculationType === 'gpa' ? '绩点' : '成绩',
                dataIndex: calculationType === 'gpa' ? 'gradePoint' : 'grade',
                key: 'scoreOrGpa',
              },
              {
                title: '课程性质',
                key: 'courseType',
                render: (_, record) => getCourseTypeTag(record.courseType)
              }
            ]}
          />
        </div>
      ))}
    </div>
  );
};

  return (
    <Layout className="gpa-calculator-page">
                  <Header>
                    <Button
          type="text"
          icon={<ArrowLeftOutlined style={{ fontSize: 22 }} />}
          onClick={() => history.goBack()}
          style={{ color: '#fff', position: 'absolute', left: 8, top: 20 }}
        />
                <div className="header-content" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    position: 'relative',
                    padding: '0 16px'
                }}>
                    <h1 style={{ margin: 0 }}>小狮成绩计算器</h1>
                </div>
            </Header>
      <Content style={{ padding: '20px', background: '#f0f2f5' }}>
        <div className="calculator-container" style={{ background: '#fff', padding: '24px', borderRadius: '8px', minHeight: 'calc(100vh - 160px)' }}>
          <Spin spinning={loading} tip="加载成绩数据中...">
            {/* 结果显示区域 */}
            <div className="result-display" style={{ marginBottom: '24px', textAlign: 'center' }}>
              <Title level={3} style={{ margin: '0 0 16px 0' }}>
                您的{calculationType === 'gpa' ? '平均学分绩点(GPA)' : '加权平均分'}为：<br/>
                <Text strong style={{ color: '#f50', fontSize: '45px', margin: '0 8px' }}>
                  {result ? (calculationType === 'gpa' ? result.gpa : result.weightedAvg) : '——'}
                  <br/>
                </Text>
                {result && (
                  <Text type="secondary">
                    (总学分: {result.credits})
                    <br/>
                    
                  </Text>
                )}
              </Title>
            </div>

            {/* 操作按钮区域 */}
            <div className="operation-buttons" style={{display: 'flex', justifyContent: 'center',marginBottom: '24px' }}>
              <Button onClick={selectAll} size="small">全选</Button>
              <Button onClick={selectInverse} size="small">反选</Button>
              <Button onClick={() => selectByType('必修')} size="small">必修</Button>
              <Button onClick={() => selectByType('选修')} size="small">选修</Button>
              <Button onClick={() => selectByType('素质类')} size="small">其他</Button>
            </div>

            {/* 计算类型选择 */}
            <div className="calculation-type" style={{ display: 'flex', justifyContent: 'center',marginBottom: '24px' }}>
              <Radio.Group
                value={calculationType}
                onChange={(e) => setCalculationType(e.target.value)}
                buttonStyle="solid"
              >
                <Radio.Button value="gpa">&nbsp;&nbsp;&nbsp;&nbsp;GPA&nbsp;&nbsp;&nbsp;&nbsp;</Radio.Button>
                <Radio.Button value="weighted">加权平均分</Radio.Button>
              </Radio.Group>
            </div>
            <div style={{ display: 'flex', justifyContent: 'center',marginBottom: '5px' }}>
               <Button
                type="primary"
                onClick={calculateGPA}
                style={{ marginLeft: '16px',fontSize: '20px' }}
              >
                计算
              </Button>
            </div>
            <br/>
            <br/>

            {/* 成绩表格区域 */}
            <div className="scores-table-container">
              {Object.keys(groupedScores).length > 0 ? (
                renderSemesterTables()
              ) : (
                !loading && <div style={{ textAlign: 'center', padding: '48px', color: '#999' }}>暂无成绩数据</div>
              )}
            </div>
          </Spin>
        </div>
      </Content>
    </Layout>
  );
};

export default GPACalculatorPage;