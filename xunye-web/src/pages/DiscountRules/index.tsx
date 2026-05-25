import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Form, Input, Select, InputNumber, Switch, message, Tag, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import request from '@/api/request';

const { Option } = Select;
const { TextArea } = Input;

interface DiscountRule {
  id: number;
  name: string;
  description: string;
  ruleType: string;
  priority: number;
  conditions: Record<string, any>;
  exclusiveGroups: string;
  stackable: number;
  maxDiscountAmount: number;
  minPayAmount: number;
  status: number;
}

const DiscountRules: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<DiscountRule[]>([]);
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
      const response = await request.get('/api/admin/discount-rules', {
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

  const handleEdit = async (record: DiscountRule) => {
    try {
      const response = await request.get(`/api/admin/discount-rules/${record.id}`);
      form.setFieldsValue(response.data);
      setEditingId(record.id);
      setModalVisible(true);
    } catch (error) {
      message.error('加载详情失败');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/api/admin/discount-rules/${id}`);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleStatusChange = async (id: number, status: number) => {
    try {
      await request.patch(`/api/admin/discount-rules/${id}/status`, null, {
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
      const submitData = {
        ...values,
        conditions: values.conditions || {},
      };

      if (editingId) {
        await request.put(`/api/admin/discount-rules/${editingId}`, submitData);
        message.success('更新成功');
      } else {
        await request.post('/api/admin/discount-rules', submitData);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns: ColumnsType<DiscountRule> = [
    {
      title: 'ID',
      dataIndex: 'id',
      width: 80,
    },
    {
      title: '规则名称',
      dataIndex: 'name',
      width: 150,
    },
    {
      title: '规则类型',
      dataIndex: 'ruleType',
      width: 120,
      render: (type: string) => {
        const map: Record<string, { text: string; color: string }> = {
          MEMBER: { text: '会员折扣', color: 'blue' },
          ACTIVITY: { text: '活动折扣', color: 'green' },
          COUPON: { text: '优惠券', color: 'orange' },
        };
        const config = map[type] || { text: type, color: 'default' };
        return <Tag color={config.color}>{config.text}</Tag>;
      },
    },
    {
      title: '优先级',
      dataIndex: 'priority',
      width: 100,
      sorter: (a, b) => a.priority - b.priority,
      render: (priority: number) => (
        <Tag color={priority >= 100 ? 'red' : priority >= 50 ? 'orange' : 'default'}>
          {priority}
        </Tag>
      ),
    },
    {
      title: '互斥组',
      dataIndex: 'exclusiveGroups',
      width: 150,
      render: (groups: string) => groups || '-',
    },
    {
      title: '可叠加',
      dataIndex: 'stackable',
      width: 100,
      render: (stackable: number) => (
        <Tag color={stackable ? 'green' : 'red'}>
          {stackable ? '是' : '否'}
        </Tag>
      ),
    },
    {
      title: '最大优惠',
      dataIndex: 'maxDiscountAmount',
      width: 120,
      render: (amount: number) => amount ? `¥${amount}` : '不限制',
    },
    {
      title: '最低支付',
      dataIndex: 'minPayAmount',
      width: 120,
      render: (amount: number) => amount ? `¥${amount}` : '不限制',
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
        <h2>折扣规则管理</h2>
        <Space>
          <Input.Search
            placeholder="搜索规则"
            allowClear
            style={{ width: 250 }}
            onSearch={setKeyword}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新建规则
          </Button>
        </Space>
      </div>

      <Table
        loading={loading}
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        scroll={{ x: 1400 }}
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
        title={editingId ? '编辑折扣规则' : '新建折扣规则'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
        destroyOnClose
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="规则名称"
            rules={[{ required: true, message: '请输入规则名称' }]}
          >
            <Input placeholder="如：会员等级折扣" />
          </Form.Item>

          <Form.Item name="description" label="规则描述">
            <TextArea rows={2} placeholder="如：根据会员等级自动应用折扣" />
          </Form.Item>

          <Form.Item
            name="ruleType"
            label="规则类型"
            rules={[{ required: true, message: '请选择规则类型' }]}
          >
            <Select placeholder="请选择">
              <Option value="MEMBER">会员折扣</Option>
              <Option value="ACTIVITY">活动折扣</Option>
              <Option value="COUPON">优惠券</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="priority"
            label="优先级"
            rules={[{ required: true, message: '请输入优先级' }]}
            tooltip="数字越大优先级越高"
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item
            name="exclusiveGroups"
            label="互斥组"
            tooltip="同组规则不能同时使用，多个组用逗号分隔"
          >
            <Input placeholder="如：member_discount,activity_discount" />
          </Form.Item>

          <Form.Item
            name="stackable"
            label="是否可叠加"
            initialValue={1}
            valuePropName="checked"
          >
            <Switch checkedChildren="可叠加" unCheckedChildren="不可叠加" />
          </Form.Item>

          <Form.Item name="maxDiscountAmount" label="最大优惠金额">
            <InputNumber
              min={0}
              precision={2}
              style={{ width: '100%' }}
              placeholder="不填表示不限制"
              addonAfter="元"
            />
          </Form.Item>

          <Form.Item name="minPayAmount" label="最低支付金额">
            <InputNumber
              min={0}
              precision={2}
              style={{ width: '100%' }}
              placeholder="不填表示不限制"
              addonAfter="元"
            />
          </Form.Item>

          <Form.Item name="status" label="状态" initialValue={1} valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DiscountRules;
