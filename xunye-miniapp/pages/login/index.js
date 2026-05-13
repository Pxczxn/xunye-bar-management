Page({
  data: {
    loading: false,
  },

  onLogin() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
    } else {
      wx.reLaunch({ url: '/pages/menu/index' });
    }
  },
});
