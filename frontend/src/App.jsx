import { useState, useEffect } from 'react'
import UserLogin from './components/UserLogin'
import AdminLogin from './components/AdminLogin'
import UserDashboard from './components/UserDashboard'
import AdminDashboard from './components/AdminDashboard'
import NotificationCenter from './components/NotificationCenter'
import { websocketService } from './services/websocketService'
import './App.css'

function App() {
  const [token, setToken] = useState(localStorage.getItem('jwtToken'))
  const [user, setUser] = useState(JSON.parse(localStorage.getItem('user') || 'null'))
  const [loginType, setLoginType] = useState('user') // 'user' or 'admin'

  // Connect WebSocket on mount
  useEffect(() => {
    websocketService.connect()
      .then(() => {
        websocketService.subscribeToNotifications((notification) => {
          // Pass notification to NotificationCenter
          if (window.notificationCenter) {
            window.notificationCenter.addNotification(notification)
          }
        })
      })
      .catch((err) => {
        console.error('Failed to connect WebSocket:', err)
      })

    return () => {
      websocketService.disconnect()
    }
  }, [])

  const handleLogin = (tokenData) => {
    setToken(tokenData.jwtToken)
    setUser({ 
      username: tokenData.username, 
      expiresAt: tokenData.expiresAt,
      role: tokenData.role 
    })
    localStorage.setItem('jwtToken', tokenData.jwtToken)
    localStorage.setItem('user', JSON.stringify({ 
      username: tokenData.username, 
      expiresAt: tokenData.expiresAt,
      role: tokenData.role 
    }))
  }

  const handleLogout = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem('jwtToken')
    localStorage.removeItem('user')
  }

  // Render login screen
  if (!token) {
    return (
      <div className="App">
        <NotificationCenter />
        <div className="login-type-selector">
          <button 
            className={`type-btn ${loginType === 'user' ? 'active' : ''}`}
            onClick={() => setLoginType('user')}
          >
            👤 User Login
          </button>
          <button 
            className={`type-btn ${loginType === 'admin' ? 'active' : ''}`}
            onClick={() => setLoginType('admin')}
          >
            🔐 Admin Login
          </button>
        </div>
        
        {loginType === 'user' ? (
          <UserLogin onLogin={handleLogin} />
        ) : (
          <AdminLogin onLogin={handleLogin} />
        )}
      </div>
    )
  }

  // Render dashboard based on role
  return (
    <div className="App">
      <NotificationCenter />
      {user.role === 'admin' ? (
        <AdminDashboard token={token} user={user} onLogout={handleLogout} />
      ) : (
        <UserDashboard token={token} user={user} onLogout={handleLogout} />
      )}
    </div>
  )
}

export default App
