import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Form, Input, message } from 'antd';
import React from 'react';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { saveToken } from '../utils/request';
import './AdminLogin.css';

export default function AdminLogin() {
  const history = useHistory();
  const onFinish = async (values) => {
    try {
      const response = await fetch(API_PATHS.ADMIN_LOGIN, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(values),
      });

      const result = await response.json();

      if (result.code === 1) {
        saveToken(result.data.token, true);
        message.success('登录成功');
        history.push('/admin/dashboard');
      } else {
        message.error(result.msg || '登录失败');
      }
    } catch (error) {
      message.error('网络连接异常');
    }
  };

  return (
    <div className="login-container">
      <Card
        title="河小狮Lite管理系统"
        className="login-card"
        headStyle={{ fontSize: 24, textAlign: 'center' }}
      >
        <Form
          name="login"
          initialValues={{ remember: true }}
          onFinish={onFinish}
        >
          <Form.Item
            name="sid"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              size="large"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              size="large"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              size="large"
              className="login-btn"
            >
              登录
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
}