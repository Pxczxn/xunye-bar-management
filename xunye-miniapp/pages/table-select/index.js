const { listTables } = require('../../services/customer');

Page({
  data: {
    tables: [],
    loading: true,
    error: false,
    currentTableId: null,
  },

  onLoad() {
    this.loadTables();
  },

  onShow() {
    const currentTable = wx.getStorageSync('currentTable');
    if (currentTable) {
      this.setData({ currentTableId: currentTable.id });
    }
  },

  loadTables() {
    this.setData({ loading: true, error: false });

    listTables()
      .then((tables) => {
        this.setData({ tables: tables || [], loading: false });
      })
      .catch(() => {
        this.setData({ loading: false, error: true });
      });
  },

  onRetry() {
    this.loadTables();
  },

  onSelect(e) {
    const table = e.currentTarget.dataset.table;
    wx.setStorageSync('currentTable', table);
    wx.showToast({ title: '已选择 ' + table.areaName + ' ' + table.name, icon: 'success', duration: 1500 });
    setTimeout(() => {
      const pages = getCurrentPages();
      if (pages.length > 1) {
        wx.navigateBack();
      } else {
        wx.reLaunch({ url: '/pages/menu/index' });
      }
    }, 1500);
  },
});
