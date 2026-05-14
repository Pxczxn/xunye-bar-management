import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import AdminLayout from '../layouts/AdminLayout';
import Login from '../pages/Login';
import Dashboard from '../pages/Dashboard';
import Products from '../pages/Products';
import Orders from '../pages/Orders';
import Kitchen from '../pages/Kitchen';
import Inventory from '../pages/Inventory';
import Categories from '../pages/Categories';
import Tables from '../pages/Tables';
import Pos from '../pages/Pos';
import Settings from '../pages/Settings';
import Staff from '../pages/Staff';

function getRole(): string {
  try { return JSON.parse(localStorage.getItem('user') || '{}').role || ''; } catch { return ''; }
}

function defaultHome(role: string) {
  return role === 'STAFF' ? '/kitchen' : '/dashboard';
}

function RequireAuth() {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return <Outlet />;
}

function GuestOnly() {
  const token = localStorage.getItem('token');
  if (token) return <Navigate to={defaultHome(getRole())} replace />;
  return <Outlet />;
}

function RoleRoute({ roles }: { roles: string[] }) {
  const role = getRole();
  if (roles.includes(role)) return <Outlet />;
  return <Navigate to={defaultHome(role)} replace />;
}

const router = createBrowserRouter([
  {
    element: <GuestOnly />,
    children: [
      { path: '/login', element: <Login /> },
    ],
  },
  {
    element: <RequireAuth />,
    children: [
      {
        path: '/',
        element: <AdminLayout />,
        children: [
          { index: true, element: <Navigate to={defaultHome(getRole())} replace /> },
          { element: <RoleRoute roles={['BOSS', 'MANAGER']} />, children: [
            { path: 'dashboard', element: <Dashboard /> },
            { path: 'products', element: <Products /> },
            { path: 'inventory', element: <Inventory /> },
            { path: 'categories', element: <Categories /> },
          ]},
          { element: <RoleRoute roles={['BOSS']} />, children: [
            { path: 'employees', element: <Staff /> },
            { path: 'settings', element: <Settings /> },
          ]},
          { path: 'kitchen', element: <Kitchen /> },
          { path: 'orders', element: <Orders /> },
          { path: 'pos', element: <Pos /> },
          { path: 'tables', element: <Tables /> },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to={defaultHome(getRole())} replace /> },
]);

export default router;
