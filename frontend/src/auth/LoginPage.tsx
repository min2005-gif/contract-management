import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../api/client';
import { useAuth } from './AuthContext';

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
      <div className="card login-card">
        <div className="brand-row">
          <span className="mark">V</span>
          <div>
            <h1>Quản lý hợp đồng VATM</h1>
            <span className="muted">Tổng công ty Quản lý bay Việt Nam</span>
          </div>
        </div>
        <form onSubmit={submit}>
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
            Môi trường phát triển · đăng nhập thử nghiệm
          </span>
        </form>
      </div>
    </div>
  );
}
