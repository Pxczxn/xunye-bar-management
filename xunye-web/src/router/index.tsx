import { createBrowserRouter, Navigate } from 'react-router-dom';
import AdminLayout from '../layouts/AdminLayout';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import Products from '../pages/Products';
import Orders from '../pages/Orders';
import Inventory from '../pages/Inventory';
import Categories from '../pages/Categories';
import Settings from '../pages/Settings';
import Placeholder from '../pages/Placeholder';

const router = createBrowserRouter([
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/',
    element: <AdminLayout />,
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <Dashboard /> },
      { path: 'products', element: <Products /> },
      { path: 'orders', element: <Orders /> },
      { path: 'inventory', element: <Inventory /> },
      { path: 'pos', element: <Placeholder title="吧台点单" /> },
      { path: 'categories', element: <Categories /> },
      { path: 'tables', element: <Placeholder title="桌台区域" /> },
      { path: 'employees', element: <Placeholder title="员工账号" /> },
      { path: 'settings', element: <Settings /> },
    ],
  },
  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);

export default router;
