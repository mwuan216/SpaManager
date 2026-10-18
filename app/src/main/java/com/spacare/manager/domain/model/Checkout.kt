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
    BankOption("970415", "VietinBank", "Ngân hàng Công Thương Việt Nam (CTG)"),
    BankOption("970418", "BIDV", "Ngân hàng Đầu tư và Phát triển Việt Nam"),
    BankOption("970405", "Agribank", "Ngân hàng Nông nghiệp & PT Nông thôn Việt Nam (VBA)"),
    BankOption("970407", "Techcombank", "Ngân hàng Kỹ Thương Việt Nam (TCB)"),
    BankOption("970422", "MBBank", "Ngân hàng Quân Đội (MB)"),
    BankOption("970432", "VPBank", "Ngân hàng Việt Nam Thịnh Vượng"),
    BankOption("970416", "ACB", "Ngân hàng Á Châu"),
    BankOption("970458", "TPBank", "Ngân hàng Tiên Phong"),
    BankOption("970403", "Sacombank", "Ngân hàng Sài Gòn Thương Tín"),
    BankOption("970437", "HDBank", "Ngân hàng Phát triển TP.HCM"),
    BankOption("970441", "VIB", "Ngân hàng Quốc Tế"),
    BankOption("970443", "SHB", "Ngân hàng Sài Gòn - Hà Nội"),
    BankOption("970426", "MSB", "Ngân hàng Hàng Hải Việt Nam"),
    BankOption("970448", "OCB", "Ngân hàng Phương Đông"),
    BankOption("970449", "LPBank", "Ngân hàng Lộc Phát Việt Nam"),
    BankOption("970440", "SeABank", "Ngân hàng Đông Nam Á"),
    BankOption("970431", "Eximbank", "Ngân hàng Xuất Nhập Khẩu Việt Nam"),
    BankOption("970409", "BacABank", "Ngân hàng Bắc Á"),
    BankOption("970412", "PVcomBank", "Ngân hàng Đại Chúng Việt Nam"),
    BankOption("970438", "BaoVietBank", "Ngân hàng Bảo Việt"),
    BankOption("970428", "NamABank", "Ngân hàng Nam Á"),
    BankOption("970400", "SaigonBank", "Ngân hàng Sài Gòn Công Thương"),
    BankOption("970452", "Kienlongbank", "Ngân hàng Kiên Long"),
    BankOption("970454", "BVBank", "Ngân hàng Bản Việt"),
    BankOption("970427", "VietABank", "Ngân hàng Việt Á"),
    BankOption("970414", "OceanBank", "Ngân hàng Đại Dương"),
    BankOption("970408", "GPBank", "Ngân hàng Dầu khí Toàn Cầu"),
    BankOption("970444", "CBBank", "Ngân hàng Xây Dựng"),
    BankOption("970430", "PGBank", "Ngân hàng Thịnh vượng và Phát triển"),
    BankOption("970424", "ShinhanBank", "Ngân hàng Shinhan Việt Nam"),
    BankOption("970457", "WooriBank", "Ngân hàng Woori Việt Nam"),
    BankOption("970439", "PublicBank", "Ngân hàng Public Bank Việt Nam"),
    BankOption("970442", "HongLeongBank", "Ngân hàng Hong Leong Việt Nam"),
    BankOption("422589", "CIMBBank", "Ngân hàng CIMB Việt Nam"),
    BankOption("970455", "UOB", "Ngân hàng United Overseas Bank Việt Nam"),
    BankOption("458761", "HSBC", "Ngân hàng HSBC Việt Nam"),
    BankOption("970410", "StandardChartered", "Ngân hàng Standard Chartered Việt Nam"),
    BankOption("970434", "IndovinaBank", "Ngân hàng TNHH Indovina"),
    BankOption("970421", "VRB", "Ngân hàng Liên doanh Việt - Nga"),
    BankOption("963388", "Timo", "Ngân hàng số Timo by BVBank"),
    BankOption("546034", "Cake", "Ngân hàng số Cake by VPBank"),
    BankOption("971005", "ViettelMoney", "Viettel Money (Tổng Công ty Dịch vụ số Viettel)"),
    BankOption("971011", "VNPTMoney", "VNPT Money (Tập đoàn Bưu chính Viễn thông VN)")
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
        get() = "https://img.vietqr.io/image/$bankId-$accountNumber-compact2.png?amount=$amount&addInfo=${java.net.URLEncoder.encode(referenceCode, "UTF-8")}&accountName=${java.net.URLEncoder.encode(accountHolder, "UTF-8")}"
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
    val accountHolder: String = "SPA CARE WELLNESS CENTER",
    val defaultMemo: String = "SPACARE",
    val appointmentId: String? = null,
    val staffName: String? = null
) {
    val subtotal: Long get() = items.sumOf { it.totalPrice }
    val finalTotal: Long get() = (subtotal - discountAmount + tipAmount).coerceAtLeast(0)

    val vietQrInfo: VietQrInfo
        get() {
            val memo = if (defaultMemo.isNotBlank()) "$defaultMemo $invoiceCode".trim() else invoiceCode
            return VietQrInfo(
                bankId = selectedBank.bin,
                bankShortName = selectedBank.shortName,
                accountNumber = accountNumber,
                accountHolder = accountHolder,
                amount = finalTotal,
                referenceCode = memo,
                description = "Thanh toan hoa don $invoiceCode"
            )
        }
}

data class InvoiceTransactionRecord(
    val id: String,
    val invoiceCode: String,
    val clientName: String,
    val clientPhone: String,
    val serviceName: String,
    val technicianName: String,
    val time: String,
    val date: String,
    val amount: Long,
    val paymentMethod: String = "VietQR Chuyển Khoản", // VietQR Chuyển Khoản / Tiền mặt
    val isPaid: Boolean = true
)

