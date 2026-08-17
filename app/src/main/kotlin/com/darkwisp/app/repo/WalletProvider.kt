package com.darkwisp.app.repo

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Whether a payment actually settled, or is still in flight.
 *
 * Reporting an in-flight payment as settled tells the user sats left their
 * wallet when they may still return.
 */
enum class PaymentSettlement { COMPLETED, PENDING }

/**
 * Outcome of a successful `pay_invoice` call. [reference] is the preimage for
 * NWC (which only exists once settled) or the payment id for Spark.
 */
data class WalletPayment(
    val reference: String,
    val settlement: PaymentSettlement
)

interface WalletProvider {
    val balance: StateFlow<Long?>
    val isConnected: StateFlow<Boolean>
    val statusLog: SharedFlow<String>

    /** Emits the amount in msats whenever an incoming payment is received. */
    val paymentReceived: SharedFlow<Long>

    fun hasConnection(): Boolean
    fun connect()
    fun disconnect()
    suspend fun fetchBalance(): Result<Long>
    /**
     * Pay a BOLT11 invoice. A returned [Result.success] means the wallet
     * accepted it — check [WalletPayment.settlement] before telling the user
     * it landed.
     */
    suspend fun payInvoice(bolt11: String): Result<WalletPayment>
    suspend fun makeInvoice(amountMsats: Long, description: String): Result<String>
    suspend fun listTransactions(limit: Int = 50, offset: Int = 0): Result<List<WalletTransaction>>
}

data class WalletTransaction(
    val type: String,
    val description: String?,
    val paymentHash: String,
    val amountMsats: Long,
    val feeMsats: Long = 0,
    val createdAt: Long,
    val settledAt: Long?,
    /** Pubkey of the counterparty (recipient for outgoing, sender for incoming zaps). */
    val counterpartyPubkey: String? = null
)
