# Firebase Scripts (Admin Node.js environment)

This directory contains standalone Node.js admin scripts for managing Firebase resources (Firestore database, Auth users, ProPass codes, analytics, etc.) using `firebase-admin`.

## 📁 Directory Structure
- `index.js`: Main entry script or CLI runner.
- `package.json`: Dependencies for Node.js admin scripts (`firebase-admin`).
- `.gitignore`: Ensures service account credentials (`service-account.json`) and `node_modules/` are kept private and ignored.

## 🚀 Setup & Execution

1. Navigate to this directory:
   ```bash
   cd firebase-script
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Place your Firebase service account JSON key file as `service-account.json` in this folder (never commit this file to git).

4. Run a script:
   ```bash
   node index.js
   ```
