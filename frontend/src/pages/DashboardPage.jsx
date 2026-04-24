import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchTickets } from '../services/api';
import { useAuth } from '../context/AuthContext';
import TicketCard from '../components/TicketCard';
import Pagination from '../components/Pagination';
import './DashboardPage.css';

/**
 * DashboardPage — main landing page after login.
 * Shows a searchable, filterable, paginated list of tickets.
 */
export default function DashboardPage() {
  const { hasRole } = useAuth();
  const navigate = useNavigate();

  // Filter state
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');

  // Pagination state (0-indexed)
  const [page, setPage] = useState(0);

  // Data state
  const [tickets, setTickets] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  /** Fetch tickets from the search API */
  const fetchTickets = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await searchTickets({ keyword, status, priority, page, size: 10 });
      setTickets(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
    } catch (err) {
      setError(err.message);
      setTickets([]);
    } finally {
      setLoading(false);
    }
  }, [keyword, status, priority, page]);

  // Fetch on mount and when filters/page change
  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  /** Handle search — reset to page 0 */
  function handleSearch(e) {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  }

  /** Clear all filters */
  function handleClear() {
    setKeyword('');
    setStatus('');
    setPriority('');
    setPage(0);
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-title">Tickets</h1>
          <p className="dashboard-subtitle">{totalElements} total ticket{totalElements !== 1 ? 's' : ''}</p>
        </div>
        {hasRole('EMPLOYEE', 'ADMIN') && (
          <button
            className="btn btn-primary"
            onClick={() => navigate('/tickets/new')}
            id="new-ticket-btn"
          >
            + New Ticket
          </button>
        )}
      </div>

      {/* Filters */}
      <form className="dashboard-filters" onSubmit={handleSearch}>
        <input
          type="text"
          className="form-input filter-search"
          placeholder="Search by title or keyword…"
          value={keyword}
          onChange={e => setKeyword(e.target.value)}
          id="search-input"
        />
        <select
          className="form-input filter-select"
          value={status}
          onChange={e => { setStatus(e.target.value); setPage(0); }}
          id="status-filter"
        >
          <option value="">All Statuses</option>
          <option value="OPEN">Open</option>
          <option value="PENDING_ASSIGNMENT">Pending Assignment</option>
          <option value="ASSIGNED">Assigned</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="RESOLVED">Resolved</option>
          <option value="CLOSED">Closed</option>
        </select>
        <select
          className="form-input filter-select"
          value={priority}
          onChange={e => { setPriority(e.target.value); setPage(0); }}
          id="priority-filter"
        >
          <option value="">All Priorities</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="CRITICAL">Critical</option>
        </select>
        <button type="submit" className="btn btn-primary btn-sm" id="search-btn">Search</button>
        {(keyword || status || priority) && (
          <button type="button" className="btn btn-secondary btn-sm" onClick={handleClear}>Clear</button>
        )}
      </form>

      {/* Error */}
      {error && <div className="error-alert">{error}</div>}

      {/* Loading */}
      {loading && (
        <div className="loading-container">
          <span className="spinner spinner-lg" />
          <span>Loading tickets…</span>
        </div>
      )}

      {/* Ticket list */}
      {!loading && !error && (
        <>
          {tickets.length > 0 ? (
            <div className="ticket-list">
              {tickets.map(t => (
                <TicketCard key={t.ticketId} ticket={t} />
              ))}
            </div>
          ) : (
            <div className="empty-state">
              <p>No tickets found.</p>
            </div>
          )}

          <Pagination
            currentPage={page}
            totalPages={totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </div>
  );
}
