import { useEffect } from 'react';
import { Percent, Ticket, Coins, Flame } from 'lucide-react';

type ActivityType = 'DISCOUNT' | 'COUPON' | 'POINTS' | 'SPECIAL';

interface ActivityTypeGridProps {
  value?: ActivityType;
  onChange?: (value: ActivityType) => void;
}

export default function ActivityTypeGrid({ value, onChange }: ActivityTypeGridProps) {
  useEffect(() => {
    console.log('ActivityTypeGrid mounted/updated, value:', value, 'onChange:', !!onChange);
  }, [value, onChange]);

  const options = [
    { label: '折扣活动', desc: '满减打折，刺激消费', value: 'DISCOUNT', icon: Percent },
    { label: '优惠礼券', desc: '发放抵扣券，提高复购', value: 'COUPON', icon: Ticket },
    { label: '双倍积分', desc: '积分翻倍，提升粘性', value: 'POINTS', icon: Coins },
    { label: '特惠单品', desc: '爆款单品，限量特价', value: 'SPECIAL', icon: Flame },
  ] as const;

  return (
    <div className="grid grid-cols-2 gap-2.5">
      {options.map((opt) => {
        const Icon = opt.icon;
        const isActive = value === opt.value;
        return (
          <button
            key={opt.value}
            type="button"
            onClick={() => {
              console.log('ActivityTypeGrid clicked:', opt.value, 'onChange exists:', !!onChange);
              onChange?.(opt.value);
            }}
            className={`flex flex-col items-start p-2.5 rounded-lg border text-left transition-all duration-300 relative overflow-hidden group select-none ${
              isActive
                ? 'border-brand-gold bg-brand-gold/10 shadow-lg shadow-brand-gold/5'
                : 'border-border-dark bg-[#111114] hover:border-brand-gold/40 hover:bg-[#15151A]'
            }`}
          >
            {isActive && (
              <div className="absolute right-0 top-0 w-7 h-7 bg-brand-gold/15 rounded-bl-lg flex items-center justify-center border-l border-b border-brand-gold/20">
                <div className="w-1.5 h-1.5 rounded-full bg-brand-gold animate-pulse"></div>
              </div>
            )}
            
            <div className={`p-1.5 rounded-md mb-1.5 transition-all duration-300 ${
              isActive ? 'bg-brand-gold text-page-bg font-bold scale-105' : 'bg-page-bg text-brand-gold group-hover:scale-105'
            }`}>
              <Icon size={14} />
            </div>
            
            <div className={`text-xs font-bold transition-colors ${isActive ? 'text-brand-gold' : 'text-text-main'}`}>
              {opt.label}
            </div>
            <div className="text-[9px] text-text-weak mt-0.5 line-clamp-1 w-full truncate">
              {opt.desc}
            </div>
          </button>
        );
      })}
    </div>
  );
}
