import { api } from './client';

export type Role = 'DATA_ENTRY' | 'UNIT_HEAD' | 'MANAGEMENT' | 'ADMIN';

export interface Unit {
  id: string;
  code: string;
  name: string;
  type: string;
  active: boolean;
}

export interface UserView {
  id: string;
  externalSubject: string;
  fullName: string | null;
  email: string | null;
  unitId: string;
  unitCode: string;
  active: boolean;
  roles: Role[];
}

export interface UserUpsert {
  externalSubject: string;
  fullName: string;
  email: string;
  unitCode: string;
  active: boolean;
  roles: Role[];
}

export async function listUnits(): Promise<Unit[]> {
  const { data } = await api.get<Unit[]>('/api/v1/units');
  return data;
}

export async function listUsers(): Promise<UserView[]> {
  const { data } = await api.get<UserView[]>('/api/v1/admin/users');
  return data;
}

export async function createUser(req: UserUpsert): Promise<UserView> {
  const { data } = await api.post<UserView>('/api/v1/admin/users', req);
  return data;
}

export async function updateUser(id: string, req: UserUpsert): Promise<UserView> {
  const { data } = await api.put<UserView>(`/api/v1/admin/users/${id}`, req);
  return data;
}
