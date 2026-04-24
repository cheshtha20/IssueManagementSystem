import { createContext, useContext, useState, useEffect } from 'react';

/**
 * AuthContext
 * -----------
 * Manages JWT authentication state across the app.
 * - Stores token, user info in localStorage for persistence.
 * - Provides login/logout helpers and role-checking utilities.
 */

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, restore user from localStorage if available
  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        // Corrupted data — clear it
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  /**
   * Call after a successful login API response.
   * Stores the JWT and user details.
   */
  function loginUser(authResponse) {
    const userData = {
      username: authResponse.username,
      email: authResponse.email,
      role: authResponse.role,
    };
    localStorage.setItem('token', authResponse.token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  }

  /**
   * Clear auth state and redirect to login.
   */
  function logoutUser() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }

  /**
   * Check if the current user has one of the given roles.
   * Roles are stored as e.g. "ROLE_ADMIN", but we accept both
   * "ADMIN" and "ROLE_ADMIN" for convenience.
   */
  function hasRole(...roles) {
    if (!user?.role) return false;
    return roles.some(r => {
      const normalized = r.startsWith('ROLE_') ? r : `ROLE_${r}`;
      return user.role === normalized || user.role === r;
    });
  }

  return (
    <AuthContext.Provider value={{ user, loading, loginUser, logoutUser, hasRole }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook to access auth context from any component.
 */
export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
