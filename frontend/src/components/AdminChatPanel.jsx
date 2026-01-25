import React, { useState, useEffect, useRef } from 'react';
import { chatService } from '../services/chatService';
import { websocketService } from '../services/websocketService';
import './AdminChatPanel.css';

const AdminChatPanel = () => {
  const [sessions, setSessions] = useState([]);
  const [selectedSession, setSelectedSession] = useState(null);
  const [chatHistory, setChatHistory] = useState([]);
  const [adminMessage, setAdminMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [sending, setSending] = useState(false);
  const chatEndRef = useRef(null);
  const selectedUserIdRef = useRef(null);

  // Load active sessions on mount
  useEffect(() => {
    loadActiveSessions();
    
    // Wait for WebSocket to connect before subscribing
    const connectAndSubscribe = async () => {
      // Ensure WebSocket is connected
      if (!websocketService.isConnectedToServer()) {
        console.log('⏳ Waiting for WebSocket connection...');
        await websocketService.connect().catch(err => {
          console.error('Failed to connect WebSocket:', err);
        });
      }
      
      // Subscribe to admin chat topic for real-time updates
      const unsubscribe = websocketService.subscribeToAdminChat((message) => {
        console.log('📨 AdminChatPanel received message:', message);
        
        // Reload sessions list to update unread counts
        loadActiveSessions();
        
        // If this message is for the currently selected user, reload their chat history
        if (selectedUserIdRef.current && message.userId === selectedUserIdRef.current) {
          console.log('🔄 Reloading chat history for selected user:', message.userId);
          reloadSelectedUserHistory();
        }
      });
      
      return unsubscribe;
    };
    
    let unsubscribe;
    connectAndSubscribe().then(unsub => {
      unsubscribe = unsub;
    });

    return () => {
      if (unsubscribe) unsubscribe();
    };
  }, []);
  
  const reloadSelectedUserHistory = async () => {
    if (!selectedUserIdRef.current) return;
    
    try {
      const history = await chatService.getAdminChatHistory(selectedUserIdRef.current);
      const formattedHistory = (history || []).map(msg => ({
        id: msg.messageId,
        message: msg.message,
        response: msg.response,
        responseSource: msg.responseSource,
        timestamp: msg.timestamp,
        isAdmin: msg.responseSource === 'ADMIN'
      }));
      console.log('✅ Reloaded chat history:', formattedHistory.length, 'messages');
      setChatHistory(formattedHistory);
    } catch (error) {
      console.error('Failed to reload chat history:', error);
    }
  };

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollToBottom();
  }, [chatHistory]);

  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const loadActiveSessions = async () => {
    try {
      setLoading(true);
      const data = await chatService.getActiveSessions();
      setSessions(data || []);
    } catch (error) {
      console.error('Failed to load sessions:', error);
    } finally {
      setLoading(false);
    }
  };

  const selectSession = async (session) => {
    console.log('🔵 Loading chat for user:', session.userId);
    setSelectedSession(session);
    selectedUserIdRef.current = session.userId;
    
    try {
      setLoading(true);
      const history = await chatService.getAdminChatHistory(session.userId);
      
      const formattedHistory = (history || []).map(msg => ({
        id: msg.messageId,
        message: msg.message,
        response: msg.response,
        responseSource: msg.responseSource,
        timestamp: msg.timestamp
      }));
      
      console.log(`✅ Loaded ${formattedHistory.length} messages, ADMIN count: ${formattedHistory.filter(m => m.responseSource === 'ADMIN').length}`);
      setChatHistory(formattedHistory);
    } catch (error) {
      console.error('Failed to load chat:', error);
      setChatHistory([]);
    } finally {
      setLoading(false);
    }
  };

  const sendAdminMessage = async (e) => {
    e.preventDefault();
    
    if (!adminMessage.trim() || !selectedUserIdRef.current) return;
    
    const userId = selectedUserIdRef.current;
    const messageText = adminMessage;
    
    try {
      setSending(true);
      await chatService.sendAdminMessage({
        userId: userId,
        message: messageText,
        adminUsername: 'Support Admin'
      });

      setAdminMessage('');
      console.log('✅ Message sent, reloading history...');
      
      // Reload history from backend after it's saved
      setTimeout(async () => {
        try {
          const history = await chatService.getAdminChatHistory(userId);
          const formattedHistory = (history || []).map(msg => ({
            id: msg.messageId,
            message: msg.message,
            response: msg.response,
            responseSource: msg.responseSource,
            timestamp: msg.timestamp,
            isAdmin: msg.responseSource === 'ADMIN'
          }));
          console.log('✅ Reloaded messages:', formattedHistory.length);
          setChatHistory(formattedHistory);
        } catch (err) {
          console.error('Failed to reload history:', err);
        }
      }, 800);
      
    } catch (error) {
      console.error('Failed to send message:', error);
      alert('Failed to send message');
    } finally {
      setSending(false);
    }
  };

  const formatTimestamp = (timestamp) => {
    if (!timestamp) return '';
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffMins < 1440) return `${Math.floor(diffMins / 60)}h ago`;
    return date.toLocaleDateString();
  };

  const getResponseSourceBadge = (source) => {
    const badges = {
      'RULE_BASED': '🤖 Bot',
      'AI_GENERATED': '🧠 AI',
      'ADMIN': '👤 Admin',
      'ADMIN_MANUAL': '👤 Admin'
    };
    return badges[source] || source;
  };

  return (
    <div className="admin-chat-panel">
      <div className="chat-sessions-sidebar">
        <div className="sessions-header">
          <h3>💬 Active Chats</h3>
        </div>

        {loading && sessions.length === 0 ? (
          <div className="sessions-loading">Loading sessions...</div>
        ) : sessions.length === 0 ? (
          <div className="no-sessions">
            <p>No active chat sessions</p>
            <span>💭</span>
          </div>
        ) : (
          <div className="sessions-list">
            {sessions.map((session) => (
              <div
                key={session.sessionId}
                className={`session-item ${selectedSession?.sessionId === session.sessionId ? 'active' : ''}`}
                onClick={() => selectSession(session)}
              >
                <div className="session-avatar">
                  {session.username?.charAt(0).toUpperCase() || 'U'}
                </div>
                <div className="session-info">
                  <div className="session-username">{session.username || 'User'}</div>
                  <div className="session-preview">
                    {session.lastMessage || 'New conversation'}
                  </div>
                  <div className="session-time">
                    {formatTimestamp(session.lastMessageTime)}
                  </div>
                </div>
                {session.unreadCount > 0 && (
                  <div className="unread-badge">{session.unreadCount}</div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="chat-conversation-panel">
        {!selectedSession ? (
          <div className="no-chat-selected">
            <span className="no-chat-icon">💬</span>
            <h3>Select a chat to start</h3>
            <p>Choose a conversation from the sidebar to view and respond</p>
          </div>
        ) : (
          <>
            <div className="chat-header">
              <div className="chat-header-info">
                <div className="chat-avatar">
                  {selectedSession.username?.charAt(0).toUpperCase() || 'U'}
                </div>
                <div>
                  <h3>{selectedSession.username || 'User'}</h3>
                  <span className="chat-user-id">ID: {selectedSession.userId?.substring(0, 8)}</span>
                </div>
              </div>
            </div>

            <div className="chat-messages">
              {loading && chatHistory.length === 0 ? (
                <div className="messages-loading">Loading messages...</div>
              ) : chatHistory.length === 0 ? (
                <div className="no-messages">
                  <p>No messages yet</p>
                </div>
              ) : (
                chatHistory.map((msg, index) => {
                  const isAdminMsg = msg.responseSource === 'ADMIN';
                  const hasMessage = msg.message && msg.message.trim();
                  const hasResponse = msg.response && msg.response.trim();
                  
                  if (index === 0) {
                    console.log('📊 Message structure sample:', {
                      messageId: msg.id,
                      responseSource: msg.responseSource,
                      isAdminMsg,
                      hasMessage,
                      hasResponse,
                      message: msg.message,
                      response: msg.response
                    });
                  }
                  
                  return (
                  <React.Fragment key={msg.id || index}>
                    {/* Admin message */}
                    {isAdminMsg && hasMessage && (
                      <div className="message admin-message">
                        <div className="message-content">
                          <div className="message-badge">👤 Admin</div>
                          <div className="message-text">{msg.message}</div>
                          <div className="message-time">
                            {formatTimestamp(msg.timestamp)}
                          </div>
                        </div>
                      </div>
                    )}
                    
                    {/* User message */}
                    {!isAdminMsg && hasMessage && (
                      <div className="message user-message">
                        <div className="message-content">
                          <div className="message-badge">👤 User</div>
                          <div className="message-text">{msg.message}</div>
                          <div className="message-time">
                            {formatTimestamp(msg.timestamp)}
                          </div>
                        </div>
                      </div>
                    )}

                    {/* Bot response */}
                    {!isAdminMsg && hasResponse && (
                      <div className="message bot-message">
                        <div className="message-content">
                          <div className="message-badge">
                            {getResponseSourceBadge(msg.responseSource)}
                          </div>
                          <div className="message-text">{msg.response}</div>
                          <div className="message-time">
                            {formatTimestamp(msg.timestamp)}
                          </div>
                        </div>
                      </div>
                    )}
                  </React.Fragment>
                  );
                })
              )}
              <div ref={chatEndRef} />
            </div>

            <form className="chat-input-form" onSubmit={sendAdminMessage}>
              <input
                type="text"
                className="chat-input"
                placeholder="Type your message to the user..."
                value={adminMessage}
                onChange={(e) => setAdminMessage(e.target.value)}
                disabled={sending}
              />
              <button 
                type="submit" 
                className="send-btn"
                disabled={!adminMessage.trim() || sending}
              >
                {sending ? '⏳' : '📤'} Send
              </button>
            </form>
          </>
        )}
      </div>
    </div>
  );
};

export default AdminChatPanel;
