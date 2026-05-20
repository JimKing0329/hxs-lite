import React, { useState, useEffect } from 'react';
import { message } from 'antd';
import { Typography } from 'antd';
import './MobileLogin.css';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { encrypt, decrypt } from '../utils/crypto';
import { saveToken } from '../utils/request';
import { EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons';

export default function MobileLogin() {
  const history = useHistory();

  const [sid, setSid] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [loading, setLoading] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  const [countdown, setCountdown] = useState(0);

  // 在组件加载时检查是否有保存的登录信息
  useEffect(() => {
    const savedSid = localStorage.getItem('rememberedSid');
    const savedPassword = localStorage.getItem('rememberedPassword');
    if (savedSid && savedPassword) {
      try {
        setSid(savedSid);
        setPassword(decrypt(savedPassword));
        setRemember(true);
      } catch (error) {
        console.error('Failed to decrypt saved password:', error);
        // 如果解密失败，清除保存的信息
        localStorage.removeItem('rememberedSid');
        localStorage.removeItem('rememberedPassword');
      }
    }
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setCountdown(10); // 设置10秒倒计时

    // 倒计时逻辑
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          if (loading) { // 如果还在加载状态，则超时
            setLoading(false);
            message.error('登录超时，请检查网络连接后重试');
          }
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    try {
      const response = await fetch(API_PATHS.LOGIN, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          sid,
          password,
        })
      });

      clearInterval(timer); // 请求完成后清除倒计时
      setCountdown(0);

      const result = await response.json();

      if (result.code === 1) {
        // 如果选择了"保持登录"，保存登录信息
        if (remember) {
          localStorage.setItem('rememberedSid', sid);
          localStorage.setItem('rememberedPassword', encrypt(password));
        } else {
          // 如果没有选择"保持登录"，清除保存的信息
          localStorage.removeItem('rememberedSid');
          localStorage.removeItem('rememberedPassword');
        }

        saveToken(result.data.token);

        message.success('登录成功');
        setTimeout(() => history.push('/dashboard'), 500);
      } else {
        message.error(result.msg || '登录失败');
        setLoading(false);
      }
    } catch (error) {
      clearInterval(timer); // 网络错误时也清除倒计时
      setCountdown(0);
      message.error('网络连接异常');
      setLoading(false);
    }
  };

  return (
    <div className="mobile-container">
      <div className="brand-header">
        <h1>河小狮lite</h1>
        <p>教务查询系统</p>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="input-field">
          <input
            type="tel"
            value={sid}
            onChange={(e) => setSid(e.target.value.replace(/[^0-9]/g, ''))}
            placeholder="学号/工号"
            pattern="[0-9]*"
            required
          />
        </div>

        <div className="input-field password-wrapper">
          <input
            type={isPasswordVisible ? "text" : "password"}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="密码"
            required
          />
          <span
            className="toggle-password"
            onClick={() => setIsPasswordVisible(!isPasswordVisible)}
          >
            {isPasswordVisible ? <EyeOutlined/> : <EyeInvisibleOutlined/>}
          </span>
        </div>

        <div className="remember-section">
          <label>
            <input
              type="checkbox"
              checked={remember}
              onChange={(e) => setRemember(e.target.checked)}
            />
            记住密码
          </label>
        </div>

        {/* 添加隐私声明 */}
        <div className="privacy-notice">
          <Typography.Text type="secondary">
            🔒 您的密码会经过加密处理，本平台不会存储您的明文密码及其他个人信息
          </Typography.Text>
          <br/><br/>
          <Typography.Text type="secondary">
            ❗ 2025级新同学如果登录失败，可<a href="http://jwgl.hebtu.edu.cn/xtgl/login_slogin.html">点击进入河北师大教务管理系统</a>修改密码后重新尝试登录（默认密码为身份证后六或八位）
          </Typography.Text>
        </div>

        <button
          type="submit"
          className="login-btn"
          disabled={loading}
        >
          {loading ? (countdown > 0 ? `登录中(${countdown})...` : '登录中...') : '立即登录'}
        </button>
      </form>
    </div>
  );
}