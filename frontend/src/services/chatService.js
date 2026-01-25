/**
 * Chat Service for Customer Support
 * Handles communication with chat-service microservice
 */

const CHAT_API_URL = 'http://localhost/api/chat';

class ChatService {
  /**
   * Send a message to customer support
   */
  async sendMessage(messageData) {
    try {
      const response = await fetch(`${CHAT_API_URL}/send`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(messageData)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Chat message sent successfully:', data);
      return data;
    } catch (error) {
      console.error('❌ Failed to send chat message:', error);
      throw error;
    }
  }

  /**
   * Get chat history for a user
   */
  async getChatHistory(userId) {
    try {
      const response = await fetch(`${CHAT_API_URL}/history/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Chat history retrieved:', data);
      return data;
    } catch (error) {
      console.error('❌ Failed to get chat history:', error);
      throw error;
    }
  }

  /**
   * Get active chat sessions (for admin)
   */
  async getActiveSessions() {
    try {
      const response = await fetch(`${CHAT_API_URL}/admin/sessions`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Active sessions retrieved:', data);
      return data;
    } catch (error) {
      console.error('❌ Failed to get active sessions:', error);
      throw error;
    }
  }

  /**
   * Send admin message to user
   */
  async sendAdminMessage(messageData) {
    try {
      // Ensure correct DTO structure for AdminMessageDto
      const payload = {
        userId: messageData.userId,  // Target user UUID
        message: messageData.message,  // Admin's message
        adminUsername: messageData.adminUsername || 'Support Admin'  // Admin identifier
      };
      
      const response = await fetch(`${CHAT_API_URL}/admin/send`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload)
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Admin send error response:', errorText);
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Admin message sent successfully:', data);
      return data;
    } catch (error) {
      console.error('❌ Failed to send admin message:', error);
      throw error;
    }
  }

  /**
   * Get chat history for a specific user (admin)
   */
  async getAdminChatHistory(userId) {
    try {
      const response = await fetch(`${CHAT_API_URL}/history/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ Chat history retrieved for admin:', data);
      return data;
    } catch (error) {
      console.error('❌ Failed to get chat history:', error);
      throw error;
    }
  }
}

export const chatService = new ChatService();
