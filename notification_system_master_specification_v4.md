# notification_system_master_specification_v4.md

# Expense Tracker Notification System - Master Implementation Specification

## Overview

This document defines the complete notification architecture, UX, subscription gating, recurring-rule integration, audit requirements, settings behavior, and implementation constraints for the Expense Tracker application.

## CRITICAL FIRST STEP

Before implementation, perform two audits:

### Audit 1: Existing Notification System

Already implemented:

- Budget Alert Notifications
- Daily Reminder Notifications
- Missed Entry Reminder Notifications
- Recurring rules  notifications

Audit:

- Notification channels
- WorkManager jobs
- Scheduling logic
- Notification settings
- Notification preferences
- Architecture quality
- Duplicate functionality

### Audit 2: Existing Recurring Rules System

Audit:

- Recurring rule entities
- Frequency calculations
- Due date generation
- Next occurrence calculations
- Existing recurrence engine
- Scheduling logic

Do not duplicate recurrence calculations.

---

# PRODUCT PHILOSOPHY

- Users control notification categories, not individual notification types.
- Keep the UI simple.
- All notifications enabled by default.
- Premium notifications automatically activate for Premium users.
- Notification Settings screen is the single source of truth.

---

# SUBSCRIPTION MODEL

## Free User

Access to:

1. Expense Reminders
2. Budget Alerts
3. Large Transaction Alerts
4. Weekly Summary

## Premium User

Everything in Free plus:

5. Financial Insights
6. Savings Goals
7. Bill & Subscription Reminders
8. Cloud & Security Alerts

No rewarded ads.
No temporary unlocks.

---

# NOTIFICATION SETTINGS SCREEN

A dedicated Notification Settings screen already exists.

Requirements:

- Reuse existing screen.
- Do not create another notification settings screen.
- Do not place notification controls elsewhere.
- All notification configuration must live here.

---

# FREE USER TOGGLES

## 1. Expense Reminders

Controls:

- Daily Expense Reminder
- Missed Entry Reminder

Default: Enabled

Reminder Time:
- Default 8:00 PM

### Info Button

Includes:

- Daily Expense Reminder
- Missed Entry Reminder

Purpose:
Helps users consistently record expenses.

---

## 2. Budget Alerts

Controls:

- Budget Warning
- Budget Reached
- Budget Exceeded

Default: Enabled

Internal notifications:

- 75%
- 90%
- 100%
- Exceeded

### Info Button

Purpose:
Warns users when budgets are nearing limits or exceeded.

---

## 3. Large Transaction Alerts

Controls:

- Large Expense Detection

Default: Enabled

Threshold:

- ₹1,000
- ₹5,000
- ₹10,000
- Custom

Default:
₹5,000

### Info Button

Purpose:
Alerts users when a transaction exceeds the configured threshold.

---

## 4. Weekly Summary

Controls:

- Weekly Spending Summary

Default: Enabled

Time:

Sunday 8:00 PM

### Info Button

Purpose:
Provides weekly spending insights and trends.

---

# PREMIUM USER TOGGLES

## 5. Financial Insights ⭐

Controls:

- Spending Trend Analysis
- Spending Spike Detection
- Highest Spending Category
- Spending Pattern Detection
- Financial Health Reports
- Monthly Spending Insights
- Monthly Savings Insights
- Spending Forecast
- Savings Forecast
- Budget Risk Prediction
- Smart Budget Recommendations
- Personalized Savings Suggestions

Default: Enabled

### Info Button

Explains all included insight notifications.

---

## 6. Savings Goals ⭐

Controls:

- Goal Progress
- Goal Achieved
- Goal Behind Schedule

Default: Enabled

### Info Button

Explains savings milestone notifications.

---

## 7. Bill & Subscription Reminders ⭐

Controls:

- EMI
- Rent
- Electricity
- Internet
- Insurance
- Subscription Renewals

Default: Enabled

Reminder Schedule:

