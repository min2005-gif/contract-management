import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage } from '../../api/client';
import { performWorkflow, type WorkflowAction } from '../../api/workflow';
import type { Contract, ContractStatus } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';
import { contractStatusLabels } from '../../i18n';

interface Props {
  contract: Contract;
}

interface ActionDef {
  action: WorkflowAction;
  label: string;
  needsReason?: boolean;
}

// The five lifecycle stages, and which contract statuses fall under each.
const STEPS: { label: string; statuses: ContractStatus[] }[] = [
  { label: 'Tạo', statuses: ['DRAFT'] },
  { label: 'Kiểm tra', statuses: ['PENDING_CHECK'] },
  { label: 'Duyệt', statuses: ['PENDING_TCT_APPROVAL'] },
  { label: 'Thực hiện', statuses: ['ACTIVE', 'IN_PROGRESS', 'COMPLETED'] },
  { label: 'Thanh lý', statuses: ['LIQUIDATED'] },
];

// What happens next at each status, and who is responsible.
const GUIDE: Record<ContractStatus, string> = {
  DRAFT: 'Bản nháp. Bước tiếp theo: Nhân viên nhập liệu bấm “Trình duyệt” để gửi Trưởng đơn vị.',
  PENDING_CHECK: 'Đang chờ Trưởng đơn vị kiểm tra và duyệt.',
  PENDING_TCT_APPROVAL: 'Đang chờ TCT (cấp trên) duyệt chính thức.',
  ACTIVE: 'Đã có hiệu lực. Trưởng đơn vị/Quản trị có thể thanh lý khi kết thúc.',
  IN_PROGRESS: 'Đang thực hiện.',
  COMPLETED: 'Đã hoàn thành. Có thể thanh lý.',
  LIQUIDATED: 'Đã thanh lý (kết thúc).',
};

export function WorkflowActions({ contract }: Props) {
  const { profile } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);

  const roles = profile?.roles ?? [];
  const isTct = profile?.unit === 'TCT';
  const has = (...r: string[]) => r.some((x) => roles.includes(x));

  const mutation = useMutation({
    mutationFn: ({ action, reason }: { action: WorkflowAction; reason?: string }) =>
      performWorkflow(contract.id, action, reason),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['contract', contract.id] });
      queryClient.invalidateQueries({ queryKey: ['workflow', contract.id] });
      queryClient.invalidateQueries({ queryKey: ['contracts'] });
    },
    onError: (err) => setError(errorMessage(err)),
  });

  const actions: ActionDef[] = [];
  switch (contract.status) {
    case 'DRAFT':
      if (has('DATA_ENTRY', 'UNIT_HEAD', 'ADMIN')) {
        actions.push({ action: 'SUBMIT', label: 'Trình duyệt' });
      }
      break;
    case 'PENDING_CHECK':
      if (has('UNIT_HEAD', 'ADMIN')) {
        actions.push({ action: 'CHECK_APPROVE', label: 'Trưởng đơn vị duyệt' });
        actions.push({ action: 'REJECT', label: 'Từ chối', needsReason: true });
      }
      break;
    case 'PENDING_TCT_APPROVAL':
      if (isTct && has('UNIT_HEAD', 'ADMIN')) {
        actions.push({ action: 'TCT_APPROVE', label: 'TCT duyệt' });
        actions.push({ action: 'REJECT', label: 'Từ chối', needsReason: true });
      }
      break;
    case 'ACTIVE':
    case 'IN_PROGRESS':
    case 'COMPLETED':
      if (has('UNIT_HEAD', 'ADMIN')) {
        actions.push({ action: 'LIQUIDATE', label: 'Thanh lý' });
      }
      break;
    default:
      break;
  }

  function run(def: ActionDef) {
    setError(null);
    let reason: string | undefined;
    if (def.needsReason) {
      reason = window.prompt('Lý do từ chối:') ?? undefined;
      if (!reason) return;
    }
    mutation.mutate({ action: def.action, reason });
  }

  const activeStepIndex = STEPS.findIndex((s) => s.statuses.includes(contract.status));

  return (
    <div className="card">
      <h3>Quy trình duyệt</h3>

      <div className="stepper">
        {STEPS.map((s, i) => (
          <div
            key={s.label}
            className={`step${i === activeStepIndex ? ' step-active' : ''}${
              i < activeStepIndex ? ' step-done' : ''
            }`}
          >
            <span className="step-dot">{i + 1}</span>
            <span className="step-label">{s.label}</span>
          </div>
        ))}
      </div>

      <p className="workflow-status">
        Trạng thái hiện tại:{' '}
        <span className={`badge status-${contract.status}`}>
          {contractStatusLabels[contract.status]}
        </span>
      </p>
      <p className="muted">{GUIDE[contract.status]}</p>

      {error && <p className="error">{error}</p>}

      {actions.length > 0 ? (
        <div className="toolbar" style={{ marginTop: '0.5rem', marginBottom: 0 }}>
          {actions.map((a) => (
            <button
              key={a.action}
              className={a.action === 'REJECT' ? 'secondary' : ''}
              disabled={mutation.isPending}
              onClick={() => run(a)}
            >
              {a.label}
            </button>
          ))}
        </div>
      ) : (
        <p className="field-hint">
          Bước hiện tại không thuộc vai trò của bạn — đăng nhập đúng vai trò ở bảng trên để thao
          tác.
        </p>
      )}
    </div>
  );
}
