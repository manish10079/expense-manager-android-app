package com.mknlabs.expensetracker.data.local.room

import com.mknlabs.expensetracker.data.local.room.entities.BudgetEntity
import com.mknlabs.expensetracker.data.local.room.entities.CategoryEntity
import com.mknlabs.expensetracker.data.local.room.entities.GoalEntity
import com.mknlabs.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mknlabs.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mknlabs.expensetracker.data.local.room.entities.TransactionEntity
import com.mknlabs.expensetracker.models.Budget
import com.mknlabs.expensetracker.models.CategoryType
import com.mknlabs.expensetracker.models.Goal
import com.mknlabs.expensetracker.models.PaymentType
import com.mknlabs.expensetracker.models.RecurringTransactionRule
import com.mknlabs.expensetracker.models.Transaction

fun GoalEntity.toDomain(): Goal {
    return Goal(
        id = id,
        name = name,
        targetAmountMinor = targetAmountMinor,
        currentAmountMinor = currentAmountMinor,
        deadlineAt = deadlineAt,
        iconKey = iconKey,
        colorHex = colorHex,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState
    )
}

fun Goal.toEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        name = name,
        targetAmountMinor = targetAmountMinor,
        currentAmountMinor = currentAmountMinor,
        deadlineAt = deadlineAt,
        iconKey = iconKey,
        colorHex = colorHex,
        isCompleted = isCompleted,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState
    )
}

fun CategoryEntity.toDomain(): CategoryType {
    return CategoryType(
        id = id,
        name = name,
        iconKey = iconKey,
        transactionTypeId = transactionTypeId,
        isSystem = isSystem,
        sortOrder = sortOrder,
        isDeleted = isDeleted,
        syncState = syncState,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CategoryType.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        name = name,
        transactionTypeId = transactionTypeId,
        iconKey = iconKey,
        isSystem = isSystem,
        sortOrder = sortOrder,
        isDeleted = isDeleted,
        syncState = syncState,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PaymentMethodEntity.toDomain(): PaymentType {
    return PaymentType(
        id = id,
        name = name,
        iconKey = iconKey,
        isSystem = isSystem,
        sortOrder = sortOrder,
        isDeleted = isDeleted,
        syncState = syncState,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PaymentType.toEntity(): PaymentMethodEntity {
    return PaymentMethodEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        isSystem = isSystem,
        sortOrder = sortOrder,
        isDeleted = isDeleted,
        syncState = syncState,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        note = note,
        createdAt = occurredAt,
        amountMinor = amountMinor,
        transactionTypeId = transactionTypeId,
        paymentTypeId = paymentMethodId,
        categoryId = categoryId,
        contentHash = contentHash,
        syncState = syncState,
        isDeleted = isDeleted,
        updatedAt = updatedAt,
        sourceRecurringRuleId = sourceRecurringRuleId
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        note = note,
        amountMinor = amountMinor,
        occurredAt = createdAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        transactionTypeId = transactionTypeId,
        categoryId = categoryId,
        paymentMethodId = paymentTypeId,
        isDeleted = isDeleted,
        syncState = syncState,
        contentHash = contentHash,
        sourceRecurringRuleId = sourceRecurringRuleId
    )
}

fun BudgetEntity.toDomain(): Budget {
    return Budget(
        id = id,
        categoryId = categoryId,
        monthStart = monthStart,
        limitMinor = limitMinor,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        editCount = editCount,
        isDeleted = isDeleted
    )
}

fun Budget.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        categoryId = categoryId,
        monthStart = monthStart,
        limitMinor = limitMinor,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        editCount = editCount,
        isDeleted = isDeleted
    )
}

fun RecurringRuleEntity.toDomain(): RecurringTransactionRule {
    return RecurringTransactionRule(
        id = id,
        transactionId = transactionId,
        frequency = frequency,
        repeatCount = repeatCount,
        isEnabled = isEnabled,
        intervalCount = intervalCount,
        remainingCount = remainingCount,
        anchorAt = anchorAt,
        nextRunAt = nextRunAt,
        lastRunAt = lastRunAt,
        lastNotifiedOccurrenceAt = lastNotifiedOccurrenceAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        isDeleted = isDeleted
    )
}

fun RecurringTransactionRule.toEntity(): RecurringRuleEntity {
    return RecurringRuleEntity(
        id = id,
        transactionId = transactionId,
        frequency = frequency,
        intervalCount = intervalCount,
        repeatCount = repeatCount,
        remainingCount = remainingCount,
        anchorAt = anchorAt,
        nextRunAt = nextRunAt,
        lastRunAt = lastRunAt,
        lastNotifiedOccurrenceAt = lastNotifiedOccurrenceAt,
        isEnabled = isEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        isDeleted = isDeleted
    )
}
