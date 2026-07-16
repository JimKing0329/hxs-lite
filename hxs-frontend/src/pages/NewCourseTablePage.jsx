import React, { useState, useEffect, useMemo } from 'react';
import { Layout, Select, Button, message, Spin } from 'antd';
import { LeftOutlined, RightOutlined } from '@ant-design/icons';
import './NewCourseTablePage.css';
import { API_PATHS } from '../constants/api';
import axios from 'axios';

const { Header, Content } = Layout;
const { Option } = Select;

const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日'];
const WEEKDAY_FULL = ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'];
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

// 解析周次字符串，返回包含的周次数组
// 支持格式: "1-16周", "1,3,5周", "1-8周(单)", "2-16周(双)"
const parseWeeks = (weeksStr) => {
    if (!weeksStr) return [];
    const weeks = new Set();

    // 移除"周"字和括号内容
    let str = weeksStr.replace(/周.*$/, '');

    // 检查是否有单双周标记
    const isOddOnly = weeksStr.includes('(单)') || weeksStr.includes('（单）');
    const isEvenOnly = weeksStr.includes('(双)') || weeksStr.includes('（双）');

    // 按逗号分割
    const parts = str.split(',');

    for (const part of parts) {
        const trimmed = part.trim();
        // 检查是否是范围格式 "1-16"
        const rangeMatch = trimmed.match(/(\d+)\s*-\s*(\d+)/);
        if (rangeMatch) {
            const start = parseInt(rangeMatch[1], 10);
            const end = parseInt(rangeMatch[2], 10);
            for (let i = start; i <= end; i++) {
                if (isOddOnly && i % 2 === 0) continue;
                if (isEvenOnly && i % 2 === 1) continue;
                weeks.add(i);
            }
        } else {
            // 单个数字
            const num = parseInt(trimmed, 10);
            if (!isNaN(num)) {
                weeks.add(num);
            }
        }
    }

    return Array.from(weeks).sort((a, b) => a - b);
};

