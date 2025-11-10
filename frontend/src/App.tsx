import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import './App.css';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import Login from './components/Login';
import Register from './components/Register';
import GuideRegistration from './components/GuideRegistration';
import ProfileEdit from './components/ProfileEdit';

function Home() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <div className="card">
      <div className="card-header">
        <h1 className="card-title">Welcome to Tours Manager</h1>
        <p className="card-subtitle">
          {isAuthenticated 
            ? `Welcome back, ${user?.firstName}!` 
            : 'Your gateway to amazing tour experiences around the world'}
        </p>
      </div>
      <div style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>
        {isAuthenticated ? (
          <>
            <p style={{ marginBottom: '1rem' }}>
              You are logged in as <strong>{user?.email}</strong>
            </p>
            <button onClick={logout} className="btn-primary" style={{ marginTop: '1rem' }}>
              Logout
            </button>
          </>
        ) : (
          <>
            <p style={{ marginBottom: '1rem' }}>
              Whether you are an experienced tour guide looking to share your expertise, 
              or a traveler seeking unforgettable adventures, you are in the right place.
            </p>
            <p>
              <Link to="/login">Login</Link> or <Link to="/register">Register</Link> to get started.
            </p>
          </>
        )}
      </div>
    </div>
  );
}

function Navbar() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/" className="navbar-brand">
          🌍 Tours Manager
        </Link>
        <ul className="navbar-menu">
          <li className="navbar-item">
            <Link to="/">🏠 Home</Link>
          </li>
          {isAuthenticated && (
            <li className="navbar-item">
              <Link to="/guides">🎯 Guides</Link>
            </li>
          )}
          {isAuthenticated && (
            <li className="navbar-item">
              <Link to="/profile">👤 My Profile</Link>
            </li>
          )}
          {!isAuthenticated ? (
            <>
              <li className="navbar-item">
                <Link to="/login">🔐 Login</Link>
              </li>
              <li className="navbar-item">
                <Link to="/register">📝 Register</Link>
              </li>
            </>
          ) : (
            <li className="navbar-item" style={{ marginLeft: 'auto' }}>
              <span style={{ marginRight: '1rem', color: 'var(--text-secondary)' }}>
                {user?.firstName}
              </span>
              <button 
                onClick={logout}
                style={{ 
                  background: 'transparent',
                  border: '1px solid var(--border-color)',
                  padding: '0.5rem 1rem',
                  borderRadius: '6px',
                  cursor: 'pointer',
                  color: 'var(--text-primary)'
                }}
              >
                Logout
              </button>
            </li>
          )}
        </ul>
      </div>
    </nav>
  );
}

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return isAuthenticated ? <>{children}</> : <Navigate to="/login" replace />;
}

function AppContent() {
  return (
    <>
      <Navbar />
      <main className="main-container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/guides"
            element={
              <ProtectedRoute>
                <GuideRegistration />
              </ProtectedRoute>
            }
          />
          <Route
            path="/profile"
            element={
              <ProtectedRoute>
                <ProfileEdit />
              </ProtectedRoute>
            }
          />
        </Routes>
      </main>
    </>
  );
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </Router>
  );
}

export default App;
