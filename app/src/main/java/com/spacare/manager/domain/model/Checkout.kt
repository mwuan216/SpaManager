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

data class BankOption(
    val bin: String,
    val shortName: String,
    val fullName: String
)

val SupportedBanks = listOf(
    BankOption("970436", "Vietcombank", "Ngân hàng Ngoại Thương Việt Nam (VCB)"),
    BankOption("970407", "Techcombank", "Ngân hàng Kỹ Thương Việt Nam (TCB)"),
    BankOption("970415", "VietinBank", "Ngân hàng Công Thương Việt Nam (CTG)"),
    BankOption("970418", "BIDV", "Ngân hàng Đầu tư và Phát triển Việt Nam"),
    BankOption("970432", "VPBank", "Ngân hàng Việt Nam Thịnh Vượng"),
    BankOption("970416", "ACB", "Ngân hàng Á Châu"),
    BankOption("970422", "MBBank", "Ngân hàng Quân Đội"),
    BankOption("970458", "TPBank", "Ngân hàng Tiên Phong"),
    BankOption("970403", "Sacombank", "Ngân hàng Sài Gòn Thương Tín")
)

data class VietQrInfo(
    val bankId: String = "970436", // Vietcombank BIN code
    val bankShortName: String = "Vietcombank",
    val accountNumber: String = "1023456789",
    val accountHolder: String = "SPA CARE WELLNESS CENTER",
    val amount: Long,
    val referenceCode: String,
    val description: String = ""
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
    val paidAt: String? = null,
    val selectedBank: BankOption = SupportedBanks[0],
    val accountNumber: String = "1023456789",
    val accountHolder: String = "SPA CARE WELLNESS CENTER"
) {
    val subtotal: Long get() = items.sumOf { it.totalPrice }
    val finalTotal: Long get() = (subtotal - discountAmount + tipAmount).coerceAtLeast(0)

    val vietQrInfo: VietQrInfo
        get() = VietQrInfo(
            bankId = selectedBank.bin,
            bankShortName = selectedBank.shortName,
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            amount = finalTotal,
            referenceCode = invoiceCode,
            description = "Thanh toan hoa don $invoiceCode"
        )
}
