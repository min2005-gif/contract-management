import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { useAuth } from './AuthContext';
import logo from '../assets/logo.png';

interface Preset {
  label: string;
  subject: string;
  unit: string;
  roles: string[];
}

// Dev sign-in presets standing in for VATM SSO/AD identities.
const PRESETS: Preset[] = [
  { label: 'Nhân viên nhập liệu (Đơn vị U01)', subject: 'nhanvien.u01', unit: 'U01', roles: ['DATA_ENTRY'] },
  { label: 'Trưởng đơn vị (U01)', subject: 'truong.u01', unit: 'U01', roles: ['UNIT_HEAD'] },
  { label: 'Nhân viên nhập liệu (Đơn vị U02)', subject: 'nhanvien.u02', unit: 'U02', roles: ['DATA_ENTRY'] },
  { label: 'Lãnh đạo TCT', subject: 'lanhdao.tct', unit: 'TCT', roles: ['MANAGEMENT'] },
  { label: 'Quản trị hệ thống (TCT)', subject: 'admin.tct', unit: 'TCT', roles: ['ADMIN'] },
];

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [index, setIndex] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    const preset = PRESETS[index];
    try {
      await login(preset.subject, preset.unit, preset.roles);
      navigate('/contracts');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login">
      <aside className="login-brand">
        <div className="login-brand-inner">
          <div className="brand-logo">
            <img src={logo} alt="Logo VATM" />
          </div>
          <h1 className="brand-title">VATM</h1>
          <p className="brand-org">Tổng công ty Quản lý bay Việt Nam</p>
          <div className="brand-divider" />
          <h2 className="brand-system">Hệ thống Quản lý Hợp đồng tập trung</h2>
          <ul className="brand-points">
            <li>Quản lý hợp đồng thống nhất toàn Tổng công ty</li>
            <li>Quy trình duyệt và cảnh báo tự động</li>
            <li>Báo cáo tổng hợp, minh bạch, kịp thời</li>
          </ul>
        </div>
      </aside>

      <main className="login-form-panel">
        <form className="login-form" onSubmit={submit}>
          <div className="login-form-logo">
            <img src={logo} alt="Logo VATM" />
          </div>
          <div>
            <h2>Đăng nhập hệ thống</h2>
            <p className="muted">Chọn tài khoản để tiếp tục</p>
          </div>
          <label>
            Vai trò đăng nhập
            <select value={index} onChange={(e) => setIndex(Number(e.target.value))}>
              {PRESETS.map((p, i) => (
                <option key={p.subject} value={i}>
                  {p.label}
                </option>
              ))}
            </select>
          </label>
          {error && <p className="error">{error}</p>}
          <button type="submit" disabled={busy}>
            {busy ? 'Đang đăng nhập…' : 'Đăng nhập'}
          </button>
          <span className="field-hint" style={{ textAlign: 'center' }}>
            Môi trường thử nghiệm · Sẽ thay bằng đăng nhập SSO của VATM khi triển khai
          </span>
        </form>
      </main>
    </div>
  );
}
