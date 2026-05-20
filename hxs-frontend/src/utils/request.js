/**
 * 统一请求工具 — 自动注入 Authorization: Bearer <token>
 * 同时兼容普通用户 token 和管理员 Authorization
 */

const TOKEN_KEY = 'token';
const ADMIN_TOKEN_KEY = 'Authorization';

/**
 * 获取当前有效的 Bearer token
 * 优先管理员 token，其次普通用户 token
 */
function getBearerToken() {
  const adminToken = localStorage.getItem(ADMIN_TOKEN_KEY);
  if (adminToken) return adminToken;
  const userToken = localStorage.getItem(TOKEN_KEY);
  if (userToken) return userToken;
  return null;
}

/**
 * 带认证头的 fetch 封装
 * 自动添加 Authorization: Bearer <token> 头
 */
export function authFetch(url, options = {}) {
  const token = getBearerToken();
  const headers = { ...options.headers };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // 如果未指定 Content-Type 且不是 GET，默认 json
  if (!headers['Content-Type'] && options.method && options.method !== 'GET') {
    headers['Content-Type'] = 'application/json';
  }

  return window.fetch(url, { ...options, headers });
}

/**
 * 检查是否已登录
 */
export function isAuthenticated() {
  return !!getBearerToken();
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem('token_expire');
  localStorage.removeItem(ADMIN_TOKEN_KEY);
}

/**
 * 存储登录 token
 */
export function saveToken(token, isAdmin = false) {
  clearAuth();
  localStorage.setItem(isAdmin ? ADMIN_TOKEN_KEY : TOKEN_KEY, token);
  localStorage.setItem('token_expire', Date.now() + 3600000);
}

export { TOKEN_KEY, ADMIN_TOKEN_KEY };
