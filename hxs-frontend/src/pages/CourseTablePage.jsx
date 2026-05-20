import React, { useState, useEffect } from 'react';
import { Layout, Select, Button, message, Spin } from 'antd';
import { ArrowLeftOutlined, ReloadOutlined, LeftOutlined, RightOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import './CourseTablePage.css';
import { API_PATHS } from '../constants/api';

const { Header, Content } = Layout;
const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日'];
const SESSIONS = [
    { idx: 1, time: '8:00' },
    { idx: 2, time: '' },
    { idx: 3, time: '9:45' },
    { idx: 4, time: '' },
    { idx: 5, time: '' },
    { idx: 6, time: '14:00' },
    { idx: 7, time: '' },
    { idx: 8, time: '' },
    { idx: 9, time: '16:35' },
    { idx: 10, time: '' },
    { idx: 11, time: '19:00' },
    { idx: 12, time: '' },
];

const COLORS = [
    '#f7b267', '#70a1d7', '#b6e388', '#f48498', '#a3c9a8', '#e2b6cf', '#f6d186'
];

export default function CourseTablePage() {
    const history = useHistory();
    const [week, setWeek] = useState(-1);
    const [loading, setLoading] = useState(false);
    const [tableData, setTableData] = useState({});

    // 获取课表数据
    const fetchTable = async (w = week) => {

        setLoading(true);
        try {
            const res = await fetch(`${API_PATHS.WEEK_COURSE}?week=${w}`, {
                headers: {
                    'token': localStorage.getItem('token'),
                }
            });
            const result = await res.json();
            if (result.code === 1) {
                setTableData(result.data.weekCourse || {});
                setWeek(result.data.week);
            } else {
                message.error(result.msg || '获取课表失败');
            }
        } catch (e) {
            message.error('网络请求异常');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTable(week);
        // eslint-disable-next-line
    }, [week]);

    // 生成每一列（周一到周日）每节课的显示内容
    const getCell = (day, sessionIdx, colorMap) => {
        const courses = tableData[day] || [];
        for (let i = 0; i < courses.length; i++) {
            const c = courses[i];
            if (c.startSession === sessionIdx) {
                // 计算rowSpan
                const rowSpan = c.endSession - c.startSession + 1;
                // 课程块内容
                return {
                    content: (
                        <div
                            className="course-block"
                            style={{ background: colorMap[c.title] || COLORS[day % COLORS.length] }}
                        >
                            <div className="course-title">{c.title}</div>
                            <div className="course-place">{c.place}</div>
                            <div className="course-teacher">{c.teacher}</div>
                        </div>
                    ),
                    rowSpan,
                };
            }
            // 如果当前节次被其他课程占用（合并单元格），返回null
            if (c.startSession < sessionIdx && c.endSession >= sessionIdx) {
                return { content: null, rowSpan: 0 };
            }
        }
        return { content: null, rowSpan: 1 };
    };

    // 颜色分配：同一课程同色
    const colorMap = {};
    let colorIdx = 0;
    Object.values(tableData).flat().forEach(c => {
        if (!colorMap[c.title]) {
            colorMap[c.title] = COLORS[colorIdx % COLORS.length];
            colorIdx++;
        }
    });

    // 添加周次切换函数
    const changeWeek = (delta) => {
        const newWeek = Math.min(Math.max(1, week + delta), 20);
        setWeek(newWeek);
    };

    return (
        <Layout className="course-table-layout">
            <Header className="course-table-header">
                <Button
                    type="text"
                    icon={<ArrowLeftOutlined style={{ fontSize: 22 }} />}
                    onClick={() => history.goBack()}
                    style={{ color: '#fff', position: 'absolute', left: 8, top: 20 }}
                />
                <h2 style={{ margin: 0 }}>周视图课表</h2>
            </Header>

            {/* 将周次选择器移到顶部，添加左右切换按钮 */}


            <Content className="course-table-content">
                <div className="table-scroll-wrapper">
                    <Spin spinning={loading}>
                        <table className="course-table">
                            <thead>
                                <tr>
                                    <th className="session-col">节次</th>
                                    {WEEKDAYS.map((d, idx) => (
                                        <th key={idx}>{`周${d}`}</th>
                                    ))}
                                </tr>
                            </thead>
                            <tbody>
                                {SESSIONS.map((s) => (
                                    <tr key={s.idx}>
                                        <td className="session-col">
                                            <div className="session-idx">{s.idx}</div>
                                            <div className="session-time">{s.time}</div>
                                        </td>
                                        {WEEKDAYS.map((_, dayIdx) => {
                                            const { content, rowSpan } = getCell(
                                                (dayIdx + 1).toString(),
                                                s.idx,
                                                colorMap
                                            );
                                            if (rowSpan === 0) return null;
                                            return (
                                                <td key={dayIdx} rowSpan={rowSpan}>
                                                    {content}
                                                </td>
                                            );
                                        })}
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </Spin>
                </div>
                <div
                    className="week-selector-container"
                    onTouchMove={(e) => {
                        e.stopPropagation();
                        e.preventDefault();
                    }}
                >
                    <Button
                        type="primary"
                        shape="circle"
                        icon={<LeftOutlined />}
                        size="small"
                        onClick={() => changeWeek(-1)}
                        disabled={week <= 1}
                    />
                    <div className="week-selector">
                        <span>周次：</span>
                        <Select
                            value={week}
                            onChange={setWeek}
                            size="small"
                            style={{ width: 120 }}
                            dropdownMatchSelectWidth={false}
                        >
                            {Array.from({ length: 20 }, (_, i) => (
                                <Select.Option key={i + 1} value={i + 1}>
                                    第{i + 1}周
                                </Select.Option>
                            ))}
                        </Select>
                    </div>
                    <Button
                        type="primary"
                        shape="circle"
                        icon={<RightOutlined />}
                        size="small"
                        onClick={() => changeWeek(1)}
                        disabled={week >= 20}
                    />
                </div>
            </Content>
        </Layout>
    );
}