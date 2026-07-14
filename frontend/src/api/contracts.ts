import { api } from './client';
import type { Attachment, Contract, ContractInput, Page } from './types';

export interface ContractQuery {
  q?: string;
  status?: string;
  type?: string;
  page?: number;
  size?: number;
}

export async function listContracts(query: ContractQuery): Promise<Page<Contract>> {
  const { data } = await api.get<Page<Contract>>('/api/v1/contracts', { params: query });
  return data;
}

export async function getContract(id: string): Promise<Contract> {
  const { data } = await api.get<Contract>(`/api/v1/contracts/${id}`);
  return data;
}

export async function createContract(input: ContractInput): Promise<Contract> {
  const { data } = await api.post<Contract>('/api/v1/contracts', input);
  return data;
}

export async function updateContract(id: string, input: ContractInput): Promise<Contract> {
  const { data } = await api.put<Contract>(`/api/v1/contracts/${id}`, input);
  return data;
}

export async function listAttachments(contractId: string): Promise<Attachment[]> {
  const { data } = await api.get<Attachment[]>(`/api/v1/contracts/${contractId}/attachments`);
  return data;
}

export async function uploadAttachment(
  contractId: string,
  file: File,
  kind: string,
): Promise<Attachment> {
  const form = new FormData();
  form.append('file', file);
  form.append('kind', kind);
  const { data } = await api.post<Attachment>(
    `/api/v1/contracts/${contractId}/attachments`,
    form,
  );
  return data;
}

export function attachmentDownloadUrl(contractId: string, attachmentId: string): string {
  return `/api/v1/contracts/${contractId}/attachments/${attachmentId}`;
}
