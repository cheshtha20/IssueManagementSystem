import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { searchTickets, getDashboardSummary, getWorkStatus } from '../services/api';
import { useAuth } from '../context/AuthContext';
import TicketCard from '../components/TicketCard';
import Pagination from '../components/Pagination';
import ProgressPieChart from '../components/ProgressPieChart';
import TicketProgressCard from '../components/TicketProgressCard';
import './DashboardPage.css';

export default function DashboardPage() {
  const { hasRole } = useAuth();
  const navigate = useNavigate();

  // Search/Filter state
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [page, setPage] = useState(0);

  // Tickets list state
  const [tickets, setTickets] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Dashboard metrics state
  const [summary, setSummary] = useState(null);
  const [workStatus, setWorkStatus] = useState([]);
  const [metricsLoading, setMetricsLoading] = useState(true);

  // Fetch ticket list for search
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

  // Fetch dashboard summary and SLA timings
  const fetchDashboardMetrics = useCallback(async () => {
    setMetricsLoading(true);
    try {
      const sumData = await getDashboardSummary();
      setSummary(sumData);

      if (hasRole('ADMIN', 'TEAM_LEAD')) {
        const wsData = await getWorkStatus();
        setWorkStatus(wsData);
      }
    } catch (err) {
      console.error('Failed to load dashboard metrics:', err);
    } finally {
      setMetricsLoading(false);
    }
  }, [hasRole]);

  // Fetch on mount and filter changes
  useEffect(() => {
    fetchTickets();
  }, [fetchTickets]);

  useEffect(() => {
    fetchDashboardMetrics();
  }, [fetchDashboardMetrics]);

  function handleSearch(e) {
    e.preventDefault();
    setPage(0);
    fetchTickets();
  }

  function handleClear() {
    setKeyword('');
    setStatus('');
    setPriority('');
    setPage(0);
  }

  const formatDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return '—';
    return new Date(dateTimeStr).toLocaleString([], {
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadgeClass = (st) => {
    return `badge-${st?.toLowerCase() || 'open'}`;
  };

  return (
    <div className="dashboard">
      {/* 1. Header Section */}
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-title">Issue Management Dashboard</h1>
          <p className="dashboard-subtitle">Track the status, progress, and updates of your submitted tickets.</p>
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

      {/* 2. Metrics Summaries */}
      {!metricsLoading && summary && (
        <>
          <div className="dashboard-metrics-grid">
            <div className="metric-card total">
              <div className="metric-icon">🎫</div>
              <div className="metric-details">
                <span className="metric-value">{summary.totalTickets}</span>
                <span className="metric-label">Active Tickets</span>
              </div>
            </div>
            <div className="metric-card open">
              <div className="metric-icon">📥</div>
              <div className="metric-details">
                <span className="metric-value">{summary.openTickets}</span>
                <span className="metric-label">Open / Reopened</span>
              </div>
            </div>
            <div className="metric-card in-progress">
              <div className="metric-icon">⚙️</div>
              <div className="metric-details">
                <span className="metric-value">{summary.inProgressTickets}</span>
                <span className="metric-label">In Progress</span>
              </div>
            </div>
            <div className="metric-card resolved">
              <div className="metric-icon">✓</div>
              <div className="metric-details">
                <span className="metric-value">{summary.resolvedTickets}</span>
                <span className="metric-label">Resolved</span>
              </div>
            </div>

            {hasRole('ADMIN', 'TEAM_LEAD') && (
              <div className="metric-card not-started-alert">
                <div className="metric-icon">🚨</div>
                <div className="metric-details">
                  <span className="metric-value">{summary.notStartedTickets}</span>
                  <span className="metric-label">Not Started Alerts</span>
                </div>
              </div>
            )}
          </div>

          {/* Average SLA durations */}
          {hasRole('ADMIN', 'TEAM_LEAD') && (
            <div className="dashboard-metrics-grid">
              <div className="metric-card total">
                <div className="metric-icon">⏳</div>
                <div className="metric-details">
                  <span className="metric-value">{summary.avgWaitingTimeMinutes} min</span>
                  <span className="metric-label">Avg. Waiting Time</span>
                </div>
              </div>
              <div className="metric-card in-progress">
                <div className="metric-icon">⚡</div>
                <div className="metric-details">
                  <span className="metric-value">{summary.avgActiveWorkTimeMinutes} min</span>
                  <span className="metric-label">Avg. Active Work Time</span>
                </div>
              </div>
            </div>
          )}

          {/* 3. Progress Visualization Section */}
          <div className="dashboard-visuals-grid">
            <div className="visual-card">
              <span className="visual-card-title">Overall Work Completion</span>
              <ProgressPieChart percent={summary.overallCompletionPercent} />
            </div>

            <div className="visual-card">
              <span className="visual-card-title">Work Completion Ratio</span>
              <div className="linear-progress-card">
                <div className="linear-progress-stat">{summary.overallCompletionPercent}%</div>
                <div className="linear-progress-track">
                  <div
                    className="linear-progress-fill"
                    style={{ width: `${summary.overallCompletionPercent}%` }}
                  />
                </div>
                <p className="linear-progress-desc">
                  Calculated based on active, non-cancelled tickets mapped to the overall progress percentage scale.
                </p>
              </div>
            </div>
          </div>

          {/* 4. Work Breakdown list */}
          {summary.ticketProgress && summary.ticketProgress.length > 0 && (
            <div className="breakdown-table-card">
              <div className="table-title-area">
                <span className="table-title">Work Breakdown Structure (WBS)</span>
              </div>
              <div className="ticket-progress-table">
                <div className="ticket-progress-header">
                  <div>Ticket ID</div>
                  <div>Title</div>
                  <div>Assignee</div>
                  <div>Status</div>
                  <div>Progress</div>
                </div>
                {summary.ticketProgress.map((t) => (
                  <TicketProgressCard key={t.ticketId} ticket={t} />
                ))}
              </div>
            </div>
          )}
        </>
      )}

      {/* 5. Team Lead / Admin Detailed Timing Table */}
      {hasRole('ADMIN', 'TEAM_LEAD') && workStatus.length > 0 && (
        <div className="breakdown-table-card">
          <div className="table-title-area">
            <span className="table-title">Ticket Work Status Detail</span>
          </div>
          <div className="work-status-table-wrapper">
            <table className="work-status-table">
              <thead>
                <tr>
                  <th>Ticket ID</th>
                  <th>Title</th>
                  <th>Assignee</th>
                  <th>Assigned At</th>
                  <th>Work Started At</th>
                  <th>Wait Time</th>
                  <th>Work Time</th>
                  <th>Hold Duration</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {workStatus.map((item) => (
                  <tr
                    key={item.ticketId}
                    className={item.notStartedAlert ? 'alert-row' : ''}
                    style={{ cursor: 'pointer' }}
                    onClick={() => navigate(`/tickets/${item.ticketId}`)}
                  >
                    <td className="progress-row-id">
                      {item.ticketId}
                      {item.notStartedAlert && (
                        <div>
                          <span className="delay-alert-badge">⚠️ Overdue (Not Started)</span>
                        </div>
                      )}
                    </td>
                    <td style={{ fontWeight: '600' }}>{item.title}</td>
                    <td>👤 {item.assignedTo}</td>
                    <td>{formatDateTime(item.assignedAt)}</td>
                    <td>{formatDateTime(item.workStartedAt)}</td>
                    <td>⏳ {item.waitingTimeMinutes} min</td>
                    <td>⚡ {item.activeWorkTimeMinutes} min</td>
                    <td>⏸ {item.totalHoldDurationMinutes} min</td>
                    <td>
                      <span className={`badge ${getStatusBadgeClass(item.status)}`}>
                        {item.status?.replace(/_/g, ' ')}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 6. Standard Filterable Ticket List */}
      <div>
        <h2 className="table-title" style={{ marginBottom: '16px', marginTop: '16px' }}>All Searchable Tickets</h2>
        <form className="dashboard-filters" onSubmit={handleSearch}>
          <input
            type="text"
            className="form-input filter-search"
            placeholder="Search by title or keyword…"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            id="search-input"
          />
          <select
            className="form-input filter-select"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value);
              setPage(0);
            }}
            id="status-filter"
          >
            <option value="">All Status</option>
            <option value="OPEN">Open</option>
            <option value="PENDING_ASSIGNMENT">Pending Assignment</option>
            <option value="ASSIGNED">Assigned</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="ON_HOLD">On Hold</option>
            <option value="RESOLVED">Resolved</option>
            <option value="CLOSED">Closed</option>
            <option value="CANCELLED">Cancelled</option>
          </select>
          <select
            className="form-input filter-select"
            value={priority}
            onChange={(e) => {
              setPriority(e.target.value);
              setPage(0);
            }}
            id="priority-filter"
          >
            <option value="">All Priorities</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>
          <button type="submit" className="btn btn-primary btn-sm" id="search-btn">
            Search
          </button>
          {(keyword || status || priority) && (
            <button type="button" className="btn btn-secondary btn-sm" onClick={handleClear}>
              Clear
            </button>
          )}
        </form>

        {error && <div className="error-alert">{error}</div>}

        {loading && (
          <div className="loading-container">
            <span className="spinner spinner-lg" />
            <span>Loading tickets…</span>
          </div>
        )}

        {!loading && !error && (
          <>
            {tickets.length > 0 ? (
              <div className="ticket-list">
                {tickets.map((t) => (
                  <TicketCard key={t.ticketId} ticket={t} />
                ))}
              </div>
            ) : (
              <div className="empty-state">
                <p>No tickets found.</p>
              </div>
            )}

            <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
