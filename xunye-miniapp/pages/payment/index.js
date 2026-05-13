const { getOrderDetail } = require('../../services/customer');
const { pay } = require('../../services/payment');

Page({
  data: {
    orderNo: '',
    tableName: '',
    totalAmount: '0.00',
    paying: false,
    refreshing: false,
  },

  onLoad(options) {
    this.setData({
      orderNo: options.orderNo || '',
      tableName: decodeURIComponent(options.tableName || ''),
      totalAmount: Number(options.totalAmount || 0).toFixed(2),
    });
  },

  onPay() {
    if (this.data.paying) return;
    this.setData({ paying: true });
    pay(this.data.orderNo)
      .then(() => {
        wx.redirectTo({ url: '/pages/order-detail/index?orderNo=' + this.data.orderNo });
      })
      .catch((err) => {
        wx.showToast({ title: err.message || '支付失败，请重试', icon: 'none' });
      })
      .finally(() => { this.setData({ paying: false }); });
  },

  onRefreshStatus() {
    if (!this.data.orderNo || this.data.refreshing) return;
    this.setData({ refreshing: true });
    getOrderDetail(this.data.orderNo)
      .then((order) => {
        if (order.status === 'PAID') {
          wx.redirectTo({ url: '/pages/order-detail/index?orderNo=' + this.data.orderNo });
          return;
        }
        wx.showToast({ title: '订单仍待支付', icon: 'none' });
      })
      .catch(() => {
        wx.showToast({ title: '刷新失败，请稍后再试', icon: 'none' });
      })
      .finally(() => {
        this.setData({ refreshing: false });
      });
  },
});
