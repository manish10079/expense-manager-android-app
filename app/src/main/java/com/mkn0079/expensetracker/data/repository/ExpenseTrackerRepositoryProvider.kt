package com.mkn0079.expensetracker.data.repository

import android.content.Context

object ExpenseTrackerRepositoryProvider {

    @Volatile
    private var transactionRepository: TransactionRepository? = null
    @Volatile
    private var categoryRepository: CategoryRepository? = null
    @Volatile
    private var paymentMethodRepository: PaymentMethodRepository? = null
    @Volatile
    private var budgetRepository: BudgetRepository? = null
    @Volatile
    private var recurringRuleRepository: RecurringRuleRepository? = null

    fun transactionRepository(context: Context): TransactionRepository {
        return transactionRepository ?: synchronized(this) {
            transactionRepository ?: TransactionRepository(context.applicationContext).also {
                transactionRepository = it
            }
        }
    }

    fun categoryRepository(context: Context): CategoryRepository {
        return categoryRepository ?: synchronized(this) {
            categoryRepository ?: CategoryRepository(context.applicationContext).also {
                categoryRepository = it
            }
        }
    }

    fun paymentMethodRepository(context: Context): PaymentMethodRepository {
        return paymentMethodRepository ?: synchronized(this) {
            paymentMethodRepository ?: PaymentMethodRepository(context.applicationContext).also {
                paymentMethodRepository = it
            }
        }
    }

    fun budgetRepository(context: Context): BudgetRepository {
        return budgetRepository ?: synchronized(this) {
            budgetRepository ?: BudgetRepository(context.applicationContext).also {
                budgetRepository = it
            }
        }
    }

    fun recurringRuleRepository(context: Context): RecurringRuleRepository {
        return recurringRuleRepository ?: synchronized(this) {
            recurringRuleRepository ?: RecurringRuleRepository(context.applicationContext).also {
                recurringRuleRepository = it
            }
        }
    }
}
