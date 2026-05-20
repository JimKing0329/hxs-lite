const handleLogin = async (values) => {
  try {
    const response = await fetch('/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(values),
    });
    
    const result = await response.json();
    if (result.code === 1) {
      // 登录成功
      if (values.remember) {
        localStorage.setItem('rememberedUsername', values.username);
        localStorage.setItem('rememberedPassword', values.password);
      } else {
        localStorage.removeItem('rememberedUsername');
        localStorage.removeItem('rememberedPassword');
      }
      // ... 其他登录成功逻辑
    }
  } catch (error) {
    // 错误处理
  }
};

useEffect(() => {
  const username = localStorage.getItem('rememberedUsername');
  const password = localStorage.getItem('rememberedPassword');
  if (username && password) {
    form.setFieldsValue({
      username,
      password,
      remember: true
    });
  }
}, []);