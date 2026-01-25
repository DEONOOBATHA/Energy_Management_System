package com.sscl.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Rule-Based Customer Support Chatbot
 * Implements 10+ rules for common customer questions.
 * Non-matching questions receive intelligent contextual responses.
 */
@Service
@Slf4j
public class RuleBasedEngine {

    private final Map<Pattern, String> rules;

    public RuleBasedEngine() {
        this.rules = new HashMap<>();
        initializeRules();
    }

    private void initializeRules() {
        // Rule 1: Greeting
        addRule("(hello|hi|hey|hei|salut|buna|greetings|good morning)", 
                "Hello! Welcome to Energy Management System support. How can I help you today?");

        // Rule 2: Login/Password
        addRule("(login|password|parola|account|sign in|forgot password|am uitat parola)", 
                "For login issues, use the 'Forgot Password' link on the login page or contact your administrator.");

        // Rule 3: Add Device
        addRule("(add device|new device|register device|device setup)", 
                "To add a device: Dashboard → Devices → 'Add New Device'. Enter device name, location, and consumption limit.");

        // Rule 4: View Consumption
        addRule("(consumption|usage|energy data|view data|check consumption|consumption details)", 
                "View consumption in Dashboard → 'Energy Consumption'. You can filter by date to see trends.");

        // Rule 5: Alerts
        addRule("(alert|overconsumption|exceed limit|warning|notification|alarm)", 
                "Real-time alerts notify you when consumption exceeds device limits. Adjust limits in Device Settings.");

        // Rule 6: Device Limits
        addRule("(device limit|max consumption|maximum|set limit|consumption limit)", 
                "Set device limits in Dashboard → Devices → Select Device → Edit. Maximum daily consumption is displayed.");

        // Rule 7: Admin Contact
        addRule("(contact admin|administrator|support|help|speak admin|admin contact)", 
                "You can reach an administrator via this chat. An admin will respond to your message shortly.");

        // Rule 8: System Status
        addRule("(status|working|down|problem|issue|error|not working)", 
                "System status is operational. For technical issues, please describe the problem and we'll help.");

        // Rule 9: How to use system
        addRule("(how.*use|tutorial|guide|help|instructions|what.*do)", 
                "Check the Help section in the Dashboard for tutorials and guides on using the EMS system.");

        // Rule 10: Goodbye
        addRule("(goodbye|bye|exit|quit|thank you|thanks|cu plăcere|mulțumesc)", 
                "Thank you for using Energy Management System. Feel free to contact us anytime!");

        log.info("Initialized {} rule-based patterns", rules.size());
    }

    private void addRule(String pattern, String response) {
        this.rules.put(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE), response);
    }

    /**
     * Process message and return response if rule matches
     * Falls back to intelligent contextual response for non-matching messages
     */
    public String processMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "Please provide a message so I can help you.";
        }

        // Try to match predefined rules
        for (Map.Entry<Pattern, String> entry : rules.entrySet()) {
            if (entry.getKey().matcher(message).find()) {
                log.info("Rule matched for message: '{}'", message);
                return entry.getValue();
            }
        }

        // No rule matched - try intelligent processing
        log.info("No rule matched for message: '{}'. Attempting intelligent response.", message);
        return "Your message has been sent to an AI entity. They will respond shortly";
    }

}
