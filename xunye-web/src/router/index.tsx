import { lazy, Suspense, type ReactNode } from 'react';
import { createBrowserRouter, Navigate, Outlet } from 'react-router-dom';
import AdminLayout from '../layouts/AdminLayout';
import { Loading } from '../components/Loading';

const Login = lazy(() => import('../pages/Login'));
const Dashboard = lazy(() => import('../pages/Dashboard'));
const Products = lazy(() => import('../pages/Products'));
const Orders = lazy(() => import('../pages/Orders'));
const Kitchen = lazy(() => import('../pages/Kitchen'));
const Inventory = lazy(() => import('../pages/Inventory'));
const Categories = lazy(() => import('../pages/Categories'));
const Tables = lazy(() => import('../pages/Tables'));
const Pos = lazy(() => import('../pages/Pos'));
const Settings = lazy(() => import('../pages/Settings'));
const Staff = lazy(() => import('../pages/Staff'));
const Members = lazy(() => import('../pages/Members'));
const Activities = lazy(() => import('../pages/Activities'));

function lazyPage(element: ReactNode) {
  return <Suspense fallback={<Loading />}>{element}</Suspense>;
}

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
      { path: '/login', element: lazyPage(<Login />) },
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
            { path: 'dashboard', element: lazyPage(<Dashboard />) },
            { path: 'products', element: lazyPage(<Products />) },
            { path: 'inventory', element: lazyPage(<Inventory />) },
            { path: 'categories', element: lazyPage(<Categories />) },
            { path: 'members', element: lazyPage(<Members />) },
            { path: 'activities', element: lazyPage(<Activities />) },
          ]},
          { element: <RoleRoute roles={['BOSS']} />, children: [
            { path: 'employees', element: lazyPage(<Staff />) },
            { path: 'settings', element: lazyPage(<Settings />) },
          ]},
          { path: 'kitchen', element: lazyPage(<Kitchen />) },
          { path: 'orders', element: lazyPage(<Orders />) },
          { path: 'pos', element: lazyPage(<Pos />) },
          { path: 'tables', element: lazyPage(<Tables />) },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to={defaultHome(getRole())} replace /> },
]);

export default router;
