import React, { useState, useEffect } from 'react';
import { Table, Button, Space, Modal, Form, Input, InputNumber, Switch, App, Tag, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import request from '@/api/request';

const { TextArea } = Input;

interface MemberLevelConfig {
  id: number;
  level: string;
  name: string;
  minAmount: number;
  upgradeOrders: number;
  discount: number;
  pointsRate: number;
  description: string;
  benefits: Record<string, any>;
  icon: string;
  color: string;
  sort: number;
  status: number;
}

const MemberLevelConfigs: React.FC = () => {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [dataSource, setDataSource] = useState<MemberLevelConfig[]>([]);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await request.get('/api/admin/member-level-configs');
      setDataSource(response);
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

  const handleEdit = async (record: MemberLevelConfig) => {
    try {
      const response = await request.get(`/api/admin/member-level-configs/${record.id}`);
      form.setFieldsValue({
        ...response,
        benefitsDescription: response.benefits?.description || '',
      });
      setEditingId(record.id);
      setModalVisible(true);
    } catch (error) {
      message.error('加载详情失败');
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await request.delete(`/api/admin/member-level-configs/${id}`);
      message.success('删除成功');
      fetchData();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleStatusChange = async (id: number, status: number) => {
    try {
      await request.patch(`/api/admin/member-level-configs/${id}/status`, null, {
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
        benefits: {
          description: values.benefitsDescription || '',
        },
      };
      delete submitData.benefitsDescription;

      if (editingId) {
        await request.put(`/api/admin/member-level-configs/${editingId}`, submitData);
        message.success('更新成功');
      } else {
        await request.post('/api/admin/member-level-configs', submitData);
        message.success('创建成功');
      }
      setModalVisible(false);
      fetchData();
    } catch (error) {
      message.error('操作失败');
    }
  };

  const columns: ColumnsType<MemberLevelConfig> = [
    {
      title: '等级',
      dataIndex: 'name',
      width: 120,
      render: (name: string, record) => (
        <Space>
          <div
            style={{
              width: 12,
              height: 12,
              borderRadius: '50%',
              backgroundColor: record.color || '#999',
            }}
          />
          <span>{name}</span>
        </Space>
      ),
    },
    {
      title: '等级代码',
      dataIndex: 'level',
      width: 120,
    },
    {
      title: '升级条件',
      width: 200,
      render: (_, record) => (
        <div>
          <div>累计消费: ¥{record.minAmount}</div>
          <div>累计订单: {record.upgradeOrders}单</div>
        </div>
      ),
    },
    {
      title: '折扣率',
      dataIndex: 'discount',
      width: 100,
      render: (discount: number) => (
        <Tag color={discount < 1 ? 'green' : 'default'}>
          {(discount * 100).toFixed(0)}折
        </Tag>
      ),
    },
    {
      title: '积分倍率',
      dataIndex: 'pointsRate',
      width: 100,
      render: (rate: number) => `${rate}倍`,
    },
    {
      title: '会员权益',
      dataIndex: 'benefits',
      width: 200,
      render: (benefits: Record<string, any>) => benefits?.description || '-',
    },
    {
      title: '排序',
      dataIndex: 'sort',
      width: 80,
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
        <h2>会员等级配置</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新建等级
        </Button>
      </div>

      <Table
        loading={loading}
        columns={columns}
        dataSource={dataSource}
        rowKey="id"
        pagination={false}
      />

      <Modal
        title={editingId ? '编辑会员等级' : '新建会员等级'}
        open={modalVisible}
        onOk={handleSubmit}
        onCancel={() => setModalVisible(false)}
        width={700}
        destroyOnHidden
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="level"
            label="等级代码"
            rules={[{ required: true, message: '请输入等级代码' }]}
          >
            <Input placeholder="如：GOLD" disabled={!!editingId} />
          </Form.Item>

          <Form.Item
            name="name"
            label="等级名称"
            rules={[{ required: true, message: '请输入等级名称' }]}
          >
            <Input placeholder="如：金卡会员" />
          </Form.Item>

          <Form.Item
            name="minAmount"
            label="升级所需累计消费金额"
            rules={[{ required: true, message: '请输入金额' }]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} addonAfter="元" />
          </Form.Item>

          <Form.Item
            name="upgradeOrders"
            label="升级所需累计订单数"
            initialValue={0}
          >
            <InputNumber min={0} style={{ width: '100%' }} addonAfter="单" />
          </Form.Item>

          <Form.Item
            name="discount"
            label="折扣率"
            rules={[{ required: true, message: '请输入折扣率' }]}
          >
            <InputNumber
              min={0}
              max={1}
              step={0.01}
              precision={2}
              style={{ width: '100%' }}
              addonAfter="(如0.95表示95折)"
            />
          </Form.Item>

          <Form.Item
            name="pointsRate"
            label="积分倍率"
            rules={[{ required: true, message: '请输入积分倍率' }]}
          >
            <InputNumber
              min={0}
              step={0.1}
              precision={2}
              style={{ width: '100%' }}
              addonAfter="倍"
            />
          </Form.Item>

          <Form.Item name="description" label="等级描述">
            <Input placeholder="如：金卡会员 - 累计消费满5000元" />
          </Form.Item>

          <Form.Item name="benefitsDescription" label="会员权益">
            <TextArea rows={3} placeholder="如：95折优惠 + 1.5倍积分 + 生日特权" />
          </Form.Item>

          <Form.Item name="color" label="等级颜色" initialValue="#ffd700">
            <Input type="color" style={{ width: 100 }} />
          </Form.Item>

          <Form.Item name="icon" label="等级图标URL">
            <Input placeholder="图标URL（可选）" />
          </Form.Item>

          <Form.Item
            name="sort"
            label="排序"
            initialValue={0}
            tooltip="数字越大等级越高"
          >
            <InputNumber style={{ width: '100%' }} />
          </Form.Item>

          <Form.Item name="status" label="状态" initialValue={1} valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="禁用" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default MemberLevelConfigs;
