import React, { useState, useEffect } from 'react';
import { Modal, Button, Checkbox, Radio, Table, Typography, message, Spin, Tag } from 'antd';
import { API_PATHS } from '../constants/api';
import './GPACalculator.css';

const { Title, Text } = Typography;

// 自定义GPA计算器组件
const GPACalculator = ({ visible, onCancel }) => {
  // 状态管理
  const [scores, setScores] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCourses, setSelectedCourses] = useState([]);
  const [calculationType, setCalculationType] = useState('weighted'); // 'gpa' or 'weighted'
  const [result, setResult] = useState(null);
  const [groupedScores, setGroupedScores] = useState({});

  // 获取成绩数据
  useEffect(() => {
    if (visible) {
      fetchScores();
    } else {
      // 重置状态
      setScores([]);
      setSelectedCourses([]);
      setResult(null);
      setGroupedScores({});
    }
  }, [visible]);

  // 从API获取成绩数据
  const fetchScores = async () => {
    try {
      setLoading(true);
      const response = await fetch('http://localhost:8008/exam/getScores', {
        headers: {
          'token': localStorage.getItem('token'),
          'Content-Type': 'application/json'
        }
      });
      const data = await response.json();
      
      if (data.code === 1 && data.data) {
        setScores(data.data);
        // 默认全选
        const allIds = data.data.map(item => item.classId);
        setSelectedCourses(allIds);
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
  };

  // 反选
  const selectInverse = () => {
    const allIds = scores.map(item => item.classId);
    const inverseIds = allIds.filter(id => !selectedCourses.includes(id));
    setSelectedCourses(inverseIds);
  };

  // 按课程类型选择
  const selectByType = (type) => {
    const filteredIds = scores
      .filter(item => item.courseType === type)
      .map(item => item.classId);
    setSelectedCourses(filteredIds);
  };

  // 计算GPA
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
        const score = isNaN(parseFloat(item.grade)) ? 0 : parseFloat(item.grade);

        totalCredits += credit;
        totalGradePoints += credit * gradePoint;
        totalWeightedScore += credit * score;
      }
    });

    if (totalCredits === 0) {
      message.warning('所选课程学分总和为0，无法计算');
      return;
    }

    const gpa = (totalGradePoints / totalCredits).toFixed(2);
    const weightedAvg = (totalWeightedScore / totalCredits).toFixed(2);

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
    return Object.entries(groupedScores).map(([semester, courses]) => (
      <div key={semester} className="semester-group">
        <Title level={5}>{semester}</Title>
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
              title: '绩点',
              dataIndex: 'gradePoint',
              key: 'gradePoint',
            },
            {
              title: '课程性质',
              key: 'courseType',
              render: (_, record) => getCourseTypeTag(record.courseType)
            }
          ]}
        />
      </div>
    ));
  };

  return (
    <Modal
      title="成绩计算器"
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={800}
      destroyOnClose
    >
      <Spin spinning={loading} tip="加载成绩数据中...">
        {/* 结果显示区域 */}
        <div className="result-display">
          <Title level={3}>
            您的{calculationType === 'gpa' ? 'GPA' : '加权平均分'}为：
            <Text strong style={{ color: '#f50' }}>
              {result ? (calculationType === 'gpa' ? result.gpa : result.weightedAvg) : '——'}
            </Text>
            {result && (
              <Text type="secondary" style={{ marginLeft: 16 }}>
                (总学分: {result.credits})
              </Text>
            )}
          </Title>
        </div>

        {/* 操作按钮区域 */}
        <div className="operation-buttons">
          <Button onClick={selectAll} size="small" style={{ marginRight: 8 }}>全选</Button>
          <Button onClick={selectInverse} size="small" style={{ marginRight: 8 }}>反选</Button>
          <Button onClick={() => selectByType('必修')} size="small" style={{ marginRight: 8 }}>必修</Button>
          <Button onClick={() => selectByType('选修')} size="small" style={{ marginRight: 8 }}>选修</Button>
          <Button onClick={() => selectByType('素质类')} size="small">其他</Button>
        </div>

        {/* 计算类型选择 */}
        <div className="calculation-type">
          <Radio.Group
            value={calculationType}
            onChange={(e) => setCalculationType(e.target.value)}
            buttonStyle="solid"
          >
            <Radio.Button value="gpa">GPA</Radio.Button>
            <Radio.Button value="weighted">加权平均分</Radio.Button>
          </Radio.Group>

          <Button
            type="primary"
            onClick={calculateGPA}
            style={{ marginLeft: 16 }}
          >
            计算
          </Button>
        </div>

        {/* 成绩表格区域 */}
        <div className="scores-table-container">
          {Object.keys(groupedScores).length > 0 ? (
            renderSemesterTables()
          ) : (
            !loading && <div className="no-data">暂无成绩数据</div>
          )}
        </div>
      </Spin>
    </Modal>
  );
};

export default GPACalculator;