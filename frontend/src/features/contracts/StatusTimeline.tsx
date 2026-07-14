import { useQuery } from '@tanstack/react-query';
import { getWorkflowHistory } from '../../api/workflow';
import { contractStatusLabels } from '../../i18n';
import type { ContractStatus } from '../../api/types';

const actionLabels: Record<string, string> = {
  SUBMIT: 'Trình duyệt',
  CHECK_APPROVE: 'Trưởng đơn vị duyệt',
  TCT_APPROVE: 'TCT duyệt',
  REJECT: 'Từ chối',
  LIQUIDATE: 'Thanh lý',
};

export function StatusTimeline({ contractId }: { contractId: string }) {
  const { data } = useQuery({
    queryKey: ['workflow', contractId],
    queryFn: () => getWorkflowHistory(contractId),
  });

  return (
    <div className="card">
      <h3>Lịch sử xử lý</h3>
      {data && data.length === 0 && <p className="muted">Chưa có thao tác nào.</p>}
      <ul className="timeline">
        {data?.map((s) => (
          <li key={s.id}>
            <strong>{actionLabels[s.action] ?? s.action}</strong>{' '}
            <span className="muted">
              {contractStatusLabels[s.fromStatus as ContractStatus]} →{' '}
              {contractStatusLabels[s.toStatus as ContractStatus]} ·{' '}
              {new Date(s.createdAt).toLocaleString('vi-VN')}
            </span>
            {s.reason && <div className="muted">Lý do: {s.reason}</div>}
          </li>
        ))}
      </ul>
    </div>
  );
}
