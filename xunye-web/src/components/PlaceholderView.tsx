import { Coffee } from 'lucide-react';

export function PlaceholderView({ title }: { title: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-40 px-4 rounded-xl border border-border-dark bg-card-bg shadow-xl">
      <div className="w-16 h-16 rounded-full bg-sidebar-bg border border-border-dark flex items-center justify-center mb-8 text-brand-gold shadow-lg">
        <Coffee size={24} strokeWidth={1.5} />
      </div>
      <h2 className="text-xl font-serif font-bold text-text-main mb-3 tracking-wide">{title}</h2>
      <p className="text-text-sub text-xs tracking-widest uppercase">功能正在建设中</p>
      <div className="mt-16 text-[10px] text-text-weak italic font-serif tracking-widest">
        "往事做序，来日为章。"
      </div>
    </div>
  );
}
