import { useState, useEffect } from 'react'
import './Notifications.css'

function NotificationCenter() {
  const [notifications, setNotifications] = useState([])

  // Add new notification
  const addNotification = (notification) => {
    const id = Date.now()
    const newNotif = {
      id,
      ...notification,
      timestamp: new Date().toLocaleTimeString(),
    }
    
    setNotifications((prev) => [newNotif, ...prev].slice(0, 10)) // Keep last 10

    // Auto-remove after 10 seconds
    setTimeout(() => {
      setNotifications((prev) => prev.filter((n) => n.id !== id))
    }, 10000)
  }

  // Expose to window for WebSocket service
  useEffect(() => {
    window.notificationCenter = { addNotification }
  }, [])

  return (
    <div className="notification-center">
      {notifications.length > 0 && (
        <div className="notifications-container">
          {notifications.map((notif) => (
            <div key={notif.id} className="notification alert-warning">
              <div className="notification-header">
                <span className="notification-icon">⚠️</span>
                <span className="notification-time">{notif.timestamp}</span>
                <button
                  className="notification-close"
                  onClick={() =>
                    setNotifications((prev) =>
                      prev.filter((n) => n.id !== notif.id)
                    )
                  }
                >
                  ✕
                </button>
              </div>
              <div className="notification-body">
                <h4>Overconsumption Alert</h4>
                <p>
                  <strong>Device:</strong> {notif.deviceId}
                </p>
                <p>
                  <strong>Consumed:</strong> {notif.consumedValue?.toFixed(2)} kWh
                </p>
                <p>
                  <strong>Max Allowed:</strong> {notif.maxAllowedValue} kWh
                </p>
                {notif.timestamp && (
                  <p>
                    <small>
                      <strong>Time:</strong> {notif.timestamp}
                    </small>
                  </p>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default NotificationCenter
