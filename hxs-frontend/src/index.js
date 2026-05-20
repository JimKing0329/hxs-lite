import React from 'react';
import { Modal } from 'antd';
import ReactDOM from 'react-dom/client';
import App from './App';
import 'antd/dist/reset.css';
import responseInterceptor from './utils/responseInterceptor';

// 安装响应拦截器
responseInterceptor.install();


const root = ReactDOM.createRoot(document.getElementById('root'));
{
  root.render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
}