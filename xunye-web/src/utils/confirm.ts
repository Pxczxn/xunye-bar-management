import { Modal, message } from 'antd';

/**
 * 确认删除对话框
 * @param name 要删除的项目名称
 * @param deleteFn 删除操作的异步函数
 * @param onSuccess 删除成功后的回调函数
 */
export function confirmDelete(
  name: string,
  deleteFn: () => Promise<void>,
  onSuccess: () => void
) {
  Modal.confirm({
    rootClassName: 'xunye-confirm-modal',
    title: '确认删除',
    content: `确定要删除「${name}」吗？`,
    okText: '删除',
    okButtonProps: { danger: true },
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteFn();
        message.success('删除成功');
        onSuccess();
      } catch (err: any) {
        message.error(err.message || '删除失败');
      }
    },
  });
}

/**
 * 通用确认对话框
 * @param title 对话框标题
 * @param content 对话框内容
 * @param confirmFn 确认操作的异步函数
 * @param onSuccess 操作成功后的回调函数
 * @param options 额外配置选项
 */
export function confirmAction(
  title: string,
  content: string,
  confirmFn: () => Promise<void>,
  onSuccess: () => void,
  options?: {
    okText?: string;
    cancelText?: string;
    danger?: boolean;
    successMessage?: string;
    errorMessage?: string;
  }
) {
  Modal.confirm({
    rootClassName: 'xunye-confirm-modal',
    title,
    content,
    okText: options?.okText || '确定',
    okButtonProps: { danger: options?.danger },
    cancelText: options?.cancelText || '取消',
    onOk: async () => {
      try {
        await confirmFn();
        message.success(options?.successMessage || '操作成功');
        onSuccess();
      } catch (err: any) {
        message.error(err.message || options?.errorMessage || '操作失败');
      }
    },
  });
}
