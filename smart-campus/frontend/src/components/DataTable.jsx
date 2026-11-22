import { useMemo, useState } from 'react';
import { Search, ChevronLeft, ChevronRight } from 'lucide-react';
import EmptyState from './EmptyState';

const PAGE_SIZE = 8;

/**
 * columns: [{ key, header, render? }]
 * rows: array of objects
 */
export default function DataTable({ columns, rows, searchable = true, emptyMessage = 'No records found', actions }) {
  const [query, setQuery] = useState('');
  const [page, setPage] = useState(0);

  const filtered = useMemo(() => {
    if (!query) return rows;
    const q = query.toLowerCase();
    return rows.filter((row) =>
      columns.some((col) => String(row[col.key] ?? '').toLowerCase().includes(q))
    );
  }, [rows, query, columns]);

  const pageCount = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paged = filtered.slice(page * PAGE_SIZE, page * PAGE_SIZE + PAGE_SIZE);

  return (
    <div className="rounded-xl border border-black/8 dark:border-white/10 bg-white dark:bg-navy-800 overflow-hidden">
      {searchable && (
        <div className="flex items-center gap-2 border-b border-black/8 dark:border-white/10 px-4 py-3">
          <Search size={16} className="text-ink-muted" />
          <input
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(0); }}
            placeholder="Search..."
            aria-label="Search table"
            className="flex-1 bg-transparent text-sm outline-none placeholder:text-ink-muted"
          />
        </div>
      )}

      {filtered.length === 0 ? (
        <EmptyState message={emptyMessage} />
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left border-b border-black/8 dark:border-white/10 text-ink-muted dark:text-white/50">
                  {columns.map((col) => (
                    <th key={col.key} className="px-4 py-3 font-medium ledger-index uppercase">{col.header}</th>
                  ))}
                  {actions && <th className="px-4 py-3" />}
                </tr>
              </thead>
              <tbody>
                {paged.map((row, i) => (
                  <tr key={row.id ?? i} className="border-b border-black/5 dark:border-white/5 last:border-0 hover:bg-navy-900/[0.02] dark:hover:bg-white/[0.02]">
                    {columns.map((col) => (
                      <td key={col.key} className="px-4 py-3">
                        {col.render ? col.render(row) : row[col.key]}
                      </td>
                    ))}
                    {actions && <td className="px-4 py-3 text-right">{actions(row)}</td>}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between px-4 py-3 text-xs text-ink-muted border-t border-black/8 dark:border-white/10">
            <span>Showing {page * PAGE_SIZE + 1}-{Math.min((page + 1) * PAGE_SIZE, filtered.length)} of {filtered.length}</span>
            <div className="flex items-center gap-1">
              <button
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
                aria-label="Previous page"
                className="p-1 rounded disabled:opacity-30 hover:bg-navy-900/5 dark:hover:bg-white/5"
              >
                <ChevronLeft size={16} />
              </button>
              <span className="font-mono">{page + 1}/{pageCount}</span>
              <button
                onClick={() => setPage((p) => Math.min(pageCount - 1, p + 1))}
                disabled={page >= pageCount - 1}
                aria-label="Next page"
                className="p-1 rounded disabled:opacity-30 hover:bg-navy-900/5 dark:hover:bg-white/5"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
