import { useQuery } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { errorMessage } from '../../api/client';
import { getContract } from '../../api/contracts';
import { listPeople } from '../../api/people';
import {
  contractStatusLabels,
  contractTypeLabels,
  formatCurrency,
  paymentStatusLabels,
} from '../../i18n';
import { Attachments } from './Attachments';
import { IntegrationActions } from './IntegrationActions';
import { StatusTimeline } from './StatusTimeline';
import { WorkflowActions } from './WorkflowActions';

export function ContractDetailPage() {
  const { id } = useParams();

  const { data: contract, isLoading, isError, error } = useQuery({
    queryKey: ['contract', id],
    queryFn: () => getContract(id as string),
  });
  const peopleQuery = useQuery({ queryKey: ['people'], queryFn: listPeople });

  if (isLoading) return <p>Đang tải…</p>;
  if (isError) return <p className="error">{errorMessage(error)}</p>;
  if (!contract) return null;

  const person = peopleQuery.data?.find((p) => p.id === contract.personInChargeId);

  return (
    <section>
      <div className="page-head">
        <div>
          <h2>{contract.name}</h2>
          <p className="muted" style={{ margin: '0.25rem 0 0' }}>
            Số HĐ: {contract.contractNumber}{' '}
            <span className={`badge status-${contract.status}`} style={{ marginLeft: '0.5rem' }}>
              {contractStatusLabels[contract.status]}
            </span>
          </p>
        </div>
        <div className="toolbar" style={{ margin: 0 }}>
          <Link className="btn secondary" to="/contracts">
            ← Danh sách
          </Link>
          <Link className="btn" to={`/contracts/${contract.id}/edit`}>
            Chỉnh sửa
          </Link>
        </div>
      </div>

      <div className="card">
        <h3>Thông tin hợp đồng</h3>
        <dl className="info-grid">
          <div>
            <dt>Loại hợp đồng</dt>
            <dd>{contractTypeLabels[contract.type]}</dd>
          </div>
          <div>
            <dt>Giá trị</dt>
            <dd>{formatCurrency(contract.value)}</dd>
          </div>
          <div>
            <dt>Bên A</dt>
            <dd>{contract.partyA}</dd>
          </div>
          <div>
            <dt>Bên B</dt>
            <dd>{contract.partyB}</dd>
          </div>
          <div>
            <dt>Ngày ký</dt>
            <dd>{contract.signDate}</dd>
          </div>
          <div>
            <dt>Thời hạn</dt>
            <dd>{contract.termEnd}</dd>
          </div>
          <div>
            <dt>Người phụ trách</dt>
            <dd>{person ? `${person.fullName} (${person.unitCode})` : '—'}</dd>
          </div>
          <div>
            <dt>Thanh toán</dt>
            <dd>{paymentStatusLabels[contract.paymentStatus]}</dd>
          </div>
          <div>
            <dt>Hợp đồng chính thức</dt>
            <dd>{contract.official ? 'Có (cần TCT duyệt)' : 'Không'}</dd>
          </div>
          <div>
            <dt>Đã ký số</dt>
            <dd>{contract.signed ? 'Rồi' : 'Chưa'}</dd>
          </div>
        </dl>
      </div>

      <WorkflowActions contract={contract} />
      <IntegrationActions contract={contract} />
      <StatusTimeline contractId={contract.id} />
      <Attachments contractId={contract.id} />
    </section>
  );
}
