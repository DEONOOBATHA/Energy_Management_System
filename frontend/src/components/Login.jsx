import { useState } from 'react'
import './Login.css'

const API_BASE_URL = 'http://localhost:8081'

function Login({ onLogin }) {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [showPassword, setShowPassword] = useState(false)

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // Clear error when user starts typing
    if (error) setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    if (!formData.username.trim() || !formData.password.trim()) {
      setError('Please fill in all fields')
      return
    }

    setLoading(true)
    setError('')

    try {
      console.log('Attempting login with:', { username: formData.username })
      
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: formData.username,
          password: formData.password
        })
      })

      console.log('Login response status:', response.status)

      if (!response.ok) {
        const errorData = await response.text()
        console.error('Login failed:', errorData)
        throw new Error(`Login failed: ${response.status} - ${errorData}`)
      }

      const tokenData = await response.json()
      console.log('Login successful, token data:', { 
        username: tokenData.username, 
        expiresAt: tokenData.expiresAt,
        tokenLength: tokenData.jwtToken?.length 
      })

      // Call parent component's login handler
      onLogin(tokenData)

    } catch (err) {
      console.error('Login error:', err)
      setError(err.message || 'Login failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const togglePasswordVisibility = () => {
    setShowPassword(!showPassword)
  }

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <div className="logo">
            <div className="logo-icon">🔧</div>
            <h1>SD Microservices</h1>
          </div>
          <p className="subtitle">Device & User Management Platform</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <h2>Welcome Back</h2>
          <p className="form-subtitle">Please sign in to your account</p>

          {error && (
            <div className="error-alert">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">
              👤 Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="Enter your username"
              required
              disabled={loading}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">
              🔒 Password
            </label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="Enter your password"
                required
                disabled={loading}
                autoComplete="current-password"
              />
              <button
                type="button"
                className="password-toggle"
                onClick={togglePasswordVisibility}
                disabled={loading}
              >
                {showPassword ? '👁️' : '👁️‍🗨️'}
              </button>
            </div>
          </div>

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? (
              <>
                <span className="spinner"></span>
                Signing In...
              </>
            ) : (
              <>
                🚀 Sign In
              </>
            )}
          </button>
        </form>

        <div className="api-info">
          <h3>🔧 API Configuration</h3>
          <div className="api-details">
            <p><strong>Base URL:</strong> {API_BASE_URL}</p>
            <p><strong>Endpoint:</strong> POST /auth/login</p>
            <p><strong>Expected Request:</strong></p>
            <pre>{JSON.stringify({
              username: "your-username",
              password: "your-password"
            }, null, 2)}</pre>
            <p><strong>Expected Response:</strong></p>
            <pre>{JSON.stringify({
              jwtToken: "eyJhbGciOiJIUzI1NiIs...",
              username: "user",
              expiresAt: 1638360000000
            }, null, 2)}</pre>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Login