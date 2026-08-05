import React, { useState, useEffect } from 'react';
import { Modal, Typography, message } from 'antd';
import { HeartOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import { API_PATHS } from '../constants/api';
import { authFetch } from '../utils/request';
import './SupportPage.css';

const { Title, Paragraph, Text } = Typography;

export default function SupportPage() {
  const history = useHistory();
  const [showMainModal, setShowMainModal] = useState(true);
  const [showReluctModal, setShowReluctModal] = useState(false);
  const [supportArticleUrl, setSupportArticleUrl] = useState(null);

  // 页面加载时预加载文章 URL
  useEffect(() => {
    preloadArticleUrl();
  }, []);

  // 预加载文章 URL
  const preloadArticleUrl = async () => {
    try {
      const response = await fetch(API_PATHS.PUBLIC.SUPPORT_ARTICLE);
      const result = await response.json();
      if (result.code === 1 && result.data?.url) {
        setSupportArticleUrl(result.data.url);
      }
    } catch (error) {
      console.error('预加载文章失败:', error);
    }
  };

  // 记录关闭弹窗（通过后端接口）
  const recordModalClosed = async () => {
    try {
      await authFetch(API_PATHS.SUPPORT_MODAL_CLOSE, { method: 'POST' });
    } catch (error) {
      console.error('记录关闭失败:', error);
    }
  };

  // 退出页面
  const handleExit = async () => {
    setShowReluctModal(false);
    await recordModalClosed();
    message.info('7天内不会再显示此弹窗');
    // 尝试关闭窗口或跳转首页
    setTimeout(() => {
      if (window.history.length > 1) {
        history.goBack();
      } else {
        history.push('/');
      }
    }, 500);
  };

  // 狠心拒绝 - 第一次
  const handleReject = () => {
    setShowMainModal(false);
    setShowReluctModal(true);
  };

  // 支持作者 - 直接使用缓存的 URL 秒跳
  const handleSupport = () => {
    if (!supportArticleUrl) {
      message.error('文章加载中，请稍后再试');
      return;
    }
    message.success('感谢你的支持！7天内不会再显示此弹窗');
    // 立即跳转
    window.location.href = supportArticleUrl;
    // 异步记录，不阻塞
    recordModalClosed();
    fetch(API_PATHS.PUBLIC.SUPPORT_CLICK, { method: 'POST' });
  };

  return (
    <div className="support-page">
      {/* 主弹窗 */}
      <Modal
        open={showMainModal}
        footer={null}
        closable={false}
        centered
        width={400}
        className="support-modal"
      >
        <div className="modal-content">
          <Title level={4} className="modal-title">各位使用河小狮的同学</Title>

          <div className="letter-box">
            <Paragraph className="letter-text">
              河小狮已经稳定运行一年有余，其实这个平台一开始只是方便自己的一个小工具，机缘巧合之下做成了现在这个较为完善的平台，感谢同学们一年以来的支持。
            </Paragraph>
            <Paragraph className="letter-text">
              这一年来的成本均由小狮个人承担，<Text strong>公众号文章广告是维系平台运转唯一的资金来源</Text>，支撑服务稳定、功能迭代与问题修复。
            </Paragraph>
            <Paragraph className="letter-text">
              由衷感谢大家长久的使用、反馈与陪伴，你们的反馈、认可、推荐都是对我最真挚的回报。
            </Paragraph>
            <Paragraph className="letter-text letter-highlight">
              恳请各位同学顺手点击广告施以微薄支持，你的每一次广告点击，都是支持平台开发的动力！❤️
            </Paragraph>
          </div>

          <button className="btn-support" onClick={handleSupport}>
            <HeartOutlined /> 点击广告支持作者
          </button>

          <button className="btn-reject" onClick={handleReject}>
            狠心拒绝
          </button>

          <p className="modal-hint">点击任一选项后，7天内不会再显示此弹窗</p>
        </div>
      </Modal>

      {/* 二次挽留弹窗 */}
      <Modal
        open={showReluctModal}
        footer={null}
        closable={false}
        centered
        width={360}
        className="support-modal reluctance-modal"
      >
        <div className="modal-content">
          <div className="icon-wrapper">
            <span className="sad-emoji">😢</span>
          </div>

          <Title level={4} className="modal-title">真的要走吗？</Title>

          <Paragraph className="modal-text">
            没有你的支持，河小狮可能撑不过今年...
          </Paragraph>

          <Paragraph className="modal-text">
            广告不会影响你的任何信息，只是增加一点点收入维持服务器运转 🙏
          </Paragraph>

          <button className="btn-support" onClick={handleSupport}>
            <HeartOutlined /> 好吧，支持一下
          </button>

          <button className="btn-reject" onClick={handleExit}>
            还是不了
          </button>

          <p className="modal-hint">关闭后，7天内不会再显示此弹窗</p>
        </div>
      </Modal>
    </div>
  );
}
