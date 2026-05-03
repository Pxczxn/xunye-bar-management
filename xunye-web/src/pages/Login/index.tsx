import { useNavigate } from 'react-router-dom';
import { FormEvent, useState } from 'react';

export default function LoginPage() {
  const navigate = useNavigate();
  
  // 预填写账号密码，方便开发和演示阶段直接点击登录
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('123456');

  const handleLogin = (e: FormEvent) => {
    e.preventDefault();
    // 实际项目中这里需要调用登录API并保存token
    // 演示阶段直接跳转
    navigate('/dashboard');
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-page-bg relative overflow-hidden">
      {/* Background decorations */}
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-brand-gold/5 rounded-full blur-[100px] pointer-events-none" />
      
      <div className="w-full max-w-md p-8 bg-card-bg border border-border-dark rounded-xl shadow-xl relative z-10 flex flex-col items-center">
        <div className="text-center mb-10">
          <h1 className="text-2xl font-serif font-bold text-text-main mb-2 tracking-wider">XUNYE</h1>
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
            className="w-full bg-brand-gold/10 text-brand-gold border border-brand-gold/30 font-bold py-3 rounded-lg mt-6 hover:bg-brand-gold hover:text-page-bg transition-colors uppercase tracking-widest text-xs"
          >
            进入系统
          </button>
        </form>

        <div className="mt-8 text-center">
          <p className="text-text-weak font-serif italic text-[11px] tracking-widest">“乘兴而去，尽兴而归。”</p>
        </div>
      </div>
    </div>
  );
}
