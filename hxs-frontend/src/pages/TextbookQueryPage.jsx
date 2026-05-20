import React, { useState, useEffect } from 'react';
import { Layout, Card, List, Select, Button, message, Modal, Typography, Space, Divider } from 'antd';
import { CopyOutlined, BookOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import './TextbookQueryPage.css';
import { API_PATHS } from '../constants/api';


const { Header, Content } = Layout;
const { Option } = Select;
const { Text, Paragraph } = Typography;

export default function TextbookQueryPage() {
  const history = useHistory();
  const [selectedYear, setSelectedYear] = useState(new Date().getFullYear().toString());
  const [selectedTerm, setSelectedTerm] = useState('1');
  const [textbooks, setTextbooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [selectedBook, setSelectedBook] = useState(null);

  // 组件挂载时自动查询当前学年和第一学期的教材
  useEffect(() => {
    if (selectedYear && selectedTerm) {
      handleQuery();
    }
  }, []); // 空依赖数组，只在组件挂载时执行一次

  // 生成年份选项：当前年份-4到当前年份+4
  const getYearOptions = () => {
    const currentYear = new Date().getFullYear();
    const years = [];
    for (let year = currentYear - 4; year <= currentYear + 4; year++) {
      years.push(year);
    }
    return years;
  };

  const handleQuery = async () => {
    if (!selectedYear || !selectedTerm) {
      message.warning('请选择学年和学期');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(`${API_PATHS.GET_TEXT_BOOK}?year=${selectedYear}&term=${selectedTerm}`, {
        headers: {
          'token': localStorage.getItem('token'),
          'Content-Type': 'application/json'
        }
      });
      const result = await response.json();
      
      if (result.code === 1) {
        setTextbooks(result.data || []);
        if (!result.data || result.data.length === 0) {
          message.info('该学期暂无教材信息');
        }
      } else {
        message.error(result.msg || '获取教材信息失败!');
      }
    } catch (error) {
      console.error('获取教材信息失败:', error);
      message.error('获取教材信息失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCopyISBN = (isbn) => {
    // 检查是否支持Clipboard API
    if (navigator.clipboard && window.isSecureContext) {
      // 使用Clipboard API (现代浏览器)
      navigator.clipboard.writeText(isbn).then(() => {
        message.success('ISBN已复制到剪贴板');
      }).catch(err => {
        console.error('复制失败:', err);
        // 如果Clipboard API失败，使用fallback方法
        fallbackCopyTextToClipboard(isbn);
      });
    } else {
      // 使用fallback方法 (兼容旧浏览器和部分移动浏览器)
      fallbackCopyTextToClipboard(isbn);
    }
  };

  // Fallback复制方法
  const fallbackCopyTextToClipboard = (text) => {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    
    // 避免滚动到底部
    textArea.style.top = '0';
    textArea.style.left = '0';
    textArea.style.position = 'fixed';
    textArea.style.opacity = '0';
    
    document.body.appendChild(textArea);
    textArea.focus();
    textArea.select();
    
    try {
      const successful = document.execCommand('copy');
      if (successful) {
        message.success('ISBN已复制到剪贴板');
      } else {
        message.error('复制失败');
      }
    } catch (err) {
      console.error('复制失败:', err);
      message.error('复制失败');
    }
    
    document.body.removeChild(textArea);
  };

  const showBookDetail = (book) => {
    setSelectedBook(book);
    setModalVisible(true);
  };

  return (
    <Layout className="textbook-query-container">
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
                    <h1 style={{ margin: 0 }}>教材查询</h1>
                </div>
      </Header>

      <Content className="textbook-content">
        <Card className="query-card">
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <div>
              <Text strong>选择学年：</Text>
              <Select
                placeholder="请选择学年"
                style={{ width: '100%', marginTop: 8 }}
                value={selectedYear}
                onChange={setSelectedYear}
              >
                {getYearOptions().map(year => (
                  <Option key={year} value={year.toString()}>
                    {year}学年
                  </Option>
                ))}
              </Select>
            </div>

            <div>
              <Text strong>选择学期：</Text>
              <Select
                placeholder="请选择学期"
                style={{ width: '100%', marginTop: 8 }}
                value={selectedTerm}
                onChange={setSelectedTerm}
              >
                <Option value="1">第1学期</Option>
                <Option value="2">第2学期</Option>
              </Select>
            </div>

            <Button
              type="primary"
              block
              loading={loading}
              onClick={handleQuery}
              disabled={!selectedYear || !selectedTerm}
            >
              查询教材
            </Button>
          </Space>
        </Card>

        <Card 
          title={
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <BookOutlined style={{ marginRight: 8 }} />
              <span>教材列表</span>
            </div>
          }
          className="textbook-list-card"
        >
          {textbooks.length > 0 ? (
            <>
              <Paragraph type="secondary" style={{ fontSize: 12, marginBottom: 16 }}>
                点击课程可查看详细信息，ISBN码可快捷复制<br/>
                (仅展示有教材信息的课程)
              </Paragraph>
              <List
                dataSource={textbooks}
                renderItem={item => (
                  <List.Item
                    className="textbook-item"
                    onClick={() => showBookDetail(item)}
                    style={{ padding: '12px 0' }}
                  >
                    <List.Item.Meta
                      title={
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                          <Text strong style={{ fontSize: 16 }}>{item.courseName}</Text>
                          <BookOutlined style={{ color: '#1890ff', fontSize: 16 }} />
                        </div>
                      }
                      description={
                        <div style={{ marginTop: 8 }}>
                          <Text style={{ fontSize: 14, color: '#666' }}>{item.bookName}</Text>
                          <div style={{ marginTop: 4, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <Text type="secondary" style={{ fontSize: 12 }}>
                              {item.press}
                            </Text>
                            <Button
                              type="text"
                              size="small"
                              icon={<CopyOutlined />}
                              onClick={(e) => {
                                e.stopPropagation();
                                handleCopyISBN(item.isbn);
                              }}
                              style={{ padding: 0, height: 'auto' }}
                            >
                              {item.isbn}
                            </Button>
                          </div>
                        </div>
                      }
                    />
                  </List.Item>
                )}
              />
            </>
          ) : (
            <div style={{ textAlign: 'center', padding: 40, color: '#999' }}>
              <BookOutlined style={{ fontSize: 48, marginBottom: 16 }} />
              <p>请选择学年和学期查询教材</p>
            </div>
          )}
        </Card>

      </Content>

      <Modal
        title="教材详情"
        visible={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        style={{ top: 20 }}
      >
        {selectedBook && (
          <div style={{ padding: 8 }}>
            <Space direction="vertical" size="middle" style={{ width: '100%' }}>
              <div>
                <Text strong>课程名称：</Text>
                <Text>{selectedBook.courseName}</Text>
              </div>
              
              <div>
                <Text strong>教材名称：</Text>
                <Text>{selectedBook.bookName}</Text>
              </div>
              
              <div>
                <Text strong>版　　本：</Text>
                <Text>{selectedBook.edition}</Text>
              </div>
              
              <div>
                <Text strong>作　　者：</Text>
                <Text>{selectedBook.author}</Text>
              </div>
              
              <div>
                <Text strong>出版社：</Text>
                <Text>{selectedBook.press}</Text>
              </div>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <Text strong>ISBN：</Text>
                  <Text code>{selectedBook.isbn}</Text>
                </div>
                <Button
                  type="primary"
                  icon={<CopyOutlined />}
                  size="small"
                  onClick={() => handleCopyISBN(selectedBook.isbn)}
                >
                  复制
                </Button>
              </div>
            </Space>
          </div>
        )}
      </Modal>

    </Layout>
  );
}