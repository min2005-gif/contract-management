import { Navigate, NavLink, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { BellIcon, ChartIcon, FileIcon, LogoutIcon } from './icons';

const roleLabels: Record<string, string> = {
  DATA_ENTRY: 'Nhập liệu',
  UNIT_HEAD: 'Trưởng đơn vị',
  MANAGEMENT: 'Lãnh đạo',
  ADMIN: 'Quản trị',
};

export function Layout() {
  const { profile, logout } = useAuth();
  const location = useLocation();

  if (!profile) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  const canSeeReports =
    profile.unit === 'TCT' ||
    profile.roles.includes('MANAGEMENT') ||
    profile.roles.includes('ADMIN');
  const initial = (profile.name || '?').charAt(0).toUpperCase();
  const rolesText = profile.roles.map((r) => roleLabels[r] ?? r).join(', ');

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="logo">
          <span className="mark">V</span>
          <span>Quản lý hợp đồng</span>
        </div>
        <nav>
          <NavLink to="/contracts" className="navlink">
            <FileIcon /> Hợp đồng
          </NavLink>
          <NavLink to="/alerts" className="navlink">
            <BellIcon /> Cảnh báo
          </NavLink>
          {canSeeReports && (
            <NavLink to="/dashboard" className="navlink">
              <ChartIcon /> Báo cáo
            </NavLink>
          )}
        </nav>
        <div className="sidebar-foot">Tổng công ty Quản lý bay Việt Nam · VATM</div>
      </aside>

      <div className="content">
        <header className="topbar">
          <span className="title">Hệ thống quản lý hợp đồng</span>
          <div className="user-chip">
            <div className="avatar">{initial}</div>
            <div className="meta">
              <span className="name">{profile.name}</span>
              <span className="sub">
                {profile.unit} · {rolesText}
              </span>
            </div>
            <button className="secondary" onClick={logout} title="Đăng xuất">
              <LogoutIcon size={16} />
            </button>
          </div>
        </header>
        <main className="page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
