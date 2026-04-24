import './Pagination.css';

/**
 * Pagination — simple prev/next pagination with page numbers.
 * Props: currentPage (0-indexed), totalPages, onPageChange callback.
 */
export default function Pagination({ currentPage, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  // Build array of page numbers to show (up to 5 around current)
  const pages = [];
  const start = Math.max(0, currentPage - 2);
  const end = Math.min(totalPages - 1, currentPage + 2);
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }

  return (
    <div className="pagination">
      <button
        className="pagination-btn"
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 0}
      >
        ← Prev
      </button>

      <div className="pagination-pages">
        {start > 0 && (
          <>
            <button className="pagination-page" onClick={() => onPageChange(0)}>1</button>
            {start > 1 && <span className="pagination-dots">…</span>}
          </>
        )}
        {pages.map(p => (
          <button
            key={p}
            className={`pagination-page ${p === currentPage ? 'active' : ''}`}
            onClick={() => onPageChange(p)}
          >
            {p + 1}
          </button>
        ))}
        {end < totalPages - 1 && (
          <>
            {end < totalPages - 2 && <span className="pagination-dots">…</span>}
            <button className="pagination-page" onClick={() => onPageChange(totalPages - 1)}>
              {totalPages}
            </button>
          </>
        )}
      </div>

      <button
        className="pagination-btn"
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
      >
        Next →
      </button>
    </div>
  );
}
