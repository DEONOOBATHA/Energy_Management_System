import { useState, useEffect, useRef, useCallback } from 'react';
import { chatService } from '../services/chatService';
import { websocketService } from '../services/websocketService';
import './ChatWidget.css';

function ChatWidget({ userId, username }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isInitializingChat, setIsInitializingChat] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Subscribe to chat messages via WebSocket
  const addMessagesToState = useCallback((entries) => {
    if (!entries || entries.length === 0) {
      return;
    }

    setMessages((prev) => mergeChatMessages(prev, entries));
  }, []);

  useEffect(() => {
    if (!isOpen || !userId) {
      return;
    }

    let isActive = true;
    let subscription;

  const initializeChat = async () => {
  setIsInitializingChat(true);

  try {
    if (!websocketService.isConnectedToServer()) {
      await websocketService.connect();
    }

    if (!isActive) {
      return;
    }

    // FIRST: Load history completely
    const history = await chatService.getChatHistory(userId);
    if (isActive && Array.isArray(history) && history.length > 0) {
      const formattedHistory = history.flatMap(formatChatEntries);
      setMessages(formattedHistory); // Use setMessages directly, not addMessagesToState
    }

    // THEN: Subscribe to new messages
    subscription = websocketService.subscribeToChatMessages(userId, (message) => {
      console.log('📨 New WebSocket message:', message);
      addMessagesToState(formatChatEntries(message));
    });

  } catch (error) {
    console.error('❌ Failed to initialize chat widget:', error);
  } finally {
    if (isActive) {
      setIsInitializingChat(false);
    }
  }
};

    initializeChat();

    return () => {
      isActive = false;
      if (subscription && typeof subscription.unsubscribe === 'function') {
        subscription.unsubscribe();
      }
    };
  }, [isOpen, userId, addMessagesToState]);

  useEffect(() => {
    if (!isOpen) {
      setIsInitializingChat(false);
    }
  }, [isOpen]);

  const handleSendMessage = async () => {
  if (!inputMessage.trim()) return;

  const messageText = inputMessage;
  const tempId = `temp-${Date.now()}`; // ID temporar unic
  
  // Add message INSTANTLY with temporary ID
  const optimisticMessage = {
    messageId: tempId,
    text: messageText,
    sender: 'user',
    timestamp: new Date(),
    source: null,
    isOptimistic: true // Flag to identify optimistic messages
  };
  
  setMessages(prev => [...prev, optimisticMessage]);
  setInputMessage('');
  setIsLoading(true);

  try {
    const response = await chatService.sendMessage({
      userId: userId,
      username: username || 'User',
      message: messageText,
      sessionId: `session-${userId}`
    });

    console.log('✅ Message sent, WebSocket will confirm...');

    // Remove the optimistic message after a delay to let WebSocket replace it
    setTimeout(() => {
      setMessages(prev => prev.filter(msg => msg.messageId !== tempId));
    }, 5000); // Clean up after 5 seconds if WebSocket didn't arrive

  } catch (error) {
    console.error('❌ Failed to send message:', error);
    
    // Remove optimistic message and show error
    setMessages(prev => prev.filter(msg => msg.messageId !== tempId));
    setMessages(prev => [...prev, {
      text: 'Sorry, I couldn\'t process your message. Please try again.',
      sender: 'system',
      timestamp: new Date()
    }]);
  } finally {
    setIsLoading(false);
  }
};

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  return (
    <div className="chat-widget">
      {/* Chat Button */}
      {!isOpen && (
        <button 
          className="chat-button"
          onClick={() => setIsOpen(true)}
          title="Chat with Support"
        >
          💬
        </button>
      )}

      {/* Chat Window */}
      {isOpen && (
        <div className="chat-window">
          {/* Header */}
          <div className="chat-header">
            <div className="chat-header-content">
              <span className="chat-header-icon">🤖</span>
              <div>
                <h3>Customer Support</h3>
                <span className="chat-status">● Online</span>
              </div>
            </div>
            <button 
              className="chat-close"
              onClick={() => setIsOpen(false)}
            >
              ✕
            </button>
          </div>

          {/* Messages */}
          <div className="chat-messages">
            {isInitializingChat && (
              <div className="chat-message bot-message">
                <div className="message-content">
                  <p>Loading conversation...</p>
                </div>
              </div>
            )}
            {messages.length === 0 && !isInitializingChat && (
              <div className="chat-welcome">
                <p>👋 Welcome! How can I help you today?</p>
                <div className="chat-suggestions">
                  <button onClick={() => setInputMessage('How do I view my energy consumption?')}>
                    📊 View consumption
                  </button>
                  <button onClick={() => setInputMessage('What are the billing options?')}>
                    💳 Billing options
                  </button>
                  <button onClick={() => setInputMessage('How do I add a new device?')}>
                    ➕ Add device
                  </button>
                </div>
              </div>
            )}
            
            {messages.map((msg) => (
              <div 
                key={msg.messageId || `${msg.sender}-${msg.timestamp.getTime()}`}
                className={`chat-message ${msg.sender === 'user' ? 'user-message' : 'bot-message'}`}
              >
                <div className="message-content">
                  <p>{msg.text}</p>
                  {msg.source && (
                    <span className="message-source">
                      {msg.source === 'RULE_BASED' ? '🤖 Rule' : msg.source === 'ADMIN' ? '👤 Admin' : '🧠 AI'}
                    </span>
                  )}
                </div>
                <span className="message-time">
                  {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                </span>
              </div>
            ))}
            
            {isLoading && (
              <div className="chat-message bot-message">
                <div className="message-content typing-indicator">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            )}
            
            <div ref={messagesEndRef} />
          </div>

          {/* Input */}
          <div className="chat-input">
            <textarea
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="Type your message..."
              rows="1"
              disabled={isLoading}
            />
            <button 
              onClick={handleSendMessage}
              disabled={!inputMessage.trim() || isLoading}
              className="send-button"
            >
              ➤
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

export default ChatWidget;

const hasContent = (value) => typeof value === 'string' && value.trim().length > 0;
const toDate = (value) => {
  const date = value ? new Date(value) : new Date();
  return Number.isNaN(date.getTime()) ? new Date() : date;
};

const formatChatEntries = (payload = {}) => {
  if (!payload) {
    return [];
  }

  const timestamp = toDate(payload.timestamp);
  const entries = [];
  const messageIdBase = payload.messageId ? payload.messageId.toString() : undefined;
  const messageType = payload.messageType || '';

  if (messageType === 'ADMIN_TO_USER' || payload.responseSource === 'ADMIN') {
    if (hasContent(payload.message)) {
      entries.push({
        messageId: messageIdBase ? `${messageIdBase}-admin` : undefined,
        text: payload.message,
        sender: 'admin',
        timestamp,
        source: payload.responseSource || 'ADMIN'
      });
    }
    return entries;
  }

  if (hasContent(payload.message)) {
    entries.push({
      messageId: messageIdBase ? `${messageIdBase}-user` : undefined,
      text: payload.message,
      sender: 'user',
      timestamp,
      source: null
    });
  }

  if (hasContent(payload.response)) {
    entries.push({
      messageId: messageIdBase ? `${messageIdBase}-response` : undefined,
      text: payload.response,
      sender: payload.responseSource === 'ADMIN' ? 'admin' : 'bot',
      timestamp,
      source: payload.responseSource
    });
  }

  return entries;
};

const mergeChatMessages = (existingMessages, incomingEntries) => {
  if (!incomingEntries || incomingEntries.length === 0) {
    return existingMessages;
  }

  // Remove optimistic messages when real ones arrive
  const withoutOptimistic = existingMessages.filter(msg => !msg.isOptimistic);

  const existingIds = new Set(
    withoutOptimistic
      .filter((msg) => Boolean(msg.messageId))
      .map((msg) => msg.messageId)
  );

  const deduped = incomingEntries.filter(
    (entry) => !entry.messageId || !existingIds.has(entry.messageId)
  );

  if (deduped.length === 0) {
    return withoutOptimistic;
  }

  const merged = [...withoutOptimistic, ...deduped];
  merged.sort((a, b) => a.timestamp - b.timestamp);
  return merged;
};