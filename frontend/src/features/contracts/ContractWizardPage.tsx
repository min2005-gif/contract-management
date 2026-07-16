import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getContract } from '../../api/contracts';
import { Attachments } from './Attachments';
import { ContractForm } from './ContractForm';
import { WorkflowActions } from './WorkflowActions';

const STEPS = ['Thông tin hợp đồng', 'Đính kèm & Trình duyệt'];

export function ContractWizardPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(0);
  const [contractId, setContractId] = useState<string | null>(null);

  const contractQuery = useQuery({
    queryKey: ['contract', contractId],
    queryFn: () => getContract(contractId as string),
    enabled: step === 1 && Boolean(contractId),
  });

  return (
    <section>
      <div className="page-head">
        <h2>Tạo hợp đồng</h2>
      </div>

      <div className="stepper" style={{ marginBottom: '1.25rem' }}>
        {STEPS.map((label, i) => (
          <div
            key={label}
            className={`step${i === step ? ' step-active' : ''}${i < step ? ' step-done' : ''}`}
          >
            <span className="step-dot">{i + 1}</span>
            <span className="step-label">{label}</span>
          </div>
        ))}
      </div>

      {step === 0 && (
        <ContractForm
          onSaved={(c) => {
            setContractId(c.id);
            setStep(1);
          }}
          onCancel={() => navigate('/contracts')}
          submitLabel="Lưu & tiếp tục →"
        />
      )}

      {step === 1 && contractId && (
        <>
          <div className="notice">
            ✓ Đã lưu hợp đồng (bản nháp). Bước tiếp theo: đính kèm tệp và trình duyệt.
          </div>
          <Attachments contractId={contractId} />
          {contractQuery.data && <WorkflowActions contract={contractQuery.data} />}
          <div className="form-actions" style={{ borderTop: 'none' }}>
            <button className="secondary" onClick={() => navigate('/contracts')}>
              Để sau
            </button>
            <button onClick={() => navigate(`/contracts/${contractId}`)}>Hoàn tất → Xem chi tiết</button>
          </div>
        </>
      )}
    </section>
  );
}
