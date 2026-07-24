import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

/**
 * Top navigation bar visible on all authenticated pages.
 * Shows app name, nav links, user info, and logout button.
 */
export default function Navbar() {
  const { user, logoutUser, hasRole } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logoutUser();
    navigate('/login');
  }

  return (
    <nav className="navbar">
      <div className="navbar-inner">
        {/* Brand */}
        <div className="navbar-brand">
          Issue Manager
        </div>

        {/* Nav links */}
        <div className="navbar-links">
          <NavLink to="/" className="navbar-link" end>Dashboard</NavLink>
          {hasRole('EMPLOYEE', 'ADMIN') && (
            <NavLink to="/tickets/new" className="navbar-link">New Ticket</NavLink>
          )}
          {hasRole('ADMIN') && (
            <NavLink to="/users" className="navbar-link">Users</NavLink>
          )}
        </div>

        {/* User section */}
        <div className="navbar-user">
          <div className="navbar-user-info">
            <span className="navbar-username">{user?.username}</span>
            <span className="navbar-role">{user?.role?.replace('ROLE_', '')}</span>
          </div>
          <button onClick={handleLogout} className="btn btn-sm btn-secondary" id="logout-btn">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}
