import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Layout, Card, List, Switch, Skeleton, Typography, Button, Spin } from 'antd';
import { ArrowLeftOutlined } from '@ant-design/icons';
import { useHistory } from 'react-router-dom';
import './FailRateRankPage.css';
import { API_PATHS } from '../constants/api';
import { authFetch } from '../utils/request';


const { Header, Content } = Layout;
const { Text } = Typography;

export default function FailRateRankPage() {
  const history = useHistory();
  const [failRateData, setFailRateData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [onlyExamined, setOnlyExamined] = useState(true);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const pageSize = 10;
  
  // 创建一个引用，用于观察元素
  const observer = useRef();
  // 创建一个引用，用于最后一个元素
  const lastElementRef = useRef();

  // 获取挂科率排行数据
  const fetchFailRateRank = async (pageNum = 1, reset = false) => {
    try {
      setLoading(true);
      const response = await authFetch(`${API_PATHS.SCORE.FAIL_RATE_RANK}?onlyExamined=${onlyExamined}&page=${pageNum}&num=${pageSize}`);
      const result = await response.json();
      if (result.code === 1) {
        if (result.data.length < pageSize) {
          setHasMore(false);
        } else {
          setHasMore(true);
        }
        
        if (reset) {
          setFailRateData(result.data);
          setPage(1);
        } else {
          setFailRateData(prev => [...prev, ...result.data]);
        }
      } else {
        setHasMore(false);
      }
    } catch (error) {
      console.error('获取挂科率排行失败:', error);
      setHasMore(false); // 出错时也设置没有更多数据
    } finally {
      setLoading(false);
    }
  };

  // 初始加载数据
  useEffect(() => {
    fetchFailRateRank(1, true);
  }, [onlyExamined]);

  // 加载更多数据
  const loadMore = useCallback(() => {
    if (!loading && hasMore) {
      const nextPage = page + 1;
      setPage(nextPage);
      fetchFailRateRank(nextPage);
    }
  }, [loading, hasMore, page]);
  
  // 设置 IntersectionObserver 来监听滚动
  useEffect(() => {
    // 如果正在加载或没有更多数据，不需要设置观察器
    if (loading || !hasMore) return;
    
    // 如果已经有观察器，先断开连接
    if (observer.current) observer.current.disconnect();
    
    // 创建新的观察器
    observer.current = new IntersectionObserver(entries => {
      // 如果最后一个元素可见，并且有更多数据可加载
      if (entries[0].isIntersecting && hasMore && !loading) {
        loadMore();
      }
    }, { threshold: 0.5 }); // 当元素50%可见时触发
    
    // 如果有最后一个元素的引用，开始观察它
    if (lastElementRef.current) {
      observer.current.observe(lastElementRef.current);
    }
    
    // 清理函数
    return () => {
      if (observer.current) observer.current.disconnect();
    };
  }, [loading, hasMore, loadMore, lastElementRef]);

  // 切换只看已考科目
  const handleSwitchChange = (checked) => {
    setOnlyExamined(checked);
    setPage(1);
  };

  // 获取挂科率对应的颜色
  const getFailRateColor = (rate) => {
    const percentage = rate * 100;
    if (percentage >= 40) return '#f5222d'; // 高挂科率 - 红色
    if (percentage >= 20) return '#fa8c16'; // 中挂科率 - 橙色
    return '#52c41a'; // 低挂科率 - 绿色
  };

  return (
    <Layout className="fail-rate-rank-container">
      <Header>
        <div className="header-content" style={{
          display: 'flex',
          alignItems: 'center',
          position: 'relative',
          padding: '0 16px',
          color: '#fff',
        }}>
          <Button 
            type="link" 
            icon={<ArrowLeftOutlined style={{ fontSize: '22px', color: '#fff' }} />} 
            onClick={() => history.goBack()} 
            style={{ position: 'absolute', left: 0 }}
          />
          <h1 style={{ margin: '0 auto', color: '#fff' }}>挂科率排名</h1>
        </div>
      </Header>
      <Content>
        <Card
          bordered={false}
          className="fail-rate-card"
        >
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center' }}>
              <Text style={{ marginRight: 8 }}>只显示本人已考科目</Text>
              <Switch checked={onlyExamined} onChange={handleSwitchChange} />
            </div>
          </div>
          
          <div style={{ display: 'flex', marginBottom: 16 }}>
            <div style={{ width: '60%', fontWeight: 'bold' }}>课程名称</div>
            <div style={{ width: '20%', textAlign: 'center', fontWeight: 'bold' }}>挂科率</div>
            <div style={{ width: '20%', textAlign: 'center', fontWeight: 'bold' }}>排名</div>
          </div>

          <Skeleton loading={loading && failRateData.length === 0} active>
            <List
            dataSource={failRateData}
            renderItem={(item, index) => {
              // 检查是否是最后一个元素
              const isLastElement = index === failRateData.length - 1;
              
              return (
                <List.Item 
                  className="fail-rate-item"
                  // 如果是最后一个元素，添加引用
                  ref={isLastElement ? lastElementRef : null}
                >
                  <div style={{ width: '60%', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {item.courseName}
                  </div>
                  <div style={{ width: '20%', textAlign: 'center' }}>
                    <span style={{
                      color: getFailRateColor(item.failRate),
                      fontWeight: 'bold'
                    }}>
                      {(item.failRate * 100).toFixed(2)}%
                    </span>
                    <div style={{ fontSize: '12px', color: '#888' }}>
                      总数: {item.allCount} 挂科: {item.failCount}
                    </div>
                  </div>
                  <div style={{ width: '20%', textAlign: 'center' }}>
                    <span style={{ 
                      fontSize: '18px', 
                      fontWeight: 'bold',
                      color: index < 3 ? '#f5222d' : '#1890ff'
                    }}>
                      #{index + 1}
                    </span>
                  </div>
                </List.Item>
              );
            }}
            />
            
            {loading && hasMore && (
               <div style={{ textAlign: 'center', marginTop: 16 }}>
                 <Spin tip="加载中..." />
               </div>
             )}
             {!hasMore && failRateData.length > 0 && (
              <div style={{ textAlign: 'center', marginTop: 16, color: '#888' }}>
                没有更多数据了
              </div>
            )}
            
            {!loading && failRateData.length === 0 && (
              <div style={{ textAlign: 'center', padding: 16, color: 'rgba(0,0,0,0.45)' }}>
                暂无挂科率数据
              </div>
            )}
          </Skeleton>
        </Card>
      </Content>
    </Layout>
  );
}