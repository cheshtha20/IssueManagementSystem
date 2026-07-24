import { useNavigate } from 'react-router-dom';

export default function TicketProgressCard({ ticket }) {
  const navigate = useNavigate();

  const getStatusClass = (status) => {
    return `badge-${status?.toLowerCase() || 'open'}`;
  };

  return (
    <div className="ticket-progress-row" onClick={() => navigate(`/tickets/${ticket.ticketId}`)}>
      <div className="progress-row-id">{ticket.ticketId}</div>
      <div className="progress-row-title">{ticket.title}</div>
      <div className="progress-row-assignee">
        {ticket.assignedTo ? (
          <span className="assignee-tag">👤 {ticket.assignedTo}</span>
        ) : (
          <span className="unassigned-tag">Unassigned</span>
        )}
      </div>
      <div className="progress-row-status">
        <span className={`badge ${getStatusClass(ticket.status)}`}>
          {ticket.status?.replace(/_/g, ' ')}
        </span>
      </div>
      <div className="progress-row-bar-cell">
        <div className="row-progress-text">{ticket.progress}%</div>
        <div className="row-progress-bar-wrapper">
          <div className="row-progress-bar-fill" style={{ width: `${ticket.progress}%` }} />
        </div>
      </div>
    </div>
  );
}
