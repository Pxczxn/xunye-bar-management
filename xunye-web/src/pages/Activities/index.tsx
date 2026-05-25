'use client';

import { useEffect, useState, useCallback, useMemo } from 'react';
import { Modal, Form, Input, Select, DatePicker, InputNumber, message } from 'antd';
import datePickerZhCN from 'antd/es/date-picker/locale/zh_CN';
import { getActivityPage, createActivity, updateActivity, deleteActivity } from '@/api/activities';
import { getSimpleProducts, getCategoryList } from '@/api/product';
import { getTableAreas, getTablePage } from '@/api/table';
import type { ActivityItem, ActivityFormData, ActivitySettings } from '@/types/api';
import { Plus, Edit, Trash2, Search, RotateCcw, X, Calendar, Clock, Sparkles } from 'lucide-react';
import dayjs, { type Dayjs } from 'dayjs';
import MiniAppPreview from './components/MiniAppPreview';
import ActivityTypeGrid from './components/ActivityTypeGrid';
import LoopTimePicker from './components/LoopTimePicker';
import { darkSelectProps } from '@/constants/antdTheme';

const { TextArea } = Input;
const { RangePicker } = DatePicker;
const MIN_ACTIVITY_DURATION_SECONDS = 10;

type ActivityDateRangeValue = [Dayjs | null, Dayjs | null];
type TimeParts = [number, number, number];
type ActivityType = 'DISCOUNT' | 'COUPON' | 'POINTS' | 'SPECIAL';

const getMinimumStartDateTime = (base = dayjs()) => {
  const roundedBase = base.startOf('minute');
  if (base.second() > 0 || base.millisecond() > 0) {
    return roundedBase.add(1, 'minute');
  }
  return roundedBase;
};

const getDefaultDateRange = () => {
  const minStart = getMinimumStartDateTime();
  return [minStart, minStart.add(MIN_ACTIVITY_DURATION_SECONDS, 'second')] as const;
};

const formatTimeUnit = (value: number) => String(value).padStart(2, '0');

const getTimeParts = (value: Dayjs | null): TimeParts => {
  if (!value) return [0, 0, 0];
  return [value.hour(), value.minute(), value.second()];
};

const mergeDateAndTime = (date: Dayjs | null, time: TimeParts) => {
  if (!date) return null;
  return date.hour(time[0]).minute(time[1]).second(time[2]).millisecond(0);
};

const clampStartDateTime = (value: Dayjs | null) => {
  if (!value) return null;
  const minStart = getMinimumStartDateTime();
  return value.isBefore(minStart) ? minStart : value;
};

const getMinimumEndDateTime = (startDateTime: Dayjs | null) => {
  if (!startDateTime) return null;
  return startDateTime.add(MIN_ACTIVITY_DURATION_SECONDS, 'second');
};

const clampEndDateTime = (endDateTime: Dayjs | null, startDateTime: Dayjs | null) => {
  if (!endDateTime) return null;
  const minimumEndDateTime = getMinimumEndDateTime(startDateTime);
  if (!minimumEndDateTime) return endDateTime;
  return endDateTime.isBefore(minimumEndDateTime) ? minimumEndDateTime : endDateTime;
};

const rangePickerLocale = {
  ...datePickerZhCN,
  lang: {
    ...datePickerZhCN.lang,
    shortWeekDays: ['日', '一', '二', '三', '四', '五', '六'],
    shortMonths: ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'],
    monthFormat: 'M月',
  },
};

const TYPE_OPTIONS = [
  { label: '折扣', value: 'DISCOUNT' },
  { label: '优惠券', value: 'COUPON' },
  { label: '积分', value: 'POINTS' },
  { label: '特惠', value: 'SPECIAL' },
] as const;

const STATUS_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: '0' },
  { label: '进行中', value: '1' },
  { label: '已结束', value: '2' },
];

const STATUS_FILTER_OPTIONS = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 0 },
  { label: '进行中', value: 1 },
  { label: '已结束', value: 2 },
];

const STATUS_MAP: Record<number, { label: string; color: string; bg: string }> = {
  0: { label: '草稿', color: 'text-text-sub', bg: 'bg-border-dark' },
  1: { label: '进行中', color: 'text-success', bg: 'bg-success/10' },
  2: { label: '已结束', color: 'text-danger', bg: 'bg-danger/10' },
};

const TYPE_MAP: Record<string, string> = {
  DISCOUNT: '折扣',
  COUPON: '优惠券',
  POINTS: '积分',
  SPECIAL: '特惠',
};

const DEFAULT_ACTIVITY_SETTINGS: Record<ActivityType, ActivitySettings> = {
  DISCOUNT: { discountRate: 8.8, minAmount: 0, scopeProductType: 'ALL', productIds: [], categoryIds: [], scopeTableType: 'ALL', tableIds: [], areaIds: [] },
  COUPON: { discountAmount: 20, minAmount: 100, scopeProductType: 'ALL', productIds: [], categoryIds: [], scopeTableType: 'ALL', tableIds: [], areaIds: [] },
  POINTS: { pointsMultiplier: 2, scopeProductType: 'ALL', productIds: [], categoryIds: [], scopeTableType: 'ALL', tableIds: [], areaIds: [] },
  SPECIAL: { specialPrice: 88, originalPrice: 108, stockLimit: 50, scopeProductType: 'ALL', productIds: [], categoryIds: [], scopeTableType: 'ALL', tableIds: [], areaIds: [] },
};

const getDefaultSettings = (type: ActivityType): ActivitySettings => ({ ...DEFAULT_ACTIVITY_SETTINGS[type] });

const normalizeSettingsByType = (type: string, settings?: ActivitySettings): ActivitySettings => {
  const mergedSettings = { ...getDefaultSettings((TYPE_MAP[type] ? type : 'DISCOUNT') as ActivityType), ...(settings || {}) };
  const scopeSettings = {
    scopeProductType: mergedSettings.scopeProductType ?? 'ALL',
    productIds: mergedSettings.productIds ?? [],
    categoryIds: mergedSettings.categoryIds ?? [],
    scopeTableType: mergedSettings.scopeTableType ?? 'ALL',
    tableIds: mergedSettings.tableIds ?? [],
    areaIds: mergedSettings.areaIds ?? [],
  };
  switch (type) {
    case 'DISCOUNT':
      return {
        discountRate: mergedSettings.discountRate,
        minAmount: mergedSettings.minAmount ?? 0,
        ...scopeSettings,
      };
    case 'COUPON':
      return {
        discountAmount: mergedSettings.discountAmount,
        minAmount: mergedSettings.minAmount,
        ...scopeSettings,
      };
    case 'POINTS':
      return {
        pointsMultiplier: mergedSettings.pointsMultiplier,
        ...scopeSettings,
      };
    case 'SPECIAL':
      return {
        specialPrice: mergedSettings.specialPrice,
        originalPrice: mergedSettings.originalPrice,
        stockLimit: mergedSettings.stockLimit,
        ...scopeSettings,
      };
    default:
      return { ...getDefaultSettings('DISCOUNT'), ...scopeSettings };
  }
};

