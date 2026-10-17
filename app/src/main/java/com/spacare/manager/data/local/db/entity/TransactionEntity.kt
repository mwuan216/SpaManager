package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.InvoiceTransactionRecord

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val invoiceCode: String,
    val clientName: String,
    val clientPhone: String,
    val serviceName: String,
    val technicianName: String,
    val time: String,
    val date: String,
    val amount: Long,
    val paymentMethod: String,
    val isPaid: Boolean
) {
    fun toDomain(): InvoiceTransactionRecord {
        return InvoiceTransactionRecord(
            id = id,
            invoiceCode = invoiceCode,
            clientName = clientName,
            clientPhone = clientPhone,
            serviceName = serviceName,
            technicianName = technicianName,
            time = time,
            date = date,
            amount = amount,
            paymentMethod = paymentMethod,
            isPaid = isPaid
        )
    }

    companion object {
        fun fromDomain(record: InvoiceTransactionRecord): TransactionEntity {
            return TransactionEntity(
                id = record.id,
                invoiceCode = record.invoiceCode,
                clientName = record.clientName,
                clientPhone = record.clientPhone,
                serviceName = record.serviceName,
                technicianName = record.technicianName,
                time = record.time,
                date = record.date,
                amount = record.amount,
                paymentMethod = record.paymentMethod,
                isPaid = record.isPaid
            )
        }
    }
}

