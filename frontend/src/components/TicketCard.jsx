import { Link } from 'react-router-dom';
import './TicketCard.css';

/**
 * TicketCard — displays a single ticket in the dashboard list.
 * Shows title, status badge, priority badge, category, assigned user, and date.
 */
export default function TicketCard({ ticket }) {
  const statusClass = `badge badge-${ticket.status?.toLowerCase()}`;
  const priorityClass = `badge badge-${ticket.priority?.toLowerCase()}`;

  return (
    <Link to={`/tickets/${ticket.ticketId}`} className="ticket-card" id={`ticket-${ticket.ticketId}`}>
      <div className="ticket-card-header">
        <span className="ticket-card-id">{ticket.ticketId}</span>
        <span className={statusClass}>{formatStatus(ticket.status)}</span>
      </div>

      <h3 className="ticket-card-title">{ticket.title}</h3>

      {ticket.description && (
        <p className="ticket-card-desc">{ticket.description.substring(0, 120)}{ticket.description.length > 120 ? '…' : ''}</p>
      )}

      <div className="ticket-card-meta">
        <span className={priorityClass}>{ticket.priority}</span>
        <span className="ticket-card-category">{ticket.category}</span>
        {ticket.assignedTo && <span className="ticket-card-assignee">→ {ticket.assignedTo}</span>}
        <span className="ticket-card-date">{formatDate(ticket.createdAt)}</span>
      </div>
    </Link>
  );
}

/** Format status for display - e.g. "PENDING_ASSIGNMENT" → "Pending Assignment" */
function formatStatus(status) {
  if (!status) return '';
  return status.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase()).replace(/\B\w+/g, w => w.toLowerCase());
}

/** Format ISO date string to a readable short date */
function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}
