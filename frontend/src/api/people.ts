import { api } from './client';

export interface Person {
  id: string;
  externalSubject: string;
  fullName: string;
  unitCode: string;
}

/** People that can be assigned as a contract's person-in-charge (scoped to the caller's unit). */
export async function listPeople(): Promise<Person[]> {
  const { data } = await api.get<Person[]>('/api/v1/users');
  return data;
}
