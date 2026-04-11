package com.mkn0079.expensetracker.data.local.room

import com.mkn0079.expensetracker.data.local.room.entities.BudgetEntity
import com.mkn0079.expensetracker.data.local.room.entities.CategoryEntity
import com.mkn0079.expensetracker.data.local.room.entities.PaymentMethodEntity
import com.mkn0079.expensetracker.data.local.room.entities.RecurringRuleEntity
import com.mkn0079.expensetracker.data.local.room.entities.TransactionEntity
import com.mkn0079.expensetracker.models.Budget
import com.mkn0079.expensetracker.models.CategoryType
import com.mkn0079.expensetracker.models.PaymentType
import com.mkn0079.expensetracker.models.RecurringTransactionRule
import com.mkn0079.expensetracker.models.Transaction

fun CategoryEntity.toDomain(): CategoryType {
    return CategoryType(
        id = id,
        name = name,
        iconKey = iconKey,
        transactionTypeId = transactionTypeId,
        isSystem = isSystem,
        sortOrder = sortOrder,
        isDeleted = isDeleted,
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
        isEnabled = isEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncState = syncState,
        isDeleted = isDeleted
    )
}
