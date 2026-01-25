import { useState, useEffect } from 'react'
import './Dashboard.css'
import AdminChatPanel from './AdminChatPanel'

const DEVICE_API = 'http://localhost/api/devices'
const USER_API_READ = 'http://localhost/api/people'  // user_microservice - GET
const USER_API_AUTH = 'http://localhost/api/auth/users'  // auth-service - POST/PUT/DELETE
const USER_API_USER = 'http://localhost/api/people'  // user_microservice - POST/PUT/DELETE
const USER_API_DEVICE = 'http://localhost/api/persons'  // device_microservice - POST/PUT/DELETE
const ASSOCIATION_API = 'http://localhost/api/user_devices'

function AdminDashboard({ token, user, onLogout }) {
  const [activeTab, setActiveTab] = useState('devices')
  const [devices, setDevices] = useState([])
  const [users, setUsers] = useState([])
  const [usersForAssociation, setUsersForAssociation] = useState([]) // Users from device microservice
  const [associations, setAssociations] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  // Device form
  const [deviceForm, setDeviceForm] = useState({ id: '', name: '', maxConsValue: '' })
  const [editingDevice, setEditingDevice] = useState(null)

  // User form
  const [userForm, setUserForm] = useState({ id: '', name: '', address: '', age: '' })
  const [editingUser, setEditingUser] = useState(null)

  // Association form
  const [associationForm, setAssociationForm] = useState({ userid: '', deviceid: '' })

  useEffect(() => {
    if (activeTab === 'devices') {
      fetchDevices()
    } else if (activeTab === 'users') {
      fetchUsers()
    } else if (activeTab === 'associations') {
      fetchAssociations()
      fetchUsersForAssociation() // Fetch users from device microservice
      fetchDevices()
    } else if (activeTab === 'chat') {
      // Chat panel handles its own data fetching
    }
  }, [activeTab])

  const fetchDevices = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(DEVICE_API, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to fetch devices')
      const data = await response.json()
      setDevices(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchUsers = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(USER_API_READ, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to fetch users')
      const data = await response.json()
      setUsers(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const fetchUsersForAssociation = async () => {
    // Fetch users from device microservice for associations
    try {
      const response = await fetch(USER_API_DEVICE, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to fetch users for association')
      const data = await response.json()
      setUsersForAssociation(data)
    } catch (err) {
      console.error('Error fetching users for association:', err)
    }
  }

  const fetchAssociations = async () => {
    setLoading(true)
    setError('')
    try {
      const response = await fetch(ASSOCIATION_API, {
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to fetch associations')
      const data = await response.json()
      setAssociations(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // Device CRUD
  const handleDeviceSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const url = editingDevice ? `${DEVICE_API}/${editingDevice}` : DEVICE_API
      const method = editingDevice ? 'PUT' : 'POST'
      
      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*'
        },
        body: JSON.stringify({
          name: deviceForm.name,
          maxConsValue: parseInt(deviceForm.maxConsValue)
        })
      })

      if (!response.ok) throw new Error(`Failed to ${editingDevice ? 'update' : 'create'} device`)
      
      setDeviceForm({ id: '', name: '', maxConsValue: '' })
      setEditingDevice(null)
      fetchDevices()
      alert(`Device ${editingDevice ? 'updated' : 'created'} successfully!`)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleDeviceEdit = (device) => {
    setDeviceForm({
      id: device.id,
      name: device.name,
      maxConsValue: device.maxConsValue
    })
    setEditingDevice(device.id)
  }

  const handleDeviceDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this device?')) return

    setLoading(true)
    try {
      const response = await fetch(`${DEVICE_API}/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to delete device')
      fetchDevices()
      alert('Device deleted successfully!')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // User CRUD - POST/PUT/DELETE to all 3 microservices
  const handleUserSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const method = editingUser ? 'PUT' : 'POST'
      
      // Generate UUID for new user to keep same ID across all services
      const userId = editingUser || crypto.randomUUID()
      
      const userData = { 
        id: userId,
        name: userForm.name,
        address: userForm.address,
        age: parseInt(userForm.age)
      }
      
      // Create/Update in all 3 microservices
      const services = [
        { name: 'auth', url: editingUser ? `${USER_API_AUTH}/${editingUser}` : USER_API_AUTH },
        { name: 'user', url: editingUser ? `${USER_API_USER}/${editingUser}` : USER_API_USER },
        { name: 'device', url: editingUser ? `${USER_API_DEVICE}/${editingUser}` : USER_API_DEVICE }
      ]

      const results = await Promise.allSettled(
        services.map(service => 
          fetch(service.url, {
            method,
            headers: {
              'Authorization': `Bearer ${token}`,
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
          }).then(async (res) => {
            if (!res.ok) {
              const errorText = await res.text()
              throw new Error(`${service.name}: ${res.status} - ${errorText}`)
            }
            const data = await res.json().catch(() => ({}))
            return { service: service.name, ok: true, data }
          })
        )
      )

      // Check results
      const succeeded = results.filter(r => r.status === 'fulfilled')
      const failed = results.filter(r => r.status === 'rejected')
      
      if (succeeded.length === 0) {
        const errors = failed.map(r => r.reason.message).join('\n')
        throw new Error(`Failed to ${editingUser ? 'update' : 'create'} user:\n${errors}`)
      }

      if (failed.length > 0) {
        const errors = failed.map(r => r.reason.message).join('\n')
        console.warn('Some services failed:', errors)
      }

      setUserForm({ id: '', name: '', address: '', age: '' })
      setEditingUser(null)
      fetchUsers()
      
      // Get password from auth-service response
      const authResult = succeeded.find(r => r.value.service === 'auth')
      const password = authResult?.value?.data?.password
      
      let message = failed.length > 0 
        ? `User ${editingUser ? 'updated' : 'created'} in ${succeeded.length}/3 services.\nFailed: ${failed.map(r => r.reason.message).join(', ')}`
        : `User ${editingUser ? 'updated' : 'created'} successfully in all 3 services!`
      
      if (!editingUser && password) {
        message += `\n\n🔑 Login credentials:\nUsername: ${userForm.name}\nPassword: ${password}\n\n⚠️ Save this password - it won't be shown again!`
      }
      
      alert(message)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleUserEdit = (user) => {
    setUserForm({ 
      id: user.id, 
      name: user.name || '', 
      address: user.address || '', 
      age: user.age || '' 
    })
    setEditingUser(user.id)
  }

  // Delete from all 3 microservices
  const handleUserDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this user?')) return
    
    try {
      const services = [
        { name: 'auth', url: `${USER_API_AUTH}/${id}` },
        { name: 'user', url: `${USER_API_USER}/${id}` },
        { name: 'device', url: `${USER_API_DEVICE}/${id}` }
      ]

      const results = await Promise.allSettled(
        services.map(service => 
          fetch(service.url, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
          }).then(async (res) => {
            if (!res.ok) {
              const errorText = await res.text()
              throw new Error(`${service.name}: ${res.status} - ${errorText}`)
            }
            return { service: service.name, ok: true }
          })
        )
      )

      const succeeded = results.filter(r => r.status === 'fulfilled')
      const failed = results.filter(r => r.status === 'rejected')
      
      if (succeeded.length === 0) {
        const errors = failed.map(r => r.reason.message).join('\n')
        throw new Error(`Failed to delete user:\n${errors}`)
      }

      fetchUsers()
      
      const message = failed.length > 0
        ? `User deleted from ${succeeded.length}/3 services.\nFailed: ${failed.map(r => r.reason.message).join(', ')}`
        : `User deleted successfully from all 3 services!`
      alert(message)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // Association CRUD
  const handleAssociationSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const response = await fetch(ASSOCIATION_API, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userid: associationForm.userid,
          deviceid: associationForm.deviceid
        })
      })

      if (!response.ok) throw new Error('Failed to create association')
      
      setAssociationForm({ userid: '', deviceid: '' })
      fetchAssociations()
      alert('Association created successfully!')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleAssociationDelete = async (id) => {
    if (!confirm('Are you sure you want to delete this association?')) return

    setLoading(true)
    try {
      const response = await fetch(`${ASSOCIATION_API}/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      })
      if (!response.ok) throw new Error('Failed to delete association')
      fetchAssociations()
      alert('Association deleted successfully!')
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const getUserName = (userid) => {
    const user = usersForAssociation.find(u => u.id === userid)
    return user ? user.name : userid
  }

  const getDeviceName = (deviceid) => {
    const device = devices.find(d => d.id === deviceid)
    return device ? device.name : deviceid
  }

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-left">
          <h1>🔐 Admin Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-info">👤 {user.username} (Admin)</span>
          <button onClick={onLogout} className="logout-btn">
            🚪 Logout
          </button>
        </div>
      </header>

      <nav className="dashboard-nav">
        <button 
          className={`nav-btn ${activeTab === 'devices' ? 'active' : ''}`}
          onClick={() => setActiveTab('devices')}
        >
          📱 Devices
        </button>
        <button 
          className={`nav-btn ${activeTab === 'users' ? 'active' : ''}`}
          onClick={() => setActiveTab('users')}
        >
          👥 Users
        </button>
        <button 
          className={`nav-btn ${activeTab === 'associations' ? 'active' : ''}`}
          onClick={() => setActiveTab('associations')}
        >
          🔗 Associations
        </button>
        <button 
          className={`nav-btn ${activeTab === 'chat' ? 'active' : ''}`}
          onClick={() => setActiveTab('chat')}
        >
          💬 Customer Support
        </button>
      </nav>

      <main className="dashboard-content">
        {error && (
          <div className="error-alert">
            <span className="error-icon">⚠️</span>
            {error}
          </div>
        )}

        {/* Devices Tab */}
        {activeTab === 'devices' && (
          <div className="tab-content">
            <h2>Device Management</h2>
            
            <form onSubmit={handleDeviceSubmit} className="crud-form">
              <h3>{editingDevice ? 'Edit Device' : 'Add New Device'}</h3>
              <div className="form-row">
                <input
                  type="text"
                  placeholder="Device Name"
                  value={deviceForm.name}
                  onChange={(e) => setDeviceForm({...deviceForm, name: e.target.value})}
                  required
                />
                <input
                  type="number"
                  placeholder="Max Consumption Value"
                  value={deviceForm.maxConsValue}
                  onChange={(e) => setDeviceForm({...deviceForm, maxConsValue: e.target.value})}
                  required
                />
              </div>
              <div className="form-actions">
                <button type="submit" disabled={loading}>
                  {editingDevice ? '💾 Update' : '➕ Add'} Device
                </button>
                {editingDevice && (
                  <button type="button" onClick={() => {
                    setEditingDevice(null)
                    setDeviceForm({ id: '', name: '', maxConsValue: '' })
                  }}>
                    ❌ Cancel
                  </button>
                )}
              </div>
            </form>

            <div className="data-table">
              <h3>All Devices ({devices.length})</h3>
              {loading ? (
                <p>Loading...</p>
              ) : devices.length === 0 ? (
                <p>No devices found. Create your first device above.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Max Consumption</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {devices.map(device => (
                      <tr key={device.id}>
                        <td>{device.id}</td>
                        <td>{device.name}</td>
                        <td>{device.maxConsValue}</td>
                        <td className="actions">
                          <button onClick={() => handleDeviceEdit(device)} className="edit-btn">
                            ✏️ Edit
                          </button>
                          <button onClick={() => handleDeviceDelete(device.id)} className="delete-btn">
                            🗑️ Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* Users Tab */}
        {activeTab === 'users' && (
          <div className="tab-content">
            <h2>User Management</h2>
            
            <form onSubmit={handleUserSubmit} className="crud-form">
              <h3>{editingUser ? 'Edit User' : 'Add New User'}</h3>
              <div className="form-row">
                <input
                  type="text"
                  placeholder="User Name"
                  value={userForm.name}
                  onChange={(e) => setUserForm({...userForm, name: e.target.value})}
                  required
                />
                <input
                  type="text"
                  placeholder="Address"
                  value={userForm.address}
                  onChange={(e) => setUserForm({...userForm, address: e.target.value})}
                  required={!editingUser}
                />
                <input
                  type="number"
                  placeholder="Age (18+)"
                  min="18"
                  value={userForm.age}
                  onChange={(e) => setUserForm({...userForm, age: e.target.value})}
                  required={!editingUser}
                />
              </div>
              <div className="form-actions">
                <button type="submit" disabled={loading}>
                  {editingUser ? '💾 Update' : '➕ Add'} User
                </button>
                {editingUser && (
                  <button type="button" onClick={() => {
                    setEditingUser(null)
                    setUserForm({ id: '', name: '', address: '', age: '' })
                  }}>
                    ❌ Cancel
                  </button>
                )}
              </div>
            </form>

            <div className="data-table">
              <h3>All Users ({users.length})</h3>
              {loading ? (
                <p>Loading...</p>
              ) : users.length === 0 ? (
                <p>No users found. Create your first user above.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Name</th>
                      <th>Address</th>
                      <th>Age</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(user => (
                      <tr key={user.id}>
                        <td>{user.id}</td>
                        <td>{user.name}</td>
                        <td>{user.address}</td>
                        <td>{user.age}</td>
                        <td className="actions">
                          <button onClick={() => handleUserEdit(user)} className="edit-btn">
                            ✏️ Edit
                          </button>
                          <button onClick={() => handleUserDelete(user.id)} className="delete-btn">
                            🗑️ Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* Associations Tab */}
        {activeTab === 'associations' && (
          <div className="tab-content">
            <h2>Device-User Associations</h2>
            
            <form onSubmit={handleAssociationSubmit} className="crud-form">
              <h3>Create New Association</h3>
              <div className="form-row">
                <select
                  value={associationForm.userid}
                  onChange={(e) => setAssociationForm({...associationForm, userid: e.target.value})}
                  required
                >
                  <option value="">Select User</option>
                  {usersForAssociation.map(user => (
                    <option key={user.id} value={user.id}>{user.name}</option>
                  ))}
                </select>
                <select
                  value={associationForm.deviceid}
                  onChange={(e) => setAssociationForm({...associationForm, deviceid: e.target.value})}
                  required
                >
                  <option value="">Select Device</option>
                  {devices.map(device => (
                    <option key={device.id} value={device.id}>{device.name}</option>
                  ))}
                </select>
              </div>
              <div className="form-actions">
                <button type="submit" disabled={loading}>
                  🔗 Create Association
                </button>
              </div>
            </form>

            <div className="data-table">
              <h3>All Associations ({associations.length})</h3>
              {loading ? (
                <p>Loading...</p>
              ) : associations.length === 0 ? (
                <p>No associations found. Create your first association above.</p>
              ) : (
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>User</th>
                      <th>Device</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {associations.map(assoc => (
                      <tr key={assoc.id}>
                        <td>{assoc.id}</td>
                        <td>{getUserName(assoc.userid)}</td>
                        <td>{getDeviceName(assoc.deviceid)}</td>
                        <td className="actions">
                          <button onClick={() => handleAssociationDelete(assoc.id)} className="delete-btn">
                            🗑️ Delete
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        )}

        {/* Customer Support Chat Tab */}
        {activeTab === 'chat' && (
          <div className="tab-content">
            <AdminChatPanel />
          </div>
        )}
      </main>
    </div>
  )
}

export default AdminDashboard