const formatSettingSummary = (type: string, settings?: ActivitySettings, fallback = '') => {
  const currentSettings = settings || {};
  switch (type) {
    case 'DISCOUNT':
      if (currentSettings.discountRate === undefined) return fallback;
      return currentSettings.minAmount && currentSettings.minAmount > 0
        ? `满${currentSettings.minAmount}元打${currentSettings.discountRate}折`
        : `直接打${currentSettings.discountRate}折`;
    case 'COUPON':
      if (currentSettings.discountAmount === undefined || currentSettings.minAmount === undefined) return fallback;
      return `满${currentSettings.minAmount}减${currentSettings.discountAmount}`;
    case 'POINTS':
      if (currentSettings.pointsMultiplier === undefined) return fallback;
      return `消费积分${currentSettings.pointsMultiplier}倍`;
    case 'SPECIAL': {
      if (currentSettings.specialPrice === undefined) return fallback;
      const parts = [`特惠价${currentSettings.specialPrice}元`];
      if (currentSettings.originalPrice !== undefined) parts.push(`原价${currentSettings.originalPrice}元`);
      if (currentSettings.stockLimit !== undefined) parts.push(`限量${currentSettings.stockLimit}份`);
      return parts.join('，');
    }
    default:
      return fallback;
  }
};

const formatScopeSummary = (
  settings?: ActivitySettings,
  products: any[] = [],
  categories: any[] = [],
  areas: any[] = [],
  tables: any[] = []
) => {
  if (!settings) return { main: '全部商品 / 全部区域', sub: '所有商品和桌台区域参与' };

  const {
    scopeProductType = 'ALL',
    productIds = [],
    categoryIds = [],
    scopeTableType = 'ALL',
    tableIds = [],
    areaIds = [],
  } = settings;

  let productText = '全部商品';
  if (scopeProductType === 'CATEGORY') {
    if (!categoryIds || categoryIds.length === 0) {
      productText = '未指定分类';
    } else {
      const selectedNames = categories
        .filter(c => categoryIds.includes(c.id))
        .map(c => c.name);
      productText = selectedNames.length > 0 ? selectedNames.join('、') : `已选 ${categoryIds.length} 个分类`;
    }
  } else if (scopeProductType === 'PRODUCT') {
    if (!productIds || productIds.length === 0) {
      productText = '未指定商品';
    } else {
      const selectedNames = products
        .filter(p => productIds.includes(p.id))
        .map(p => p.name);
      productText = selectedNames.length > 0
        ? selectedNames.slice(0, 3).join('、') + (selectedNames.length > 3 ? ` 等${selectedNames.length}件` : '')
        : `已选 ${productIds.length} 个商品`;
    }
  }

  let tableText = '全部区域';
  if (scopeTableType === 'AREA') {
    if (!areaIds || areaIds.length === 0) {
      tableText = '未指定区域';
    } else {
      const selectedNames = areas
        .filter(a => areaIds.includes(a.id))
        .map(a => a.name);
      tableText = selectedNames.length > 0 ? selectedNames.join('、') : `已选 ${areaIds.length} 个区域`;
    }
  } else if (scopeTableType === 'TABLE') {
    if (!tableIds || tableIds.length === 0) {
      tableText = '未指定桌台';
    } else {
      const selectedNames = tables
        .filter(t => tableIds.includes(t.id))
        .map(t => t.name);
      tableText = selectedNames.length > 0
        ? selectedNames.slice(0, 3).join('、') + (selectedNames.length > 3 ? ` 等${selectedNames.length}桌` : '')
        : `已选 ${tableIds.length} 个桌台`;
    }
  }

  return {
    main: `${scopeProductType === 'ALL' ? '全部商品' : scopeProductType === 'CATEGORY' ? '指定分类' : '指定商品'} / ${scopeTableType === 'ALL' ? '全部区域' : scopeTableType === 'AREA' ? '指定区域' : '指定桌台'}`,
    sub: `${productText}；${tableText}`
  };
};

const confirmModalStyles = {
  content: { background: '#1A1A1F', border: '1px solid #2A2A31' },
  header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', paddingBottom: 16 },
  body: { background: '#1A1A1F', paddingTop: 20 },
  footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31' },
};

const activityModalStyles = {
  content: { background: '#1A1A1F', padding: 0 },
  header: { background: '#1A1A1F', borderBottom: '1px solid #2A2A31', padding: '14px 20px' },
  body: { background: '#1A1A1F', padding: '20px' },
  footer: { background: '#1A1A1F', borderTop: '1px solid #2A2A31', padding: '14px 20px' },
};

interface LoopTimeInputProps {
  label: string;
  value: TimeParts;
  onChange: (value: TimeParts) => void;
  disabledOptions?: {
    hour?: (value: number) => boolean;
    minute?: (value: number) => boolean;
    second?: (value: number) => boolean;
  };
}

function LoopTimeInput({ label, value, onChange, disabledOptions }: LoopTimeInputProps) {
  const updatePart = (index: 0 | 1 | 2, nextValue: number) => {
    const nextTime = [...value] as TimeParts;
    nextTime[index] = nextValue;
    onChange(nextTime);
  };

  const handleManualChange = (index: 0 | 1 | 2, rawValue: number | null) => {
    if (rawValue === null || Number.isNaN(rawValue)) {
      return;
    }

    const max = index === 0 ? 23 : 59;
    const candidate = Math.max(0, Math.min(max, Math.trunc(rawValue)));
    const isDisabled = index === 0
      ? disabledOptions?.hour?.(candidate)
      : index === 1
        ? disabledOptions?.minute?.(candidate)
        : disabledOptions?.second?.(candidate);

    if (isDisabled) {
      return;
    }

    updatePart(index, candidate);
  };

  return (
    <div className="rounded-xl border border-border-dark bg-card-bg p-3">
      <div className="mb-2.5 flex items-center justify-between">
        <div className="text-sm font-medium tracking-wide text-text-main">{label}</div>
        <div className="text-sm font-medium text-brand-gold">
          {value.map(formatTimeUnit).join(':')}
        </div>
      </div>
      <div className="grid grid-cols-3 gap-2.5">
        <div className="rounded-lg border border-border-dark bg-page-bg/60 p-2.5">
          <div className="mb-1.5 text-[11px] uppercase tracking-wider text-text-weak">时</div>
          <InputNumber
            min={0}
            max={23}
            controls={false}
            precision={0}
            value={value[0]}
            onChange={(nextValue) => handleManualChange(0, nextValue)}
            className="!w-full"
            placeholder="00"
          />
        </div>
        <div className="rounded-lg border border-border-dark bg-page-bg/60 p-2.5">
          <div className="mb-1.5 text-[11px] uppercase tracking-wider text-text-weak">分</div>
          <InputNumber
            min={0}
            max={59}
            controls={false}
            precision={0}
            value={value[1]}
            onChange={(nextValue) => handleManualChange(1, nextValue)}
            className="!w-full"
            placeholder="00"
          />
        </div>
        <div className="rounded-lg border border-border-dark bg-page-bg/60 p-2.5">
          <div className="mb-1.5 text-[11px] uppercase tracking-wider text-text-weak">秒</div>
          <InputNumber
            min={0}
            max={59}
            controls={false}
            precision={0}
            value={value[2]}
            onChange={(nextValue) => handleManualChange(2, nextValue)}
            className="!w-full"
            placeholder="00"
          />
        </div>
      </div>
    </div>
  );
}

