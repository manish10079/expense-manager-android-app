const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");

// Initialize Admin SDK with your project credentials
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

// Replace this with the actual UID from Firebase Console Authentication tab
const reviewerUid = "2cc9FrlHqiYXVs4wpXfQNtGMZe03";

admin.auth().updateUser(reviewerUid, {
  emailVerified: true // Forces the user to be verified
})
.then(() => {
  console.log("Success! Reviewer email is now marked as verified.");
  process.exit(0);
})
.catch((error) => {
  console.error("Error updating user:", error);
  process.exit(1);
});