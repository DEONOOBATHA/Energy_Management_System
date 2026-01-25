/**
 * WebSocket Service for Real-Time Notifications
 * Connects to the WebSocket microservice and handles subscriptions
 */
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
  constructor() {
    this.stompClient = null;
    this.isConnected = false;
    this.connectingPromise = null;
    this.subscriptions = [];
    this.onNotification = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectDelay = 3000;
  }

  /**
   * Connect to WebSocket server
   */
  connect() {
    if (this.isConnected) {
      return Promise.resolve(true);
    }

    if (this.connectingPromise) {
      return this.connectingPromise;
    }

    this.connectingPromise = new Promise((resolve, reject) => {
      try {
        // Use SockJS for WebSocket with fallback support
        // Connect through nginx proxy at /ws endpoint
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable default debug logging
        this.stompClient.debug = () => {};

        this.stompClient.connect(
          {},
          (frame) => {
            console.log('✅ WebSocket Connected:', frame);
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this.connectingPromise = null;
            resolve(true);
          },
          (error) => {
            console.error('❌ WebSocket Connection Error:', error);
            this.isConnected = false;
            this.connectingPromise = null;
            this.handleConnectionError(reject);
          }
        );
        
        // Handle unexpected disconnections
        socket.onclose = () => {
          console.warn('⚠️ WebSocket closed unexpectedly');
          this.isConnected = false;
          this.connectingPromise = null;
          this.attemptReconnect();
        };
        
      } catch (err) {
        console.error('❌ WebSocket Setup Error:', err);
        this.connectingPromise = null;
        reject(err);
      }
    });

    return this.connectingPromise;
  }
  
  /**
   * Handle connection errors with reconnection logic
   */
  handleConnectionError(reject) {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      const delay = this.reconnectDelay * this.reconnectAttempts;
      console.log(`⏳ Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      setTimeout(() => {
        if (!this.connectingPromise) {
          this.connect().catch(() => {
            // Silently continue retrying
          });
        }
      }, delay);
    } else {
      console.error('❌ Max reconnection attempts reached');
      if (reject) {
        reject(new Error('WebSocket connection failed after multiple attempts'));
      }
    }
  }
  
  /**
   * Attempt automatic reconnection
   */
  attemptReconnect() {
    if (!this.isConnected && !this.connectingPromise && this.reconnectAttempts < this.maxReconnectAttempts) {
      console.log('🔄 Attempting to reconnect WebSocket...');
      this.reconnectAttempts++;
      setTimeout(() => {
        this.connect().catch(() => {
          // Continue retrying
        });
      }, this.reconnectDelay);
    }
  }

  /**
   * Subscribe to notifications topic
   */
  subscribeToNotifications(callback) {
    if (!this.isConnected) {
      console.warn('⚠️  WebSocket not connected. Cannot subscribe.');
      return;
    }

    const subscription = this.stompClient.subscribe(
      '/topic/notifications',
      (message) => {
        try {
          const notification = JSON.parse(message.body);
          console.log('📢 Received notification:', notification);
          if (callback) {
            callback(notification);
          }
        } catch (err) {
          console.error('Error parsing notification:', err);
        }
      },
      (error) => {
        console.error('Subscription error:', error);
      }
    );

    this.subscriptions.push(subscription);
    console.log('✅ Subscribed to /topic/notifications');
    return subscription;
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.stompClient && this.isConnected) {
      this.stompClient.disconnect(() => {
        console.log('✅ WebSocket Disconnected');
        this.isConnected = false;
      });
    }
  }

  /**
   * Check if connected
   */
  isConnectedToServer() {
    return this.isConnected;
  }

  /**
   * Unsubscribe from all subscriptions
   */
  unsubscribeAll() {
    this.subscriptions.forEach((sub) => {
      if (sub) {
        sub.unsubscribe();
      }
    });
    this.subscriptions = [];
  }

  /**
   * Subscribe to chat messages for a specific user
   */
  subscribeToChatMessages(userId, callback) {
    if (!this.isConnected) {
      console.warn('⚠️  WebSocket not connected. Cannot subscribe to chat.');
      return;
    }

    const topic = `/topic/chat/${userId}`;
    const subscription = this.stompClient.subscribe(
      topic,
      (message) => {
        try {
          const chatMessage = JSON.parse(message.body);
          console.log('💬 Received chat message:', chatMessage);
          if (callback) {
            callback(chatMessage);
          }
        } catch (err) {
          console.error('Error parsing chat message:', err);
        }
      },
      (error) => {
        console.error('Chat subscription error:', error);
      }
    );

    this.subscriptions.push(subscription);
    console.log(`✅ Subscribed to ${topic}`);
    return subscription;
  }

  /**
   * Subscribe to all chat messages (for admin monitoring)
   */
  subscribeToAllChats(callback) {
    if (!this.isConnected) {
      console.warn('⚠️  WebSocket not connected. Cannot subscribe to all chats.');
      return;
    }

    const topic = '/topic/chat/#';
    const subscription = this.stompClient.subscribe(
      topic,
      (message) => {
        try {
          const chatMessage = JSON.parse(message.body);
          console.log('💬 Received admin chat message:', chatMessage);
          if (callback) {
            callback(chatMessage);
          }
        } catch (err) {
          console.error('Error parsing admin chat message:', err);
        }
      },
      (error) => {
        console.error('Admin chat subscription error:', error);
      }
    );

    this.subscriptions.push(subscription);
    console.log(`✅ Admin subscribed to all chat topics`);
    return () => {
      subscription.unsubscribe();
      console.log('❌ Admin unsubscribed from all chat topics');
    };
  }

  /**
   * Subscribe to admin chat topic for real-time session updates
   */
  subscribeToAdminChat(callback) {
    if (!this.isConnected) {
      console.warn('⚠️  WebSocket not connected. Cannot subscribe to admin chat.');
      return;
    }

    const topic = '/topic/chat/admin';
    const subscription = this.stompClient.subscribe(
      topic,
      (message) => {
        try {
          const chatMessage = JSON.parse(message.body);
          console.log('📊 Admin received message:', chatMessage);
          if (callback) {
            callback(chatMessage);
          }
        } catch (err) {
          console.error('Error parsing admin message:', err);
        }
      },
      (error) => {
        console.error('Admin chat subscription error:', error);
      }
    );

    this.subscriptions.push(subscription);
    console.log(`✅ Subscribed to admin chat topic`);
    return () => {
      subscription.unsubscribe();
      console.log('❌ Unsubscribed from admin chat topic');
    };
  }
}

// Create singleton instance
export const websocketService = new WebSocketService();