- 7 Days Before
- 3 Days Before
- 1 Day Before
- Due Date

### Info Button

Explains bill and recurring-payment reminders.

---

## 8. Cloud & Security Alerts ⭐

Cloud:

- Sync Failed
- Sync Pending
- Backup Failure

Security:

- New Device Login
- Password Changed
- Email Changed
- Account Recovery Attempt

Default: Enabled

### Info Button

Explains cloud monitoring and account security notifications.

---

# RECURRING RULES INTEGRATION

IMPORTANT:

The app already contains recurring rules / recurring transactions.

The notification system must never create its own recurrence engine.

Required Flow:

Recurring Rules Engine
→ Upcoming Occurrences
→ Notification Scheduler
→ Notification Delivery

Recurring Rules remain the single source of truth.

---

# RECURRING RULE NOTIFICATIONS

Supported examples:

- Rent
- EMI
- Electricity
- Water Bill
- Internet
- Insurance
- Credit Card
- Streaming Subscription
- Loan Payment
- Custom Recurring Expenses

Reuse existing recurrence calculations.

Supported frequencies:

- Daily
- Weekly
- Monthly
- Quarterly
- Semi-Annual
- Annual
- Custom

---

# PER-RULE NOTIFICATION CONTROL

Add:

notificationsEnabled: Boolean

Default:

true

Examples:

✓ Rent Reminder Enabled

✓ EMI Reminder Enabled

✗ Netflix Reminder Disabled

Requirements:

Notification generation requires:

1. Bill & Subscription Reminders category enabled
AND
2. notificationsEnabled = true

---

# AUTOMATIC NOTIFICATION MANAGEMENT

No individual toggles for:

Financial Insights children
Savings Goal children
Cloud & Security children
Budget Alert thresholds

Parent category controls everything.

---

# DATASTORE REQUIREMENTS

Store:

- isPremium
- expenseRemindersEnabled
- budgetAlertsEnabled
- largeTransactionAlertsEnabled
- weeklySummaryEnabled
- financialInsightsEnabled
- savingsGoalsEnabled
- billRemindersEnabled
- cloudSecurityEnabled
- expenseReminderTime
- weeklySummaryTime
- largeTransactionThreshold

---

# NOTIFICATION CHANNELS

Create:

1. Budget Alerts
2. Expense Reminders
3. Weekly Reports
4. Financial Insights
5. Savings Goals
6. Bill & Subscription Reminders
7. Cloud & Security Alerts

Importance:

High:

- Budget Exceeded
- Sync Failure
- Security Events

Default:

- Reminders
- Weekly Reports
- Bill Reminders

Low:

- Insights

---

# WORKMANAGER REQUIREMENTS

Use:

- Unique Work Names
- ExistingPeriodicWorkPolicy.UPDATE

Must survive:

- Reboot
- App Updates
- Process Death

Prevent duplicate scheduling.

---

# ANALYTICS

Track:

- Notification Shown
- Notification Opened
- Notification Dismissed

Store locally.

Prepare for future Firebase Analytics integration.

---

# TESTING

Verify:

- Android 13+
- Android 14+
- Android 15+
- Permission flow
- Reboot persistence
- Offline mode
- Timezone changes
- Duplicate prevention
- Premium gating
- Scheduling accuracy
- Recurring rule integration

---

# DELIVERABLES

1. Notification Audit Report
2. Recurring Rules Audit Report
3. Existing Features List
4. Missing Features List
5. Architecture Design
6. Integration Plan
7. DataStore Changes
8. WorkManager Changes
9. UI Changes
10. Production Code
11. Migration Strategy
12. Testing Checklist
13. Future Extensibility Recommendations

---

# FINAL UI

Free Users:

- Expense Reminders
- Budget Alerts
- Large Transaction Alerts
- Weekly Summary

Premium Users:

- Financial Insights
- Savings Goals
- Bill & Subscription Reminders
- Cloud & Security Alerts

Each category must include an ⓘ Information Button explaining included notifications.
