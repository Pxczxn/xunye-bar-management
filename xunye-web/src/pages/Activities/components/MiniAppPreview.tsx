import { useMemo } from 'react';
import { Percent, Ticket, Coins, Flame, Smartphone, Sparkles } from 'lucide-react';
import { type Dayjs } from 'dayjs';
import type { ActivitySettings } from '@/types/api';

type ActivityType = 'DISCOUNT' | 'COUPON' | 'POINTS' | 'SPECIAL';

interface MiniAppPreviewProps {
  title: string;
  type: ActivityType;
  settings: ActivitySettings;
  description: string;
  startDate: Dayjs | null;
  endDate: Dayjs | null;
}

export default function MiniAppPreview({ title, type, settings, description, startDate, endDate }: MiniAppPreviewProps) {
  const displayTitle = title?.trim() || '未命名活动';
  const displayDesc = description?.trim() || '活动说明：暂无详细描述信息';
  
  const dateStr = useMemo(() => {
    if (!startDate || !endDate) return '未设置活动时间';
    return `${startDate.format('MM.DD HH:mm')} - ${endDate.format('MM.DD HH:mm')}`;
  }, [startDate, endDate]);

  return (
    <div className="w-full rounded-xl border border-border-dark bg-[#08080C] p-3 shadow-2xl relative overflow-hidden select-none">
      {/* Phone status bar simulation */}
      <div className="flex justify-between items-center mb-2 text-[10px] text-text-weak font-sans px-1 border-b border-border-dark/30 pb-1.5">
        <div className="flex items-center gap-1.5">
          <Smartphone size={10} className="text-brand-gold" />
          <span className="font-semibold text-text-sub">微信小程序预览</span>
        </div>
        <div className="flex items-center gap-1.5 font-mono text-[9px]">
          <span>5G</span>
          <div className="w-5 h-2.5 border border-text-weak/40 rounded-sm p-0.5 flex items-center">
            <div className="w-full h-full bg-text-weak/80 rounded-2xs"></div>
          </div>
        </div>
      </div>
      
      {/* App Bar title */}
      <div className="hidden">
        <div className="text-[11px] font-bold text-text-main tracking-widest">寻野酒吧 XUNYE BAR</div>
        <div className="text-[8px] text-brand-gold/60 font-serif mt-0.5 tracking-wider uppercase">EXCLUSIVE OFFER</div>
      </div>

      {/* Main card design based on type */}
      <div className="relative overflow-hidden rounded-xl border border-brand-gold/20 bg-gradient-to-br from-[#18181D] to-[#0E0E12] p-3.5 h-[118px] flex flex-col justify-between transition-all duration-300">
        
        {/* Glow effect */}
        <div className="absolute -right-8 -top-8 w-24 h-24 bg-brand-gold/5 rounded-full blur-2xl"></div>
        
        {/* Card Top: Type Label and Icon */}
        <div className="flex justify-between items-start mb-2 relative z-10">
          <div className="flex items-center gap-2">
            <span className="px-1.5 py-0.5 text-[9px] rounded font-bold bg-brand-gold/15 text-brand-gold border border-brand-gold/25">
              {type === 'DISCOUNT' && '打折特惠'}
              {type === 'COUPON' && '限时礼券'}
              {type === 'POINTS' && '积分翻倍'}
              {type === 'SPECIAL' && '精选单品'}
            </span>
          </div>
          <div className="text-brand-gold/50">
            {type === 'DISCOUNT' && <Percent size={16} />}
            {type === 'COUPON' && <Ticket size={16} />}
            {type === 'POINTS' && <Coins size={16} />}
            {type === 'SPECIAL' && <Flame size={16} />}
          </div>
        </div>

        {/* Card Middle: Main content area */}
        <div className="mb-2 relative z-10 flex-1 flex flex-col justify-center">
          {type === 'COUPON' && (
            <div className="flex items-center gap-3">
              <div className="text-center pr-3 border-r border-brand-gold/20 shrink-0">
                <span className="text-[10px] text-brand-gold font-bold">¥</span>
                <span className="text-2xl font-bold font-serif text-brand-gold tracking-tight ml-0.5">
                  {settings.discountAmount !== undefined ? settings.discountAmount : 20}
                </span>
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-xs font-semibold text-text-main line-clamp-1 truncate">{displayTitle}</h4>
                <p className="text-[9px] text-text-sub mt-0.5">
                  使用门槛：满 {settings.minAmount !== undefined ? settings.minAmount : 100} 元可用
                </p>
              </div>
            </div>
          )}

          {type === 'DISCOUNT' && (
            <div className="flex items-center gap-3">
              <div className="text-center pr-3 border-r border-brand-gold/20 shrink-0">
                <span className="text-2xl font-bold font-serif text-brand-gold tracking-tight">
                  {settings.discountRate !== undefined ? settings.discountRate : 8.8}
                </span>
                <span className="text-[10px] text-brand-gold font-bold ml-0.5">折</span>
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-xs font-semibold text-text-main line-clamp-1 truncate">{displayTitle}</h4>
                <p className="text-[9px] text-text-sub mt-0.5">
                  使用门槛：{settings.minAmount && settings.minAmount > 0 ? `满 ${settings.minAmount} 元可用` : '无门槛限制'}
                </p>
              </div>
            </div>
          )}

          {type === 'POINTS' && (
            <div className="flex items-center gap-3">
              <div className="text-center pr-3 border-r border-brand-gold/20 shrink-0">
                <span className="text-2xl font-bold font-serif text-brand-gold tracking-tight">
                  {settings.pointsMultiplier !== undefined ? settings.pointsMultiplier : 2}
                </span>
                <span className="text-[10px] text-brand-gold font-bold ml-0.5">倍</span>
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-xs font-semibold text-text-main line-clamp-1 truncate">{displayTitle}</h4>
                <p className="text-[9px] text-text-sub mt-0.5">全场订单享多倍积分加速</p>
              </div>
            </div>
          )}

          {type === 'SPECIAL' && (
            <div className="flex items-center gap-3 min-h-0">
              <div className="w-9 h-9 rounded bg-brand-gold/5 border border-brand-gold/10 flex items-center justify-center shrink-0">
                <Flame size={16} className="text-brand-gold/50 animate-pulse" />
              </div>
              <div className="flex-1 min-w-0">
                <h4 className="text-xs font-semibold text-text-main line-clamp-1 truncate">{displayTitle}</h4>
                <div className="flex min-w-0 items-baseline gap-1.5 mt-0.5">
                  <span className="text-sm font-bold text-brand-gold">
                    ¥{settings.specialPrice !== undefined ? settings.specialPrice : 88}
                  </span>
                  {settings.originalPrice !== undefined && (
                    <span className="text-[9px] text-text-weak line-through">
                      ¥{settings.originalPrice}
                    </span>
                  )}
                </div>
                {settings.stockLimit !== undefined && (
                  <p className="text-[8px] text-text-sub mt-0.5 hidden">
                    限量抢购：剩余 {settings.stockLimit} 份
                  </p>
                )}
              </div>
            </div>
          )}
        </div>

        {/* Card Bottom: Expiry Date */}
        <div className="hidden">
          <div className="min-w-0 flex-1 truncate text-[8px] text-text-weak font-mono tracking-wider">
            有效期：{dateStr}
          </div>
          <button type="button" className="h-5 shrink-0 rounded bg-brand-gold px-2 text-[8px] font-bold text-page-bg hover:brightness-110 transition-all select-none">
            {type === 'COUPON' ? '立即领取' : type === 'SPECIAL' ? '立即抢购' : '去参与'}
          </button>
        </div>

        {/* Ticket side cutouts for Coupon type */}
        {type === 'COUPON' && (
          <>
            <div className="absolute left-[-6px] top-[60%] -translate-y-1/2 w-3 h-3 bg-[#08080C] rounded-full border-r border-brand-gold/20 z-20"></div>
            <div className="absolute right-[-6px] top-[60%] -translate-y-1/2 w-3 h-3 bg-[#08080C] rounded-full border-l border-brand-gold/20 z-20"></div>
          </>
        )}
      </div>

      {/* Description text mock */}
      <div className="mt-2.5 px-1">
        <div className="text-[9px] font-semibold text-text-sub flex items-center gap-1 mb-1">
          <Sparkles size={8} className="text-brand-gold" />
          <span>活动详情</span>
        </div>
        <p className="text-[8.5px] text-text-weak leading-normal line-clamp-2">
          {displayDesc}
        </p>
      </div>
    </div>
  );
}
