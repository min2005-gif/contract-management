import { api, getStoredToken } from './client';

export interface UnitBreakdown {
  unitId: string;
  unitName: string;
  contractCount: number;
  totalValue: number;
}

export interface ReportSummary {
  totalContracts: number;
  totalValue: number;
  nearingExpiry: number;
  inProgress: number;
  perUnit: UnitBreakdown[];
}

export async function getReportSummary(): Promise<ReportSummary> {
  const { data } = await api.get<ReportSummary>('/api/v1/reports/summary');
  return data;
}

/** Downloads an export (xlsx|pdf) using the auth header and triggers a browser save. */
export async function downloadReport(format: 'xlsx' | 'pdf'): Promise<void> {
  const res = await fetch(`/api/v1/reports/export?format=${format}`, {
    headers: { Authorization: `Bearer ${getStoredToken() ?? ''}` },
  });
  if (!res.ok) throw new Error('Không thể tải tệp xuất báo cáo.');
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `bao-cao-hop-dong.${format}`;
  a.click();
  URL.revokeObjectURL(url);
}
