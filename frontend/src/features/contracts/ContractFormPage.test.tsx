import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { fireEvent, render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import { ContractFormPage } from './ContractFormPage';

function renderNewForm() {
  const queryClient = new QueryClient();
  return render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter initialEntries={['/contracts/new']}>
        <Routes>
          <Route path="/contracts/new" element={<ContractFormPage />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}

describe('ContractFormPage', () => {
  it('blocks submit and shows required-field errors when the form is empty', async () => {
    renderNewForm();
    await userEvent.click(screen.getByRole('button', { name: 'Lưu' }));

    const requiredErrors = await screen.findAllByText('Bắt buộc');
    expect(requiredErrors.length).toBeGreaterThan(0);
  });

  it('rejects a term end that is before the sign date', async () => {
    renderNewForm();
    fireEvent.change(screen.getByLabelText(/Số hợp đồng/), { target: { value: 'HD-1' } });
    fireEvent.change(screen.getByLabelText(/Tên hợp đồng/), { target: { value: 'Test' } });
    fireEvent.change(screen.getByLabelText(/Bên A/), { target: { value: 'VATM' } });
    fireEvent.change(screen.getByLabelText(/Bên B/), { target: { value: 'Đối tác' } });
    fireEvent.change(screen.getByLabelText(/Ngày ký/), { target: { value: '2026-06-01' } });
    fireEvent.change(screen.getByLabelText(/Thời hạn/), { target: { value: '2026-01-01' } });
    await userEvent.click(screen.getByRole('button', { name: 'Lưu' }));

    expect(await screen.findByText('Thời hạn phải sau ngày ký')).toBeInTheDocument();
  });
});
