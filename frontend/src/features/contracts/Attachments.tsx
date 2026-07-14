import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { errorMessage, getStoredToken } from '../../api/client';
import { attachmentDownloadUrl, listAttachments, uploadAttachment } from '../../api/contracts';

const KINDS = ['MAIN', 'SCAN', 'APPENDIX', 'OTHER'];

export function Attachments({ contractId }: { contractId: string }) {
  const queryClient = useQueryClient();
  const [kind, setKind] = useState('MAIN');
  const [error, setError] = useState<string | null>(null);

  const { data } = useQuery({
    queryKey: ['attachments', contractId],
    queryFn: () => listAttachments(contractId),
  });

  const upload = useMutation({
    mutationFn: (file: File) => uploadAttachment(contractId, file, kind),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['attachments', contractId] }),
    onError: (err) => setError(errorMessage(err)),
  });

  async function download(attachmentId: string, filename: string) {
    // Fetch with the auth header, then trigger a browser download from the blob.
    const res = await fetch(attachmentDownloadUrl(contractId, attachmentId), {
      headers: { Authorization: `Bearer ${getStoredToken() ?? ''}` },
    });
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  return (
    <div className="card">
      <h3>Tệp đính kèm</h3>
      {error && <p className="error">{error}</p>}
      <div className="toolbar">
        <select value={kind} onChange={(e) => setKind(e.target.value)}>
          {KINDS.map((k) => (
            <option key={k} value={k}>
              {k}
            </option>
          ))}
        </select>
        <input
          type="file"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) upload.mutate(file);
            e.target.value = '';
          }}
        />
      </div>
      <ul className="attachments">
        {data?.length === 0 && <li className="muted">Chưa có tệp đính kèm.</li>}
        {data?.map((a) => (
          <li key={a.id}>
            <button className="link-btn" onClick={() => download(a.id, a.filename)}>
              {a.filename}
            </button>
            <span className="muted">
              {' '}
              · {a.kind} · {(a.sizeBytes / 1024).toFixed(1)} KB
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
