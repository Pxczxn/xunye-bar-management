import { useNavigate } from 'react-router-dom';
import { FormEvent, useState } from 'react';
import { App } from 'antd';
import { login } from '@/api/auth';

export default function LoginPage() {
  const { message } = App.useApp();
  const navigate = useNavigate();
  const [username, setUsername] = useState(import.meta.env.DEV ? 'admin' : '');
  const [password, setPassword] = useState(import.meta.env.DEV ? '123456' : '');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      message.warning('请输入账号和密码');
      return;
    }
    setLoading(true);
    try {
      const res = await login({ username: username.trim(), password: password.trim() });
      localStorage.setItem('token', res.token);
      localStorage.setItem('user', JSON.stringify(res.user));
      message.success('登录成功');
      navigate('/dashboard');
    } catch (err: any) {
      message.error(err.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-page-bg relative overflow-hidden">
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-brand-gold/5 rounded-full blur-[100px] pointer-events-none" />
      
      <div className="w-full max-w-md p-8 bg-card-bg border border-border-dark rounded-xl shadow-xl relative z-10 flex flex-col items-center">
        <div className="text-center mb-10">
          <h1 className="text-2xl font-serif font-bold text-text-main mb-2 tracking-wider">寻野酒吧管理系统</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Bar Management</p>
        </div>

        <form onSubmit={handleLogin} className="w-full space-y-6">
          <div>
            <label className="block text-[10px] uppercase tracking-widest text-text-sub font-semibold mb-2">
              账号 / Username
            </label>
            <input 
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="请输入用户名" 
              className="w-full bg-sidebar-bg border border-border-dark text-text-main px-4 py-3 rounded-lg focus:outline-none focus:border-brand-gold/50 transition-colors placeholder:text-text-weak text-sm tracking-wide"
              required
            />
          </div>

          <div>
            <label className="block text-[10px] uppercase tracking-widest text-text-sub font-semibold mb-2">
              密码 / Password
            </label>
            <input 
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="请输入密码" 
              className="w-full bg-sidebar-bg border border-border-dark text-text-main px-4 py-3 rounded-lg focus:outline-none focus:border-brand-gold/50 transition-colors placeholder:text-text-weak text-sm tracking-wide"
              required
            />
          </div>

          <button 
            type="submit"
            disabled={loading}
            className="w-full bg-brand-gold/10 text-brand-gold border border-brand-gold/30 font-bold py-3 rounded-lg mt-6 hover:bg-brand-gold hover:text-page-bg transition-colors uppercase tracking-widest text-xs disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? '登录中...' : '进入系统'}
          </button>
        </form>

        <div className="mt-8 text-center">
          <p className="text-text-weak font-serif italic text-[11px] tracking-widest">"乘兴而去，尽兴而归。"</p>
        </div>
      </div>
    </div>
  );
}
