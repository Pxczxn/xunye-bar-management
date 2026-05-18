import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Coffee,
  ChefHat,
  Receipt,
  Wine,
  Tags,
  Archive,
  LayoutGrid,
  Users,
  Settings,
  Crown,
  Gift,
} from 'lucide-react';

const MENU_ITEMS = [
  { label: '营业看板', sub: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['BOSS', 'MANAGER'] },
  { label: '吧台点单', sub: 'POS', path: '/pos', icon: Coffee, roles: ['BOSS', 'MANAGER', 'STAFF'] },
  { label: '出品看板', sub: 'Kitchen', path: '/kitchen', icon: ChefHat, roles: ['BOSS', 'MANAGER', 'STAFF'] },
  { label: '订单流水', sub: 'Orders', path: '/orders', icon: Receipt, roles: ['BOSS', 'MANAGER', 'STAFF'] },
  { label: '酒水管理', sub: 'Products', path: '/products', icon: Wine, roles: ['BOSS', 'MANAGER'] },
  { label: '商品分类', sub: 'Categories', path: '/categories', icon: Tags, roles: ['BOSS', 'MANAGER'] },
  { label: '库存预警', sub: 'Inventory', path: '/inventory', icon: Archive, roles: ['BOSS', 'MANAGER'] },
  { label: '会员管理', sub: 'Members', path: '/members', icon: Crown, roles: ['BOSS', 'MANAGER'] },
  { label: '活动管理', sub: 'Activities', path: '/activities', icon: Gift, roles: ['BOSS', 'MANAGER'] },
  { label: '桌台区域', sub: 'Tables', path: '/tables', icon: LayoutGrid, roles: ['BOSS', 'MANAGER', 'STAFF'] },
  { label: '员工账号', sub: 'Staff', path: '/employees', icon: Users, roles: ['BOSS'] },
  { label: '系统设置', sub: 'Settings', path: '/settings', icon: Settings, roles: ['BOSS'] },
];

function getUser() {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}');
  } catch {
    return {};
  }
}

export function Sidebar() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const user = getUser();

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login', { replace: true });
  };

  const avatarChar = (user.nickname || 'A').charAt(0);

  return (
    <aside className="w-[260px] bg-sidebar-bg border-r border-border-dark flex flex-col h-screen fixed left-0 top-0">
      {/* 品牌区 */}
      <div className="px-6 pt-7 pb-5">
        <h1 className="text-xl font-serif font-light text-brand-gold tracking-widest uppercase mb-0.5">
          XUNYE
        </h1>
        <p className="text-[9px] text-text-weak tracking-[0.2em] uppercase font-medium">
          Bar Management
        </p>
      </div>

      {/* 菜单区 */}
      <nav className="flex-1 overflow-y-auto px-3 py-2 hide-scrollbar">
        <div className="space-y-0.5">
          {MENU_ITEMS.filter(item => item.roles.includes(user.role || 'BOSS')).map((item) => {
            const isActive =
              pathname === item.path || pathname.startsWith(item.path + '/');
            const Icon = item.icon;

            return (
              <Link
                key={item.path}
                to={item.path}
                className={
                  'relative flex items-center gap-3 px-3 rounded-lg transition-colors group ' +
                  'h-[50px] ' +
                  (isActive
                    ? 'bg-brand-gold/10'
                    : 'hover:bg-card-bg')
                }
              >
                {/* 选中竖线 */}
                {isActive && (
                  <span className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-5 rounded-r bg-brand-gold" />
                )}

                {/* 图标 */}
                <Icon
                  size={18}
                  strokeWidth={isActive ? 2 : 1.5}
                  className={
                    isActive
                      ? 'text-brand-gold'
                      : 'text-text-sub group-hover:text-brand-gold transition-colors'
                  }
                />

                {/* 文字 */}
                <div className="flex flex-col min-w-0">
                  <span
                    className={
                      'text-sm tracking-wide leading-tight ' +
                      (isActive
                        ? 'text-text-main font-medium'
                        : 'text-text-sub group-hover:text-text-main transition-colors')
                    }
                  >
                    {item.label}
                  </span>
                  <span
                    className={
                      'text-[10px] tracking-wider leading-tight ' +
                      (isActive
                        ? 'text-brand-gold'
                        : 'text-text-weak group-hover:text-text-sub transition-colors')
                    }
                  >
                    {item.sub}
                  </span>
                </div>
              </Link>
            );
          })}
        </div>
      </nav>

      {/* 底部账号区 */}
      <div className="px-4 py-4 border-t border-border-dark">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full border border-border-dark/50 p-0.5 shrink-0">
            <div className="w-full h-full rounded-full bg-brand-gold/20 flex items-center justify-center">
              <span className="text-brand-gold text-[10px] font-serif italic">{avatarChar}</span>
            </div>
          </div>
          <div className="min-w-0">
            <p className="text-xs font-medium text-text-main tracking-wide">{user.nickname || 'Admin'}</p>
            <button
              onClick={handleLogout}
              className="text-[10px] text-text-weak uppercase tracking-widest hover:text-brand-gold transition-colors bg-transparent border-none cursor-pointer p-0"
            >
              退出登录
            </button>
          </div>
        </div>
      </div>
    </aside>
  );
}