export default function ActivitiesPage() {
  const [data, setData] = useState<ActivityItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [filterType, setFilterType] = useState<string | undefined>();
  const [filterStatus, setFilterStatus] = useState<number | undefined>();
  const [searchInput, setSearchInput] = useState('');

  const [products, setProducts] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [areas, setAreas] = useState<any[]>([]);
  const [tables, setTables] = useState<any[]>([]);

  const [modalOpen, setModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ActivityItem | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [activityDateRange, setActivityDateRange] = useState<ActivityDateRangeValue>(getDefaultDateRange());
  const [configPanel, setConfigPanel] = useState<'rules' | 'time' | 'scope' | null>(null);
  const [form] = Form.useForm();
  const selectedType = (Form.useWatch('type', form) || 'DISCOUNT') as ActivityType;
  const [activePreset, setActivePreset] = useState<'1h' | '24h' | '3d' | '7d' | null>(null);

  const watchedTitle = Form.useWatch('title', form);
  const watchedType = Form.useWatch('type', form) || 'DISCOUNT';
  const watchedSettings = Form.useWatch('settings', form) || {};
  const watchedDescription = Form.useWatch('description', form);
  const watchedScopeProductType = Form.useWatch(['settings', 'scopeProductType'], form) || 'ALL';
  const watchedScopeTableType = Form.useWatch(['settings', 'scopeTableType'], form) || 'ALL';
  const ruleSummary = useMemo(
    () => formatSettingSummary(watchedType, normalizeSettingsByType(watchedType, watchedSettings), '未设置规则'),
    [watchedSettings, watchedType],
  );

  const scopeSummary = useMemo(
    () => formatScopeSummary(watchedSettings, products, categories, areas, tables),
    [watchedSettings, products, categories, areas, tables],
  );

  const handleStartDateChange = (val: Dayjs | null) => {
    if (!val) {
      setActivityDateRange([null, activityDateRange[1]]);
      return;
    }
    const minStart = getMinimumStartDateTime();
    const clampedStart = val.isBefore(minStart) ? minStart : val;
    const currentEnd = activityDateRange[1];
    const nextEnd = currentEnd && currentEnd.isBefore(clampedStart) ? clampedStart : currentEnd;
    setActivityDateRange([clampedStart, nextEnd]);
    setActivePreset(null);
  };

  const handleEndDateChange = (val: Dayjs | null) => {
    if (!val) {
      setActivityDateRange([activityDateRange[0], null]);
      return;
    }
    const startDate = activityDateRange[0] || getMinimumStartDateTime();
    const clampedEnd = val.isBefore(startDate) ? startDate : val;
    setActivityDateRange([startDate, clampedEnd]);
    setActivePreset(null);
  };

  const applyPreset = (preset: '1h' | '24h' | '3d' | '7d') => {
    const start = activityDateRange[0] || getMinimumStartDateTime();
    let end = start;
    if (preset === '1h') {
      end = start.add(1, 'hour');
    } else if (preset === '24h') {
      end = start.add(24, 'hour');
    } else if (preset === '3d') {
      end = start.add(3, 'day');
    } else if (preset === '7d') {
      end = start.add(7, 'day');
    }
    setActivityDateRange([start, end]);
    setActivePreset(preset);
  };

  const activityTimePreview = useMemo(() => {
    const [startDateTime, endDateTime] = activityDateRange;
    if (!startDateTime || !endDateTime) {
      return '未设置活动时间';
    }
    return `${startDateTime.format('YYYY-MM-DD HH:mm:ss')} - ${endDateTime.format('YYYY-MM-DD HH:mm:ss')}`;
  }, [activityDateRange]);

  const activityDurationPreview = useMemo(() => {
    const [startDateTime, endDateTime] = activityDateRange;
    if (!startDateTime || !endDateTime) {
      return '未设置活动时长';
    }

    const totalHours = Math.max(0, endDateTime.diff(startDateTime, 'hour', true));
    const dayCount = Math.floor(totalHours / 24);
    const hourCount = Math.floor(totalHours - dayCount * 24);

    return `${dayCount}天 ${hourCount}小时`;
  }, [activityDateRange]);
  const activityTimeDisplay = useMemo(() => {
    const [startDateTime, endDateTime] = activityDateRange;
    if (!startDateTime || !endDateTime) {
      return '暂未设置生效时间';
    }
    return `${startDateTime.format('M月D日 HH:mm')} 开始，${endDateTime.format('M月D日 HH:mm')} 结束`;
  }, [activityDateRange]);

  const fetchData = useCallback(async (page = pageNum, size = pageSize) => {
    setLoading(true);
    try {
      const params: any = { pageNum: page, pageSize: size };
      if (keyword) params.keyword = keyword;
      if (filterType) params.type = filterType;
      if (filterStatus !== undefined) params.status = filterStatus;
      const res = await getActivityPage(params);
      setData(res?.records ?? []);
      setTotal(res?.total ?? 0);
    } catch (err: any) {
      message.error(err.message || '获取活动列表失败');
      setData([]);
    } finally {
      setLoading(false);
    }
  }, [pageNum, pageSize, keyword, filterType, filterStatus]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  useEffect(() => {
    const fetchScopeData = async () => {
      try {
        const [prodRes, catRes, areaRes, tableRes] = await Promise.all([
          getSimpleProducts(),
          getCategoryList(),
          getTableAreas(),
          getTablePage({ pageNum: 1, pageSize: 1000 }),
        ]);
        setProducts(prodRes || []);
        setCategories(catRes || []);
        setAreas(areaRes || []);
        setTables(tableRes?.records || []);
      } catch (err) {
        console.error('Failed to fetch activity scope support data:', err);
      }
    };
    fetchScopeData();
  }, []);

  const handleSearch = () => {
    setKeyword(searchInput);
    setPageNum(1);
  };

  const handleReset = () => {
    setSearchInput('');
    setKeyword('');
    setFilterType(undefined);
    setFilterStatus(undefined);
    setPageNum(1);
  };

  const totalPages = Math.ceil(total / pageSize) || 1;

  const openAddModal = () => {
    setEditingItem(null);
    form.resetFields();

    // 默认预设为 24 小时
    const minStart = getMinimumStartDateTime();
    const defaultEnd = minStart.add(24, 'hour');
    setActivityDateRange([minStart, defaultEnd]);
    setActivePreset('24h');

    form.setFieldsValue({ type: 'DISCOUNT', sort: 0, settings: getDefaultSettings('DISCOUNT') });
    setModalOpen(true);
  };

  const openEditModal = (item: ActivityItem) => {
    setEditingItem(item);
    setActivityDateRange([
      item.startDate ? dayjs(item.startDate) : null,
      item.endDate ? dayjs(item.endDate) : null,
    ]);
    setActivePreset(null);
    console.log('Opening edit modal for item:', item);

    // 先重置表单
    form.resetFields();

    // 打开对话框
    setModalOpen(true);

    // 使用 setTimeout 确保对话框已经渲染
    setTimeout(() => {
      form.setFieldsValue({
        title: item.title,
        description: item.description,
        type: item.type,
        settings: normalizeSettingsByType(item.type, item.settings),
        status: item.status,
        sort: item.sort,
      });
      console.log('Form values after setFieldsValue:', form.getFieldsValue());
      console.log('Form getFieldValue(type):', form.getFieldValue('type'));
    }, 100);
  };

  const handleTypeChange = (type: ActivityType) => {
    console.log('Type changed to:', type);
    const currentSettings = form.getFieldValue('settings') || {};
    const scopeSettings = {
      scopeProductType: currentSettings.scopeProductType ?? 'ALL',
      productIds: currentSettings.productIds ?? [],
      categoryIds: currentSettings.categoryIds ?? [],
      scopeTableType: currentSettings.scopeTableType ?? 'ALL',
      tableIds: currentSettings.tableIds ?? [],
      areaIds: currentSettings.areaIds ?? [],
    };
    form.setFieldValue('type', type);
    form.setFieldValue('settings', {
      ...getDefaultSettings(type),
      ...scopeSettings,
    });
  };

  const closeEditorPanel = () => {
    setModalOpen(false);
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      console.log('Form values:', values);
      setSubmitting(true);

      // 如果 type 为 undefined，从 editingItem 中获取（编辑模式）或使用默认值（新建模式）
      const activityType = values.type || (editingItem?.type) || 'DISCOUNT';

      const payload: ActivityFormData = {
        title: values.title,
        description: values.description || '',
        type: activityType,
        startDate: activityDateRange[0]?.format('YYYY-MM-DD HH:mm:ss') || null,
        endDate: activityDateRange[1]?.format('YYYY-MM-DD HH:mm:ss') || null,
        coverImage: '',
        settings: normalizeSettingsByType(activityType, values.settings),
        status: editingItem ? (values.status ?? 0) : 0,
        sort: values.sort ?? 0,
      };
      console.log('Payload:', payload);
      if (editingItem) {
        await updateActivity(editingItem.id, payload);
        message.success('修改成功');
      } else {
        await createActivity(payload);
        message.success('新增成功');
      }
      closeEditorPanel();
      fetchData();
    } catch (err: any) {
      if (err?.errorFields) return;
      message.error(err.message || '操作失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (item: ActivityItem) => {
    Modal.confirm({
      rootClassName: 'xunye-confirm-modal',
      title: '确认删除',
      content: `确定要删除活动「${item.title}」吗？`,
      okText: '删除',
      okButtonProps: { danger: true },
      cancelText: '取消',
      styles: confirmModalStyles,
      onOk: async () => {
        try {
          await deleteActivity(item.id);
          setData(prev => prev.filter(activity => activity.id !== item.id));
          setTotal(prev => Math.max(0, prev - 1));
          message.success('删除成功');
          await fetchData();
        } catch (err: any) {
          message.error(err.message || '删除失败');
        }
      },
    });
  };

  return (
    <div className="relative min-h-[calc(100vh-120px)] space-y-4 pb-8">
      {/* 标题栏 */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-end gap-6 mb-6">
        <div>
          <h1 className="text-2xl font-serif font-bold text-text-main tracking-wider mb-1">活动管理</h1>
          <p className="text-[10px] text-brand-gold uppercase tracking-widest font-medium">Activities</p>
        </div>
        <button
          onClick={openAddModal}
          className="flex items-center space-x-2 bg-brand-gold text-page-bg px-4 py-2 rounded-lg font-semibold text-sm hover:bg-brand-gold/90 transition-colors shrink-0 tracking-widest uppercase"
        >
          <Plus size={16} />
          <span>新增活动</span>
        </button>
      </div>

      {/* 搜索栏 */}
          <div className="bg-card-bg border border-border-dark rounded-xl p-4">
            <div className="flex flex-wrap items-center gap-3">
              <div className="relative flex-1 min-w-[200px]">
                <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-text-weak" />
                <input
                  type="text"
                  placeholder="搜索活动标题..."
                  value={searchInput}
                  onChange={e => setSearchInput(e.target.value)}
                  onKeyDown={e => e.key === 'Enter' && handleSearch()}
                  className="w-full h-9 bg-page-bg border border-border-dark rounded-lg pl-9 pr-3 text-xs text-text-main placeholder:text-text-weak outline-none focus:border-brand-gold/50 transition-colors"
                />
              </div>
              <Select
                placeholder="活动类型"
                allowClear
                value={filterType}
                onChange={val => setFilterType(val || undefined)}
                options={TYPE_OPTIONS}
                style={{ width: 120, height: 36 }}
                {...darkSelectProps}
              />
              <Select
                placeholder="活动状态"
                allowClear
                value={filterStatus}
                onChange={val => setFilterStatus(val !== undefined ? val : undefined)}
                options={STATUS_FILTER_OPTIONS}
                style={{ width: 120, height: 36 }}
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
          <h3 className="text-sm font-semibold text-text-main">活动列表</h3>
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
                <th className="px-6 py-3 font-medium">活动标题</th>
                <th className="px-6 py-3 font-medium text-center">类型</th>
                <th className="px-6 py-3 font-medium text-center">规则</th>
                <th className="px-6 py-3 font-medium text-center">状态</th>
                <th className="px-6 py-3 font-medium text-center">时间</th>
                <th className="px-6 py-3 font-medium text-center">排序</th>
                <th className="px-6 py-3 font-medium text-center">操作</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-dark/50 text-xs">
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center text-text-weak">
                    <div className="flex items-center justify-center space-x-2">
                      <div className="w-4 h-4 border-2 border-brand-gold border-t-transparent rounded-full animate-spin" />
                      <span>加载中...</span>
                    </div>
                  </td>
                </tr>
              ) : data.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center text-text-weak font-serif italic">
                    暂无活动数据
                  </td>
                </tr>
              ) : (
                data.map((item) => {
                  const statusInfo = STATUS_MAP[item.status] || STATUS_MAP[0];
                  return (
                    <tr key={item.id} className="hover:bg-border-dark/20 transition-colors">
                      <td className="px-6 py-3">
                        <span className="font-medium text-text-main tracking-wide">{item.title}</span>
                      </td>
                      <td className="px-6 py-3 text-center">
                        <span className="text-text-sub">{TYPE_MAP[item.type] || item.type}</span>
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub">
                        {item.settingSummary || formatSettingSummary(item.type, item.settings, '--')}
                      </td>
                      <td className="px-6 py-3 text-center">
                        <span className={`inline-flex items-center px-2 py-0.5 rounded text-[10px] ${statusInfo.bg} ${statusInfo.color}`}>
                          {statusInfo.label}
                        </span>
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub font-mono text-[10px]">
                        {item.startDate ? (
                          <>
                            {item.startDate.slice(5, 10)}<br />~ {item.endDate?.slice(5, 10)}
                          </>
                        ) : '--'}
                      </td>
                      <td className="px-6 py-3 text-center text-text-sub">{item.sort}</td>
                      <td className="px-6 py-3">
                        <div className="flex items-center justify-center space-x-3 text-text-sub">
                          <button
                            onClick={() => openEditModal(item)}
                            className="hover:text-brand-gold transition-colors"
                            title="编辑"
                            type="button"
                          >
                            <Edit size={15} />
                          </button>
                          <button
                            onClick={() => handleDelete(item)}
                            className="hover:text-danger transition-colors"
                            title="删除"
                            type="button"
                          >
                            <Trash2 size={15} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
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

      <Modal
        open={modalOpen}
        onCancel={closeEditorPanel}
        footer={null}
        width={1500}
        rootClassName="xunye-activity-modal"
        styles={activityModalStyles}
        closeIcon={<X size={22} />}
      >
        <div className="flex flex-col">
          {/* 弹窗标题区 */}
          <div className="relative mb-3 border-b border-brand-gold/20 pb-3">
            <div className="flex items-center gap-3">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand-gold/10 border border-brand-gold/30 shadow-lg shadow-brand-gold/10">
                <Plus size={20} className="text-brand-gold" />
              </div>
              <div>
                <h2 className="text-xl font-serif font-bold text-brand-gold tracking-wide">
                  {editingItem ? '编辑活动' : '新建活动'}
                </h2>
                <p className="mt-0.5 text-xs text-text-weak tracking-wide">
                  配置活动基本信息、优惠规则与生效时段
                </p>
              </div>
            </div>
          </div>

          <Form
            form={form}
            layout="vertical"
            initialValues={{ type: 'DISCOUNT', sort: 0 }}
            className="min-h-0"
            onValuesChange={(changedValues) => {
              if (changedValues.type) {
                handleTypeChange(changedValues.type);
              }
            }}
          >
            {/* Split Dual-Column Layout */}
            <div className="grid grid-cols-1 items-start gap-4 md:grid-cols-12">

              {/* Left Column - Form configurations (60% width) */}
              <div className="space-y-3 md:col-span-7">

                {/* 基础信息卡片 */}
                <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                  <button type="button" onClick={() => setConfigPanel('rules')} className="rounded-xl border border-border-dark bg-[#111114] p-4 text-left transition-colors hover:border-brand-gold/50">
                    <div className="text-[10px] uppercase tracking-widest text-brand-gold">规则设置</div>
                    <div className="mt-2 text-sm font-semibold text-text-main">{TYPE_MAP[watchedType]}</div>
                    <div className="mt-1 truncate text-[11px] text-text-weak">{ruleSummary}</div>
                  </button>
                  <button type="button" onClick={() => setConfigPanel('time')} className="rounded-xl border border-border-dark bg-[#111114] p-4 text-left transition-colors hover:border-brand-gold/50">
                    <div className="text-[10px] uppercase tracking-widest text-brand-gold">生效时间</div>
                    <div className="mt-2 text-sm font-semibold text-text-main">{activityDurationPreview}</div>
                    <div className="mt-1 truncate text-[11px] text-text-weak">{activityTimeDisplay}</div>
                  </button>
                  <button type="button" onClick={() => setConfigPanel('scope')} className="rounded-xl border border-border-dark bg-[#111114] p-4 text-left transition-colors hover:border-brand-gold/50">
                    <div className="text-[10px] uppercase tracking-widest text-brand-gold">活动范围</div>
                    <div className="mt-2 text-sm font-semibold text-text-main">{scopeSummary.main}</div>
                    <div className="mt-1 truncate text-[11px] text-text-weak" title={scopeSummary.sub}>{scopeSummary.sub}</div>
                  </button>
                </div>

                <div className="group relative overflow-hidden rounded-xl border border-border-dark bg-gradient-to-br from-card-bg to-page-bg/60 p-3 transition-all duration-300 hover:border-brand-gold/30 hover:shadow-lg hover:shadow-brand-gold/5">
                  <div className="absolute right-0 top-0 h-32 w-32 bg-brand-gold/5 blur-3xl"></div>
                  <div className="relative">
                    <div className="mb-2 flex items-center gap-2">
                      <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                      <h3 className="text-base font-semibold uppercase tracking-widest text-brand-gold">基础信息</h3>
                      <div className="h-px flex-1 bg-gradient-to-r from-brand-gold/30 to-transparent"></div>
                    </div>

                    <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                      <div className="md:col-span-2">
                        <Form.Item
                          name="title"
                          label={<span className="text-sm font-medium text-text-sub">活动标题</span>}
                          rules={[{ required: true, message: '请输入活动标题' }]}
                          className="mb-0"
                        >
                          <Input
                            placeholder="例如：尊享会员特惠夜"
                            className="h-10 rounded-lg text-sm"
                          />
                        </Form.Item>
                      </div>
                      <div>
                        <Form.Item
                          name="sort"
                          label={<span className="text-sm font-medium text-text-sub">显示排序</span>}
                          tooltip="数值越小越靠前"
                          className="mb-0"
                        >
                          <InputNumber
                            min={0}
                            className="!w-full h-10 rounded-lg text-sm"
                            placeholder="0"
                          />
                        </Form.Item>
                      </div>
                    </div>

                    <Form.Item
                      name="description"
                      label={<span className="text-sm font-medium text-text-sub">活动描述</span>}
                      className="mt-3 mb-0"
                    >
                      <TextArea
                        rows={1}
                        placeholder="填写活动的详细规则或前台展示描述（例如：本活动仅限到店消费，卡座与包厢除外，不与其它优惠同享）"
                        className="rounded-lg"
                      />
                    </Form.Item>
                  </div>
                </div>

                {/* 规则设置卡片 */}
                {false && <div className="group relative overflow-hidden rounded-xl border border-border-dark bg-gradient-to-br from-card-bg to-page-bg/60 p-3 transition-all duration-300 hover:border-brand-gold/30 hover:shadow-lg hover:shadow-brand-gold/5">
                  <div className="absolute left-0 top-0 h-32 w-32 bg-brand-gold/5 blur-3xl"></div>
                  <div className="relative">
                    <div className="mb-2 flex items-center gap-2">
                      <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                      <h3 className="text-base font-semibold uppercase tracking-widest text-brand-gold">规则设置</h3>
                      <div className="h-px flex-1 bg-gradient-to-r from-brand-gold/30 to-transparent"></div>
                    </div>

                    <div className="space-y-2">
                      <Form.Item
                        name="type"
                        label={<span className="text-sm font-medium text-text-sub">活动类型</span>}
                        rules={[{ required: true, message: '请选择活动类型' }]}
                        className="mb-0"
                      >
                        <ActivityTypeGrid />
                      </Form.Item>

                      {/* 动态规则参数区域 */}
                      <div className="rounded-xl border border-border-dark/50 bg-[#111114] p-2.5 transition-all">
                        {selectedType === 'DISCOUNT' && (
                          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <Form.Item
                              name={['settings', 'discountRate']}
                              label={<span className="text-sm font-medium text-text-sub">折扣力度</span>}
                              tooltip="例如 8.5 代表打 8.5 折"
                              rules={[{ required: true, message: '请输入折扣力度' }]}
                              className="mb-0"
                            >
                              <InputNumber
                                min={0.1}
                                max={9.9}
                                step={0.1}
                                precision={1}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="8.8"
                                addonAfter="折"
                              />
                            </Form.Item>
                            <Form.Item
                              name={['settings', 'minAmount']}
                              label={<span className="text-sm font-medium text-text-sub">门槛金额</span>}
                              tooltip="0 代表无门槛"
                              className="mb-0"
                            >
                              <InputNumber
                                min={0}
                                step={1}
                                precision={2}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="0.00"
                                addonAfter="元"
                              />
                            </Form.Item>
                          </div>
                        )}

                        {selectedType === 'COUPON' && (
                          <div className="grid grid-cols-1 gap-3 md:grid-cols-2">
                            <Form.Item
                              name={['settings', 'discountAmount']}
                              label={<span className="text-sm font-medium text-text-sub">优惠金额</span>}
                              rules={[{ required: true, message: '请输入优惠金额' }]}
                              className="mb-0"
                            >
                              <InputNumber
                                min={0.01}
                                step={1}
                                precision={2}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="20"
                                addonAfter="元"
                              />
                            </Form.Item>
                            <Form.Item
                              name={['settings', 'minAmount']}
                              label={<span className="text-sm font-medium text-text-sub">使用门槛</span>}
                              rules={[{ required: true, message: '请输入使用门槛' }]}
                              className="mb-0"
                            >
                              <InputNumber
                                min={0.01}
                                step={1}
                                precision={2}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="100"
                                addonAfter="元"
                              />
                            </Form.Item>
                          </div>
                        )}

                        {selectedType === 'POINTS' && (
                          <Form.Item
                            name={['settings', 'pointsMultiplier']}
                            label={<span className="text-sm font-medium text-text-sub">积分倍率</span>}
                            tooltip="例如 2 代表双倍积分"
                            rules={[{ required: true, message: '请输入积分倍率' }]}
                            className="mb-0"
                          >
                            <InputNumber
                              min={1}
                              max={10}
                              step={0.5}
                              precision={1}
                              className="!w-full h-10 rounded-lg text-sm"
                              placeholder="2"
                              addonAfter="倍"
                            />
                          </Form.Item>
                        )}

                        {selectedType === 'SPECIAL' && (
                          <div className="grid grid-cols-1 gap-3 md:grid-cols-3">
                            <Form.Item
                              name={['settings', 'specialPrice']}
                              label={<span className="text-sm font-medium text-text-sub">特惠价</span>}
                              rules={[{ required: true, message: '请输入特惠价' }]}
                              className="mb-0"
                            >
                              <InputNumber
                                min={0.01}
                                step={1}
                                precision={2}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="88"
                                addonAfter="元"
                              />
                            </Form.Item>
                            <Form.Item
                              name={['settings', 'originalPrice']}
                              label={<span className="text-sm font-medium text-text-sub">原价</span>}
                              className="mb-0"
                            >
                              <InputNumber
                                min={0.01}
                                step={1}
                                precision={2}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="108"
                                addonAfter="元"
                              />
                            </Form.Item>
                            <Form.Item
                              name={['settings', 'stockLimit']}
                              label={<span className="text-sm font-medium text-text-sub">限量份数</span>}
                              className="mb-0"
                            >
                              <InputNumber
                                min={1}
                                step={1}
                                precision={0}
                                className="!w-full h-10 rounded-lg text-sm"
                                placeholder="50"
                                addonAfter="份"
                              />
                            </Form.Item>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>}

                {/* 活动状态设置 (编辑时可见) */}
                {editingItem ? (
                  <div className="group relative overflow-hidden rounded-xl border border-border-dark bg-gradient-to-br from-card-bg to-page-bg/60 p-4 transition-all duration-300 hover:border-brand-gold/30 hover:shadow-lg hover:shadow-brand-gold/5">
                    <div className="relative">
                      <div className="mb-3 flex items-center gap-2">
                        <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                        <h3 className="text-base font-semibold uppercase tracking-widest text-brand-gold">活动状态</h3>
                        <div className="h-px flex-1 bg-gradient-to-r from-brand-gold/30 to-transparent"></div>
                      </div>
                      <Form.Item
                        name="status"
                        label={<span className="text-sm font-medium text-text-sub">活动状态</span>}
                        className="mb-0"
                      >
                        <Select
                          options={STATUS_OPTIONS}
                          {...darkSelectProps}
                          className="h-10"
                        />
                      </Form.Item>
                    </div>
                  </div>
                ) : false && (
                  <div className="flex items-center rounded-xl border border-brand-gold/25 bg-brand-gold/5 px-4 py-3.5">
                    <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-gold/20 shrink-0">
                      <Sparkles size={14} className="text-brand-gold animate-pulse" />
                    </div>
                    <div className="ml-3">
                      <div className="text-xs font-bold text-text-main">新建活动默认为草稿</div>
                      <div className="text-[10px] text-text-weak mt-0.5">保存后可在列表中修改状态为“进行中”</div>
                    </div>
                  </div>
                )}
              </div>

              {/* Right Column - Live Preview & Validity Time (40% width) */}
              <div className="space-y-3 md:col-span-5">

                {/* 微信小程序端卡片实时预览 */}
                <div className="rounded-xl border border-border-dark bg-card-bg/40 p-3 relative overflow-hidden">
                  <div className="absolute right-0 top-0 h-24 w-24 bg-brand-gold/5 blur-2xl"></div>
                  <div className="relative">
                    <div className="mb-2 text-[10px] font-bold uppercase tracking-widest text-brand-gold flex items-center gap-1.5">
                      <Sparkles size={11} className="text-brand-gold" />
                      <span>手机端显示预览</span>
                    </div>
                    <MiniAppPreview
                      title={watchedTitle}
                      type={watchedType}
                      settings={watchedSettings}
                      description={watchedDescription}
                      startDate={activityDateRange[0]}
                      endDate={activityDateRange[1]}
                    />
                  </div>
                </div>

                {/* 生效时间设置 */}
                {false && <div className="group relative overflow-hidden rounded-xl border border-border-dark bg-gradient-to-br from-card-bg to-page-bg/60 p-4 transition-all duration-300 hover:border-brand-gold/30 hover:shadow-lg hover:shadow-brand-gold/5">
                  <div className="absolute right-0 bottom-0 h-32 w-32 bg-brand-gold/5 blur-3xl"></div>
                  <div className="relative">
                    <div className="mb-3 flex items-center gap-2">
                      <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                      <h3 className="text-base font-semibold uppercase tracking-widest text-brand-gold">生效时间</h3>
                      <div className="h-px flex-1 bg-gradient-to-r from-brand-gold/30 to-transparent"></div>
                    </div>

                    {/* 快捷一键设置 */}
                    <div className="mb-2">
                      <div className="mb-1.5 hidden text-[10px] font-semibold uppercase tracking-widest text-text-weak xl:block">
                        快捷一键设置
                      </div>
                      <div className="flex flex-wrap gap-1.5">
                        {[
                          { label: '1小时', value: '1h' },
                          { label: '24小时', value: '24h' },
                          { label: '3天', value: '3d' },
                          { label: '7天', value: '7d' },
                        ].map((preset) => {
                          const isActive = activePreset === preset.value;
                          return (
                            <button
                              key={preset.value}
                              type="button"
                              onClick={() => applyPreset(preset.value as any)}
                              className={`px-2.5 py-1 rounded-md text-[11px] font-semibold tracking-wider transition-all duration-200 cursor-pointer select-none ${
                                isActive
                                  ? 'bg-brand-gold text-page-bg font-bold shadow-md shadow-brand-gold/20'
                                  : 'border border-border-dark text-text-sub bg-[#111114] hover:border-brand-gold/40 hover:text-brand-gold'
                              }`}
                            >
                              {preset.label}
                            </button>
                          );
                        })}
                        <button
                          type="button"
                          className={`px-2.5 py-1 rounded-md text-[11px] font-semibold tracking-wider border select-none ${
                            !activePreset
                              ? 'border-brand-gold/30 bg-brand-gold/5 text-brand-gold'
                              : 'border-border-dark text-text-weak'
                          }`}
                          disabled
                        >
                          自定义
                        </button>
                      </div>
                    </div>

                    {/* 精准自定义细调 */}
                    <div className="bg-[#111114] border border-border-dark/60 rounded-xl p-2.5">
                      <div className="grid grid-cols-2 gap-2 relative">
                        {/* Middle separator line */}
                        <div className="absolute left-1/2 top-0 bottom-0 w-px bg-border-dark/40 -translate-x-1/2 hidden sm:block"></div>

                        {/* 开始日期时间 */}
                        <div className="space-y-2">
                          <div>
                            <div className="flex items-center gap-1.5 mb-1">
                              <Calendar size={12} className="text-brand-gold" />
                              <span className="text-[10px] font-semibold text-text-sub uppercase tracking-wider">开始日期</span>
                            </div>
                            <DatePicker
                              format="YYYY-MM-DD"
                              locale={rangePickerLocale}
                              value={activityDateRange[0]}
                              onChange={(newDate) => {
                                if (!newDate) {
                                  handleStartDateChange(null);
                                  return;
                                }
                                const current = activityDateRange[0] || dayjs();
                                const nextDate = newDate
                                  .hour(current.hour())
                                  .minute(current.minute())
                                  .second(current.second())
                                  .millisecond(0);
                                handleStartDateChange(nextDate);
                              }}
                              disabledDate={(current) => current && current.isBefore(dayjs().startOf('day'))}
                              className="xunye-picker w-full h-9 rounded-lg text-xs"
                              placeholder="选择开始日期"
                              popupStyle={{ backgroundColor: '#1A1A1F', border: '1px solid #2A2A31' }}
                            />
                          </div>

                          {activityDateRange[0] && (
                            <LoopTimePicker
                              label="开始时间微调"
                              value={activityDateRange[0]}
                              onChange={handleStartDateChange}
                              compact
                            />
                          )}
                        </div>

                        {/* 结束日期时间 */}
                        <div className="space-y-2">
                          <div>
                            <div className="flex items-center gap-1.5 mb-1">
                              <Clock size={12} className="text-brand-gold" />
                              <span className="text-[10px] font-semibold text-text-sub uppercase tracking-wider">结束日期</span>
                            </div>
                            <DatePicker
                              format="YYYY-MM-DD"
                              locale={rangePickerLocale}
                              value={activityDateRange[1]}
                              onChange={(newDate) => {
                                if (!newDate) {
                                  handleEndDateChange(null);
                                  return;
                                }
                                const current = activityDateRange[1] || dayjs();
                                const nextDate = newDate
                                  .hour(current.hour())
                                  .minute(current.minute())
                                  .second(current.second())
                                  .millisecond(0);
                                handleEndDateChange(nextDate);
                              }}
                              disabledDate={(current) => {
                                if (!activityDateRange[0]) return false;
                                return current && current.isBefore(activityDateRange[0].startOf('day'));
                              }}
                              className="xunye-picker w-full h-9 rounded-lg text-xs"
                              placeholder="选择结束日期"
                              popupStyle={{ backgroundColor: '#1A1A1F', border: '1px solid #2A2A31' }}
                            />
                          </div>

                          {activityDateRange[1] && (
                            <LoopTimePicker
                              label="结束时间微调"
                              value={activityDateRange[1]}
                              onChange={handleEndDateChange}
                              compact
                            />
                          )}
                        </div>
                      </div>
                    </div>

                    {/* 活动时长预览小字 */}
                    <div className="mt-1.5 flex items-center justify-between gap-2 text-[9px] text-text-weak bg-[#111114] border border-border-dark/40 rounded-lg p-1.5 font-mono">
                      <span>已选时长：{activityDurationPreview}</span>
                      <span className="max-w-[170px] truncate text-right text-brand-gold/60">{activityTimeDisplay}</span>
                    </div>

                  </div>
                </div>}

              </div>

            </div>
          </Form>

          {/* 底部操作按钮 */}
          <div className="mt-2 flex h-11 shrink-0 items-center justify-end gap-2 border-t border-brand-gold/20 pt-2">
            <button
              type="button"
              onClick={closeEditorPanel}
              className="group flex h-9 items-center gap-2 rounded-lg border border-border-dark bg-transparent px-5 text-sm font-medium text-text-sub transition-all duration-300 hover:border-brand-gold/50 hover:bg-brand-gold/5 hover:text-text-main"
            >
              <X size={16} className="transition-transform duration-300 group-hover:rotate-90" />
              <span>取消</span>
            </button>
            <button
              type="button"
              onClick={handleSubmit}
              disabled={submitting}
              className="flex h-9 items-center gap-2 rounded-lg bg-gradient-to-r from-brand-gold to-brand-gold/90 px-6 text-sm font-bold text-page-bg shadow-lg shadow-brand-gold/20 transition-all duration-300 hover:shadow-xl hover:shadow-brand-gold/30 disabled:cursor-not-allowed disabled:opacity-50 disabled:shadow-none"
            >
              {submitting ? (
                <>
                  <div className="h-4 w-4 animate-spin rounded-full border-2 border-page-bg border-t-transparent"></div>
                  <span>保存中...</span>
                </>
              ) : (
                <>
                  <Plus size={16} />
                  <span>{editingItem ? '保存修改' : '创建活动'}</span>
                </>
              )}
            </button>
          </div>
        </div>
      </Modal>
      <Modal
        open={!!configPanel}
        onCancel={() => setConfigPanel(null)}
        footer={null}
        width={configPanel === 'time' ? 760 : 820}
        rootClassName="xunye-config-modal"
        styles={activityModalStyles}
        title={
          <span className="text-brand-gold font-serif tracking-wider">
            {configPanel === 'rules' ? '规则设置' : configPanel === 'time' ? '生效时间' : '活动范围'}
          </span>
        }
      >
        <Form
          form={form}
          layout="vertical"
          component={false}
          onValuesChange={(changedValues) => {
            if (changedValues.type) {
              handleTypeChange(changedValues.type);
            }
          }}
        >
          {configPanel === 'rules' && (
          <div className="space-y-4">
            <Form.Item
              name="type"
              label={<span className="text-sm font-medium text-text-sub">活动类型</span>}
              rules={[{ required: true, message: '请选择活动类型' }]}
              className="mb-0"
            >
              <ActivityTypeGrid />
            </Form.Item>
            <div className="rounded-xl border border-border-dark/50 bg-[#111114] p-4">
              {selectedType === 'DISCOUNT' && (
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <Form.Item name={['settings', 'discountRate']} label={<span className="text-sm font-medium text-text-sub">折扣力度</span>} rules={[{ required: true, message: '请输入折扣力度' }]} className="mb-0">
                    <InputNumber min={0.1} max={9.9} step={0.1} precision={1} className="!w-full h-10 rounded-lg text-sm" placeholder="8.8" addonAfter="折" />
                  </Form.Item>
                  <Form.Item name={['settings', 'minAmount']} label={<span className="text-sm font-medium text-text-sub">门槛金额</span>} className="mb-0">
                    <InputNumber min={0} step={1} precision={2} className="!w-full h-10 rounded-lg text-sm" placeholder="0.00" addonAfter="元" />
                  </Form.Item>
                </div>
              )}
              {selectedType === 'COUPON' && (
                <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                  <Form.Item name={['settings', 'discountAmount']} label={<span className="text-sm font-medium text-text-sub">优惠金额</span>} rules={[{ required: true, message: '请输入优惠金额' }]} className="mb-0">
                    <InputNumber min={0.01} step={1} precision={2} className="!w-full h-10 rounded-lg text-sm" placeholder="20" addonAfter="元" />
                  </Form.Item>
                  <Form.Item name={['settings', 'minAmount']} label={<span className="text-sm font-medium text-text-sub">使用门槛</span>} rules={[{ required: true, message: '请输入使用门槛' }]} className="mb-0">
                    <InputNumber min={0.01} step={1} precision={2} className="!w-full h-10 rounded-lg text-sm" placeholder="100" addonAfter="元" />
                  </Form.Item>
                </div>
              )}
              {selectedType === 'POINTS' && (
                <Form.Item name={['settings', 'pointsMultiplier']} label={<span className="text-sm font-medium text-text-sub">积分倍率</span>} rules={[{ required: true, message: '请输入积分倍率' }]} className="mb-0">
                  <InputNumber min={1} max={10} step={0.5} precision={1} className="!w-full h-10 rounded-lg text-sm" placeholder="2" addonAfter="倍" />
                </Form.Item>
              )}
              {selectedType === 'SPECIAL' && (
                <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                  <Form.Item name={['settings', 'specialPrice']} label={<span className="text-sm font-medium text-text-sub">特惠价</span>} rules={[{ required: true, message: '请输入特惠价' }]} className="mb-0">
                    <InputNumber min={0.01} step={1} precision={2} className="!w-full h-10 rounded-lg text-sm" placeholder="88" addonAfter="元" />
                  </Form.Item>
                  <Form.Item name={['settings', 'originalPrice']} label={<span className="text-sm font-medium text-text-sub">原价</span>} className="mb-0">
                    <InputNumber min={0.01} step={1} precision={2} className="!w-full h-10 rounded-lg text-sm" placeholder="108" addonAfter="元" />
                  </Form.Item>
                  <Form.Item name={['settings', 'stockLimit']} label={<span className="text-sm font-medium text-text-sub">限量份数</span>} className="mb-0">
                    <InputNumber min={1} step={1} precision={0} className="!w-full h-10 rounded-lg text-sm" placeholder="50" addonAfter="份" />
                  </Form.Item>
                </div>
              )}
            </div>
            <div className="flex justify-end">
              <button type="button" onClick={() => setConfigPanel(null)} className="h-9 rounded-lg bg-brand-gold px-5 text-sm font-bold text-page-bg">完成</button>
            </div>
          </div>
        )}
          {configPanel === 'time' && (
          <div className="space-y-4">
            <div className="flex flex-wrap gap-2">
              {[{ label: '1小时', value: '1h' }, { label: '24小时', value: '24h' }, { label: '3天', value: '3d' }, { label: '7天', value: '7d' }].map((preset) => (
                <button key={preset.value} type="button" onClick={() => applyPreset(preset.value as any)} className={`px-3 py-1.5 rounded-lg text-xs font-semibold ${activePreset === preset.value ? 'bg-brand-gold text-page-bg' : 'border border-border-dark text-text-sub'}`}>
                  {preset.label}
                </button>
              ))}
            </div>
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <DatePicker format="YYYY-MM-DD" locale={rangePickerLocale} value={activityDateRange[0]} onChange={(newDate) => newDate && handleStartDateChange(newDate.hour(activityDateRange[0]?.hour() ?? dayjs().hour()).minute(activityDateRange[0]?.minute() ?? dayjs().minute()).second(activityDateRange[0]?.second() ?? 0).millisecond(0))} disabledDate={(current) => current && current.isBefore(dayjs().startOf('day'))} className="xunye-picker w-full h-10 rounded-lg text-xs" />
              <DatePicker format="YYYY-MM-DD" locale={rangePickerLocale} value={activityDateRange[1]} onChange={(newDate) => newDate && handleEndDateChange(newDate.hour(activityDateRange[1]?.hour() ?? dayjs().hour()).minute(activityDateRange[1]?.minute() ?? dayjs().minute()).second(activityDateRange[1]?.second() ?? 0).millisecond(0))} className="xunye-picker w-full h-10 rounded-lg text-xs" />
              <LoopTimePicker label="开始时间微调" value={activityDateRange[0]} onChange={handleStartDateChange} />
              <LoopTimePicker label="结束时间微调" value={activityDateRange[1]} onChange={handleEndDateChange} />
            </div>
            <div className="rounded-lg border border-border-dark bg-[#111114] p-3 text-xs text-text-sub">{activityTimeDisplay}</div>
            <div className="flex justify-end">
              <button type="button" onClick={() => setConfigPanel(null)} className="h-9 rounded-lg bg-brand-gold px-5 text-sm font-bold text-page-bg">完成</button>
            </div>
          </div>
        )}
          {configPanel === 'scope' && (
            <div className="space-y-4">
              {/* Product Range Selection */}
              <div className="rounded-xl border border-border-dark bg-[#111114] p-4 space-y-3">
                <div className="flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                  <span className="text-xs font-bold uppercase tracking-widest text-brand-gold">商品活动范围</span>
                </div>
                
                <Form.Item name={['settings', 'scopeProductType']} className="mb-0">
                  <Select
                    {...darkSelectProps}
                    options={[
                      { label: '全部商品参与', value: 'ALL' },
                      { label: '指定分类参与', value: 'CATEGORY' },
                      { label: '指定商品参与', value: 'PRODUCT' }
                    ]}
                  />
                </Form.Item>

                {watchedScopeProductType === 'CATEGORY' && (
                  <div className="space-y-1.5 animate-fadeIn">
                    <div className="text-[10px] text-text-weak font-semibold uppercase tracking-wider">请选择参与活动的商品分类</div>
                    <Form.Item name={['settings', 'categoryIds']} className="mb-0">
                      <Select
                        {...darkSelectProps}
                        mode="multiple"
                        placeholder="选择商品分类（可多选）"
                        allowClear
                        options={categories.map(c => ({ label: c.name, value: c.id }))}
                      />
                    </Form.Item>
                  </div>
                )}

                {watchedScopeProductType === 'PRODUCT' && (
                  <div className="space-y-1.5 animate-fadeIn">
                    <div className="text-[10px] text-text-weak font-semibold uppercase tracking-wider">请选择参与活动的具体商品</div>
                    <Form.Item name={['settings', 'productIds']} className="mb-0">
                      <Select
                        {...darkSelectProps}
                        mode="multiple"
                        placeholder="选择具体商品（可多选）"
                        allowClear
                        showSearch
                        optionFilterProp="label"
                        options={products.map(p => ({
                          label: `${p.name} (${p.category} - 售价¥${p.price})`,
                          value: p.id
                        }))}
                      />
                    </Form.Item>
                  </div>
                )}
              </div>

              {/* Table Area Range Selection */}
              <div className="rounded-xl border border-border-dark bg-[#111114] p-4 space-y-3">
                <div className="flex items-center gap-2">
                  <div className="h-1.5 w-1.5 rounded-full bg-brand-gold"></div>
                  <span className="text-xs font-bold uppercase tracking-widest text-brand-gold">桌台区域活动范围</span>
                </div>

                <Form.Item name={['settings', 'scopeTableType']} className="mb-0">
                  <Select
                    {...darkSelectProps}
                    options={[
                      { label: '全部区域参与', value: 'ALL' },
                      { label: '指定区域参与', value: 'AREA' },
                      { label: '指定桌台参与', value: 'TABLE' }
                    ]}
                  />
                </Form.Item>

                {watchedScopeTableType === 'AREA' && (
                  <div className="space-y-1.5 animate-fadeIn">
                    <div className="text-[10px] text-text-weak font-semibold uppercase tracking-wider">请选择参与活动的桌台区域</div>
                    <Form.Item name={['settings', 'areaIds']} className="mb-0">
                      <Select
                        {...darkSelectProps}
                        mode="multiple"
                        placeholder="选择桌台区域（可多选）"
                        allowClear
                        options={areas.map(a => ({ label: a.name, value: a.id }))}
                      />
                    </Form.Item>
                  </div>
                )}

                {watchedScopeTableType === 'TABLE' && (
                  <div className="space-y-1.5 animate-fadeIn">
                    <div className="text-[10px] text-text-weak font-semibold uppercase tracking-wider">请选择参与活动的具体桌台</div>
                    <Form.Item name={['settings', 'tableIds']} className="mb-0">
                      <Select
                        {...darkSelectProps}
                        mode="multiple"
                        placeholder="选择具体桌台（可多选）"
                        allowClear
                        showSearch
                        optionFilterProp="label"
                        options={tables.map(t => ({
                          label: `${t.name} (${t.areaName} - 容纳${t.capacity}人)`,
                          value: t.id
                        }))}
                      />
                    </Form.Item>
                  </div>
                )}
              </div>

              <div className="flex justify-end pt-2">
                <button type="button" onClick={() => setConfigPanel(null)} className="h-9 rounded-lg bg-brand-gold px-5 text-sm font-bold text-page-bg hover:brightness-110 transition-all">完成</button>
              </div>
            </div>
          )}
        </Form>
      </Modal>
    </div>
  );
}
