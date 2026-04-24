import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTicket, assignTicket, updateTicketStatus, getComments, getRemarks, getAvailableAssignees, getAllAssignees } from '../services/api';
import { useAuth } from '../context/AuthContext';
import CommentSection from '../components/CommentSection';
import './TicketDetailPage.css';

/**
 * TicketDetailPage — shows full details of a single ticket,
 * with actions (assign, reroute) and comments/remarks sections.
 */
export default function TicketDetailPage() {
  const { ticketId } = useParams();
  const navigate = useNavigate();
  const { user, hasRole } = useAuth();

  const [ticket, setTicket] = useState(null);
  const [comments, setComments] = useState([]);
  const [remarks, setRemarks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Assign form
  const [assignEmail, setAssignEmail] = useState('');
  const [assigning, setAssigning] = useState(false);
  const [assignError, setAssignError] = useState('');
  const [assignSuccess, setAssignSuccess] = useState('');
  const [availableAssignees, setAvailableAssignees] = useState([]);
  const [assigneeLoadError, setAssigneeLoadError] = useState('');
  const [showAssignForm, setShowAssignForm] = useState(false);

  // Status update
  const [updatingStatus, setUpdatingStatus] = useState(false);
  const [statusUpdateError, setStatusUpdateError] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');

  /** Fetch ticket data by searching for it via keyword (ticketId) */
  const fetchTicket = useCallback(async () => {
    try {
      const data = await getTicket(ticketId);
      setTicket(data);
    } catch (err) {
      setError(err.message);
    }
  }, [ticketId]);

  /** Fetch comments and remarks */
  const fetchConversation = useCallback(async () => {
    try {
      const [c, r] = await Promise.all([
        getComments(ticketId),
        getRemarks(ticketId),
      ]);
      setComments(c || []);
      setRemarks(r || []);
    } catch (err) {
      // Non-fatal — comments may fail if user lacks access
      console.error('Failed to load conversation:', err);
    }
  }, [ticketId]);

  /** Load all data on mount */
  useEffect(() => {
    async function load() {
      setLoading(true);
      await fetchTicket();
      await fetchConversation();
      setLoading(false);
    }
    load();
  }, [fetchTicket, fetchConversation]);

  useEffect(() => {
    async function loadAssignees() {
      if (ticket && hasRole('ADMIN', 'TEAM_LEAD')) {
        setAssigneeLoadError('');
        try {
          const list = await getAllAssignees();
          setAvailableAssignees(list || []);
        } catch (err) {
          console.error('Failed to load assignees:', err);
          setAssigneeLoadError(`Failed to load member list: ${err.message || 'Unknown Error'}`);
        }
      }
    }
    loadAssignees();
  }, [ticket, hasRole]);

  /** Set initial selected status when ticket loads */
  useEffect(() => {
    if (ticket) {
      setSelectedStatus(ticket.status);
      // Auto-hide assignment form if already assigned
      setShowAssignForm(!ticket.assignedTo);
    }
  }, [ticket]);

  /** Handle ticket assignment */
  async function handleAssign(e) {
    e.preventDefault();
    setAssigning(true);
    setAssignError('');
    setAssignSuccess('');
    try {
      await assignTicket(ticketId, assignEmail);
      setAssignSuccess(`Ticket assigned to ${assignEmail}`);
      setAssignEmail('');
      fetchTicket(); // refresh ticket data
    } catch (err) {
      setAssignError(err.message);
    } finally {
      setAssigning(false);
      setShowAssignForm(false);
    }
  }

  async function handleStatusUpdate(e) {
    e.preventDefault();
    if (!selectedStatus || selectedStatus === ticket.status) return;

    setUpdatingStatus(true);
    setStatusUpdateError('');
    try {
      await updateTicketStatus(ticketId, selectedStatus);
      fetchTicket();
      fetchConversation();
    } catch (err) {
      setStatusUpdateError(err.message);
    } finally {
      setUpdatingStatus(false);
    }
  }

  if (loading) {
    return (
      <div className="loading-container">
        <span className="spinner spinner-lg" />
        <span>Loading ticket…</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="ticket-detail">
        <div className="error-alert">{error}</div>
        <button className="btn btn-secondary" onClick={() => navigate('/')} style={{ marginTop: '1rem' }}>
          ← Back to Dashboard
        </button>
      </div>
    );
  }

  if (!ticket) return null;

  const statusClass = `badge badge-${ticket.status?.toLowerCase()}`;
  const priorityClass = `badge badge-${ticket.priority?.toLowerCase()}`;

  return (
    <div className="ticket-detail">
      {/* Back link */}
      <button className="btn btn-secondary btn-sm" onClick={() => navigate('/')} id="back-btn">
        ← Back
      </button>

      {/* Ticket header */}
      <div className="ticket-detail-header">
        <div className="ticket-detail-title-row">
          <h1 className="ticket-detail-title">{ticket.title}</h1>
          <span className={statusClass}>{formatStatus(ticket.status)}</span>
        </div>
        <div className="ticket-detail-id">{ticket.ticketId}</div>
      </div>

      {/* Info grid */}
      <div className="ticket-info-grid">
        <div className="ticket-info-item">
          <span className="ticket-info-label">Priority</span>
          <span className={priorityClass}>{ticket.priority}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Category</span>
          <span>{ticket.category}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Department</span>
          <span>{ticket.department || '—'}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Raised By</span>
          <span>{ticket.raisedBy}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Assigned Team</span>
          <span>{ticket.assignedTeam || '—'}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Assigned To</span>
          <span>{ticket.assignedTo || 'Unassigned'}</span>
        </div>
        <div className="ticket-info-item">
          <span className="ticket-info-label">Created</span>
          <span>{formatDate(ticket.createdAt)}</span>
        </div>
      </div>

      {/* Description */}
      {ticket.description && (
        <div className="ticket-description">
          <h3>Description</h3>
          <p>{ticket.description}</p>
        </div>
      )}

      {/* Actions section — only for authorized roles */}
      <div className="ticket-actions">
        {/* Assign (admin/manager/lead) - only if NOT closed or resolved */}
        {hasRole('ADMIN', 'TEAM_LEAD') && ticket.status !== 'CLOSED' && ticket.status !== 'RESOLVED' && (
          <div className="action-card">
            <h3>Assign Ticket</h3>
            {assignSuccess && <div className="success-alert">{assignSuccess}</div>}
            {assignError && <div className="error-alert">{assignError}</div>}
            {assigneeLoadError && <div className="error-alert">{assigneeLoadError}</div>}
            
            {showAssignForm ? (
              <form className="action-form" onSubmit={handleAssign}>
                {availableAssignees.length > 0 ? (
                  <select
                    className="form-input"
                    value={assignEmail}
                    onChange={e => setAssignEmail(e.target.value)}
                    required
                    id="assign-email-input"
                  >
                    <option value="">— Select Assignee —</option>
                    {availableAssignees.map(user => (
                      <option key={user.email} value={user.email}>
                        {user.username} ({user.email}) — Team: {user.team || 'None'}
                      </option>
                    ))}
                  </select>
                ) : (
                  <div className="empty-state-info" style={{ marginBottom: '1rem' }}>
                    <p style={{ color: '#666', fontSize: '0.9rem', marginBottom: '0.5rem' }}>
                      ⚠️ No active support engineers found in the system.
                    </p>
                    <input
                      type="email"
                      className="form-input"
                      placeholder="Assignee email (manual)"
                      value={assignEmail}
                      onChange={e => setAssignEmail(e.target.value)}
                      required
                      id="assign-email-input"
                    />
                  </div>
                )}
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <button
                    type="submit"
                    className="btn btn-primary btn-sm"
                    disabled={assigning}
                    id="assign-btn"
                  >
                    {assigning ? <span className="spinner" /> : 'Confirm Assignment'}
                  </button>
                  {ticket.assignedTo && (
                    <button 
                      type="button" 
                      className="btn btn-secondary btn-sm"
                      onClick={() => setShowAssignForm(false)}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </form>
            ) : (
              <div className="assignment-summary">
                <span>Currently assigned to <strong>{ticket.assignedTo}</strong></span>
                <button 
                  className="btn btn-secondary btn-sm" 
                  onClick={() => setShowAssignForm(true)}
                  style={{ marginLeft: '1rem' }}
                >
                  Change Assignee
                </button>
              </div>
            )}
          </div>
        )}


        {/* Update Status (admin/manager/lead/support engineer, OR the raiser if resolved/closed) */}
        {(hasRole('ADMIN', 'TEAM_LEAD', 'SUPPORT_ENGINEER') || (hasRole('EMPLOYEE') && (ticket.status === 'RESOLVED' || ticket.status === 'CLOSED'))) && (
          <div className="action-card">
            <h3>Update Status</h3>
            {statusUpdateError && <div className="error-alert">{statusUpdateError}</div>}
            <form className="action-form" onSubmit={handleStatusUpdate}>
              <select
                className="form-input"
                value={selectedStatus}
                onChange={e => setSelectedStatus(e.target.value)}
              >
                {getAllowedStatuses(ticket, user, hasRole).map(opt => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>

              <button
                type="submit"
                className="btn btn-primary btn-sm"
                disabled={updatingStatus || selectedStatus === ticket.status}
                id="update-status-btn"
              >
                {updatingStatus ? <span className="spinner" /> : 'Update Status'}
              </button>
            </form>
          </div>
        )}
      </div>

      {/* Comments & Remarks */}
      <CommentSection
        ticketId={ticketId}
        comments={comments}
        remarks={remarks}
        onRefresh={() => { fetchConversation(); }}
      />
    </div>
  );
}

/** Get allowed status transitions based on current status and user role */
function getAllowedStatuses(ticket, user, hasRole) {
  const currentStatus = ticket.status;
  const isRaiser = user?.email === ticket.raisedByEmail;

  if (currentStatus === 'CLOSED') {
    // Raiser (Employee) or Admin can reopen
    if ((hasRole('EMPLOYEE') && isRaiser) || hasRole('ADMIN')) {
      return [
        { value: 'CLOSED', label: 'Closed (current)' },
        { value: 'IN_PROGRESS', label: '↺ Reopen Ticket' },
      ];
    }
    return [{ value: 'CLOSED', label: 'Closed (current)' }];
  }

  if (currentStatus === 'RESOLVED') {
    // Raiser or Admin can Reopen.
    if ((hasRole('EMPLOYEE') && isRaiser) || hasRole('ADMIN')) {
      return [
        { value: 'RESOLVED', label: 'Resolved (current)' },
        { value: 'CLOSED', label: 'Close Ticket' },
        { value: 'IN_PROGRESS', label: '↺ Reopen Ticket' },
      ];
    }
    // Others can only Close
    return [
      { value: 'RESOLVED', label: 'Resolved (current)' },
      { value: 'CLOSED', label: 'Close Ticket' },
    ];
  }

  // For all other statuses, show all options excluding Reopen
  const allStatuses = [
    { value: 'OPEN', label: 'Open' },
    { value: 'PENDING_ASSIGNMENT', label: 'Pending Assignment' },
    { value: 'ASSIGNED', label: 'Assigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'RESOLVED', label: 'Resolved' },
    { value: 'CLOSED', label: 'Closed' },
  ];

  return allStatuses
    .filter(s => s.value !== 'IN_PROGRESS') // Reopen is only for Resolved/Closed
    .map(s => s.value === currentStatus ? { ...s, label: s.label + ' (current)' } : s);
}

/** Format status for display */
function formatStatus(status) {
  if (!status) return '';
  return status.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/\B\w+/g, w => w.toLowerCase());
}

/** Format ISO date string */
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' });
}
