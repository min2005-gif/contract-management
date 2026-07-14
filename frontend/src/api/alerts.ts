import { api } from './client';

export type AlertType = 'NEARING_EXPIRY' | 'UNSIGNED' | 'UNPAID' | 'BEHIND_SCHEDULE';
export type AlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'RESOLVED';

export interface Alert {
  id: string;
  contractId: string;
  owningUnitId: string;
  type: AlertType;
  status: AlertStatus;
  notifiedUserId: string | null;
  raisedAt: string;
}

export async function listAlerts(status?: AlertStatus): Promise<Alert[]> {
  const { data } = await api.get<Alert[]>('/api/v1/alerts', {
    params: status ? { status } : {},
  });
  return data;
}

export async function updateAlertStatus(id: string, status: AlertStatus): Promise<Alert> {
  const { data } = await api.patch<Alert>(`/api/v1/alerts/${id}`, { status });
  return data;
}

export async function triggerEvaluation(): Promise<number> {
  const { data } = await api.post<{ raised: number }>('/api/v1/alerts/evaluate');
  return data.raised;
}
