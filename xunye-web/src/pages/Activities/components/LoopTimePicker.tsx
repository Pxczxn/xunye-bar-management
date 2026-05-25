import React from 'react';
import { ChevronUp, ChevronDown } from 'lucide-react';
import { type Dayjs } from 'dayjs';

interface LoopTimePickerProps {
  label: string;
  value: Dayjs | null;
  onChange: (newValue: Dayjs) => void;
  compact?: boolean;
}

interface ColumnInputProps {
  value: number;
  max: number;
  onChange: (val: number) => void;
}

function ColumnInput({ value, max, onChange }: ColumnInputProps) {
  const [localVal, setLocalVal] = React.useState(String(value).padStart(2, '0'));

  React.useEffect(() => {
    setLocalVal(String(value).padStart(2, '0'));
  }, [value]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const text = e.target.value;
    if (text === '') {
      setLocalVal('');
      return;
    }
    const val = parseInt(text, 10);
    if (!isNaN(val)) {
      const clamped = Math.max(0, Math.min(max, val));
      setLocalVal(text);
      onChange(clamped);
    }
  };

  const handleBlur = () => {
    setLocalVal(String(value).padStart(2, '0'));
  };

  return (
    <input
      type="text"
      value={localVal}
      onChange={handleChange}
      onBlur={handleBlur}
      className="w-full text-center bg-transparent border-none text-brand-gold font-mono font-bold text-xs outline-none p-0 focus:text-text-main"
    />
  );
}

export default function LoopTimePicker({ label, value, onChange, compact = false }: LoopTimePickerProps) {
  if (!value) return null;

  const hour = value.hour();
  const minute = value.minute();
  const second = value.second();

  const handleAdjust = (type: 'hour' | 'minute' | 'second', delta: number) => {
    let nextValue = value;
    if (type === 'hour') {
      const nextHour = (hour + delta + 24) % 24;
      nextValue = value.hour(nextHour);
    } else if (type === 'minute') {
      const nextMinute = (minute + delta + 60) % 60;
      nextValue = value.minute(nextMinute);
    } else if (type === 'second') {
      const nextSecond = (second + delta + 60) % 60;
      nextValue = value.second(nextSecond);
    }
    onChange(nextValue);
  };

  const handleWheel = (e: React.WheelEvent, type: 'hour' | 'minute' | 'second') => {
    e.preventDefault();
    const delta = e.deltaY < 0 ? 1 : -1;
    handleAdjust(type, delta);
  };

  const handleColumnChange = (type: 'hour' | 'minute' | 'second', val: number) => {
    let nextValue = value;
    if (type === 'hour') {
      nextValue = value.hour(val);
    } else if (type === 'minute') {
      nextValue = value.minute(val);
    } else if (type === 'second') {
      nextValue = value.second(val);
    }
    onChange(nextValue);
  };

  const renderColumn = (type: 'hour' | 'minute' | 'second', currentVal: number, max: number) => {
    return (
      <div 
        className="flex flex-col items-center bg-[#16161A] border border-border-dark/60 rounded-md p-0.5 w-8 xl:w-12 group/col"
        onWheel={(e) => handleWheel(e, type)}
      >
        <button
          type="button"
          onClick={() => handleAdjust(type, 1)}
          className="text-text-weak hover:text-brand-gold p-0 transition-colors cursor-pointer flex items-center justify-center"
        >
          <ChevronUp size={12} className="transition-transform group-hover/col:-translate-y-0.5" />
        </button>
        
        <ColumnInput
          value={currentVal}
          max={max}
          onChange={(newVal) => handleColumnChange(type, newVal)}
        />
        
        <button
          type="button"
          onClick={() => handleAdjust(type, -1)}
          className="text-text-weak hover:text-brand-gold p-0 transition-colors cursor-pointer flex items-center justify-center"
        >
          <ChevronDown size={12} className="transition-transform group-hover/col:translate-y-0.5" />
        </button>
      </div>
    );
  };

  return (
    <div className={`flex flex-col bg-[#111114] border border-border-dark/65 rounded-lg w-full select-none ${compact ? 'p-1.5' : 'p-2'}`}>
      <div className={`flex justify-between items-center ${compact ? 'mb-0.5' : 'mb-1'}`}>
        <span className="text-[9px] font-semibold text-text-sub tracking-wider uppercase">{label}</span>
        <span className="text-[10px] font-bold font-mono text-brand-gold">{value.format('HH:mm:ss')}</span>
      </div>
      {!compact && (
        <div className="flex justify-center items-center gap-1">
          {renderColumn('hour', hour, 23)}
          <span className="text-text-weak font-bold font-mono -mt-1">:</span>
          {renderColumn('minute', minute, 59)}
          <span className="text-text-weak font-bold font-mono -mt-1">:</span>
          {renderColumn('second', second, 59)}
        </div>
      )}
    </div>
  );
}
