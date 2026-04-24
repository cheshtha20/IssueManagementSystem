import { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { login } from '../services/api';
import ReCAPTCHA from 'react-google-recaptcha';
import './LoginPage.css';

/**
 * LoginPage — email + password form that calls the /api/auth/login endpoint.
 * On success, stores the JWT and redirects to the dashboard.
 */
export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [captchaToken, setCaptchaToken] = useState(null);
  const recaptchaRef = useRef();
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await login(email, password, captchaToken);
      loginUser(data);
      navigate('/');
    } catch (err) {
      if (err.message && err.message.toLowerCase().includes('email verification is pending')) {
        navigate(`/verify-email?email=${encodeURIComponent(email)}`);
      } else {
        setError(err.message || 'Login failed. Check your credentials.');
        // Reset ReCAPTCHA on failure so user has to verify again
        if (recaptchaRef.current) {
          recaptchaRef.current.reset();
          setCaptchaToken(null);
        }
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Issue Manager</h1>
          <p>Sign in to your account</p>
        </div>

        {error && <div className="error-alert">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              className="form-input"
              placeholder="you@company.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              className="form-input"
              placeholder="••••••••"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
            />
          </div>

          <div className="form-group recaptcha-container" style={{ marginBottom: '1.5rem', display: 'flex', justifyContent: 'center' }}>
            <ReCAPTCHA
              ref={recaptchaRef}
              sitekey="6Lf47cMsAAAAANYvbB9Lbme8vYvLMdWJMeq8rDNG"
              onChange={token => setCaptchaToken(token)}
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary auth-btn"
            disabled={loading || !captchaToken}
            id="login-btn"
          >
            {loading ? <span className="spinner" /> : 'Sign In'}
          </button>
        </form>

        <p className="auth-footer">
          Don't have an account? <Link to="/signup">Sign up</Link>
        </p>
      </div>
    </div>
  );
}
