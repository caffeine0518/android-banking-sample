package com.study.bank.domain.model

/** Non-negative monetary amount in KRW (Korean won, no fractional unit). */
@JvmInline
value class Money(val amount: Long) : Comparable<Money> {

    init {
        require(amount >= 0) { "Money cannot be negative: $amount" }
    }

    operator fun plus(other: Money): Money = Money(Math.addExact(amount, other.amount))

    operator fun minus(other: Money): Money {
        require(amount >= other.amount) {
            "Cannot subtract $other from $this (result would be negative)"
        }
        return Money(amount - other.amount)
    }

    override fun compareTo(other: Money): Int = amount.compareTo(other.amount)

    companion object {
        val ZERO: Money = Money(0)
    }
}
