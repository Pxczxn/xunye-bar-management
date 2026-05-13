const { getOrderDetail } = require('../../services/customer');

const STATUS_MAP = {
  UNPAID: '待付款',
  PAID: '已付款，请等待酒水',
  CANCELLED: '已取消',
};

const SERVE_STATUS_MAP = {
  PENDING: '待处理',
  MAKING: '制作中',
  FINISHED: '已完成',
};

Page({
  data: {
    loading: true,
    error: false,
    refreshing: false,
    order: null,
  },

  _orderNo: null,
  _pollTimer: null,

  onLoad(options) {
    if (options.orderNo) {
      this._orderNo = options.orderNo;
      this.loadOrder(options.orderNo);
    } else {
      this.setData({ loading: false, error: true });
    }
  },

  onUnload() {
    this._clearPoll();
  },

  _clearPoll() {
    if (this._pollTimer) {
      clearTimeout(this._pollTimer);
      this._pollTimer = null;
    }
  },

  _schedulePoll(order) {
    this._clearPoll();
    if (order.status !== 'CANCELLED' && (order.serveStatus || 'PENDING') !== 'FINISHED') {
      this._pollTimer = setTimeout(() => {
        this.loadOrder(this._orderNo);
      }, 9000);
    }
  },

  loadOrder(orderNo, options = {}) {
    this._clearPoll();
    const silent = options.silent === true;
    if (silent) {
      this.setData({ refreshing: true, error: false });
    } else if (!this.data.order) {
      this.setData({ loading: true, error: false });
    }

    getOrderDetail(orderNo)
      .then((order) => {
        order.statusText = STATUS_MAP[order.status] || order.status || '';
        order.serveStatusText = SERVE_STATUS_MAP[order.serveStatus || 'PENDING'] || '待处理';
        order.totalAmountFormatted = Number(order.totalAmount || 0).toFixed(2);
        if (order.items) {
          order.items = order.items.map((item) => {
            item.amountFormatted = Number(item.amount || 0).toFixed(2);
            item.priceFormatted = Number(item.price || 0).toFixed(2);
            return item;
          });
        }
        this.setData({ order, loading: false, refreshing: false });
        this._schedulePoll(order);
      })
      .catch(() => {
        this.setData({ loading: false, refreshing: false, error: !silent });
        if (silent) {
          wx.showToast({ title: '刷新失败，请稍后再试', icon: 'none' });
        }
      });
  },

  onRetry() {
    if (this._orderNo) {
      this.loadOrder(this._orderNo);
    }
  },

  onRefreshStatus() {
    if (this._orderNo && !this.data.refreshing) {
      this.loadOrder(this._orderNo, { silent: true });
    }
  },
});
