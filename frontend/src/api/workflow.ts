import { api } from './client';
import type { Contract } from './types';

export type WorkflowAction =
  | 'SUBMIT'
  | 'CHECK_APPROVE'
  | 'TCT_APPROVE'
  | 'REJECT'
  | 'LIQUIDATE';

export interface WorkflowStep {
  id: string;
  action: WorkflowAction;
  fromStatus: string;
  toStatus: string;
  actorId: string;
  reason: string | null;
  createdAt: string;
}

export async function performWorkflow(
  contractId: string,
  action: WorkflowAction,
  reason?: string,
): Promise<Contract> {
  const { data } = await api.post<Contract>(`/api/v1/contracts/${contractId}/workflow`, {
    action,
    reason,
  });
  return data;
}

export async function getWorkflowHistory(contractId: string): Promise<WorkflowStep[]> {
  const { data } = await api.get<WorkflowStep[]>(`/api/v1/contracts/${contractId}/workflow`);
  return data;
}
