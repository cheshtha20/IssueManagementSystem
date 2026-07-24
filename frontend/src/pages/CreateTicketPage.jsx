import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createTicket } from '../services/api';
import './CreateTicketPage.css';

/**
 * CreateTicketPage — form to raise a new ticket.
 * Requires the EMPLOYEE role on the backend.
 */
export default function CreateTicketPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    title: '',
    category: 'BUG',
    priority: 'MEDIUM',
    department: '',
    description: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const ticket = await createTicket(form);
      // Navigate to the newly created ticket
      navigate(`/tickets/${ticket.ticketId}`);
    } catch (err) {
      setError(err.message || 'Failed to create ticket.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="create-ticket">
      <h1 className="page-title">Create New Ticket</h1>

      {error && <div className="error-alert">{error}</div>}

      <form className="create-ticket-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="title">Title *</label>
          <input
            id="title"
            name="title"
            className="form-input"
            placeholder="Brief description of the issue"
            value={form.title}
            onChange={handleChange}
            required
            maxLength={150}
          />
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="category">Category *</label>
            <select
              id="category"
              name="category"
              className="form-input"
              value={form.category}
              onChange={handleChange}
            >
              <option value="BUG">Bug</option>
              <option value="ENHANCEMENT">Enhancement</option>
              <option value="OPERATIONS">Operations</option>
              <option value="NETWORKING">Networking</option>
              <option value="SERVERS">Servers</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="priority">Priority *</label>
            <select
              id="priority"
              name="priority"
              className="form-input"
              value={form.priority}
              onChange={handleChange}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="department">Department (optional)</label>
          <input
            id="department"
            name="department"
            className="form-input"
            placeholder="e.g. Engineering, Support"
            value={form.department}
            onChange={handleChange}
            maxLength={100}
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <textarea
            id="description"
            name="description"
            className="form-input"
            placeholder="Detailed description of the issue…"
            value={form.description}
            onChange={handleChange}
            rows={5}
            maxLength={500}
          />
        </div>

        <div className="form-actions">
          <button
            type="button"
            className="btn btn-secondary"
            onClick={() => navigate('/')}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            id="submit-ticket-btn"
          >
            {loading ? <span className="spinner" /> : 'Create Ticket'}
          </button>
        </div>
      </form>
    </div>
  );
}
