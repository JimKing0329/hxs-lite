import { message } from 'antd';

const responseInterceptor = {
    install: () => {
        const originalFetch = window.fetch;
        
        window.fetch = async (...args) => {
            const response = await originalFetch(...args);
            
            // 检查401状态码
            if (response.status === 401) {
                message.error('登录过期');
                localStorage.removeItem('token');
                setTimeout(() => {
                    const isAdminPath = window.location.pathname.startsWith('/admin/');
window.location.href = isAdminPath ? '/admin/login' : '/';
                }, 1000);
                return Promise.reject('Unauthorized');
            }
            
            // 检查响应内容
            const clonedResponse = response.clone();
            const result = await clonedResponse.json().catch(() => ({}));
            
            if (result.msg && result.msg.includes('未登录')) {
                message.error('教务系统登录已过期');
                localStorage.removeItem('token');
                setTimeout(() => {
                    window.location.href = '/';
                }, 1000);
                return Promise.reject('Not logged in');
            }
            
            return response;
        };
    }
};

export default responseInterceptor;