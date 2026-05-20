import React, { useState } from 'react';
import { Layout, Card, Select, Button, Collapse, Spin, Empty, message, Modal } from 'antd';
import { useHistory } from 'react-router-dom';
import BottomNav from '../components/BottomNav';
import { API_PATHS } from '../constants/api';
import './EmptyClassroomPage.css';

const { Header, Content } = Layout;
const { Option } = Select;
const { Panel } = Collapse;

export default function EmptyClassroomPage() {
  const history = useHistory();
  const [weekday, setWeekday] = useState(null);
  const [startSession, setStartSession] = useState(null);
  const [endSession, setEndSession] = useState(null);
  const [loading, setLoading] = useState(false);
  const [classrooms, setClassrooms] = useState([]);
  const [groupedClassrooms, setGroupedClassrooms] = useState({});
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedClassroom, setSelectedClassroom] = useState(null);

  // 获取今天是星期几（1-7对应周一到周日）
  const getTodayWeekday = () => {
    const today = new Date();
    const day = today.getDay();
    return day === 0 ? 7 : day; // 转换为1-7格式
  };

  // 格式化日期为 YYYY年MM月DD日
  const formatDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;
    const day = today.getDate();
    return `${year}年${month}月${day}日`;
  };

  // 初始化星期为今天
  React.useEffect(() => {
    setWeekday(getTodayWeekday());
  }, []);

  // 按教学楼分组并排序
  const groupByBuilding = (data) => {
    const groups = {};
    data.forEach(classroom => {
      if (!groups[classroom.building]) {
        groups[classroom.building] = [];
      }
      groups[classroom.building].push(classroom);
    });

    // 对每个教学楼的教室按classId排序
    Object.keys(groups).forEach(building => {
      groups[building].sort((a, b) => a.classId.localeCompare(b.classId));
    });

    return groups;
  };

  const showClassroomDetail = (classroom) => {
    setSelectedClassroom(classroom);
    setModalVisible(true);
  };

  // 查询空教室
  const handleQuery = async () => {
    if (!weekday) {
      message.warning('请选择星期几');
      return;
    }

    setLoading(true);
    try {
      const params = new URLSearchParams({
        weekday: weekday.toString()
      });

      if (startSession) {
        params.append('startSession', startSession.toString());
      }
      if (endSession) {
        params.append('endSession', endSession.toString());
      }

      const response = await fetch(`${API_PATHS.EMPTY_CLASSROOM}?${params}`, {
        headers: {
          'token': localStorage.getItem('token'),
          'Content-Type': 'application/json'
        }
      });

      const result = await response.json();
      
      if (result.code === 1) {
        const grouped = groupByBuilding(result.data);
        setClassrooms(result.data);
        setGroupedClassrooms(grouped);
        if (result.data.length === 0) {
          message.info('当前条件下没有找到空教室');
        }
      } else {
        message.error(result.msg || '查询失败');
      }
    } catch (error) {
      console.error('查询空教室失败:', error);
      message.error('查询失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 生成节次选项
  const sessionOptions = Array.from({ length: 13 }, (_, i) => i + 1);

  return (
    <Layout className="empty-classroom-container">
      <Header>
                <div className="header-content" style={{
                    display: 'flex',
                    justifyContent: 'center',
                    position: 'relative',
                    padding: '0 16px'
                }}>
                    <h1 style={{ margin: 0, fontSize: 28 }}>空教室</h1>
                </div>
            </Header>
      
      <Content>
        <Card className="query-card">
          <div style={{ marginBottom: 16, paddingBottom: 12, borderBottom: '1px solid #f0f0f0', fontSize: 16, color: '#666' }}>
            今日日期：{formatDate()}
          </div>
          <div className="query-form">
            <div className="form-item">
              <label>星期：</label>
              <Select
                value={weekday}
                onChange={setWeekday}
                placeholder="请选择星期"
                style={{ width: 120 }}
              >
                <Option value={1}>周一</Option>
                <Option value={2}>周二</Option>
                <Option value={3}>周三</Option>
                <Option value={4}>周四</Option>
                <Option value={5}>周五</Option>
                <Option value={6}>周六</Option>
                <Option value={7}>周日</Option>
              </Select>
            </div>

            <div className="form-item">
              <label>开始节次：</label>
              <Select
                value={startSession}
                onChange={setStartSession}
                placeholder="非必选"
                style={{ width: 120 }}
                allowClear
              >
                {sessionOptions.map(session => (
                  <Option key={session} value={session}>{session}</Option>
                ))}
              </Select>
            </div>

            <div className="form-item">
              <label>结束节次：</label>
              <Select
                value={endSession}
                onChange={setEndSession}
                placeholder="非必选"
                style={{ width: 120 }}
                allowClear
              >
                {sessionOptions.map(session => (
                  <Option key={session} value={session}>{session}</Option>
                ))}
              </Select>
            </div>

            <Button 
              type="primary" 
              onClick={handleQuery}
              loading={loading}
              style={{ marginLeft: 16 }}
            >
              查询
            </Button>
          </div>
        </Card>

        <Card className="result-card">
          <Spin spinning={loading}>
            {Object.keys(groupedClassrooms).length === 0 ? (
              <Empty description="暂无数据，请先查询" />
            ) : (
              <Collapse accordion>
                {Object.entries(groupedClassrooms).sort(([a], [b]) => a.localeCompare(b, 'zh-CN')).map(([building, classrooms]) => (
                  <Panel 
                    header={
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '4px 0' }}>
                        <span style={{ fontWeight: 500, fontSize: '15px' }}>{building}</span>
                        <span style={{ color: '#666', fontSize: '13px', backgroundColor: '#f0f0f0', padding: '2px 8px', borderRadius: '10px' }}>
                          {classrooms.length}间
                        </span>
                      </div>
                    } 
                    key={building}
                  >
                    <div className="classroom-list">
                      {classrooms
                        .sort((a, b) => a.className.localeCompare(b.className, 'zh-CN'))
                        .map(classroom => (
                        <div 
                          key={classroom.id} 
                          className="classroom-item"
                          onClick={() => showClassroomDetail(classroom)}
                        >
                          <div className="classroom-name">{classroom.className}</div>
                        </div>
                      ))}
                    </div>
                  </Panel>
                ))}
              </Collapse>
            )}
          </Spin>
        </Card>
      </Content>

      <BottomNav />
      
      <Modal
        title="教室详情"
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setModalVisible(false)}>
            关闭
          </Button>
        ]}
      >
        {selectedClassroom && (
          <div>
            <div><strong>教室名称：</strong>{selectedClassroom.className}</div>
            <div><strong>教室类型：</strong>{selectedClassroom.classCategory}</div>
            <div><strong>所在校区：</strong>{selectedClassroom.campusName}</div>
            <div><strong>教学楼：</strong>{selectedClassroom.building}</div>
          </div>
        )}
      </Modal>
    </Layout>
  );
}