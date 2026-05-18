package com.mkn0079.expensetracker.monetization

/**
 * Manages temporary feature unlocks (e.g., from watching ads).
 * In the professional Global Pass version, we rely on persistence.
 */
object AdAccessStore {
    // We keep this object for backward compatibility with the existing architecture,
    // but the actual logic is now delegated to the persistent MonetizationDataStore
    // via the Repository.
}
