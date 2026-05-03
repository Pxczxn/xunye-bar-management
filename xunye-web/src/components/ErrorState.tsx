import { AlertCircle } from 'lucide-react';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export function ErrorState({ message = '接口请求失败，请稍后重试', onRetry }: ErrorStateProps) {
  return (
    <div className="bg-danger/10 border border-danger/20 rounded-xl p-6 flex flex-col items-center justify-center text-center">
      <div className="w-12 h-12 rounded-full bg-danger/20 flex items-center justify-center text-danger mb-4">
        <AlertCircle size={24} />
      </div>
      <p className="text-text-main font-medium mb-1">加载失败</p>
      <p className="text-text-sub text-sm mb-6 max-w-sm">{message}</p>
      {onRetry && (
        <button 
          onClick={onRetry}
          className="px-6 py-2 bg-card-bg border border-border-dark rounded-lg text-sm text-text-main hover:bg-border-dark transition-colors"
        >
          重新尝试
        </button>
      )}
    </div>
  );
}
