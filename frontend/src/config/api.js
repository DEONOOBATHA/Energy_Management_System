// API Configuration
export const API_CONFIG = {
  AUTH_API: import.meta.env.VITE_AUTH_API || 'http://localhost:8081',
  DEVICE_API: import.meta.env.VITE_DEVICE_API || 'http://localhost:8082/devices',
  USER_API: import.meta.env.VITE_USER_API || 'http://localhost:8082/persons',
  ASSOCIATION_API: import.meta.env.VITE_ASSOCIATION_API || 'http://localhost:8082/user_devices',
}

export default API_CONFIG
