package com.mknlabs.expensetracker.widget.donut

import android.content.Context
import android.graphics.Color
import com.mknlabs.expensetracker.data.local.AppSettingsDataStore

import com.mknlabs.expensetracker.utils.getCurrency
import com.mknlabs.expensetracker.widget.di.WidgetEntryPoint
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownKind
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownPanel
import com.mknlabs.expensetracker.widget.model.WidgetBreakdownSlice
import com.mknlabs.expensetracker.widget.ui.formatWidgetAmount
import kotlinx.coroutines.flow.first

internal object WidgetDonutLiveData {

    private val palette = intArrayOf(
        0xFFF59E0B.toInt(),
        0xFFF43F5E.toInt(),
        0xFF8B5CF6.toInt(),
        0xFF10B981.toInt(),
        0xFF6366F1.toInt(),
        0xFF38BDF8.toInt(),
        0xFFC084FC.toInt(),
        0xFF94A3B8.toInt()
    )

    suspend fun panel(
        context: Context,
        kind: WidgetBreakdownKind,
        startMillis: Long,
        endExclusive: Long
    ): WidgetBreakdownPanel {
        return try {
            build(context, kind, startMillis, endExclusive)
        } catch (_: Exception) {
            emptyPanel(kind, symbol = "₹")
        }
    }

    private suspend fun build(
        context: Context,
        kind: WidgetBreakdownKind,
        startMillis: Long,
        endExclusive: Long
    ): WidgetBreakdownPanel {
        val ep = WidgetEntryPoint.get(context)
        val settings = AppSettingsDataStore.getAppSettingsFlow(context).first()
        val symbol = getCurrency(settings.currencyId).currencySymbol
        val txs = ep.transactionDao().getInRange(startMillis, endExclusive)
        val expenses = txs.filter { it.transactionTypeId != 1 }
        val incomes = txs.filter { it.transactionTypeId == 1 }
        return when (kind) {
            WidgetBreakdownKind.EXPENSE -> {
                val cats = ep.categoryDao().getActiveCategories().associateBy { it.id }
                fromGroups(
                    kind = kind,
                    title = "Expense breakdown",
                    groups = expenses.groupBy { it.categoryId }.map { (id, rows) ->
                        val cat = cats[id]
                        Group(cat?.name.orEmpty().ifBlank { "Other" }, rows.sumOf { it.amountMinor }, parseArgb(cat?.colorHex), id)
                    },
                    symbol = symbol,
                    trailing = { total, _ -> "Total: ${formatWidgetAmount(symbol, total)}" }
                )
            }
            WidgetBreakdownKind.PAYMENT -> {
                val methods = ep.paymentMethodDao().getActivePaymentMethods().associateBy { it.id }
                fromGroups(
                    kind = kind,
                    title = "Payment breakdown",
                    groups = expenses.groupBy { it.paymentMethodId }.map { (id, rows) ->
                        val m = methods[id]
                        Group(m?.name.orEmpty().ifBlank { "Other" }, rows.sumOf { it.amountMinor }, parseArgb(m?.colorHex), id)
                    },
                    symbol = symbol,
                    trailing = { _, n -> "$n txns" }
                )
            }
            WidgetBreakdownKind.INCOME -> {
                val cats = ep.categoryDao().getActiveCategories().associateBy { it.id }
                fromGroups(
                    kind = kind,
                    title = "Income breakdown",
                    groups = incomes.groupBy { it.categoryId }.map { (id, rows) ->
                        val cat = cats[id]
                        Group(cat?.name.orEmpty().ifBlank { "Other" }, rows.sumOf { it.amountMinor }, parseArgb(cat?.colorHex), id)
                    },
                    symbol = symbol,
                    trailing = { total, _ -> "Total: ${formatWidgetAmount(symbol, total)}" }
                )
            }
            WidgetBreakdownKind.TAGS -> {
                val links = ep.tagDao().observeAllTransactionTags().first()
                    .groupBy { it.transactionId }
                val byTag = linkedMapOf<String, Pair<String, Long>>()
                val colors = linkedMapOf<String, Int?>()
                expenses.forEach { tx ->
                    links[tx.id].orEmpty().forEach { row ->
                        val prev = byTag[row.tagId]
                        byTag[row.tagId] = (row.name to (prev?.second ?: 0L) + tx.amountMinor)
                        colors[row.tagId] = parseArgb(row.colorHex)
                    }
                }
                fromGroups(
                    kind = kind,
                    title = "Tags breakdown",
                    groups = byTag.entries.mapIndexed { i, e ->
                        Group(e.value.first.ifBlank { "Other" }, e.value.second, colors[e.key], i)
                    },
                    symbol = symbol,
                    trailing = { _, _ -> "${byTag.size} tags" }
                )
            }
            WidgetBreakdownKind.FUNDS -> {
                val funds = ep.fundDao().getActiveFunds().associateBy { it.id }
                fromGroups(
                    kind = kind,
                    title = "Funds breakdown",
                    groups = expenses.filter { it.fundId != null }.groupBy { it.fundId }.map { (id, rows) ->
                        val fund = id?.let { funds[it] }
                        Group(fund?.name.orEmpty().ifBlank { "Other" }, rows.sumOf { it.amountMinor }, parseArgb(fund?.colorHex), id.hashCode())
                    },
                    symbol = symbol,
                    trailing = { _, n -> "$n funds" }
                )
            }
            WidgetBreakdownKind.TOP10 -> {
                val cats = ep.categoryDao().getActiveCategories().associateBy { it.id }
                val top = expenses.sortedByDescending { it.amountMinor }.take(6)
                val total = top.sumOf { it.amountMinor }.coerceAtLeast(1L)
                val slices = top.mapIndexed { i, tx ->
                    val label = tx.note.ifBlank { cats[tx.categoryId]?.name.orEmpty().ifBlank { "Expense" } }
                    WidgetBreakdownSlice(
                        label = label.take(18),
                        percent = ((tx.amountMinor * 100L) / total).toInt(),
                        amountText = formatWidgetAmount(symbol, tx.amountMinor),
                        colorArgb = parseArgb(cats[tx.categoryId]?.colorHex) ?: palette[i % palette.size]
                    )
                }
                WidgetBreakdownPanel(
                    kind = kind,
                    title = "Top 10 spending",
                    trailing = "This month",
                    slices = slices
                )
            }
        }
    }

