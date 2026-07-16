// Vietnamese labels for the enum values surfaced in the UI.
import type { ContractStatus, ContractType, PaymentStatus } from './api/types';

export const contractTypeLabels: Record<ContractType, string> = {
  PURCHASE_SALE: 'Mua bán',
  SERVICE: 'Dịch vụ',
  CONSTRUCTION: 'Xây dựng',
  LEASE: 'Thuê',
  LABOR: 'Lao động',
};

export const contractStatusLabels: Record<ContractStatus, string> = {
  DRAFT: 'Nháp',
  PENDING_CHECK: 'Chờ kiểm tra',
  PENDING_TCT_APPROVAL: 'Chờ TCT duyệt',
  ACTIVE: 'Hiệu lực',
  IN_PROGRESS: 'Đang thực hiện',
  COMPLETED: 'Hoàn thành',
  LIQUIDATED: 'Đã thanh lý',
};

export const paymentStatusLabels: Record<PaymentStatus, string> = {
  UNPAID: 'Chưa thanh toán',
  PARTIAL: 'Thanh toán một phần',
  PAID: 'Đã thanh toán',
};

export const roleLabels: Record<string, string> = {
  DATA_ENTRY: 'Nhân viên nhập liệu',
  UNIT_HEAD: 'Trưởng đơn vị',
  MANAGEMENT: 'Lãnh đạo / Quản lý',
  ADMIN: 'Quản trị hệ thống',
};

export const alertTypeLabels: Record<string, string> = {
  NEARING_EXPIRY: 'Sắp hết hạn',
  UNSIGNED: 'Chưa ký',
  UNPAID: 'Chưa thanh toán',
  BEHIND_SCHEDULE: 'Chậm tiến độ',
};

export const alertStatusLabels: Record<string, string> = {
  OPEN: 'Mở',
  ACKNOWLEDGED: 'Đã tiếp nhận',
  RESOLVED: 'Đã xử lý',
};

export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    maximumFractionDigits: 0,
  }).format(value);
}
