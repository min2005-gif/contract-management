import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage } from '../../api/client';
import {
  linkEDocument,
  reconcileContract,
  signContract,
  type ReconciliationResult,
} from '../../api/integrations';
import type { Contract } from '../../api/types';
import { formatCurrency } from '../../i18n';

export function IntegrationActions({ contract }: { contract: Contract }) {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [reconcile, setReconcile] = useState<ReconciliationResult | null>(null);
  const [docRef, setDocRef] = useState('');

  const invalidate = () =>
    queryClient.invalidateQueries({ queryKey: ['contract', contract.id] });

  const sign = useMutation({
    mutationFn: () => signContract(contract.id),
    onSuccess: invalidate,
    onError: (err) => setError(errorMessage(err)),
  });

  const recon = useMutation({
    mutationFn: () => reconcileContract(contract.id),
    onSuccess: (r) => setReconcile(r),
    onError: (err) => setError(errorMessage(err)),
  });

  const link = useMutation({
    mutationFn: () => linkEDocument(contract.id, docRef.trim()),
    onSuccess: () => {
      setDocRef('');
      invalidate();
    },
    onError: (err) => setError(errorMessage(err)),
  });

  const linkedDocs = (contract.extraFields?.eDocuments as string[] | undefined) ?? [];

  return (
    <div className="card">
      <h3>Tích hợp hệ thống</h3>
      {error && <p className="error">{error}</p>}

      <div className="toolbar">
        <button disabled={sign.isPending || contract.signed} onClick={() => sign.mutate()}>
          {contract.signed ? 'Đã ký số ✓' : sign.isPending ? 'Đang ký…' : 'Ký số'}
        </button>
        <button className="secondary" disabled={recon.isPending} onClick={() => recon.mutate()}>
          {recon.isPending ? 'Đang đối chiếu…' : 'Đối chiếu kế toán'}
        </button>
      </div>

      {reconcile && (
        <p className="muted">
          {reconcile.message} — Mã: {reconcile.accountingReference}, Giá trị:{' '}
          {formatCurrency(reconcile.contractValue)}, Khớp: {reconcile.matched ? 'Có' : 'Không'}
        </p>
      )}

      <div className="toolbar" style={{ marginTop: '0.75rem' }}>
        <input
          placeholder="Mã văn bản điện tử (VPĐT)…"
          value={docRef}
          onChange={(e) => setDocRef(e.target.value)}
        />
        <button disabled={!docRef.trim() || link.isPending} onClick={() => link.mutate()}>
          Liên kết văn bản
        </button>
      </div>
      {linkedDocs.length > 0 && (
        <p className="muted">Văn bản đã liên kết: {linkedDocs.join(', ')}</p>
      )}
    </div>
  );
}
