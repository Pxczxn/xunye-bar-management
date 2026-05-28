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
  Ticket,
  Award,
  Percent,
  ChevronDown,
  ChevronRight,
} from 'lucide-react';
import { useState } from 'react';

interface MenuItem {
  label: string;
  sub: string;
  path: string;
  icon: any;
  roles: string[];
}

interface MenuGroup {
  label: string;
  sub: string;
  icon: any;
  roles: string[];
  children: MenuItem[];
}

const MENU_ITEMS: (MenuItem | MenuGroup)[] = [
  { label: '营业看板', sub: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: ['BOSS', 'MANAGER'] },

  // 订单管理分组
  {
    label: '订单管理',
    sub: 'Orders',
    icon: Receipt,
    roles: ['BOSS', 'MANAGER', 'STAFF'],
    children: [
      { label: '吧台点单', sub: 'POS', path: '/pos', icon: Coffee, roles: ['BOSS', 'MANAGER', 'STAFF'] },
      { label: '出品看板', sub: 'Kitchen', path: '/kitchen', icon: ChefHat, roles: ['BOSS', 'MANAGER', 'STAFF'] },
      { label: '订单流水', sub: 'History', path: '/orders', icon: Receipt, roles: ['BOSS', 'MANAGER', 'STAFF'] },
    ],
  },

  // 商品管理分组
  {
    label: '商品管理',
    sub: 'Products',
    icon: Wine,
    roles: ['BOSS', 'MANAGER'],
    children: [
      { label: '酒水管理', sub: 'Drinks', path: '/products', icon: Wine, roles: ['BOSS', 'MANAGER'] },
      { label: '商品分类', sub: 'Categories', path: '/categories', icon: Tags, roles: ['BOSS', 'MANAGER'] },
      { label: '库存预警', sub: 'Inventory', path: '/inventory', icon: Archive, roles: ['BOSS', 'MANAGER'] },
      { label: '桌台区域', sub: 'Tables', path: '/tables', icon: LayoutGrid, roles: ['BOSS', 'MANAGER'] },
    ],
  },

  // 会员管理分组
  {
    label: '会员管理',
    sub: 'Members',
    icon: Crown,
    roles: ['BOSS', 'MANAGER'],
    children: [
      { label: '会员列表', sub: 'List', path: '/members', icon: Crown, roles: ['BOSS', 'MANAGER'] },
      { label: '会员等级', sub: 'Levels', path: '/member-level-configs', icon: Award, roles: ['BOSS', 'MANAGER'] },
    ],
  },

  // 营销管理分组
  {
    label: '营销管理',
    sub: 'Marketing',
    icon: Gift,
    roles: ['BOSS', 'MANAGER'],
    children: [
      { label: '活动管理', sub: 'Activities', path: '/activities', icon: Gift, roles: ['BOSS', 'MANAGER'] },
      { label: '优惠券管理', sub: 'Coupons', path: '/coupon-templates', icon: Ticket, roles: ['BOSS', 'MANAGER'] },
      { label: '折扣规则', sub: 'Discounts', path: '/discount-rules', icon: Percent, roles: ['BOSS', 'MANAGER'] },
    ],
  },

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

function isMenuGroup(item: MenuItem | MenuGroup): item is MenuGroup {
  return 'children' in item;
}

export function Sidebar() {
  const { pathname } = useLocation();
  const navigate = useNavigate();
  const user = getUser();
  const [expandedGroups, setExpandedGroups] = useState<string[]>(['订单管理', '商品管理', '会员管理', '营销管理']);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login', { replace: true });
  };

  const toggleGroup = (label: string) => {
    setExpandedGroups(prev =>
      prev.includes(label) ? prev.filter(g => g !== label) : [...prev, label]
    );
  };

  const avatarChar = (user.nickname || 'A').charAt(0);

  const renderMenuItem = (item: MenuItem) => {
    const isActive = pathname === item.path || pathname.startsWith(item.path + '/');
    const Icon = item.icon;

    return (
      <Link
        key={item.path}
        to={item.path}
        className={
          'relative flex items-center gap-3 px-3 rounded-lg transition-colors group h-[44px] ' +
          (isActive ? 'bg-brand-gold/10' : 'hover:bg-card-bg')
        }
      >
        {isActive && (
          <span className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-4 rounded-r bg-brand-gold" />
        )}
        <Icon
          size={16}
          strokeWidth={isActive ? 2 : 1.5}
          className={
            isActive
              ? 'text-brand-gold'
              : 'text-text-sub group-hover:text-brand-gold transition-colors'
          }
        />
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
              'text-[9px] tracking-wider leading-tight ' +
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
  };

  const renderMenuGroup = (group: MenuGroup) => {
    const isExpanded = expandedGroups.includes(group.label);
    const isAnyChildActive = group.children.some(
      child => pathname === child.path || pathname.startsWith(child.path + '/')
    );
    const Icon = group.icon;
    const ChevronIcon = isExpanded ? ChevronDown : ChevronRight;

    return (
      <div key={group.label}>
        <button
          onClick={() => toggleGroup(group.label)}
          className={
            'w-full relative flex items-center gap-3 px-3 rounded-lg transition-colors group h-[44px] ' +
            (isAnyChildActive ? 'bg-brand-gold/5' : 'hover:bg-card-bg')
          }
        >
          <Icon
            size={16}
            strokeWidth={isAnyChildActive ? 2 : 1.5}
            className={
              isAnyChildActive
                ? 'text-brand-gold'
                : 'text-text-sub group-hover:text-brand-gold transition-colors'
            }
          />
          <div className="flex flex-col min-w-0 flex-1">
            <span
              className={
                'text-sm tracking-wide leading-tight text-left ' +
                (isAnyChildActive
                  ? 'text-text-main font-medium'
                  : 'text-text-sub group-hover:text-text-main transition-colors')
              }
            >
              {group.label}
            </span>
            <span
              className={
                'text-[9px] tracking-wider leading-tight text-left ' +
                (isAnyChildActive
                  ? 'text-brand-gold'
                  : 'text-text-weak group-hover:text-text-sub transition-colors')
              }
            >
              {group.sub}
            </span>
          </div>
          <ChevronIcon
            size={14}
            className={
              isAnyChildActive
                ? 'text-brand-gold'
                : 'text-text-weak group-hover:text-text-sub transition-colors'
            }
          />
        </button>
        {isExpanded && (
          <div className="ml-6 mt-1 space-y-0.5">
            {group.children
              .filter(child => child.roles.includes(user.role || 'BOSS'))
              .map(renderMenuItem)}
          </div>
        )}
      </div>
    );
  };

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
            if (isMenuGroup(item)) {
              return renderMenuGroup(item);
            }
            return renderMenuItem(item);
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
