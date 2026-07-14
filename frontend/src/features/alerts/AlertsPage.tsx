import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { errorMessage } from '../../api/client';
import {
  listAlerts,
  triggerEvaluation,
  updateAlertStatus,
  type Alert,
  type AlertStatus,
} from '../../api/alerts';
import { useAuth } from '../../auth/AuthContext';
import { alertStatusLabels, alertTypeLabels } from '../../i18n';

export function AlertsPage() {
  const { profile } = useAuth();
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const isAdmin = profile?.roles.includes('ADMIN') ?? false;

  const { data, isLoading } = useQuery({ queryKey: ['alerts'], queryFn: () => listAlerts() });

  const update = useMutation({
    mutationFn: ({ id, status }: { id: string; status: AlertStatus }) =>
      updateAlertStatus(id, status),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alerts'] }),
    onError: (err) => setError(errorMessage(err)),
  });

  const evaluate = useMutation({
    mutationFn: triggerEvaluation,
    onSuccess: (raised) => {
      setNotice(`Đã đánh giá: ${raised} cảnh báo mới.`);
      queryClient.invalidateQueries({ queryKey: ['alerts'] });
    },
    onError: (err) => setError(errorMessage(err)),
  });

  function badgeClass(a: Alert) {
    return a.status === 'OPEN' ? 'status-PENDING_CHECK' : 'status-ACTIVE';
  }

  return (
    <section>
      <div className="page-head">
        <h2>Cảnh báo hợp đồng</h2>
        {isAdmin && (
          <button disabled={evaluate.isPending} onClick={() => evaluate.mutate()}>
            {evaluate.isPending ? 'Đang đánh giá…' : 'Đánh giá ngay'}
          </button>
        )}
      </div>

      {error && <p className="error">{error}</p>}
      {notice && <p className="muted">{notice}</p>}
      {isLoading && <p>Đang tải…</p>}

      {data && (
        <table className="grid">
          <thead>
            <tr>
              <th>Loại</th>
              <th>Hợp đồng</th>
              <th>Trạng thái</th>
              <th>Thời điểm</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {data.length === 0 && (
              <tr>
                <td colSpan={5} className="muted">
                  Không có cảnh báo.
                </td>
              </tr>
            )}
            {data.map((a) => (
              <tr key={a.id}>
                <td>{alertTypeLabels[a.type] ?? a.type}</td>
                <td>
                  <button className="link-btn" onClick={() => navigate(`/contracts/${a.contractId}`)}>
                    Xem hợp đồng
                  </button>
                </td>
                <td>
                  <span className={`badge ${badgeClass(a)}`}>{alertStatusLabels[a.status]}</span>
                </td>
                <td>{new Date(a.raisedAt).toLocaleString('vi-VN')}</td>
                <td>
                  {a.status === 'OPEN' && (
                    <button
                      className="link-btn"
                      onClick={() => update.mutate({ id: a.id, status: 'ACKNOWLEDGED' })}
                    >
                      Tiếp nhận
                    </button>
                  )}
                  {a.status !== 'RESOLVED' && (
                    <button
                      className="link-btn"
                      style={{ marginLeft: '0.75rem' }}
                      onClick={() => update.mutate({ id: a.id, status: 'RESOLVED' })}
                    >
                      Xử lý
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
