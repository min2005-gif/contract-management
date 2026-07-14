import { api } from './client';
import type { Contract } from './types';

export interface ReconciliationResult {
  contractId: string;
  contractValue: number;
  accountingReference: string;
  matched: boolean;
  message: string;
}

export async function signContract(contractId: string): Promise<Contract> {
  const { data } = await api.post<Contract>(`/api/v1/integrations/contracts/${contractId}/sign`);
  return data;
}

export async function reconcileContract(contractId: string): Promise<ReconciliationResult> {
  const { data } = await api.post<ReconciliationResult>(
    `/api/v1/integrations/contracts/${contractId}/accounting/reconcile`,
  );
  return data;
}

export async function linkEDocument(contractId: string, documentRef: string): Promise<Contract> {
  const { data } = await api.post<Contract>(
    `/api/v1/integrations/contracts/${contractId}/edocument`,
    { documentRef },
  );
  return data;
}
