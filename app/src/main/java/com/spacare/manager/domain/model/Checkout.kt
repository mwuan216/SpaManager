package com.spacare.manager.domain.model

enum class PaymentStatus {
    WAITING_FOR_PAYMENT,
    PAYMENT_SUCCESS,
    FAILED
}

enum class ItemType {
    SERVICE,
    PRODUCT,
    PACKAGE
}

data class OrderItem(
    val id: String,
    val name: String,
    val type: ItemType,
    val quantity: Int,
    val unitPrice: Long
) {
    val totalPrice: Long get() = quantity * unitPrice
}

data class VietQrInfo(
    val bankId: String = "970422", // MBBank BIN code
    val bankShortName: String = "MBBank",
    val accountNumber: String = "0888999888",
    val accountHolder: String = "SPA CARE WELLNESS CENTER",
    val amount: Long,
    val referenceCode: String,
    val description: String
) {
    // URL chuẩn VietQR Quicklink tiện lợi & tương thích mọi app ngân hàng Việt Nam
    val qrImageUrl: String
        get() = "https://img.vietqr.io/image/$bankId-$accountNumber-compact2.png?amount=$amount&addInfo=$referenceCode&accountName=${java.net.URLEncoder.encode(accountHolder, "UTF-8")}"
}

data class CheckoutInvoice(
    val invoiceId: String,
    val invoiceCode: String,
    val clientName: String,
    val clientPhone: String,
    val items: List<OrderItem>,
    val discountAmount: Long = 0,
    val tipAmount: Long = 0,
    val paymentStatus: PaymentStatus = PaymentStatus.WAITING_FOR_PAYMENT,
    val transactionRef: String? = null,
    val paidAt: String? = null
) {
    val subtotal: Long get() = items.sumOf { it.totalPrice }
    val finalTotal: Long get() = (subtotal - discountAmount + tipAmount).coerceAtLeast(0)

    val vietQrInfo: VietQrInfo
        get() = VietQrInfo(
            amount = finalTotal,
            referenceCode = invoiceCode,
            description = "Thanh toan hoa don $invoiceCode"
        )
}

