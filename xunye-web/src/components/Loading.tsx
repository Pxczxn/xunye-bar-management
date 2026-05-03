import { Loader2 } from 'lucide-react';

export function Loading() {
  return (
    <div className="flex flex-col items-center justify-center p-12">
      <Loader2 className="w-8 h-8 text-brand-gold animate-spin mb-4" />
      <p className="text-text-sub text-sm">数据加载中...</p>
    </div>
  );
}
