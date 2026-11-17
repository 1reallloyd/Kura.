# Kura User Guide

This guide explains every screen and feature in **Kura** for shop owners, chop bar operators, and testers.

---

## 1. First Launch Flow

When you open Kura for the first time, the app walks through four brief stages:

1. **Splash Animation**: Displays the Kura saffron ledger icon and motto (*"Know who owes you."*). Tap anywhere to advance immediately or wait ~1.7 seconds.
2. **Shop & Chop Owner Welcome Screen**: Features an illustration of a shopkeeper with a yellow ledger notebook and summarizes what Kura does:
   - Track credit & cash payments at the counter
   - Send polite WhatsApp reminders
   - Stay offline & protected by your 4-digit passcode
   - Tap **Continue** (or **Skip** at the top right) to move on.
3. **Feature Slides (3 Pages)**: Swipe through *Record credit in seconds*, *Remind on WhatsApp*, and *Money at a glance*, then tap **Get started**.
4. **3-Step Setup**:
   - **Step 1 of 3 — Set up your profile**: Enter your name (required), plus optional business name, phone number, and location.
   - **Step 2 of 3 — Bring in your contacts**: Import customers or suppliers straight from your phonebook, or tap **Skip for now**.
   - **Step 3 of 3 — Protect your ledger**: Choose a 4-digit passcode and confirm it, or tap **Skip for now** to enable it later in Settings.

---

## 2. Locking & Unlocking with Passcode

- **Setting a Passcode**: During Step 3 of setup, or anytime under **Settings -> Security -> Passcode lock**, enter a 4-digit code and enter it a second time to confirm.
- **Unlock Screen**: Whenever a passcode is enabled, Kura asks for your 4-digit passcode on launch and automatically re-locks if the app stays in the background for more than **30 seconds**.
- **Failed Attempts Protection**: After 5 incorrect passcode attempts, Kura temporarily locks entry with an increasing cooldown timer (starting at 30 seconds).
- **Removing Passcode**: Go to **Settings -> Security -> Passcode lock** and confirm **Remove**.
- **Forgot Passcode?**: Tap **Forgot passcode?** on the lock screen to erase local ledger data and clear the passcode so you can start fresh or restore from a saved JSON backup.

---

## 3. Importing Contacts from Your Phonebook

Kura lets you bring in existing customers and suppliers directly from your Android phonebook:

1. Open **Import contacts** (during Step 2 of setup, from the empty ledger prompt, or via **Settings -> Your data -> Import contacts**).
2. Choose whether you are importing **As customers** or **As suppliers**.
3. Choose either:
   - **Choose from my phonebook**: Requests `READ_CONTACTS` permission, then opens a searchable multi-select sheet of your phonebook contacts so you can check multiple people and import them in one batch.
   - **Pick a contact from phonebook**: Uses Android's system contact picker to select one contact at a time without granting full list permission.
4. **Duplicate Protection**: Contacts whose last 9 phone digits already exist in Kura are automatically skipped so your ledger stays clean.

---

## 4. Managing Customers & Suppliers

### Home (Customers Tab)
- **Net Balance Card**: Shows your overall position (*Surplus*, *Deficit*, or *Even*), a receivables vs. payables progress bar, and tappable **Owed to you** / **You owe** tiles.
- **Overdue Banner**: Highlights customers who have owed a balance for more than 7 days with no new activity.
- **Search & Filters**: Search by name, phone number, or location, and filter by **All**, **Owe me**, or **I owe**.
- **Add Contact FAB**: Tap **+ Add contact** to create a new customer or supplier manually.

### Contact Detail Screen
Tap any contact card to view their balance and full transaction history:

- **For Customers**:
  - **Gave credit**: Increases what the customer owes you (`+`).
  - **Got payment**: Records money received from the customer (`-`).
  - **Remind on WhatsApp**: Appears whenever a customer owes you a positive balance. Opens a preview sheet where you can edit/save a missing phone number, launch WhatsApp with the pre-filled message, or copy the message.
- **For Suppliers**:
  - **Bought on credit**: Records stock taken on credit (`-`, increases what you owe).
  - **I paid**: Records a payment made to the supplier (`+`, reduces what you owe).
- **Tap Phone Number Pill**: Opens your phone's dialer (`ACTION_DIAL`) with their number ready.
- **Edit or Void an Entry**: Tap any active entry in **History** to save a corrected amount/note (which marks the original entry voided with strikethrough and links a replacement entry) or void it completely.
- **Delete Contact**: Moves the contact and its history to **Recently deleted** (soft delete).

---

## 5. Summary & Settings Tabs

### Summary Tab
- Displays overall **Net balance**, total **Owed to you**, and **You owe**.
- **Last 7 days**: Visual comparison of *Credit given* vs. *Payments collected* for customers.
- **Who owes you most**: Ranked bar list of your top 5 owing customers; tap any row to open their ledger.

### Settings Tab
- **Profile Card**: Tap **Edit profile** to update your name, shop/chop bar name, phone number, and location.
- **Security**: Turn **Passcode lock** On or Off.
- **Your data**:
  - **Import contacts**: Open phonebook importer.
  - **Back up data**: Copy or share your entire ledger (active contacts, trash, transactions, profile) as JSON text.
  - **Restore from backup**: Paste a previously saved Kura backup JSON to restore your ledger.
  - **Recently deleted**: Restore soft-deleted contacts back into your active ledger.
- **Preferences**: Set **Currency and country** (default `GH₵` and dialing code `+233` for WhatsApp formatting).
- **Data & reset**:
  - **Load sample data**: Populates demo Ghanaian customers and a supplier so you can test features immediately.
  - **Erase everything**: Clears all contacts and transactions from the database.
