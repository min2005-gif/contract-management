import { useQuery } from '@tanstack/react-query';
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { errorMessage } from '../../api/client';
import { downloadReport, getReportSummary } from '../../api/reports';
import { formatCurrency } from '../../i18n';
import { ChartIcon, ClockIcon, FileIcon, MoneyIcon } from '../../components/icons';

export function DashboardPage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['report-summary'],
    queryFn: getReportSummary,
  });

  if (isLoading) return <p>Đang tải…</p>;
  if (isError) return <p className="error">{errorMessage(error)}</p>;
  if (!data) return null;

  const chartData = data.perUnit.map((u) => ({
    name: u.unitName.length > 16 ? u.unitName.slice(0, 16) + '…' : u.unitName,
    count: u.contractCount,
  }));

  return (
    <section>
      <div className="page-head">
        <h2>Báo cáo tổng hợp</h2>
        <div className="toolbar">
          <button onClick={() => downloadReport('xlsx')}>Xuất Excel</button>
          <button className="secondary" onClick={() => downloadReport('pdf')}>
            Xuất PDF
          </button>
        </div>
      </div>

      <div className="tiles">
        <div className="tile card">
          <span className="tile-icon">
            <FileIcon size={20} />
          </span>
          <span className="tile-value">{data.totalContracts}</span>
          <span className="tile-label">Tổng số hợp đồng</span>
        </div>
        <div className="tile card accent-green">
          <span className="tile-icon">
            <MoneyIcon size={20} />
          </span>
          <span className="tile-value">{formatCurrency(data.totalValue)}</span>
          <span className="tile-label">Tổng giá trị</span>
        </div>
        <div className="tile card accent-amber">
          <span className="tile-icon">
            <ClockIcon size={20} />
          </span>
          <span className="tile-value">{data.nearingExpiry}</span>
          <span className="tile-label">Sắp hết hạn</span>
        </div>
        <div className="tile card accent-teal">
          <span className="tile-icon">
            <ChartIcon size={20} />
          </span>
          <span className="tile-value">{data.inProgress}</span>
          <span className="tile-label">Đang thực hiện</span>
        </div>
      </div>

      <div className="card">
        <h3>Số hợp đồng theo đơn vị</h3>
        <div style={{ width: '100%', height: 280 }}>
          <ResponsiveContainer>
            <BarChart data={chartData} margin={{ top: 8, right: 16, bottom: 8, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="count" fill="#0b5cad" name="Số hợp đồng" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <table className="grid">
        <thead>
          <tr>
            <th>Đơn vị</th>
            <th>Số hợp đồng</th>
            <th>Tổng giá trị</th>
          </tr>
        </thead>
        <tbody>
          {data.perUnit.map((u) => (
            <tr key={u.unitId}>
              <td>{u.unitName}</td>
              <td>{u.contractCount}</td>
              <td>{formatCurrency(u.totalValue)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}
