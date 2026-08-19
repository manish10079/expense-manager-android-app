/**
 * Firebase Admin Script Template
 * 
 * Usage:
 * 1. Download your service account key file from Firebase Console:
 *    Project Settings -> Service accounts -> Generate new private key
 * 2. Save it as `service-account.json` in this folder (ignored by git).
 * 3. Run: `node index.js`
 */

const admin = require('firebase-admin');
const path = require('path');
const fs = require('fs');

const serviceAccountPath = path.join(__dirname, 'service-account.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error('❌ Error: service-account.json key file missing!');
  console.error('Please place your Firebase service account JSON key in this directory before running admin scripts.');
  process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function main() {
  console.log('🚀 Firebase Admin script initialized successfully.');

  // Example Admin Action:
  // const snapshot = await db.collection('users').get();
  // console.log(`Total users in Firestore: ${snapshot.size}`);
}

main().catch(err => {
  console.error('❌ Script execution error:', err);
  process.exit(1);
});
