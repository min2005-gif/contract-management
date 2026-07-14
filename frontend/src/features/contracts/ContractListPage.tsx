import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { listContracts } from '../../api/contracts';
import { errorMessage } from '../../api/client';
import { contractStatusLabels, contractTypeLabels, formatCurrency } from '../../i18n';
import { PlusIcon } from '../../components/icons';

export function ContractListPage() {
  const [search, setSearch] = useState('');
  const [q, setQ] = useState('');
  const navigate = useNavigate();

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['contracts', q],
    queryFn: () => listContracts({ q: q || undefined, size: 50 }),
  });

  return (
    <section>
      <div className="page-head">
        <h2>Danh sách hợp đồng</h2>
        <Link className="btn" to="/contracts/new">
          <PlusIcon size={16} /> Tạo hợp đồng
        </Link>
      </div>

      <form
        className="toolbar"
        onSubmit={(e) => {
          e.preventDefault();
          setQ(search.trim());
        }}
      >
        <input
          placeholder="Tìm theo số hợp đồng, tên, bên A/B…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <button type="submit">Tìm kiếm</button>
      </form>

      {isLoading && <p>Đang tải…</p>}
      {isError && <p className="error">{errorMessage(error)}</p>}

      {data && (
        <table className="grid">
          <thead>
            <tr>
              <th>Số HĐ</th>
              <th>Tên hợp đồng</th>
              <th>Loại</th>
              <th>Giá trị</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {data.content.length === 0 && (
              <tr>
                <td colSpan={5} className="empty">
                  Chưa có hợp đồng nào. Nhấn “Tạo hợp đồng” để bắt đầu.
                </td>
              </tr>
            )}
            {data.content.map((c) => (
              <tr key={c.id} className="row-link" onClick={() => navigate(`/contracts/${c.id}`)}>
                <td>{c.contractNumber}</td>
                <td>{c.name}</td>
                <td>{contractTypeLabels[c.type]}</td>
                <td>{formatCurrency(c.value)}</td>
                <td>
                  <span className={`badge status-${c.status}`}>{contractStatusLabels[c.status]}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
