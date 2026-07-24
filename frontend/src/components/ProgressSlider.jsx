import { useState, useEffect, useRef } from 'react';
import { updateTicketProgress } from '../services/api';
import './ProgressSlider.css';

export default function ProgressSlider({ ticketId, initialProgress, isEditable, onProgressUpdate }) {
  const [progress, setProgress] = useState(initialProgress);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const debounceTimer = useRef(null);

  // Sync with initialProgress if it changes from outside
  useEffect(() => {
    setProgress(initialProgress);
  }, [initialProgress]);

  const saveProgress = async (val) => {
    setSaving(true);
    setError('');
    setSuccess(false);
    try {
      const updatedTicket = await updateTicketProgress(ticketId, val);
      setSuccess(true);
      if (onProgressUpdate) {
        onProgressUpdate(updatedTicket);
      }
      setTimeout(() => setSuccess(false), 2000);
    } catch (err) {
      setError(err.message || 'Failed to update progress');
      // Reset back to initial if failed
      setProgress(initialProgress);
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e) => {
    const val = parseInt(e.target.value, 10);
    if (val < initialProgress) {
      return;
    }
    setProgress(val);

    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      saveProgress(val);
    }, 500);
  };

  return (
    <div className="progress-slider-card">
      <div className="progress-slider-header">
        <span className="progress-slider-label">Work Progress: <strong>{progress}%</strong></span>
        <div className="progress-status-indicators">
          {saving && <span className="spinner spinner-sm" />}
          {success && <span className="status-badge success-indicator">✓ Saved</span>}
          {error && <span className="status-badge error-indicator">✕ {error}</span>}
        </div>
      </div>

      <div className="progress-slider-input-wrapper">
        <input
          type="range"
          min="0"
          max="100"
          value={progress}
          onChange={handleChange}
          disabled={!isEditable || saving}
          className={`progress-range-input ${!isEditable ? 'disabled' : ''}`}
          id="progress-slider-input"
        />
        <div className="progress-track-fill" style={{ width: `${progress}%` }} />
      </div>


    </div>
  );
}
