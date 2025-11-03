import { useState } from 'react'
import './App.css'
import GuideRegistration from './components/GuideRegistration'

type MenuItem = 'home' | 'register-guide' | 'register-user';

function App() {
  const [activeMenu, setActiveMenu] = useState<MenuItem>('home');

  return (
    <>
      <nav className="navbar">
        <div className="navbar-container">
          <a href="#" className="navbar-brand" onClick={() => setActiveMenu('home')}>
            🌍 Tours Manager
          </a>
          <ul className="navbar-menu">
            <li 
              className={`navbar-item ${activeMenu === 'home' ? 'active' : ''}`}
              onClick={() => setActiveMenu('home')}
            >
              🏠 Home
            </li>
            <li 
              className={`navbar-item ${activeMenu === 'register-guide' ? 'active' : ''}`}
              onClick={() => setActiveMenu('register-guide')}
            >
              🎯 Guides
            </li>
            <li 
              className={`navbar-item ${activeMenu === 'register-user' ? 'active' : ''}`}
              onClick={() => setActiveMenu('register-user')}
            >
              👤 Users
            </li>
          </ul>
        </div>
      </nav>

      <main className="main-container">
        {activeMenu === 'home' && (
          <div className="card">
            <div className="card-header">
              <h1 className="card-title">Welcome to Tours Manager</h1>
              <p className="card-subtitle">
                Your gateway to amazing tour experiences around the world
              </p>
            </div>
            <div style={{ color: 'var(--text-secondary)', lineHeight: '1.6' }}>
              <p style={{ marginBottom: '1rem' }}>
                Whether you're an experienced tour guide looking to share your expertise, 
                or a traveler seeking unforgettable adventures, you're in the right place.
              </p>
              <p>
                Get started by registering using the menu above.
              </p>
            </div>
          </div>
        )}

        {activeMenu === 'register-guide' && <GuideRegistration />}

        {activeMenu === 'register-user' && (
          <div className="card">
            <div className="card-header">
              <h2 className="card-title">Register as a User</h2>
              <p className="card-subtitle">Coming soon...</p>
            </div>
            <p style={{ color: 'var(--text-secondary)' }}>
              User registration will be available in the next update.
            </p>
          </div>
        )}
      </main>
    </>
  )
}

export default App
