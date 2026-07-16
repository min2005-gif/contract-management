import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { LoginPage } from './auth/LoginPage';
import { ContractListPage } from './features/contracts/ContractListPage';
import { ContractFormPage } from './features/contracts/ContractFormPage';
import { ContractDetailPage } from './features/contracts/ContractDetailPage';
import { DashboardPage } from './features/reports/DashboardPage';
import { AlertsPage } from './features/alerts/AlertsPage';
import { AdminUsersPage } from './features/admin/AdminUsersPage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/contracts" element={<ContractListPage />} />
        <Route path="/contracts/new" element={<ContractFormPage />} />
        <Route path="/contracts/:id/edit" element={<ContractFormPage />} />
        <Route path="/contracts/:id" element={<ContractDetailPage />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/alerts" element={<AlertsPage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
        <Route path="/" element={<Navigate to="/contracts" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/contracts" replace />} />
    </Routes>
  );
}
