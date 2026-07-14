// Types mirroring the backend REST contract (contracts/openapi.yaml).

export type ContractType =
  | 'PURCHASE_SALE'
  | 'SERVICE'
  | 'CONSTRUCTION'
  | 'LEASE'
  | 'LABOR';

export type ContractStatus =
  | 'DRAFT'
  | 'PENDING_CHECK'
  | 'PENDING_TCT_APPROVAL'
  | 'ACTIVE'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'LIQUIDATED';

export type PaymentStatus = 'UNPAID' | 'PARTIAL' | 'PAID';

export interface ContractInput {
  contractNumber: string;
  name: string;
  type: ContractType;
  partyA: string;
  partyB: string;
  value: number;
  signDate: string;
  termEnd: string;
  personInChargeId: string;
  official?: boolean;
  extraFields?: Record<string, unknown>;
}

export interface Contract extends ContractInput {
  id: string;
  status: ContractStatus;
  owningUnitId: string;
  signed: boolean;
  paymentStatus: PaymentStatus;
  progressPct: number;
  version: number;
  createdAt: string;
  updatedAt: string;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  total: number;
}

export interface Attachment {
  id: string;
  contractId: string;
  filename: string;
  contentType: string;
  kind: 'MAIN' | 'SCAN' | 'APPENDIX' | 'OTHER';
  sizeBytes: number;
  uploadedAt: string;
}

export interface AuthProfile {
  token: string;
  name: string;
  unit: string;
  roles: string[];
}
