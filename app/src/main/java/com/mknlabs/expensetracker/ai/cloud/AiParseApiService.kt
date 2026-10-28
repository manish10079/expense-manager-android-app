package com.mknlabs.expensetracker.ai.cloud

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiParseApiService @Inject constructor() {
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()

    suspend fun parseExpenseText(request: AiParseRequest): AiParseResponse {
        val result = functions
            .getHttpsCallable("parseExpenseText")
            .call(request.toCallableMap())
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<*, *> ?: emptyMap<String, Any>()
        return AiParseContract.responseFromCallableData(data)
    }
}
