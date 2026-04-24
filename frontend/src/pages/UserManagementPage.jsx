import { useState, useEffect } from 'react';
import { getAllUsers, updateUserRole } from '../services/api';
import './UserManagementPage.css';

/**
 * UserManagementPage — Admin-only screen to view all users
 * and change their roles dynamically.
 */
export default function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [updatingId, setUpdatingId] = useState(null);

  useEffect(() => {
    fetchUsers();
  }, []);

  async function fetchUsers() {
    setLoading(true);
    try {
      const data = await getAllUsers();
      // Filter out Admins (don't show them in the management list)
      const filtered = (data || []).filter(u => u.role !== 'ROLE_ADMIN');
      // Sort by username
      const sorted = filtered.sort((a, b) => a.username.localeCompare(b.username));
      setUsers(sorted);
    } catch (err) {
      setError(err.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  }

  async function handleRoleChange(userId, newRole) {
    setError('');
    setSuccess('');
    setUpdatingId(userId);
    try {
      await updateUserRole(userId, newRole);
      setSuccess(`User role updated to ${newRole.replace('ROLE_', '')}`);
      // Update local state
      setUsers(prev => prev.map(u => u.id === userId ? { ...u, role: newRole } : u));
    } catch (err) {
      setError(err.message || 'Failed to update role');
    } finally {
      setUpdatingId(null);
    }
  }

  if (loading) {
    return (
      <div className="loading-container">
        <span className="spinner spinner-lg" />
        <span>Loading user directory...</span>
      </div>
    );
  }

  return (
    <div className="user-mgmt-page">
      <div className="user-mgmt-header">
        <h1>User Management</h1>
        <p>View all registered users and manage their access levels.</p>
      </div>

      {error && <div className="error-alert">{error}</div>}
      {success && <div className="success-alert">{success}</div>}

      <div className="user-table-container">
        <table className="user-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Username</th>
              <th>Email</th>
              <th>Current Role</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.id}>
                <td>{user.id}</td>
                <td className="user-name-cell">
                  <strong>{user.username}</strong>
                  {!user.emailVerified && <span className="unverified-tag">Unverified</span>}
                </td>
                <td>{user.email}</td>
                <td>
                  <span className={`role-badge role-${user.role.toLowerCase().replace('role_', '')}`}>
                    {user.role.replace('ROLE_', '')}
                  </span>
                </td>
                <td>
                  <select
                    className="form-input role-select-sm"
                    value={user.role}
                    onChange={(e) => handleRoleChange(user.id, e.target.value)}
                    disabled={updatingId === user.id || user.role === 'ROLE_ADMIN'}
                  >
                    <option value="ROLE_EMPLOYEE">Employee</option>
                    <option value="ROLE_SUPPORT_ENGINEER">Support Engineer</option>
                    <option value="ROLE_TEAM_LEAD">Team Lead</option>
                    <option value="ROLE_ADMIN">Admin</option>
                  </select>
                  {updatingId === user.id && <span className="spinner spinner-sm inline-spinner" />}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
