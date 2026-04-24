import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signup } from '../services/api';
import './SignupPage.css';

/**
 * SignupPage — user registration form.
 * Note: The backend's POST /api/users requires ADMIN role,
 * so this page can also be used as an admin "create user" form.
 * For a public self-registration, the backend would need a separate endpoint.
 */
export default function SignupPage() {
  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    role: 'ROLE_EMPLOYEE',
    team: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  function handleChange(e) {
    setForm(f => ({ ...f, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const payload = { ...form };
      if (!payload.team) delete payload.team; // optional field
      await signup(payload);
      setSuccess('Account created successfully! Redirecting to email verification…');
      setTimeout(() => navigate(`/verify-email?email=${encodeURIComponent(form.email)}`), 1500);
    } catch (err) {
      setError(err.message || 'Registration failed.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card signup-card">
        <div className="auth-header">
          <h1>Create Account</h1>
          <p>Set up a new user account</p>
        </div>

        {error && <div className="error-alert">{error}</div>}
        {success && <div className="success-alert">{success}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="username">Username</label>
            <input
              id="username"
              name="username"
              className="form-input"
              placeholder="johndoe"
              value={form.username}
              onChange={handleChange}
              required
              minLength={3}
              maxLength={100}
            />
          </div>

          <div className="form-group">
            <label htmlFor="signup-email">Email</label>
            <input
              id="signup-email"
              name="email"
              type="email"
              className="form-input"
              placeholder="john@company.com"
              value={form.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="signup-password">Password</label>
            <input
              id="signup-password"
              name="password"
              type="password"
              className="form-input"
              placeholder="Min 8 characters"
              value={form.password}
              onChange={handleChange}
              required
              minLength={8}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="role">Role</label>
              <select
                id="role"
                name="role"
                className="form-input"
                value={form.role}
                onChange={handleChange}
              >
                <option value="ROLE_EMPLOYEE">Employee</option>
                <option value="ROLE_SUPPORT_ENGINEER">Support Engineer</option>
                <option value="ROLE_TEAM_LEAD">Team Lead</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="team">Team (optional)</label>
              <select
                id="team"
                name="team"
                className="form-input"
                value={form.team}
                onChange={handleChange}
              >
                <option value="">— None —</option>
                <option value="DEVELOPMENT">Development</option>
                <option value="OPERATIONS">Operations</option>
                <option value="NETWORK">Network</option>
              </select>
            </div>
          </div>

          <button type="submit" className="btn btn-primary auth-btn" disabled={loading} id="signup-btn">
            {loading ? <span className="spinner" /> : 'Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
