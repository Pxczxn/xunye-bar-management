import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

interface PaginationProps {
  current: number;
  pageSize: number;
  total: number;
  onChange: (page: number, pageSize: number) => void;
  pageSizeOptions?: number[];
  showSizeChanger?: boolean;
}

export default function Pagination({
  current,
  pageSize,
  total,
  onChange,
  pageSizeOptions = [10, 20, 50, 100],
  showSizeChanger = true,
}: PaginationProps) {
  const totalPages = Math.ceil(total / pageSize) || 1;

  const handlePageChange = (page: number) => {
    if (page < 1 || page > totalPages || page === current) return;
    onChange(page, pageSize);
  };

  const handlePageSizeChange = (newSize: number) => {
    if (newSize === pageSize) return;
    const newPage = Math.min(current, Math.ceil(total / newSize));
    onChange(newPage, newSize);
  };

  const renderPageNumbers = () => {
    const pages: (number | string)[] = [];
    const showPages = 5;

    if (totalPages <= showPages + 2) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);

      let start = Math.max(2, current - 1);
      let end = Math.min(totalPages - 1, current + 1);

      if (current <= 3) {
        end = showPages;
      } else if (current >= totalPages - 2) {
        start = totalPages - showPages + 1;
      }

      if (start > 2) {
        pages.push('...');
      }

      for (let i = start; i <= end; i++) {
        pages.push(i);
      }

      if (end < totalPages - 1) {
        pages.push('...');
      }

      pages.push(totalPages);
    }

    return pages;
  };

  return (
    <div className="flex items-center justify-between px-6 py-4 border-t border-border-dark bg-bg-primary">
      <div className="text-sm text-text-weak font-serif">
        共 {total} 条记录，第 {current} / {totalPages} 页
      </div>

      <div className="flex items-center space-x-4">
        {showSizeChanger && (
          <div className="flex items-center space-x-2">
            <span className="text-sm text-text-weak">每页</span>
            <select
              value={pageSize}
              onChange={(e) => handlePageSizeChange(Number(e.target.value))}
              className="px-3 py-1.5 bg-bg-secondary border border-border-dark rounded-lg text-text-primary text-sm focus:outline-none focus:border-brand-gold transition-colors"
            >
              {pageSizeOptions.map((size) => (
                <option key={size} value={size}>
                  {size}
                </option>
              ))}
            </select>
            <span className="text-sm text-text-weak">条</span>
          </div>
        )}

        <div className="flex items-center space-x-1">
          <button
            onClick={() => handlePageChange(current - 1)}
            disabled={current === 1}
            className="p-2 rounded-lg border border-border-dark text-text-weak hover:text-brand-gold hover:border-brand-gold disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:text-text-weak disabled:hover:border-border-dark transition-colors"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>

          {renderPageNumbers().map((page, index) => {
            if (page === '...') {
              return (
                <span
                  key={`ellipsis-${index}`}
                  className="px-3 py-1.5 text-text-weak"
                >
                  ...
                </span>
              );
            }

            return (
              <button
                key={page}
                onClick={() => handlePageChange(page as number)}
                className={`min-w-[36px] px-3 py-1.5 rounded-lg border text-sm font-medium transition-colors ${
                  current === page
                    ? 'bg-brand-gold border-brand-gold text-bg-primary'
                    : 'border-border-dark text-text-weak hover:text-brand-gold hover:border-brand-gold'
                }`}
              >
                {page}
              </button>
            );
          })}

          <button
            onClick={() => handlePageChange(current + 1)}
            disabled={current === totalPages}
            className="p-2 rounded-lg border border-border-dark text-text-weak hover:text-brand-gold hover:border-brand-gold disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:text-text-weak disabled:hover:border-border-dark transition-colors"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
}
