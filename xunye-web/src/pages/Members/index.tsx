'use client';

import { useEffect, useState, useCallback } from 'react';
import { Modal, Select, message } from 'antd';
import { getCustomerMemberPage, getMemberLevels, updateMemberLevel } from '@/api/members';
import type { CustomerMemberItem, MemberLevelItem } from '@/types/api';
import { Search, RotateCcw, Crown } from 'lucide-react';

const LEVEL_OPTIONS = [
  { label: '全部等级', value: '' },
  { label: '普通会员', value: 'REGULAR' },
  { label: 'VIP会员', value: 'VIP' },
  { label: 'SVIP会员', value: 'SVIP' },
];

const LEVEL_COLORS: Record<string, string> = {
  REGULAR: 'text-text-sub',
  VIP: 'text-brand-gold',
  SVIP: 'text-danger',
};

const LEVEL_BG: Record<string, string> = {
  REGULAR: 'bg-border-dark/30',
  VIP: 'bg-brand-gold/10',
  SVIP: 'bg-danger/10',
};

const darkSelectProps = {
  className: 'xunye-select',
  classNames: { popup: { root: 'xunye-select-dropdown' } },
  styles: {
    root: { backgroundColor: '#101014', border: '1px solid #2A2A31' },
    content: { color: '#F4EBDD' },
    suffix: { color: '#AFA79B' },
    popup: { root: { backgroundColor: '#1A1A1F', border: '1px solid #2A2A31' } },
  },
} as const;

const modalStyles = {
  content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
  header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
  body: { background: '#1A1A1F', paddingTop: 20 },
  footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
};