export default function NewCourseTablePage() {
    // 班级选择状态
    const [allClasses, setAllClasses] = useState([]);
    const [grades, setGrades] = useState([]);
    const [colleges, setColleges] = useState([]);
    const [majors, setMajors] = useState([]);
    const [classes, setClasses] = useState([]);
    const [selectedGrade, setSelectedGrade] = useState(undefined);
    const [selectedCollege, setSelectedCollege] = useState(undefined);
    const [selectedMajor, setSelectedMajor] = useState(undefined);
    const [selectedClass, setSelectedClass] = useState(null);

    // 课表数据
    const [courseData, setCourseData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [selectedWeek, setSelectedWeek] = useState(1);

    // 初始加载所有班级数据
    useEffect(() => {
        const fetchAllClasses = async () => {
            try {
                setLoading(true);
                const response = await axios.get(API_PATHS.NEW_COURSE_TABLE.ALL_CLASS);
                if (response.data.code === 1) {
                    setAllClasses(response.data.data);
                    const uniqueGrades = [...new Set(response.data.data.map(item => item.grade))];
                    setGrades(uniqueGrades);
                } else {
                    message.error('获取班级数据失败');
                }
            } catch (error) {
                console.error('Error fetching classes:', error);
                message.error('网络错误，无法获取班级数据');
            } finally {
                setLoading(false);
            }
        };

        fetchAllClasses();
    }, []);

    // 年级变化时更新学院列表
    useEffect(() => {
        if (!selectedGrade) {
            setColleges([]);
            setSelectedCollege(undefined);
            return;
        }

        const filteredColleges = [...new Set(
            allClasses
                .filter(item => item.grade === selectedGrade)
                .map(item => item.college)
        )];
        setColleges(filteredColleges);
        setSelectedCollege(undefined);
        setMajors([]);
        setSelectedMajor(undefined);
        setClasses([]);
        setSelectedClass(null);
        setCourseData(null);
    }, [selectedGrade, allClasses]);

    // 学院变化时更新专业列表
    useEffect(() => {
        if (!selectedGrade || !selectedCollege) {
            setMajors([]);
            setSelectedMajor(undefined);
            return;
        }

        const filteredMajors = [...new Set(
            allClasses
                .filter(item => item.grade === selectedGrade && item.college === selectedCollege)
                .map(item => item.majorName)
        )];
        setMajors(filteredMajors);
        setSelectedMajor(undefined);
        setClasses([]);
        setSelectedClass(null);
        setCourseData(null);
    }, [selectedCollege, selectedGrade, allClasses]);

    // 专业变化时更新班级列表
    useEffect(() => {
        if (!selectedGrade || !selectedCollege || !selectedMajor) {
            setClasses([]);
            setSelectedClass(null);
            return;
        }

        const filteredClasses = allClasses.filter(
            item => item.grade === selectedGrade && item.college === selectedCollege && item.majorName === selectedMajor
        );
        setClasses(filteredClasses);
        setSelectedClass(null);
        setCourseData(null);
    }, [selectedMajor, selectedCollege, selectedGrade, allClasses]);

    // 查询课表
    const handleQueryCourse = async () => {
        if (!selectedClass) {
            message.warning('请选择班级');
            return;
        }

        try {
            setLoading(true);
            const response = await axios.get(API_PATHS.NEW_COURSE_TABLE.COURSE_TABLE, {
                params: {
                    className: selectedClass.className,
                    classId: selectedClass.classId
                }
            });
            if (response.data.code === 1) {
                setCourseData(response.data.data);
                // 自动设置当前周次（取第一门课的周次范围中的最小值）
                const mainCourses = response.data.data.mainCourseList || [];
                if (mainCourses.length > 0) {
                    const allWeeks = mainCourses.flatMap(c => parseWeeks(c.weeks));
                    if (allWeeks.length > 0) {
                        setSelectedWeek(Math.min(...allWeeks));
                    }
                }
            } else {
                message.error('获取课表失败');
            }
        } catch (error) {
            console.error('Error fetching course table:', error);
            message.error('网络错误，无法获取课表');
        } finally {
            setLoading(false);
        }
    };

    // 根据当前选中周次过滤课程
    const filteredCourses = useMemo(() => {
        if (!courseData) return { main: [], other: [] };

        const main = (courseData.mainCourseList || []).filter(course => {
            const weeks = parseWeeks(course.weeks);
            return weeks.includes(selectedWeek);
        });

        const other = (courseData.otherCourseList || []).filter(course => {
            const weeks = parseWeeks(course.weeks);
            return weeks.includes(selectedWeek);
        });

        return { main, other };
    }, [courseData, selectedWeek]);

    // 按星期分组课程
    const coursesByDay = useMemo(() => {
        const groups = {};
        WEEKDAY_FULL.forEach(day => {
            groups[day] = [];
        });

        filteredCourses.main.forEach(course => {
            if (groups[course.weekDay]) {
                groups[course.weekDay].push(course);
            }
        });

        return groups;
    }, [filteredCourses]);

    // 颜色分配
    const colorMap = useMemo(() => {
        const map = {};
        let idx = 0;
        filteredCourses.main.forEach(course => {
            if (!map[course.courseName]) {
                map[course.courseName] = COLORS[idx % COLORS.length];
                idx++;
            }
        });
        return map;
    }, [filteredCourses]);

    // 获取单元格内容
    const getCell = (day, sessionIdx) => {
        const courses = coursesByDay[day] || [];
        for (const c of courses) {
            if (c.startSession === sessionIdx) {
                const rowSpan = c.endSession - c.startSession + 1;
                return {
                    content: (
                        <div
                            className="new-course-block"
                            style={{ background: colorMap[c.courseName] || COLORS[0] }}
                        >
                            <div className="new-course-title">{c.courseName}</div>
                        </div>
                    ),
                    rowSpan,
                };
            }
            if (c.startSession < sessionIdx && c.endSession >= sessionIdx) {
                return { content: null, rowSpan: 0 };
            }
        }
        return { content: null, rowSpan: 1 };
    };

    // 周次切换
    const changeWeek = (delta) => {
        const newWeek = Math.min(Math.max(1, selectedWeek + delta), 20);
        setSelectedWeek(newWeek);
    };

    // 获取所有可能的周次范围
    const weekRange = useMemo(() => {
        if (!courseData) return [];
        const allWeeks = new Set();
        (courseData.mainCourseList || []).forEach(c => {
            parseWeeks(c.weeks).forEach(w => allWeeks.add(w));
        });
        (courseData.otherCourseList || []).forEach(c => {
            parseWeeks(c.weeks).forEach(w => allWeeks.add(w));
        });
        return Array.from(allWeeks).sort((a, b) => a - b);
    }, [courseData]);

    return (
        <Layout className="new-course-layout">
            <Header className="new-course-header">
                <h2 style={{ margin: 0, color: '#fff' }}>班级课表查询</h2>
            </Header>

            <div className="new-course-selector-card">
                <div className="new-course-selector-grid">
                    <div className="new-course-field">
                        <label className="new-course-label">年级</label>
                        <Select
                            placeholder="请选择"
                            value={selectedGrade}
                            onChange={setSelectedGrade}
                            className="new-course-select"
                            size="middle"
                            allowClear
                        >
                            {grades.map(g => <Option key={g} value={g}>{g}级</Option>)}
                        </Select>
                    </div>
                    <div className="new-course-field">
                        <label className="new-course-label">学院</label>
                        <Select
                            placeholder="请选择"
                            value={selectedCollege}
                            onChange={setSelectedCollege}
                            className="new-course-select"
                            size="middle"
                            disabled={!selectedGrade}
                            allowClear
                        >
                            {colleges.map(c => <Option key={c} value={c}>{c}</Option>)}
                        </Select>
                    </div>
                    <div className="new-course-field">
                        <label className="new-course-label">专业</label>
                        <Select
                            placeholder="请选择"
                            value={selectedMajor}
                            onChange={setSelectedMajor}
                            className="new-course-select"
                            size="middle"
                            disabled={!selectedCollege}
                            allowClear
                        >
                            {majors.map(m => <Option key={m} value={m}>{m}</Option>)}
                        </Select>
                    </div>
                    <div className="new-course-field">
                        <label className="new-course-label">班级</label>
                        <Select
                            placeholder="请选择"
                            value={selectedClass ? selectedClass.classId : undefined}
                            onChange={(val) => {
                                const cls = classes.find(c => c.classId === val);
                                setSelectedClass(cls);
                            }}
                            className="new-course-select"
                            size="middle"
                            disabled={!selectedMajor}
                        >
                            {classes.map(c => <Option key={c.classId} value={c.classId}>{c.className}</Option>)}
                        </Select>
                    </div>
                </div>
                <Button
                    type="primary"
                    block
                    size="large"
                    onClick={handleQueryCourse}
                    disabled={!selectedClass || loading}
                    loading={loading}
                    className="new-course-query-btn"
                >
                    查询课表
                </Button>
            </div>

            <Content className="new-course-content">
                <Spin spinning={loading}>
                    {courseData ? (
                        <>
                            <div className="new-course-table-scroll">
                                <table className="new-course-table">
                                    <thead>
                                        <tr>
                                            <th className="new-course-session-col">节次</th>
                                            {WEEKDAYS.map((d, idx) => (
                                                <th key={idx}>{`周${d}`}</th>
                                            ))}
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {SESSIONS.map((s) => (
                                            <tr key={s.idx}>
                                                <td className="new-course-session-col">
                                                    <div className="new-course-session-idx">{s.idx}</div>
                                                    <div className="new-course-session-time">{s.time}</div>
                                                </td>
                                                {WEEKDAY_FULL.map((day, dayIdx) => {
                                                    const { content, rowSpan } = getCell(day, s.idx);
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
                            </div>

                            {/* 周次选择器 */}
                            <div className="new-course-week-selector-container">
                                <Button
                                    type="primary"
                                    shape="circle"
                                    icon={<LeftOutlined />}
                                    size="small"
                                    onClick={() => changeWeek(-1)}
                                    disabled={selectedWeek <= 1}
                                />
                                <div className="new-course-week-selector">
                                    <span>周次：</span>
                                    <Select
                                        value={selectedWeek}
                                        onChange={setSelectedWeek}
                                        size="small"
                                        style={{ width: 100 }}
                                        dropdownMatchSelectWidth={false}
                                    >
                                        {weekRange.map(w => (
                                            <Option key={w} value={w}>第{w}周</Option>
                                        ))}
                                    </Select>
                                </div>
                                <Button
                                    type="primary"
                                    shape="circle"
                                    icon={<RightOutlined />}
                                    size="small"
                                    onClick={() => changeWeek(1)}
                                    disabled={selectedWeek >= 20}
                                />
                            </div>

                            {/* 其他课程 */}
                            <div className="new-course-other-section">
                                <div className="new-course-section-header">
                                    <span className="new-course-section-title">其他课程</span>
                                    <span className="new-course-section-badge">{filteredCourses.other.length}</span>
                                </div>
                                {filteredCourses.other.length > 0 ? (
                                    <div className="new-course-other-list">
                                        {filteredCourses.other.map(course => (
                                            <div key={course.id} className="new-course-other-item">
                                                <div className="new-course-other-info">
                                                    <span className="new-course-other-name">{course.courseName}</span>
                                                    <span className="new-course-other-weeks">{course.weeks}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                ) : (
                                    <div className="new-course-other-empty">
                                        本周无其他课程安排
                                    </div>
                                )}
                            </div>
                        </>
                    ) : (
                        !loading && (
                            <div className="new-course-empty">
                                <p>请选择班级并查询课表</p>
                            </div>
                        )
                    )}
                </Spin>
            </Content>
        </Layout>
    );
}
