import { Navigate, Route, Routes } from 'react-router-dom';
import { Layout } from './components/Layout';
import { LoginPage } from './auth/LoginPage';
import { ContractListPage } from './features/contracts/ContractListPage';
import { ContractFormPage } from './features/contracts/ContractFormPage';

export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<Layout />}>
        <Route path="/contracts" element={<ContractListPage />} />
        <Route path="/contracts/new" element={<ContractFormPage />} />
        <Route path="/contracts/:id" element={<ContractFormPage />} />
        <Route path="/" element={<Navigate to="/contracts" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/contracts" replace />} />
    </Routes>
  );
}
