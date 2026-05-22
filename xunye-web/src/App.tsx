import { ConfigProvider, theme } from 'antd';
import { RouterProvider } from 'react-router-dom';
import router from './router';

export default function App() {
  return (
    <ConfigProvider
      theme={{
        algorithm: theme.darkAlgorithm,
        token: {
          colorPrimary: '#D6A85A',
          colorBgBase: '#0B0B0F',
          colorBgContainer: '#111114',
          colorBgElevated: '#1A1A1F',
          colorBorder: '#2A2A31',
          colorText: '#F4EBDD',
          colorTextDescription: '#AFA79B',
          colorTextPlaceholder: '#6F6A63',
        },
        components: {
          Modal: {
            contentBg: '#1A1A1F',
            headerBg: '#1A1A1F',
            footerBg: '#1A1A1F',
          },
          InputNumber: {
            handleBg: '#111114',
            handleActiveBg: '#1A1A1F',
            handleBorderColor: '#2A2A31',
          }
        }
      }}
    >
      <RouterProvider router={router} />
    </ConfigProvider>
  );
}
