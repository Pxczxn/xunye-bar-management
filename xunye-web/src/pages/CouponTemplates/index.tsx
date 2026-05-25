import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, InputNumber, Switch, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import request from '@/api/request';

const { Option } = Select;
const { TextArea } = Input;

interface CouponTemplate {
  id: number;
  name: string;
  title: string;
  description: string;
  type: string;
  discountAmount: number;
  discountRate: number;
  minAmount: number;
  scopeType: string;
  issueType: string;
  validDays: number;
  maxUseCount: number;
  totalCount: number;
  issuedCount: number;
  usedCount: number;
  status: number;
  sort: number;
}

const CouponTemplates: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<CouponTemplate[]>([]);
  const [total, setTotal] = useState(0);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [keyword, setKeyword] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, [pageNum, pageSize, keyword]);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await request.get('/api/admin/coupon-templates', {
        params: { pageNum, pageSize, keyword }
      });
      setDataSource(response.data.records);
      setTotal(response.data.total);
    } catch (error) {
      message.error('加载失败');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    form.resetFields();
    setEditingId(null);
    setModalVisible(true);
  };

  const handleEdit = async (record: CouponTemplate) => {
    try {
      const response = await request.get(`/api/admin/coupon-templates/${record.id}`);
      form.setFieldsValue(response.data);
      setEditingId(record.id);
      setModalVisible(true);
    } catch (error) {
      message.error('加载详情失败');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/api/admin/coupon-templates/${id}`);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleStatusChange = async (id: number, status: number) => {
    try {
      await request.patch(`/api/admin/coupon-templates/${id}/status`, null, {
        params: { status: status ? 1 : 0 }
      });
      message.success('状态更新成功');
      fetchData();
    } catch (error) {
      message.error('状态更新失败');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingId) {
        await request.put(`/api/admin/coupon-templates/${editingId}`, values);
        message.success('更新成功');
      } else {
        await request.post('/api/admin/coupon-templates', values);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns: ColumnsType<CouponTemplate> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '优惠券名称',
      dataIndex: 'title',
      width: 150,
    },
    {
      title: '类型',
      dataIndex: 'type',
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'AMOUNT' ? 'blue' : 'green'}>
          {type === 'AMOUNT' ? '满减券' : '折扣券'}
        </Tag>
      ),
    },
    {
      title: '优惠内容',
      width: 120,
      render: (_, record) => (
        record.type === 'AMOUNT'
          ? `减${record.discountAmount}元`
          : `${(record.discountRate * 100).toFixed(0)}折`
      ),
    },
    {
      title: '使用门槛',
      dataIndex: 'minAmount',
      width: 100,
      render: (amount: number) => amount > 0 ? `满${amount}元` : '无门槛',
    },
    {
      title: '适用范围',
      dataIndex: 'scopeType',
      width: 100,
      render: (type: string) => {
        const map: Record<string, string> = {
          ALL: '全场',
          PRODUCT: '指定商品',
          CATEGORY: '指定分类',
          TABLE: '指定桌台',
        };
        return map[type] || type;
      },
    },
    {
      title: '发放类型',
      dataIndex: 'issueType',
      width: 100,
      render: (type: string) => {
        const map: Record<string, string> = {
          NEW_USER: '新用户',
          POINTS: '积分兑换',
          ACTIVITY: '活动赠送',
          MANUAL: '手动发放',
        };
        return map[type] || type;
      },
    },
    {
      title: '有效期',
      dataIndex: 'validDays',
      width: 100,
      render: (days: number) => `${days}天`,
    },
    {
      title: '发放/使用',
      width: 120,
      render: (_, record) => `${record.issuedCount}/${record.usedCount}`,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
      render: (status: number, record) => (
        <Switch
          checked={status === 1}
          onChange={(checked) => handleStatusChange(record.id, checked ? 1 : 0)}
        />
      ),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="page-container">
      <div className="page-header">
        <h2>优惠券管理</h2>
        <Space>
          <Input.Search
            placeholder="搜索优惠券"
            allowClear
            style={{ width: 250 }}
            onSearch={setKeyword}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建优惠券
          </Button>
        </Space>
      </div>

      <Table
        loading={loading}
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        scroll={{ x: 1500 }}
        pagination={{
          current: pageNum,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (page, size) => {
            setPageNum(page);
            setPageSize(size);
          },
        }}
      />

      <Modal
        title={editingId ? '编辑优惠券' : '新建优惠券'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={800}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="优惠券名称（内部标识）"
            rules={[{ required: true, message: '请输入优惠券名称' }]}
          >
            <Input placeholder="如：新用户满减券" />
          </Form.Item>

          <Form.Item
            name="title"
            label="优惠券标题（用户可见）"
            rules={[{ required: true, message: '请输入优惠券标题' }]}
          >
            <Input placeholder="如：满99减10" />
          </Form.Item>

          <Form.Item name="description" label="优惠券描述">
            <TextArea rows={2} placeholder="如：全场酒水可用" />
          </Form.Item>

          <Form.Item
            name="type"
            label="优惠券类型"
            rules={[{ required: true, message: '请选择优惠券类型' }]}
          >
            <Select placeholder="请选择">
              <Option value="AMOUNT">满减券</Option>
              <Option value="DISCOUNT">折扣券</Option>
            </Select>
          </Form.Item>

          <Form.Item
            noStyle
            shouldUpdate={(prevValues, currentValues) => prevValues.type !== currentValues.type}
          >
            {({ getFieldValue }) =>
              getFieldValue('type') === 'AMOUNT' ? (
                <Form.Item
                  name="discountAmount"
                  label="优惠金额"
                  rules={[{ required: true, message: '请输入优惠金额' }]}
                >
                  <InputNumber min={0} precision={2} style={{ width: '100%' }} addonAfter="元" />
                </Form.Item>
              ) : (
                <Form.Item
                  name="discountRate"
                  label="折扣率"
                  rules={[{ required: true, message: '请输入折扣率' }]}
                >
                  <InputNumber min={0} max={1} step={0.01} precision={2} style={{ width: '100%' }} addonAfter="(如0.85表示85折)" />
                </Form.Item>
              )
            }
          </Form.Item>

          <Form.Item
            name="minAmount"
            label="最低消费金额"
            rules={[{ required: true, message: '请输入最低消费金额' }]}
            initialValue={0}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} addonAfter="元（0表示无门槛）" />
          </Form.Item>

          <Form.Item
            name="scopeType"
            label="适用范围"
            rules={[{ required: true, message: '请选择适用范围' }]}
            initialValue="ALL"
          >
            <Select placeholder="请选择">
              <Option value="ALL">全场通用</Option>
              <Option value="PRODUCT">指定商品</Option>
              <Option value="CATEGORY">指定分类</Option>
              <Option value="TABLE">指定桌台</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="issueType"
            label="发放类型"
            rules={[{ required: true, message: '请选择发放类型' }]}
            initialValue="MANUAL"
          >
            <Select placeholder="请选择">
              <Option value="NEW_USER">新用户注册赠送</Option>
              <Option value="POINTS">积分兑换</Option>
              <Option value="ACTIVITY">活动赠送</Option>
              <Option value="MANUAL">手动发放</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="validDays"
            label="有效天数"
            rules={[{ required: true, message: '请输入有效天数' }]}
            initialValue={30}
          >
            <InputNumber min={1} style={{ width: '100%' }} addonAfter="天" />
          </Form.Item>

          <Form.Item name="maxUseCount" label="每人最多使用次数" initialValue={1}>
            <InputNumber min={1} style={{ width: '100%' }} addonAfter="次" />
          </Form.Item>

          <Form.Item name="totalCount" label="总发放数量">
            <InputNumber min={1} style={{ width: '100%' }} placeholder="不填表示不限制" />
          </Form.Item>

          <Form.Item name="memberLevelLimit" label="会员等级限制">
            <Input placeholder="如：GOLD,PLATINUM（逗号分隔，不填表示不限制）" />
          </Form.Item>

          <Form.Item name="sort" label="排序" initialValue={0}>
            <InputNumber style={{ width: '100%' }} placeholder="数字越大越靠前" />
          </Form.Item>

          <Form.Item name="status" label="状态" initialValue={1} valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CouponTemplates;
