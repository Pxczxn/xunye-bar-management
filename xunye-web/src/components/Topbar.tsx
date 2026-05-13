import { Bell } from 'lucide-react';

const ROLE_MAP: Record<string, string> = {
  BOSS: '老板',
  MANAGER: '店长',
  STAFF: '员工',
};

function getUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}');
  } catch {
    return {};
  }
}

export function Topbar() {
  const user = getUser();
  const currentDate = new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  });

  const avatarChar = (user.nickname || 'A').charAt(0);
  const roleText = ROLE_MAP[user.role] || '店长';

  return (
    <header className="h-16 border-b border-border-dark flex items-center justify-between px-8 bg-page-bg/80 backdrop-blur-md sticky top-0 z-10">
      <div>
        <h2 className="text-sm font-medium text-text-main">今晚的寻野，也要有序运转。</h2>
      </div>
      
      <div className="flex items-center space-x-4">
        <div className="text-right">
          <p className="text-xs text-text-main">{user.nickname || 'Admin'}</p>
          <p className="text-[10px] text-text-weak">{roleText}: {currentDate}</p>
        </div>
        <div className="w-10 h-10 rounded-full border-2 border-brand-gold bg-card-bg flex items-center justify-center font-bold text-brand-gold relative">
          {avatarChar}
          <span className="absolute top-0 right-0 w-2.5 h-2.5 rounded-full bg-danger border-2 border-card-bg" />
        </div>
      </div>
    </header>
  );
}