export default function MembersPage() {
  const [data, setData] = useState<CustomerMemberItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [filterLevel, setFilterLevel] = useState<string | undefined>();
  const [searchInput, setSearchInput] = useState('');

  const [levelModalOpen, setLevelModalOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<CustomerMemberItem | null>(null);
  const [newLevel, setNewLevel] = useState<string>('REGULAR');
  const [submitting, setSubmitting] = useState(false);
  const [memberLevels, setMemberLevels] = useState<MemberLevelItem[]>([]);

  const fetchData = useCallback(async (page = pageNum, size = pageSize) => {
    setLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (keyword) params.keyword = keyword;
      if (filterLevel) params.memberLevel = filterLevel;
      const res = await getCustomerMemberPage(params);
      setData(res?.records ?? []);
      setTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取会员列表失败');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, keyword, filterLevel]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    getMemberLevels().then(res => setMemberLevels(Array.isArray(res) ? res : [])).catch(() => {});
  }, []);

  const handleSearch = () => {
    setKeyword(searchInput);
    setPageNum(1);
  };

  const handleReset = () => {
    setSearchInput('');
    setKeyword('');
    setFilterLevel(undefined);
    setPageNum(1);
  };

  const totalPages = Math.ceil(total / pageSize) || 1;

  const openLevelModal = (customer: CustomerMemberItem) => {
    setSelectedCustomer(customer);
    setNewLevel(customer.memberLevel);
    setLevelModalOpen(true);
  };

  const handleLevelSubmit = async () => {
    if (!selectedCustomer) return;
    setSubmitting(true);
    try {
      await updateMemberLevel(selectedCustomer.id, newLevel);
      message.success('修改成功');
      setLevelModalOpen(false);
      fetchData();
    } catch (err: any) {
      message.error(err.message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* 标题栏 */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">会员管理</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Member Management</p>
        </div>
      </div>

      {/* 搜索栏 */}
      <div className="bg-card-bg border border-border-dark rounded-xl p-4">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative flex-1 min-w-[200px]">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-weak" />
            <input
              type="text"
              placeholder="搜索昵称 / 手机号..."
              value={searchInput}
              onChange={e => setSearchInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSearch()}
              className="w-full h-9 bg-page-bg border border-border-dark rounded-lg pl-9 pr-3 text-xs text-text-main placeholder:text-text-weak outline-none focus:border-brand-gold/50 transition-colors"
            />
          </div>
          <Select
            placeholder="会员等级"
            allowClear
            value={filterLevel}
            onChange={val => setFilterLevel(val || undefined)}
            options={LEVEL_OPTIONS}
            style={{ width: 140, height: 36 }}
            {...darkSelectProps}
          />
          <button
            onClick={handleSearch}
            className="h-9 px-4 bg-brand-gold text-page-bg rounded-lg text-xs font-semibold hover:bg-brand-gold/90 transition-colors tracking-wider"
          >
            查询
          </button>
          <button
            onClick={handleReset}
            className="h-9 px-4 border border-border-dark text-text-sub rounded-lg text-xs hover:text-text-main hover:border-text-sub transition-colors"
          >
            重置
          </button>
        </div>
      </div>

      {/* 数据表格 */}
      <div className="bg-card-bg border border-border-dark rounded-xl overflow-hidden shadow-xl">
        <div className="p-4 border-b border-border-dark flex justify-between items-center">
          <h3 className="text-sm font-semibold text-text-main">会员列表</h3>
          <button
            onClick={() => fetchData()}
            className="flex items-center space-x-1.5 text-text-sub hover:text-brand-gold transition-colors"
          >
            <RotateCcw size={14} />
            <span className="text-xs">刷新</span>
          </button>
        </div>
        <div className="overflow-x-auto hide-scrollbar">
          <table className="w-full text-left text-sm whitespace-nowrap">
            <thead>
              <tr className="bg-sidebar-bg text-text-weak uppercase text-[10px] tracking-wider">
                <th className="px-6 py-3 font-medium">昵称</th>
                <th className="px-6 py-3 font-medium">手机号</th>
                <th className="px-6 py-3 font-medium text-center">会员等级</th>
                <th className="px-6 py-3 font-medium text-right">积分</th>
                <th className="px-6 py-3 font-medium text-right">余额</th>
                <th className="px-6 py-3 font-medium text-center">订单数</th>
                <th className="px-6 py-3 font-medium text-right">累计消费</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={8} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无会员数据
                  </td>
                </tr>
              ) : (
                data.map((item) => (
                  <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                    <td className="px-6 py-3">
                      <div className="flex items-center gap-2">
                        <div className="w-7 h-7 rounded-full bg-brand-gold/20 flex items-center justify-center">
                          <span className="text-brand-gold text-[10px] font-serif">
                            {(item.nickname || '?').charAt(0)}
                          </span>
                        </div>
                        <span className="font-medium text-text-main tracking-wide">{item.nickname || '--'}</span>
                      </div>
                    </td>
                    <td className="px-6 py-3 text-text-sub font-mono text-[11px]">{item.phone || '--'}</td>
                    <td className="px-6 py-3 text-center">
                      <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded text-[10px] ${LEVEL_BG[item.memberLevel] || LEVEL_BG.REGULAR} ${LEVEL_COLORS[item.memberLevel] || LEVEL_COLORS.REGULAR}`}>
                        <Crown size={10} />
                        {item.memberLevelName}
                      </span>
                    </td>
                    <td className="px-6 py-3 text-right text-text-sub font-mono">{item.points}</td>
                    <td className="px-6 py-3 text-right text-text-sub font-mono">¥{item.balance.toFixed(2)}</td>
                    <td className="px-6 py-3 text-center text-text-sub">{item.totalOrders}</td>
                    <td className="px-6 py-3 text-right font-mono text-brand-gold">¥{item.totalAmount.toFixed(2)}</td>
                    <td className="px-6 py-3 text-center">
                      <button
                        onClick={() => openLevelModal(item)}
                        className="text-text-sub hover:text-brand-gold transition-colors text-[11px] tracking-wider"
                        type="button"
                      >
                        修改等级
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* 分页 */}
        {!loading && data.length > 0 && (
          <div className="px-6 py-3 border-t border-border-dark flex items-center justify-between text-xs text-text-sub">
            <span>第 {pageNum} / {totalPages} 页，共 {total} 条</span>
            <div className="flex items-center space-x-2">
              <button
                disabled={pageNum <= 1}
                onClick={() => setPageNum(p => Math.max(1, p - 1))}
                className="px-3 py-1.5 rounded border border-border-dark disabled:opacity-30 hover:border-text-sub transition-colors"
              >
                上一页
              </button>
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const start = Math.max(1, Math.min(pageNum - 2, totalPages - 4));
                const page = start + i;
                if (page > totalPages) return null;
                return (
                  <button
                    key={page}
                    onClick={() => setPageNum(page)}
                    className={`w-7 h-7 rounded text-xs transition-colors ${page === pageNum ? 'bg-brand-gold text-page-bg' : 'border border-border-dark text-text-sub hover:border-text-sub'}`}
                  >
                    {page}
                  </button>
                );
              })}
              <button
                disabled={pageNum >= totalPages}
                onClick={() => setPageNum(p => Math.min(totalPages, p + 1))}
                className="px-3 py-1.5 rounded border border-border-dark disabled:opacity-30 hover:border-text-sub transition-colors"
              >
                下一页
              </button>
              <select
                value={pageSize}
                onChange={e => { setPageSize(Number(e.target.value)); setPageNum(1); }}
                className="bg-page-bg border border-border-dark rounded px-2 py-1.5 text-text-sub text-xs outline-none"
              >
                <option value={10}>10条/页</option>
                <option value={20}>20条/页</option>
                <option value={50}>50条/页</option>
              </select>
            </div>
          </div>
        )}
      </div>

      {/* 修改等级弹窗 */}
      <Modal
        title={<span className="text-brand-gold font-serif tracking-wider">修改会员等级</span>}
        open={levelModalOpen}
        onCancel={() => setLevelModalOpen(false)}
        onOk={handleLevelSubmit}
        confirmLoading={submitting}
        okText="保存"
        cancelText="取消"
        width={400}
        destroyOnHidden
        styles={modalStyles}
      >
        <div className="space-y-4 py-2">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-full bg-brand-gold/20 flex items-center justify-center">
              <span className="text-brand-gold text-sm font-serif">
                {(selectedCustomer?.nickname || '?').charAt(0)}
              </span>
            </div>
            <div>
              <p className="text-sm font-medium text-text-main">{selectedCustomer?.nickname || '--'}</p>
              <p className="text-[11px] text-text-sub font-mono">{selectedCustomer?.phone || '--'}</p>
            </div>
          </div>
          <div>
            <p className="text-xs text-text-sub mb-2 uppercase tracking-wider">当前等级</p>
            <span className={`inline-flex items-center gap-1 px-3 py-1 rounded text-xs ${LEVEL_BG[selectedCustomer?.memberLevel || 'REGULAR']} ${LEVEL_COLORS[selectedCustomer?.memberLevel || 'REGULAR']}`}>
              <Crown size={12} />
              {selectedCustomer?.memberLevelName}
            </span>
          </div>
          <div>
            <p className="text-xs text-text-sub mb-2 uppercase tracking-wider">修改为</p>
            <div className="flex gap-2">
              {memberLevels.map((level) => (
                <button
                  key={level.level}
                  onClick={() => setNewLevel(level.level)}
                  className={`flex-1 px-3 py-2 rounded-lg border text-xs transition-colors ${
                    newLevel === level.level
                      ? 'border-brand-gold bg-brand-gold/10 text-brand-gold'
                      : 'border-border-dark text-text-sub hover:border-text-sub'
                  }`}
                  type="button"
                >
                  {level.name}
                </button>
              ))}
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
}
