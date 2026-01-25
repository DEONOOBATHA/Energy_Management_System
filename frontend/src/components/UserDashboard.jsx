import { useState, useEffect, useCallback, useRef } from 'react'
import ChatWidget from './ChatWidget'
import { websocketService } from '../services/websocketService'
import './Dashboard.css'

const DEVICE_API = 'http://localhost/api/devices'
const ASSOCIATION_API = 'http://localhost/api/user_devices'
const ENERGY_API = 'http://localhost/api/energy'

function UserDashboard({ token, user, onLogout }) {
  const [devices, setDevices] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [userId, setUserId] = useState(null)
  const [energyData, setEnergyData] = useState([])
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0])
  const [loadingEnergy, setLoadingEnergy] = useState(false)
  const [energyError, setEnergyError] = useState('')
  const energyRetryTimerRef = useRef(null)
  
  // Notification states
  const [notifications, setNotifications] = useState([])
  const [showNotificationBanner, setShowNotificationBanner] = useState(false)
  const [latestNotification, setLatestNotification] = useState(null)

  useEffect(() => {
    // Get user ID from /auth/me endpoint
    const fetchUserId = async () => {
      try {
        const response = await fetch('http://localhost/api/auth/me', {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (!response.ok) throw new Error('Failed to fetch user info')
        const userData = await response.json()
        setUserId(userData.id)
      } catch (err) {
        console.error('Error fetching user ID:', err)
      }
    }
    fetchUserId()
  }, [token])

  // Subscribe to overconsumption notifications
  useEffect(() => {
    if (!userId) return;

    let isActive = true;
    let subscription;

    const connectAndSubscribeToNotifications = async () => {
      try {
        // Ensure WebSocket is connected
        if (!websocketService.isConnectedToServer()) {
          console.log('⏳ Connecting to WebSocket for notifications...');
          await websocketService.connect();
        }

        if (!isActive) return;

        // Subscribe to notifications
        subscription = websocketService.subscribeToNotifications((notification) => {
          console.log('🚨 Overconsumption notification received:', notification);
          
          if (!isActive) return;

          // Add to notifications list
          setNotifications(prev => [notification, ...prev]);
          
          // Show banner
          setLatestNotification(notification);
          setShowNotificationBanner(true);
          
          // Auto-hide banner after 10 seconds
          setTimeout(() => {
            if (isActive) {
              setShowNotificationBanner(false);
            }
          }, 10000);
        });

        console.log('✅ Subscribed to overconsumption notifications');
      } catch (error) {
        console.error('❌ Failed to subscribe to notifications:', error);
      }
    };

    connectAndSubscribeToNotifications();

    return () => {
      isActive = false;
      if (subscription && typeof subscription.unsubscribe === 'function') {
        subscription.unsubscribe();
      }
    };
  }, [userId]);

  useEffect(() => {
    return () => {
      if (energyRetryTimerRef.current) {
        clearTimeout(energyRetryTimerRef.current)
      }
    }
  }, [])

  const fetchUserDevices = async () => {
    setLoading(true)
    setError('')
    try {
      // Get the user's device associations (returns array)
      const assocResponse = await fetch(`${ASSOCIATION_API}/by_user/${userId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      
      if (!assocResponse.ok) {
        if (assocResponse.status === 404) {
          // No associations found
          setDevices([])
          return
        }
        throw new Error('Failed to fetch device associations')
      }

      const associations = await assocResponse.json()
      
      // Fetch all devices from associations
      if (Array.isArray(associations) && associations.length > 0) {
        const devicePromises = associations.map(assoc =>
          fetch(`${DEVICE_API}/${assoc.deviceid}`, {
            headers: { 'Authorization': `Bearer ${token}` }
          }).then(res => {
            if (!res.ok) throw new Error('Failed to fetch device details')
            return res.json()
          })
        )
        
        const devicesData = await Promise.all(devicePromises)
        setDevices(devicesData)
      } else {
        setDevices([])
      }

    } catch (err) {
      console.error('Error fetching devices:', err)
      setError(err.message)
      setDevices([])
    } finally {
      setLoading(false)
    }
  }

  // Alternative approach: fetch all associations and filter
  const fetchAllUserDevices = async () => {
    setLoading(true)
    setError('')
    try {
      // Get all associations
      const assocResponse = await fetch(ASSOCIATION_API, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      
      if (!assocResponse.ok) throw new Error('Failed to fetch associations')
      
      const associations = await assocResponse.json()
      
      // Filter by user ID
      const userAssociations = associations.filter(a => a.userid === userId)
      
      if (userAssociations.length === 0) {
        setDevices([])
        return
      }

      // Fetch device details for each association
      const devicePromises = userAssociations.map(async (assoc) => {
        const response = await fetch(`${DEVICE_API}/${assoc.deviceid}`, {
          headers: { 'Authorization': `Bearer ${token}` }
        })
        if (response.ok) {
          return await response.json()
        }
        return null
      })

      const deviceResults = await Promise.all(devicePromises)
      setDevices(deviceResults.filter(d => d !== null))

    } catch (err) {
      console.error('Error fetching devices:', err)
      setError(err.message)
      setDevices([])
    } finally {
      setLoading(false)
    }
  }

  const fetchEnergyData = useCallback(async (attempt = 0) => {
    if (!userId) return

    if (attempt === 0) {
      if (energyRetryTimerRef.current) {
        clearTimeout(energyRetryTimerRef.current)
        energyRetryTimerRef.current = null
      }
      setLoadingEnergy(true)
      setEnergyError('')
    }

    try {
      const response = await fetch(`${ENERGY_API}/user/${userId}?date=${selectedDate}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      })

      if (response.ok) {
        const data = await response.json()
        setEnergyData(data)
        setEnergyError('')
        setLoadingEnergy(false)
        return
      }

      if (response.status === 404) {
        setEnergyData([])
        setEnergyError('')
        setLoadingEnergy(false)
        return
      }

      throw new Error(`Energy API responded with status ${response.status}`)
    } catch (err) {
      console.error('Error fetching energy data:', err)
      if (attempt < 3) {
        energyRetryTimerRef.current = setTimeout(() => {
          fetchEnergyData(attempt + 1)
        }, 1500 * (attempt + 1))
      } else {
        setEnergyData([])
        setEnergyError('Unable to load energy data. Please try again shortly.')
        setLoadingEnergy(false)
      }
    }
  }, [userId, token, selectedDate])

  useEffect(() => {
    if (userId) {
      fetchUserDevices()
      fetchEnergyData()
    }
  }, [userId, selectedDate, fetchEnergyData])

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>👤 User Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-info">👤 {user.username}</span>
          <button onClick={onLogout} className="logout-btn">
            🚪 Logout
          </button>
        </div>
      </header>

      {/* Overconsumption Notification Banner */}
      {showNotificationBanner && latestNotification && (
        <div className="notification-banner" style={{
          position: 'fixed',
          top: '80px',
          right: '20px',
          maxWidth: '400px',
          padding: '1rem',
          background: '#fed7d7',
          border: '2px solid #fc8181',
          borderRadius: '8px',
          boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
          zIndex: 1000,
          animation: 'slideIn 0.3s ease-out'
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start' }}>
            <div>
              <h4 style={{ color: '#c53030', margin: '0 0 0.5rem 0', fontSize: '1rem' }}>
                🚨 Overconsumption Alert!
              </h4>
              <p style={{ margin: '0', fontSize: '0.875rem', color: '#742a2a' }}>
                Device exceeded limit: <strong>{latestNotification.consumedValue?.toFixed(2)} kWh</strong>
                {' '}(max: {latestNotification.maxAllowedValue} kWh)
              </p>
              <p style={{ margin: '0.25rem 0 0 0', fontSize: '0.75rem', color: '#9b2c2c' }}>
                Device ID: {latestNotification.deviceId}
              </p>
            </div>
            <button 
              onClick={() => setShowNotificationBanner(false)}
              style={{
                background: 'none',
                border: 'none',
                fontSize: '1.25rem',
                cursor: 'pointer',
                color: '#c53030',
                padding: '0',
                marginLeft: '1rem'
              }}
            >
              ✕
            </button>
          </div>
        </div>
      )}

      <main className="dashboard-content">
        <div className="tab-content">
          <h2>My Devices</h2>
          
          {error && (
            <div className="error-alert">
              <span className="error-icon">⚠️</span>
              {error}
            </div>
          )}

          <div className="data-table">
            {loading ? (
              <div className="loading-state">
                <span className="spinner"></span>
                <p>Loading your devices...</p>
              </div>
            ) : devices.length === 0 ? (
              <div className="empty-state">
                <div className="empty-icon">📱</div>
                <h3>No Devices Assigned</h3>
                <p>You don't have any devices assigned to your account yet.</p>
                <p>Please contact your administrator to assign devices.</p>
              </div>
            ) : (
              <>
                <h3>Assigned Devices ({devices.length})</h3>
                <div className="device-grid">
                  {devices.map(device => (
                    <div key={device.id} className="device-card">
                      <div className="device-card-header">
                        <div className="device-icon">📱</div>
                        <h4>{device.name}</h4>
                      </div>
                      <div className="device-card-body">
                        <div className="device-detail">
                          <span className="detail-label">Max Consumption:</span>
                          <span className="detail-value">{device.maxConsValue} kWh</span>
                        </div>
                        <div className="device-detail">
                          <span className="detail-label">Device ID:</span>
                          <span className="detail-value small">{device.id}</span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </>
            )}
          </div>

          {/* Energy Consumption Section */}
          <div className="energy-section" style={{marginTop: '3rem'}}>
            <div className="energy-header">
              <h2>⚡ Energy Consumption</h2>
              <div className="date-picker">
                <label htmlFor="date-select">Select Date: </label>
                <input 
                  id="date-select"
                  type="date" 
                  value={selectedDate} 
                  onChange={(e) => setSelectedDate(e.target.value)}
                  max={new Date().toISOString().split('T')[0]}
                  style={{padding: '0.5rem', borderRadius: '4px', border: '1px solid #ddd'}}
                />
                <button 
                  onClick={fetchEnergyData}
                  className="refresh-btn"
                  style={{marginLeft: '1rem'}}
                  disabled={loadingEnergy}
                >
                  🔄 Refresh
                </button>
              </div>
              {energyError && (
                <div className="error-alert" style={{ marginTop: '1rem' }}>
                  <span className="error-icon">⚠️</span>
                  {energyError}
                </div>
              )}
            </div>

            {loadingEnergy ? (
              <div className="loading-state" style={{marginTop: '2rem'}}>
                <span className="spinner"></span>
                <p>Loading energy data...</p>
              </div>
            ) : energyData.length === 0 ? (
              <div className="empty-state" style={{marginTop: '2rem'}}>
                <div className="empty-icon">📊</div>
                <h3>No Energy Data</h3>
                <p>No energy consumption data available for {selectedDate}</p>
              </div>
            ) : (
              <div className="energy-chart" style={{marginTop: '2rem'}}>
                <h3>Hourly Consumption ({energyData.length} readings)</h3>
                <div className="chart-container" style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(24, 1fr)',
                  gap: '4px',
                  padding: '2rem 1rem',
                  background: '#f7fafc',
                  borderRadius: '8px',
                  alignItems: 'end',
                  height: '300px'
                }}>
                  {(() => {
                    // Calculate total per hour first
                    const hourlyTotals = Array.from({length: 24}, (_, hour) => {
                      const hourData = energyData.filter(d => d.hour === hour)
                      return hourData.reduce((sum, d) => sum + d.energyValue, 0)
                    })
                    
                    // Find max total across all hours
                    const maxValue = Math.max(...hourlyTotals, 1)
                    
                    return Array.from({length: 24}, (_, hour) => {
                      const hourData = energyData.filter(d => d.hour === hour)
                      const total = hourData.reduce((sum, d) => sum + d.energyValue, 0)
                      const height = (total / maxValue) * 100
                      
                      return (
                      <div 
                        key={hour}
                        style={{
                          display: 'flex',
                          flexDirection: 'column',
                          alignItems: 'center',
                          height: '100%',
                          justifyContent: 'flex-end'
                        }}
                        title={`Hour ${hour}: ${total.toFixed(2)} kWh`}
                      >
                        <div style={{
                          width: '100%',
                          height: `${height}%`,
                          background: total > 0 
                            ? (hour >= 6 && hour <= 22 ? '#4299e1' : '#805ad5')
                            : '#e2e8f0',
                          borderRadius: '4px 4px 0 0',
                          transition: 'all 0.3s',
                          cursor: 'pointer',
                          minHeight: total > 0 ? '4px' : '2px'
                        }}></div>
                        <span style={{
                          fontSize: '0.65rem',
                          marginTop: '4px',
                          color: '#4a5568',
                          fontWeight: hour % 3 === 0 ? 'bold' : 'normal'
                        }}>
                          {hour % 3 === 0 ? hour : ''}
                        </span>
                      </div>
                      )
                    })
                  })()}
                </div>
                
                <div className="energy-stats" style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                  gap: '1rem',
                  marginTop: '2rem'
                }}>
                  <div style={{
                    padding: '1rem',
                    background: 'white',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0'
                  }}>
                    <div style={{fontSize: '0.875rem', color: '#718096'}}>Total Consumption</div>
                    <div style={{fontSize: '1.5rem', fontWeight: 'bold', color: '#2d3748'}}>
                      {energyData.reduce((sum, d) => sum + d.energyValue, 0).toFixed(2)} kWh
                    </div>
                  </div>
                  <div style={{
                    padding: '1rem',
                    background: 'white',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0'
                  }}>
                    <div style={{fontSize: '0.875rem', color: '#718096'}}>Average/Hour</div>
                    <div style={{fontSize: '1.5rem', fontWeight: 'bold', color: '#2d3748'}}>
                      {(energyData.reduce((sum, d) => sum + d.energyValue, 0) / 24).toFixed(2)} kWh
                    </div>
                  </div>
                  <div style={{
                    padding: '1rem',
                    background: 'white',
                    borderRadius: '8px',
                    border: '1px solid #e2e8f0'
                  }}>
                    <div style={{fontSize: '0.875rem', color: '#718096'}}>Peak Hour</div>
                    <div style={{fontSize: '1.5rem', fontWeight: 'bold', color: '#2d3748'}}>
                      {energyData.length > 0 
                        ? energyData.reduce((max, d) => d.energyValue > max.energyValue ? d : max, energyData[0]).hour + ':00'
                        : 'N/A'}
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>

          <div className="info-card" style={{marginTop: '2rem'}}>
            <h3 style={{color: '#1a202c'}}>ℹ️ Information</h3>
            <p style={{color: '#4a5568'}}><strong style={{color: '#2d3748'}}>User ID:</strong> {userId || 'Loading...'}</p>
            <p style={{color: '#4a5568'}}><strong style={{color: '#2d3748'}}>Username:</strong> {user.username}</p>
            <p style={{color: '#4a5568'}}><strong style={{color: '#2d3748'}}>Session Expires:</strong> {new Date(user.expiresAt).toLocaleString()}</p>
          </div>

          <div className="refresh-section">
            <button 
              onClick={() => userId && fetchAllUserDevices()} 
              className="refresh-btn"
              disabled={loading || !userId}
            >
              🔄 Refresh Devices
            </button>
          </div>
        </div>
      </main>

      {/* Chat Widget */}
      {userId && (
        <ChatWidget 
          userId={userId} 
          username={user.username} 
        />
      )}
    </div>
  )
}

export default UserDashboard