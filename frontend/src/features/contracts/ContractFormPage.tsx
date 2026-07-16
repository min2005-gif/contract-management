import { useNavigate, useParams } from 'react-router-dom';
import { ContractForm } from './ContractForm';

/** Edit an existing contract's fields. On save, returns to its detail page. */
export function ContractFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  return (
    <section>
      <div className="page-head">
        <h2>Chỉnh sửa hợp đồng</h2>
      </div>
      <ContractForm
        contractId={id}
        onSaved={(c) => navigate(`/contracts/${c.id}`)}
        onCancel={() => navigate(id ? `/contracts/${id}` : '/contracts')}
        submitLabel="Lưu"
      />
    </section>
  );
}
