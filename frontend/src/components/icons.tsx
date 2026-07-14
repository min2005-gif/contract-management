// Minimal inline-SVG icon set (no external dependencies), stroke-based.
interface Props {
  size?: number;
}

function svg(children: React.ReactNode, size = 18) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.9"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {children}
    </svg>
  );
}

export const FileIcon = ({ size }: Props) =>
  svg(
    <>
      <path d="M14 3v4a1 1 0 0 0 1 1h4" />
      <path d="M17 21H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h7l5 5v11a2 2 0 0 1-2 2z" />
      <path d="M9 9h1M9 13h6M9 17h6" />
    </>,
    size,
  );

export const BellIcon = ({ size }: Props) =>
  svg(
    <>
      <path d="M18 8a6 6 0 1 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
      <path d="M13.7 21a2 2 0 0 1-3.4 0" />
    </>,
    size,
  );

export const ChartIcon = ({ size }: Props) =>
  svg(
    <>
      <path d="M3 3v18h18" />
      <rect x="7" y="11" width="3" height="6" rx="0.5" />
      <rect x="12" y="7" width="3" height="10" rx="0.5" />
      <rect x="17" y="13" width="3" height="4" rx="0.5" />
    </>,
    size,
  );

export const LogoutIcon = ({ size }: Props) =>
  svg(
    <>
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
      <path d="M16 17l5-5-5-5M21 12H9" />
    </>,
    size,
  );

export const PlusIcon = ({ size }: Props) => svg(<path d="M12 5v14M5 12h14" />, size);

export const ClockIcon = ({ size }: Props) =>
  svg(
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </>,
    size,
  );

export const CheckIcon = ({ size }: Props) =>
  svg(<path d="M20 6 9 17l-5-5" />, size);

export const MoneyIcon = ({ size }: Props) =>
  svg(
    <>
      <rect x="2" y="6" width="20" height="12" rx="2" />
      <circle cx="12" cy="12" r="2.5" />
      <path d="M6 12h.01M18 12h.01" />
    </>,
    size,
  );
