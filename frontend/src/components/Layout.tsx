import { Link, Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

/** Guards child routes and renders the app shell (header + nav) for authenticated users. */
export function Layout() {
  const { profile, logout } = useAuth();
  const location = useLocation();

  if (!profile) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <Link to="/contracts">Quản lý hợp đồng VATM</Link>
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
