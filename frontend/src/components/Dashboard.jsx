import { useState, useEffect } from 'react'
import './Dashboard.css'

const API_BASE_URL = 'http://localhost:8081'

function Dashboard({ token, user, onLogout }) {
  const [activeTab, setActiveTab] = useState('profile')
  const [tokenInfo, setTokenInfo] = useState(null)

  useEffect(() => {
    if (token) {
      try {
        // Decode JWT token to show info (basic decoding without verification)
        const payload = JSON.parse(atob(token.split('.')[1]))
        setTokenInfo(payload)
      } catch (err) {
        console.error('Error decoding token:', err)
      }
    }
  }, [token])

  const handleTestEndpoint = async (endpoint) => {
    try {
      const response = await fetch(`${API_BASE_URL}${endpoint}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      })

      const data = await response.text()
      alert(`Response from ${endpoint}:\nStatus: ${response.status}\nData: ${data}`)
    } catch (err) {
      alert(`Error calling ${endpoint}: ${err.message}`)
    }
  }

  const copyTokenToClipboard = () => {
    navigator.clipboard.writeText(token)
    alert('Token copied to clipboard!')
  }

  const formatTimestamp = (timestamp) => {
    return new Date(timestamp * 1000).toLocaleString()
  }

  return (
    <div className="dashboard-container">
      {/* Header */}
      <header className="dashboard-header">
        <div className="header-left">
          <h1>🔧 SD Microservices Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-info">👤 {user.username}</span>
          <button onClick={onLogout} className="logout-btn">
            🚪 Logout
          </button>
        </div>
      </header>

      {/* Navigation */}
      <nav className="dashboard-nav">
        <button 
          className={`nav-btn ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          👤 Profile
        </button>
        <button 
          className={`nav-btn ${activeTab === 'token' ? 'active' : ''}`}
          onClick={() => setActiveTab('token')}
        >
          🔑 JWT Token
        </button>
        <button 
          className={`nav-btn ${activeTab === 'api' ? 'active' : ''}`}
          onClick={() => setActiveTab('api')}
        >
          🌐 API Testing
        </button>
      </nav>

      {/* Content */}
      <main className="dashboard-content">
        {activeTab === 'profile' && (
          <div className="tab-content">
            <h2>User Profile</h2>
            <div className="info-card">
              <div className="info-row">
                <span className="info-label">Username:</span>
                <span className="info-value">{user.username}</span>
              </div>
              <div className="info-row">
                <span className="info-label">Session Expires:</span>
                <span className="info-value">
                  {new Date(user.expiresAt).toLocaleString()}
                </span>
              </div>
              <div className="info-row">
                <span className="info-label">Login Time:</span>
                <span className="info-value">
                  {new Date().toLocaleString()}
                </span>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'token' && (
          <div className="tab-content">
            <h2>JWT Token Information</h2>
            
            <div className="token-section">
              <h3>🔑 Token</h3>
              <div className="token-display">
                <textarea 
                  value={token} 
                  readOnly 
                  rows="4"
                  className="token-textarea"
                />
                <button onClick={copyTokenToClipboard} className="copy-btn">
                  📋 Copy Token
                </button>
              </div>
            </div>

            {tokenInfo && (
              <div className="token-section">
                <h3>📊 Token Payload</h3>
                <div className="info-card">
                  <div className="info-row">
                    <span className="info-label">Subject (User):</span>
                    <span className="info-value">{tokenInfo.sub}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Issuer:</span>
                    <span className="info-value">{tokenInfo.iss}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Issued At:</span>
                    <span className="info-value">{formatTimestamp(tokenInfo.iat)}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Expires At:</span>
                    <span className="info-value">{formatTimestamp(tokenInfo.exp)}</span>
                  </div>
                  <div className="info-row">
                    <span className="info-label">Scope:</span>
                    <span className="info-value">{tokenInfo.scope || 'N/A'}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'api' && (
          <div className="tab-content">
            <h2>API Endpoint Testing</h2>
            <p>Test various microservice endpoints with your JWT token:</p>

            <div className="api-section">
              <h3>🔐 Auth Service</h3>
              <button 
                onClick={() => handleTestEndpoint('/auth/validate')}
                className="test-btn"
              >
                Test Token Validation
              </button>
            </div>

            <div className="api-section">
              <h3>👥 User Service</h3>
              <button 
                onClick={() => handleTestEndpoint('/people')}
                className="test-btn"
              >
                Get All People
              </button>
            </div>

            <div className="api-section">
              <h3>📱 Device Service</h3>
              <div className="button-group">
                <button 
                  onClick={() => handleTestEndpoint('/devices')}
                  className="test-btn"
                >
                  Get All Devices
                </button>
                <button 
                  onClick={() => handleTestEndpoint('/persons')}
                  className="test-btn"
                >
                  Get All Persons
                </button>
                <button 
                  onClick={() => handleTestEndpoint('/user_devices')}
                  className="test-btn"
                >
                  Get User Devices
                </button>
              </div>
            </div>

            <div className="api-info">
              <h3>📡 API Configuration</h3>
              <div className="info-card">
                <div className="info-row">
                  <span className="info-label">Base URL:</span>
                  <span className="info-value">{API_BASE_URL}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Authorization:</span>
                  <span className="info-value">Bearer {token.substring(0, 20)}...</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default Dashboard