import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { errorMessage } from '../../api/client';
import { createContract, getContract, updateContract } from '../../api/contracts';
import type { ContractInput, ContractType } from '../../api/types';
import { contractStatusLabels, contractTypeLabels } from '../../i18n';
import { Attachments } from './Attachments';
import { WorkflowActions } from './WorkflowActions';
import { StatusTimeline } from './StatusTimeline';
import { IntegrationActions } from './IntegrationActions';

const TYPES: ContractType[] = ['PURCHASE_SALE', 'SERVICE', 'CONSTRUCTION', 'LEASE', 'LABOR'];

function emptyForm(): ContractInput {
  return {
    contractNumber: '',
    name: '',
    type: 'SERVICE',
    partyA: '',
    partyB: '',
    value: 0,
    signDate: '',
    termEnd: '',
    personInChargeId: crypto.randomUUID(),
    official: false,
  };
}

export function ContractFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [form, setForm] = useState<ContractInput>(emptyForm);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverError, setServerError] = useState<string | null>(null);

  const existing = useQuery({
    queryKey: ['contract', id],
    queryFn: () => getContract(id as string),
    enabled: isEdit,
  });

  useEffect(() => {
    if (existing.data) {
      const c = existing.data;
      setForm({
        contractNumber: c.contractNumber,
        name: c.name,
        type: c.type,
        partyA: c.partyA,
        partyB: c.partyB,
        value: c.value,
        signDate: c.signDate,
        termEnd: c.termEnd,
        personInChargeId: c.personInChargeId,
        official: c.official,
      });
    }
  }, [existing.data]);

  const mutation = useMutation({
    mutationFn: (input: ContractInput) =>
      isEdit ? updateContract(id as string, input) : createContract(input),
    onSuccess: (saved) => {
      queryClient.invalidateQueries({ queryKey: ['contracts'] });
      navigate(`/contracts/${saved.id}`);
    },
    onError: (err) => setServerError(errorMessage(err)),
  });

  function validate(): boolean {
    const e: Record<string, string> = {};
    if (!form.contractNumber.trim()) e.contractNumber = 'Bắt buộc';
    if (!form.name.trim()) e.name = 'Bắt buộc';
    if (!form.partyA.trim()) e.partyA = 'Bắt buộc';
    if (!form.partyB.trim()) e.partyB = 'Bắt buộc';
    if (!(form.value >= 0)) e.value = 'Giá trị phải ≥ 0';
    if (!form.signDate) e.signDate = 'Bắt buộc';
    if (!form.termEnd) e.termEnd = 'Bắt buộc';
    if (form.signDate && form.termEnd && form.termEnd < form.signDate) {
      e.termEnd = 'Thời hạn phải sau ngày ký';
    }
    setErrors(e);
    return Object.keys(e).length === 0;
  }

  function submit(ev: React.FormEvent) {
    ev.preventDefault();
    setServerError(null);
    if (validate()) mutation.mutate(form);
  }

  function field<K extends keyof ContractInput>(key: K, value: ContractInput[K]) {
    setForm((f) => ({ ...f, [key]: value }));
  }

  if (isEdit && existing.isLoading) return <p>Đang tải…</p>;

  return (
    <section>
      <div className="page-head">
        <h2>{isEdit ? 'Chỉnh sửa hợp đồng' : 'Tạo hợp đồng'}</h2>
        {existing.data && (
          <span className={`badge status-${existing.data.status}`}>
            {contractStatusLabels[existing.data.status]}
          </span>
        )}
      </div>

      {serverError && <p className="error">{serverError}</p>}

      <form className="form card" onSubmit={submit} noValidate>
        <div className="form-grid">
          <label>
            Số hợp đồng *
            <input
              value={form.contractNumber}
              onChange={(e) => field('contractNumber', e.target.value)}
            />
            {errors.contractNumber && <small className="error">{errors.contractNumber}</small>}
          </label>
          <label>
            Loại hợp đồng *
            <select value={form.type} onChange={(e) => field('type', e.target.value as ContractType)}>
              {TYPES.map((t) => (
                <option key={t} value={t}>
                  {contractTypeLabels[t]}
                </option>
              ))}
            </select>
          </label>
          <label className="col-2">
            Tên hợp đồng *
            <input value={form.name} onChange={(e) => field('name', e.target.value)} />
            {errors.name && <small className="error">{errors.name}</small>}
          </label>
          <label>
            Bên A *
            <input value={form.partyA} onChange={(e) => field('partyA', e.target.value)} />
            {errors.partyA && <small className="error">{errors.partyA}</small>}
          </label>
          <label>
            Bên B *
            <input value={form.partyB} onChange={(e) => field('partyB', e.target.value)} />
            {errors.partyB && <small className="error">{errors.partyB}</small>}
          </label>
          <label>
            Giá trị (VND) *
            <input
              type="number"
              min={0}
              value={form.value}
              onChange={(e) => field('value', Number(e.target.value))}
            />
            {errors.value && <small className="error">{errors.value}</small>}
          </label>
          <label>
            Người phụ trách
            <input
              value={form.personInChargeId}
              onChange={(e) => field('personInChargeId', e.target.value)}
            />
            <span className="field-hint">Mã định danh người phụ trách (tự sinh)</span>
          </label>
          <label>
            Ngày ký *
            <input
              type="date"
              value={form.signDate}
              onChange={(e) => field('signDate', e.target.value)}
            />
            {errors.signDate && <small className="error">{errors.signDate}</small>}
          </label>
          <label>
            Thời hạn (kết thúc) *
            <input
              type="date"
              value={form.termEnd}
              onChange={(e) => field('termEnd', e.target.value)}
            />
            {errors.termEnd && <small className="error">{errors.termEnd}</small>}
          </label>
          <label className="checkbox">
            <input
              type="checkbox"
              checked={form.official ?? false}
              onChange={(e) => field('official', e.target.checked)}
            />
            Hợp đồng chính thức (cần TCT duyệt)
          </label>
        </div>

        <div className="form-actions">
          <button type="button" className="secondary" onClick={() => navigate('/contracts')}>
            Hủy
          </button>
          <button type="submit" disabled={mutation.isPending}>
            {mutation.isPending ? 'Đang lưu…' : 'Lưu'}
          </button>
        </div>
      </form>

      {isEdit && existing.data && <WorkflowActions contract={existing.data} />}
      {isEdit && existing.data && <IntegrationActions contract={existing.data} />}
      {isEdit && id && <StatusTimeline contractId={id} />}
      {isEdit && id && <Attachments contractId={id} />}
    </section>
  );
}
