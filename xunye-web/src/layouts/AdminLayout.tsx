import { Outlet } from 'react-router-dom';
import { Sidebar } from '../components/Sidebar';
import { Topbar } from '../components/Topbar';

export default function AdminLayout() {
  return (
    <div className="flex min-h-screen bg-page-bg text-text-main relative selection:bg-brand-gold/20 selection:text-brand-gold font-sans">
      <Sidebar />
      <div className="flex-1 ml-[260px] flex flex-col">
        <Topbar />
        <main className="flex-1 p-8 overflow-x-hidden min-h-[calc(100vh-64px)] overflow-y-auto">
          <div className="max-w-7xl mx-auto w-full">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
}