    private data class Group(
        val label: String,
        val amountMinor: Long,
        val colorArgb: Int?,
        val paletteKey: Int
    )

    private fun fromGroups(
        kind: WidgetBreakdownKind,
        title: String,
        groups: List<Group>,
        symbol: String,
        trailing: (total: Long, count: Int) -> String
    ): WidgetBreakdownPanel {
        val sorted = groups.filter { it.amountMinor > 0 }.sortedByDescending { it.amountMinor }
        if (sorted.isEmpty()) return emptyPanel(kind, symbol, title)
        val total = sorted.sumOf { it.amountMinor }.coerceAtLeast(1L)
        val head = sorted.take(5)
        val rest = sorted.drop(5).sumOf { it.amountMinor }
        val slices = head.mapIndexed { i, g ->
            WidgetBreakdownSlice(
                label = g.label,
                percent = ((g.amountMinor * 100L) / total).toInt(),
                amountText = formatWidgetAmount(symbol, g.amountMinor),
                colorArgb = g.colorArgb ?: palette[kotlin.math.abs(g.paletteKey) % palette.size]
            )
        }.toMutableList()
        if (rest > 0) {
            slices += WidgetBreakdownSlice(
                label = "Other",
                percent = ((rest * 100L) / total).toInt(),
                amountText = formatWidgetAmount(symbol, rest),
                colorArgb = palette.last()
            )
        }
        return WidgetBreakdownPanel(
            kind = kind,
            title = title,
            trailing = trailing(sorted.sumOf { it.amountMinor }, sorted.size),
            slices = slices
        )
    }

    private fun emptyPanel(kind: WidgetBreakdownKind, symbol: String, title: String? = null) =
        WidgetBreakdownPanel(
            kind = kind,
            title = title ?: when (kind) {
                WidgetBreakdownKind.EXPENSE -> "Expense breakdown"
                WidgetBreakdownKind.PAYMENT -> "Payment breakdown"
                WidgetBreakdownKind.INCOME -> "Income breakdown"
                WidgetBreakdownKind.TAGS -> "Tags breakdown"
                WidgetBreakdownKind.FUNDS -> "Funds breakdown"
                WidgetBreakdownKind.TOP10 -> "Top 10 spending"
            },
            trailing = "Total: ${formatWidgetAmount(symbol, 0)}",
            slices = emptyList()
        )

    private fun parseArgb(hex: String?): Int? {
        if (hex.isNullOrBlank()) return null
        return try {
            Color.parseColor(hex)
        } catch (_: Exception) {
            null
        }
    }
}
