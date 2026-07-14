import React from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { HomeOutlined, ScheduleFilled, ScheduleOutlined, UserOutlined, HomeFilled } from '@ant-design/icons';
import './BottomNav.css';

export default function BottomNav() {
    const history = useHistory();
    const location = useLocation();

    const handleNavigation = (path) => {
        history.push(path);
    };

    return (
        <div className="bottom-nav">
            <div
                className={`nav-item ${location.pathname === '/dashboard' ? 'active' : ''}`}
                onClick={() => handleNavigation('/dashboard')}
            >
                <span className="nav-icon">
                    <HomeFilled style={{ fontSize: '20px' }} />
                </span>
                <span className="nav-text">主页</span>
            </div>

            <div
                className={`nav-item ${location.pathname === '/execute-plan' ? 'active' : ''}`}
                onClick={() => handleNavigation('/execute-plan')}
            >
                <span className="nav-icon">
                    <ScheduleOutlined style={{ fontSize: '20px' }} />
                </span>
                <span className="nav-text">执行计划</span>
            </div>


            <div
                className={`nav-item ${location.pathname === '/empty-classroom' ? 'active' : ''}`}
                onClick={() => handleNavigation('/empty-classroom')}
            >
                <span className="nav-icon">
                    <HomeOutlined style={{ fontSize: '20px' }} />
                </span>
                <span className="nav-text">空教室</span>
            </div>

            <div
                className={`nav-item ${location.pathname === '/user-profile' ? 'active' : ''}`}
                onClick={() => handleNavigation('/user-profile')}
            >
                <span className="nav-icon">
                    <UserOutlined style={{ fontSize: '20px' }} />
                </span>
                <span className="nav-text">我的</span>
            </div>
        </div>
    );
}