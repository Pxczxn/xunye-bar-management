const { getTableInfo } = require('../../services/customer');

Page({
  data: {
    appName: '寻野',
    slogan: 'XUNYE BAR',
    tableCode: '',
    tableName: '',
  },

  onLoad(options) {
    if (options.tableCode) {
      this.setData({ tableCode: options.tableCode });
      this.loadTableInfo(options.tableCode);
    }
  },

  loadTableInfo(tableCode) {
    getTableInfo(tableCode)
      .then((tableInfo) => {
        wx.setStorageSync('currentTable', tableInfo);
        this.setData({ tableName: tableInfo.areaName + ' ' + tableInfo.name });
      })
      .catch(() => {});
  },

  onStartOrder() {
    const currentTable = wx.getStorageSync('currentTable');
    const url = (currentTable && currentTable.id) ? '/pages/menu/index' : '/pages/table-select/index';
    wx.navigateTo({ url });
  },
});
