import { useState } from 'react'
import './Login.css'

const API_BASE_URL = 'http://localhost/api'

function AdminLogin({ onLogin }) {
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

      if (!response.ok) {
        const errorData = await response.text()
        throw new Error(`Login failed: ${response.status}`)
      }

      const tokenData = await response.json()
      onLogin({ ...tokenData, role: 'admin' })

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
            <div className="logo-icon">🔐</div>
            <h1>Admin Portal</h1>
          </div>
          <p className="subtitle">System Administration</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <h2>Admin Access</h2>
          <p className="form-subtitle">Sign in with administrator credentials</p>

          {error && (
            <div className="error-alert">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <div className="form-group">
            <label htmlFor="username">
              👤 Admin Username
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="Enter admin username"
              required
              disabled={loading}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">
              🔒 Admin Password
            </label>
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                name="password"
                value={formData.password}
                onChange={handleInputChange}
                placeholder="Enter admin password"
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
                🔐 Admin Sign In
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  )
}

export default AdminLogin
