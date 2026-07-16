import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage } from '../../api/client';
import {
  createUser,
  listUnits,
  listUsers,
  updateUser,
  type Role,
  type UserUpsert,
  type UserView,
} from '../../api/admin';
import { roleLabels } from '../../i18n';

const ALL_ROLES: Role[] = ['DATA_ENTRY', 'UNIT_HEAD', 'MANAGEMENT', 'ADMIN'];

interface FormState {
  id: string | null;
  externalSubject: string;
  fullName: string;
  email: string;
  unitCode: string;
  active: boolean;
  roles: Role[];
}

function blankForm(): FormState {
  return {
    id: null,
    externalSubject: '',
    fullName: '',
    email: '',
    unitCode: '',
    active: true,
    roles: ['DATA_ENTRY'],
  };
}

export function AdminUsersPage() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState<FormState>(blankForm);
  const [error, setError] = useState<string | null>(null);

  const usersQuery = useQuery({ queryKey: ['admin-users'], queryFn: listUsers });
  const unitsQuery = useQuery({ queryKey: ['units'], queryFn: listUnits });

  const save = useMutation({
    mutationFn: (f: FormState) => {
      const payload: UserUpsert = {
        externalSubject: f.externalSubject.trim(),
        fullName: f.fullName.trim(),
        email: f.email.trim(),
        unitCode: f.unitCode,
        active: f.active,
        roles: f.roles,
      };
      return f.id ? updateUser(f.id, payload) : createUser(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-users'] });
      setForm(blankForm());
      setError(null);
    },
    onError: (err) => setError(errorMessage(err)),
  });

  function edit(u: UserView) {
    setForm({
      id: u.id,
      externalSubject: u.externalSubject,
      fullName: u.fullName ?? '',
      email: u.email ?? '',
      unitCode: u.unitCode,
      active: u.active,
      roles: u.roles,
    });
    setError(null);
  }

  function toggleRole(role: Role) {
    setForm((f) => ({
      ...f,
      roles: f.roles.includes(role) ? f.roles.filter((r) => r !== role) : [...f.roles, role],
    }));
  }

  function submit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.externalSubject.trim() || !form.unitCode || form.roles.length === 0) {
      setError('Cần nhập tên đăng nhập, đơn vị và ít nhất 1 vai trò.');
      return;
    }
    save.mutate(form);
  }

  const units = unitsQuery.data ?? [];

  return (
    <section>
      <div className="page-head">
        <h2>Quản lý người dùng</h2>
      </div>

      <form className="form card" onSubmit={submit}>
        <h3>{form.id ? 'Chỉnh sửa người dùng' : 'Thêm người dùng'}</h3>
        {error && <p className="error">{error}</p>}
        <div className="form-grid">
          <label>
            Tên đăng nhập (định danh SSO) *
            <input
              value={form.externalSubject}
              disabled={Boolean(form.id)}
              onChange={(e) => setForm((f) => ({ ...f, externalSubject: e.target.value }))}
            />
          </label>
          <label>
            Đơn vị *
            <select
              value={form.unitCode}
              onChange={(e) => setForm((f) => ({ ...f, unitCode: e.target.value }))}
            >
              <option value="">— Chọn đơn vị —</option>
              {units.map((u) => (
                <option key={u.id} value={u.code}>
                  {u.code} — {u.name}
                </option>
              ))}
            </select>
          </label>
          <label>
            Họ tên
            <input
              value={form.fullName}
              onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))}
            />
          </label>
          <label>
            Email
            <input
              value={form.email}
              onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
            />
          </label>
          <div className="col-2">
            <span style={{ fontSize: '0.86rem', fontWeight: 550, color: 'var(--text-2)' }}>
              Vai trò *
            </span>
            <div className="role-checks">
              {ALL_ROLES.map((r) => (
                <label key={r} className="role-check">
                  <input
                    type="checkbox"
                    checked={form.roles.includes(r)}
                    onChange={() => toggleRole(r)}
                  />
                  {roleLabels[r]}
                </label>
              ))}
            </div>
          </div>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={form.active}
              onChange={(e) => setForm((f) => ({ ...f, active: e.target.checked }))}
            />
            Đang hoạt động
          </label>
        </div>
        <div className="form-actions">
          {form.id && (
            <button type="button" className="secondary" onClick={() => setForm(blankForm())}>
              Hủy
            </button>
          )}
          <button type="submit" disabled={save.isPending}>
            {save.isPending ? 'Đang lưu…' : form.id ? 'Cập nhật' : 'Thêm mới'}
          </button>
        </div>
      </form>

      {usersQuery.isLoading && <p>Đang tải…</p>}
      {usersQuery.data && (
        <table className="grid">
          <thead>
            <tr>
              <th>Tên đăng nhập</th>
              <th>Họ tên</th>
              <th>Đơn vị</th>
              <th>Vai trò</th>
              <th>Trạng thái</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {usersQuery.data.length === 0 && (
              <tr>
                <td colSpan={6} className="empty">
                  Chưa có người dùng nào.
                </td>
              </tr>
            )}
            {usersQuery.data.map((u) => (
              <tr key={u.id}>
                <td>{u.externalSubject}</td>
                <td>{u.fullName || '—'}</td>
                <td>{u.unitCode}</td>
                <td>{u.roles.map((r) => roleLabels[r]).join(', ')}</td>
                <td>
                  <span className={`badge ${u.active ? 'status-ACTIVE' : 'status-LIQUIDATED'}`}>
                    {u.active ? 'Hoạt động' : 'Khóa'}
                  </span>
                </td>
                <td>
                  <button className="link-btn" onClick={() => edit(u)}>
                    Sửa
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
