import { useState } from 'react';
import { addComment, addRemark } from '../services/api';
import './CommentSection.css';

/**
 * CommentSection — displays comments and remarks for a ticket,
 * and provides forms to add new ones.
 */
export default function CommentSection({ ticketId, comments, remarks, onRefresh }) {
  const [activeTab, setActiveTab] = useState('comments');
  const [commentText, setCommentText] = useState('');
  const [remarkText, setRemarkText] = useState('');
  const [remarkType, setRemarkType] = useState('COMMENT');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  /** Submit a new comment */
  async function handleAddComment(e) {
    e.preventDefault();
    if (!commentText.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      await addComment(ticketId, commentText.trim());
      setCommentText('');
      onRefresh(); // re-fetch data
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  /** Submit a new remark */
  async function handleAddRemark(e) {
    e.preventDefault();
    if (!remarkText.trim()) return;
    setSubmitting(true);
    setError('');
    try {
      await addRemark(ticketId, { content: remarkText.trim(), type: remarkType });
      setRemarkText('');
      onRefresh();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="comment-section">
      {/* Tab headers */}
      <div className="comment-tabs">
        <button
          className={`comment-tab ${activeTab === 'comments' ? 'active' : ''}`}
          onClick={() => setActiveTab('comments')}
        >
          Comments ({comments?.length || 0})
        </button>
        <button
          className={`comment-tab ${activeTab === 'remarks' ? 'active' : ''}`}
          onClick={() => setActiveTab('remarks')}
        >
          Remarks ({remarks?.length || 0})
        </button>
      </div>

      {error && <div className="error-alert">{error}</div>}

      {/* Comments tab */}
      {activeTab === 'comments' && (
        <div className="comment-list">
          {comments?.length > 0 ? (
            comments.map(c => (
              <div key={c.id} className="comment-item">
                <div className="comment-header">
                  <span className="comment-author">{c.username}</span>
                  <span className="comment-time">{formatDateTime(c.createdAt)}</span>
                </div>
                <p className="comment-text">{c.text}</p>
              </div>
            ))
          ) : (
            <p className="empty-state">No comments yet.</p>
          )}

          {/* Add comment form */}
          <form className="comment-form" onSubmit={handleAddComment}>
            <textarea
              className="form-input comment-textarea"
              placeholder="Write a comment…"
              value={commentText}
              onChange={e => setCommentText(e.target.value)}
              rows={3}
              maxLength={1000}
              id="comment-input"
            />
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={submitting || !commentText.trim()}
              id="add-comment-btn"
            >
              {submitting ? <span className="spinner" /> : 'Add Comment'}
            </button>
          </form>
        </div>
      )}

      {/* Remarks tab */}
      {activeTab === 'remarks' && (
        <div className="comment-list">
          {remarks?.length > 0 ? (
            remarks.map(r => (
              <div key={r.id} className="comment-item">
                <div className="comment-header">
                  <span className="comment-author">{r.authorUsername}</span>
                  <span className="remark-type-badge">{r.type}</span>
                  <span className="comment-time">{formatDateTime(r.createdAt)}</span>
                </div>
                <p className="comment-text">{r.content}</p>
              </div>
            ))
          ) : (
            <p className="empty-state">No remarks yet.</p>
          )}

          {/* Add remark form */}
          <form className="comment-form" onSubmit={handleAddRemark}>
            <div className="remark-form-row">
              <select
                className="form-input"
                value={remarkType}
                onChange={e => setRemarkType(e.target.value)}
              >
                <option value="COMMENT">Comment</option>
                <option value="INTERNAL">Internal</option>
                <option value="RESOLUTION">Resolution</option>
              </select>
            </div>
            <textarea
              className="form-input comment-textarea"
              placeholder="Write a remark…"
              value={remarkText}
              onChange={e => setRemarkText(e.target.value)}
              rows={3}
              maxLength={1000}
              id="remark-input"
            />
            <button
              type="submit"
              className="btn btn-primary btn-sm"
              disabled={submitting || !remarkText.trim()}
              id="add-remark-btn"
            >
              {submitting ? <span className="spinner" /> : 'Add Remark'}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

/** Format ISO datetime to readable string */
function formatDateTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleString('en-US', {
    month: 'short', day: 'numeric', year: 'numeric',
    hour: 'numeric', minute: '2-digit',
  });
}
