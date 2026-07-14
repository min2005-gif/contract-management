import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage } from '../../api/client';
import { performWorkflow, type WorkflowAction } from '../../api/workflow';
import type { Contract } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';

interface Props {
  contract: Contract;
}

interface ActionDef {
  action: WorkflowAction;
  label: string;
  needsReason?: boolean;
}

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

  if (actions.length === 0) {
    return (
      <div className="card">
        <h3>Quy trình duyệt</h3>
        <p className="muted">Không có thao tác nào khả dụng cho vai trò/ trạng thái hiện tại.</p>
      </div>
    );
  }

  return (
    <div className="card">
      <h3>Quy trình duyệt</h3>
      {error && <p className="error">{error}</p>}
      <div className="toolbar">
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
    </div>
  );
}
