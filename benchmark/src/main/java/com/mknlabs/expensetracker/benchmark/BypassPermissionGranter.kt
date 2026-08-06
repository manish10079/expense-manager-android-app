package com.mknlabs.expensetracker.benchmark

import androidx.test.internal.platform.content.PermissionGranter

class BypassPermissionGranter : PermissionGranter {
    override fun addPermissions(vararg permissions: String) {
        // Do nothing
    }

    override fun requestPermissions() {
        // Do nothing to bypass failing GrantPermissionRule
    }
}
