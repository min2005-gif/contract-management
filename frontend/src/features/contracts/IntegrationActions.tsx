import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage } from '../../api/client';
import { linkEDocument, signContract } from '../../api/integrations';
import type { Contract } from '../../api/types';
import { useAuth } from '../../auth/AuthContext';

export function IntegrationActions({ contract }: { contract: Contract }) {
  const { profile } = useAuth();
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [docRef, setDocRef] = useState('');
  const [signPhase, setSignPhase] = useState<'idle' | 'pending'>('idle');

  const canSign =
    (profile?.roles.includes('UNIT_HEAD') || profile?.roles.includes('ADMIN')) ?? false;

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['contract', contract.id] });

  const sign = useMutation({
    mutationFn: () => signContract(contract.id),
    onSuccess: () => {
      setSignPhase('idle');
      invalidate();
    },
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
      <h3>Ký số & Văn bản điện tử</h3>
      {error && <p className="error">{error}</p>}

      {contract.signed ? (
        <p>
          <span className="badge status-COMPLETED">Đã ký số ✓</span>
        </p>
      ) : !canSign ? (
        <p className="field-hint">
          Chỉ Trưởng đơn vị / Quản trị (người có thẩm quyền) mới được ký số.
        </p>
      ) : signPhase === 'idle' ? (
        <div className="toolbar">
          <button onClick={() => setSignPhase('pending')}>Ký số</button>
        </div>
      ) : (
        <div className="notice">
          📱 <strong>Yêu cầu ký số đã được gửi.</strong> Người ký mở ứng dụng <strong>SmartCA</strong>{' '}
          trên điện thoại và bấm xác nhận để ký.
          <div className="toolbar" style={{ marginTop: '0.6rem', marginBottom: 0 }}>
            <button disabled={sign.isPending} onClick={() => sign.mutate()}>
              {sign.isPending ? 'Đang ký…' : 'Xác nhận ký (mô phỏng)'}
            </button>
            <button className="secondary" onClick={() => setSignPhase('idle')}>
              Hủy
            </button>
          </div>
          <span className="field-hint">
            Demo: bấm “Xác nhận ký” thay cho thao tác xác nhận trên điện thoại.
          </span>
        </div>
      )}

      <div className="toolbar" style={{ marginTop: '1rem' }}>
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
