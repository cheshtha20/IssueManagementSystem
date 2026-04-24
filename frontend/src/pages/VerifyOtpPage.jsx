import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { confirmEmail, requestOtp } from '../services/api';
import './VerifyOtpPage.css';

/**
 * VerifyOtpPage — screen to enter the 6-digit email verification code.
 * Gets the email from the URL query params.
 */
export default function VerifyOtpPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const query = new URLSearchParams(location.search);
  const email = query.get('email');

  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);

  // If no email is provided, redirect to login
  useEffect(() => {
    if (!email) {
      navigate('/login');
    }
  }, [email, navigate]);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      await confirmEmail(email, otp);
      setSuccess('Email verified successfully! You can now log in.');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.message || 'Verification failed. Please check the code.');
    } finally {
      setLoading(false);
    }
  }

  async function handleResend() {
    setError('');
    setSuccess('');
    setResending(true);
    try {
      await requestOtp(email);
      setSuccess('A new verification code has been sent to your email.');
    } catch (err) {
      setError(err.message || 'Failed to resend code.');
    } finally {
      setResending(false);
    }
  }

  if (!email) return null;

  return (
    <div className="verify-otp-page">
      <div className="verify-otp-card">
        <div className="verify-otp-header">
          <h1>Verify Your Email</h1>
          <p>We've sent a 6-digit code to <strong>{email}</strong>. Please enter it below to activate your account.</p>
        </div>

        {error && <div className="error-alert">{error}</div>}
        {success && <div className="success-alert">{success}</div>}

        <form onSubmit={handleSubmit} className="verify-otp-form">
          <div className="otp-input-group">
            <input
              type="text"
              className="otp-input"
              placeholder="000000"
              value={otp}
              onChange={e => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
              required
              autoFocus
              autoComplete="one-time-code"
              maxLength={6}
            />
          </div>

          <button type="submit" className="btn btn-primary auth-btn" disabled={loading || otp.length !== 6}>
            {loading ? <span className="spinner" /> : 'Verify Account'}
          </button>
        </form>

        <div className="resend-section">
          Didn't receive the code?
          <button
            type="button"
            className="resend-btn"
            onClick={handleResend}
            disabled={resending}
          >
            {resending ? 'Sending…' : 'Resend Code'}
          </button>
        </div>

        <div style={{ marginTop: '2rem' }}>
          <button className="btn btn-secondary btn-sm" onClick={() => navigate('/login')}>
            ← Back to Login
          </button>
        </div>
      </div>
    </div>
  );
}
