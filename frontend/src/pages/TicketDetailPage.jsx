import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getTicket, assignTicket, updateTicketStatus, getComments, getRemarks, getAllAssignees } from '../services/api';
import { useAuth } from '../context/AuthContext';
import CommentSection from '../components/CommentSection';
import ProgressSlider from '../components/ProgressSlider';
import './TicketDetailPage.css';

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

  const fetchTicket = useCallback(async () => {
    try {
      const data = await getTicket(ticketId);
      setTicket(data);
    } catch (err) {
      setError(err.message);
    }
  }, [ticketId]);

  const fetchConversation = useCallback(async () => {
    try {
      const [c, r] = await Promise.all([
        getComments(ticketId),
        getRemarks(ticketId),
      ]);
      setComments(c || []);
      setRemarks(r || []);
    } catch (err) {
      console.error('Failed to load conversation:', err);
    }
  }, [ticketId]);

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

  useEffect(() => {
    if (ticket) {
      setSelectedStatus(ticket.status);
      setShowAssignForm(!ticket.assignedTo);
    }
  }, [ticket]);

  async function handleAssign(e) {
    e.preventDefault();
    setAssigning(true);
    setAssignError('');
    setAssignSuccess('');
    try {
      await assignTicket(ticketId, assignEmail);
      setAssignSuccess(`Ticket assigned to ${assignEmail}`);
      setAssignEmail('');
      fetchTicket();
    } catch (err) {
      setAssignError(err.message);
    } finally {
      setAssigning(false);
      setShowAssignForm(false);
    }
  }

  async function handleStatusUpdate(e) {
    if (e && e.preventDefault) e.preventDefault();
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

  const handleQuickStatusChange = async (newStatus) => {
    setUpdatingStatus(true);
    setStatusUpdateError('');
    try {
      await updateTicketStatus(ticketId, newStatus);
      await fetchTicket();
      await fetchConversation();
    } catch (err) {
      setStatusUpdateError(err.message);
    } finally {
      setUpdatingStatus(false);
    }
  };

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

  const isAssignedResolver = user && ticket.assignedTo === user.username && hasRole('SUPPORT_ENGINEER');
  const isSliderEditable = isAssignedResolver && (ticket.status === 'IN_PROGRESS' || ticket.status === 'ON_HOLD');

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

      {/* Progress Slider (Only for Support Engineers when work is active) */}
      <ProgressSlider
        ticketId={ticket.ticketId}
        initialProgress={ticket.progress || 0}
        isEditable={isSliderEditable}
        onProgressUpdate={(updatedTicket) => {
          setTicket(updatedTicket);
          if (updatedTicket.progress === 100 && ticket.status === 'IN_PROGRESS') {
            handleQuickStatusChange('RESOLVED');
          }
        }}
      />

      {/* SLA Action Cards for Support Engineer */}
      {isAssignedResolver && ticket.status === 'ASSIGNED' && (
        <div className="quick-action-sla-card start-work">
          <div className="sla-card-content">
            <h4>⚡ SLA Timer Pending Start</h4>
            <p>You have been assigned this ticket. Click below to mark work as started and initialize the active SLA timer.</p>
          </div>
          <button
            className="btn btn-success btn-lg"
            onClick={() => handleQuickStatusChange('IN_PROGRESS')}
            disabled={updatingStatus}
          >
            {updatingStatus ? 'Starting...' : '⚡ Start Working'}
          </button>
        </div>
      )}

      {isAssignedResolver && ticket.status === 'ON_HOLD' && (
        <div className="quick-action-sla-card resume-work">
          <div className="sla-card-content">
            <h4>⏸ Work Currently On Hold</h4>
            <p>This ticket is currently paused. Resume working to continue the SLA timer.</p>
          </div>
          <button
            className="btn btn-primary btn-lg"
            onClick={() => handleQuickStatusChange('IN_PROGRESS')}
            disabled={updatingStatus}
          >
            {updatingStatus ? 'Resuming...' : '▶ Resume Work'}
          </button>
        </div>
      )}

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

      {/* SLA Timeline Grid */}
      {ticket.assignedAt && (
        <div className="ticket-timeline-card">
          <h3 className="section-title">Work Timeline & SLA</h3>
          <div className="timeline-grid">
            <div className="timeline-item">
              <span className="timeline-label">Assigned At</span>
              <span className="timeline-value">{formatDate(ticket.assignedAt)}</span>
            </div>
            <div className="timeline-item">
              <span className="timeline-label">Work Started At</span>
              <span className="timeline-value">{formatDate(ticket.workStartedAt) || 'Not Started'}</span>
            </div>
            {ticket.resolvedAt && (
              <div className="timeline-item">
                <span className="timeline-label">Resolved At</span>
                <span className="timeline-value">{formatDate(ticket.resolvedAt)}</span>
              </div>
            )}
            <div className="timeline-item highlight">
              <span className="timeline-label">Waiting Time before start</span>
              <span className="timeline-value">⏳ {ticket.waitingTimeMinutes} min</span>
            </div>
            <div className="timeline-item highlight">
              <span className="timeline-label">Active Work Time</span>
              <span className="timeline-value">⚡ {ticket.activeWorkTimeMinutes} min</span>
            </div>
            <div className="timeline-item">
              <span className="timeline-label">Total Hold Duration</span>
              <span className="timeline-value">⏸ {ticket.totalHoldDurationMinutes} min</span>
            </div>
          </div>
        </div>
      )}

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
                {getAllowedStatus(ticket, user, hasRole).map(opt => (
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

function getAllowedStatus(ticket, user, hasRole) {
  const currentStatus = ticket.status;
  const isRaiser = user?.email === ticket.raisedByEmail;

  if (currentStatus === 'CLOSED') {
    if ((hasRole('EMPLOYEE') && isRaiser) || hasRole('ADMIN')) {
      return [
        { value: 'CLOSED', label: 'Closed (current)' },
        { value: 'IN_PROGRESS', label: '↺ Reopen Ticket' },
      ];
    }
    return [{ value: 'CLOSED', label: 'Closed (current)' }];
  }

  if (currentStatus === 'RESOLVED') {
    if ((hasRole('EMPLOYEE') && isRaiser) || hasRole('ADMIN')) {
      return [
        { value: 'RESOLVED', label: 'Resolved (current)' },
        { value: 'CLOSED', label: 'Close Ticket' },
        { value: 'IN_PROGRESS', label: '↺ Reopen Ticket' },
      ];
    }
    return [
      { value: 'RESOLVED', label: 'Resolved (current)' },
      { value: 'CLOSED', label: 'Close Ticket' },
    ];
  }

  const allStatus = [
    { value: 'OPEN', label: 'Open' },
    { value: 'PENDING_ASSIGNMENT', label: 'Pending Assignment' },
    { value: 'ASSIGNED', label: 'Assigned' },
    { value: 'IN_PROGRESS', label: 'In Progress' },
    { value: 'ON_HOLD', label: 'On Hold' },
    { value: 'RESOLVED', label: 'Resolved' },
    { value: 'CLOSED', label: 'Closed' },
    { value: 'CANCELLED', label: 'Cancelled' },
  ];

  return allStatus.map(s => s.value === currentStatus ? { ...s, label: s.label + ' (current)' } : s);
}

function formatStatus(status) {
  if (!status) return '';
  return status.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/\B\w+/g, w => w.toLowerCase());
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: 'numeric', minute: '2-digit' });
}
