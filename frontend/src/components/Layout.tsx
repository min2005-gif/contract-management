import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Guards child routes and renders the app shell (header + nav) for authenticated users. */
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

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <Link to="/contracts">Quản lý hợp đồng VATM</Link>
          <nav className="nav">
            <Link to="/contracts">Hợp đồng</Link>
            <Link to="/alerts">Cảnh báo</Link>
            {canSeeReports && <Link to="/dashboard">Báo cáo</Link>}
          </nav>
        </div>
        <div className="user">
          <span>
            {profile.name} · {profile.unit} · {profile.roles.join(', ')}
          </span>
          <button className="link-btn" onClick={logout}>
            Đăng xuất
          </button>
        </div>
      </header>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  );
}
