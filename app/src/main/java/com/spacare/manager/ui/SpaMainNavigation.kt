package com.spacare.manager.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.data.repository.SpaRepository
import com.spacare.manager.feature.calendar.CalendarSchedulerScreen
import com.spacare.manager.feature.checkout.CheckoutQrScreen
import com.spacare.manager.feature.crm.ClientListScreen
import com.spacare.manager.feature.crm.ClientProfileScreen
import com.spacare.manager.feature.dashboard.RevenueDashboardScreen
import com.spacare.manager.feature.settings.StoreSettingsScreen
import com.spacare.manager.core.license.LicenseManager
import com.spacare.manager.feature.license.TrialExpiredLockDialog
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import kotlinx.coroutines.launch

import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

enum class SpaNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CALENDAR("Lịch Hẹn", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    CRM("Khách Hàng", Icons.Filled.People, Icons.Outlined.People),
    CHECKOUT("Thu Ngân", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    DASHBOARD("Báo Cáo", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    SETTINGS("Cài Đặt", Icons.Filled.Storefront, Icons.Outlined.Storefront)
}

@Composable
fun SpaMainApp(
    repository: SpaRepository? = null
) {
    var currentDestination by remember { mutableStateOf(SpaNavDestination.CALENDAR) }
    var previousDestination by remember { mutableStateOf(SpaNavDestination.CALENDAR) }
    var checkoutFromAppointment by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = repository ?: remember { SpaRepository(context) }
    val licenseManager = remember { LicenseManager.getInstance(context) }
    var licenseStatus by remember { mutableStateOf(licenseManager.getLicenseStatus()) }

    LaunchedEffect(currentDestination) {
        licenseStatus = licenseManager.getLicenseStatus()
    }

    // Tự động khôi phục bản quyền từ đám mây khi mở app (trường hợp cài lại app hoặc xóa dữ liệu)
    LaunchedEffect(Unit) {
        if (!licenseStatus.isActivated) {
            val restoreResult = licenseManager.restoreLicenseOnline()
            if (restoreResult.success) {
                licenseStatus = licenseManager.getLicenseStatus()
                Toast.makeText(
                    context,
                    "Chào mừng trở lại! Đã tự động khôi phục gói bản quyền của bạn.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val isRealUsageMode by repository.isRealUsageMode.collectAsState(initial = false)
    val clients by repository.clients.collectAsState(initial = emptyList())
    val selectedClientId by repository.selectedClientId.collectAsState(initial = null)
    val appointments by repository.appointments.collectAsState(initial = emptyList())
    val currentInvoice by repository.currentInvoice.collectAsState(initial = null)
    val storeProfile by repository.storeProfile.collectAsState(initial = com.spacare.manager.domain.model.StoreProfile())
    val catalogPackages by repository.catalogPackages.collectAsState(initial = emptyList())
    val technicians by repository.technicians.collectAsState(initial = emptyList())
    val transactions by repository.transactions.collectAsState(initial = emptyList())

    val todayDateStr = remember {
        java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    }
    var selectedDashboardDate by remember { mutableStateOf(todayDateStr) }

    val selectedClient = remember(clients, selectedClientId) {
        clients.find { it.id == selectedClientId }
    }

    val unpaidAppointments = remember(appointments) {
        appointments.filter {
            it.status != com.spacare.manager.domain.model.AppointmentStatus.COMPLETED &&
            it.status != com.spacare.manager.domain.model.AppointmentStatus.CANCELLED &&
            it.price > 0L // Bỏ các lịch hẹn trừ gói dịch vụ (0đ) khỏi danh sách chờ thu ngân QR
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = WarmBeigeSurface,
                tonalElevation = 8.dp
            ) {
                SpaNavDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (destination != currentDestination) {
                                previousDestination = currentDestination
                            }
                            if (destination == SpaNavDestination.CRM) {
                                // Luôn mở màn danh sách khách hàng mới khi ấn vào tab Khách Hàng
                                repository.selectClient(null)
                            }
                            if (destination == SpaNavDestination.CHECKOUT) {
                                checkoutFromAppointment = false
                            }
                            if (destination == SpaNavDestination.DASHBOARD) {
                                selectedDashboardDate = todayDateStr
                            }
                            currentDestination = destination
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SageGreenPrimary,
                            selectedTextColor = SageGreenPrimary,
                            indicatorColor = SageGreenContainer,
                            unselectedIconColor = CharcoalTextMuted,
                            unselectedTextColor = CharcoalTextMuted
                        )
                    )
                }
            }
        },
        containerColor = WarmBeigeBackground
    ) { paddingValues ->
        val screenModifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        when (currentDestination) {
            SpaNavDestination.CALENDAR -> {
                CalendarSchedulerScreen(
                    appointments = appointments,
                    technicians = technicians,
                    clients = clients,
                    catalogPackages = catalogPackages,
                    onAppointmentClick = { appt ->
                        val matchedClient = clients.find { it.fullName.equals(appt.clientName, ignoreCase = true) || it.phone == appt.clientPhone }
                        if (matchedClient != null) {
                            repository.selectClient(matchedClient.id)
                            currentDestination = SpaNavDestination.CRM
                        }
                    },
                    onAddAppointment = { newAppt ->
                        repository.addAppointment(newAppt)
                    },
                    onAddNewClient = { newClient ->
                        repository.addClient(newClient)
                    },
                    onUpdateAppointmentStatus = { id, status ->
                        repository.updateAppointmentStatus(id, status)
                    },
                    onDeleteAppointment = { id ->
                        repository.deleteAppointment(id)
                    },
                    onGoToCheckout = { appt ->
                        repository.createInvoiceFromAppointment(appt)
                        checkoutFromAppointment = true
                        previousDestination = SpaNavDestination.CALENDAR
                        currentDestination = SpaNavDestination.CHECKOUT
                    },
                    modifier = screenModifier
                )
            }
            SpaNavDestination.CRM -> {
                if (selectedClient != null) {
                    ClientProfileScreen(
                        client = selectedClient,
                        onBackClick = {
                            repository.selectClient(null)
                        },
                        onBookAppointmentClick = {
                            previousDestination = SpaNavDestination.CRM
                            currentDestination = SpaNavDestination.CALENDAR
                        },
                        onCheckoutClick = {
                            val clientAppt = unpaidAppointments.find { it.clientPhone == selectedClient.phone || it.clientName.equals(selectedClient.fullName, ignoreCase = true) }
                            if (clientAppt != null) {
                                repository.createInvoiceFromAppointment(clientAppt)
                            } else {
                                repository.createInvoiceForClient(selectedClient)
                            }
                            checkoutFromAppointment = true
                            previousDestination = SpaNavDestination.CRM
                            currentDestination = SpaNavDestination.CHECKOUT
                        },
                        onUpdateClient = { updatedClient ->
                            repository.updateClient(updatedClient)
                        },
                        onDeleteClient = { clientId ->
                            repository.deleteClient(clientId)
                        },
                        onCheckInSession = { packageId, intervalDays, nextDate ->
                            repository.checkInSession(selectedClient.id, packageId, intervalDays, nextDate)
                        },
                        onAddTreatmentPackage = { pkg ->
                            repository.addTreatmentPackage(selectedClient.id, pkg)
                        },
                        onUpdatePackageCycle = { packageId, intervalDays, nextDate ->
                            repository.updatePackageCycle(selectedClient.id, packageId, intervalDays, nextDate)
                        },
                        onDeleteTreatmentPackage = { packageId ->
                            repository.deleteTreatmentPackage(selectedClient.id, packageId)
                        },
                        onDeleteServiceRecord = { srvId ->
                            repository.deleteServiceRecord(selectedClient.id, srvId)
                        },
                        onDeleteSessionPhoto = { photoId ->
                            repository.deleteSessionPhoto(selectedClient.id, photoId)
                        },
                        onDeleteBeforeAfter = { compId ->
                            repository.deleteBeforeAfter(selectedClient.id, compId)
                        },
                        onAddServiceRecord = { record ->
                            repository.addServiceRecord(selectedClient.id, record)
                        },
                        onAddBeforeAfter = { record ->
                            repository.addBeforeAfter(selectedClient.id, record)
                        },
                        onAddAppointment = { newAppt ->
                            repository.addAppointment(newAppt)
                        },
                        onAddSessionPhoto = { photo ->
                            repository.addSessionPhoto(selectedClient.id, photo)
                        },
                        onUpdateSessionPhoto = { photo ->
                            repository.updateSessionPhoto(selectedClient.id, photo)
                        },
                        catalogPackages = catalogPackages,
                        technicians = technicians,
                        modifier = screenModifier
                    )
                } else {
                    ClientListScreen(
                        clients = clients,
                        onSelectClient = { client ->
                            repository.selectClient(client.id)
                        },
                        onAddClient = { newClient ->
                            repository.addClient(newClient)
                        },
                        onUpdateClient = { updatedClient ->
                            repository.updateClient(updatedClient)
                        },
                        onDeleteClient = { clientId ->
                            repository.deleteClient(clientId)
                        },
                        modifier = screenModifier
                    )
                }
            }
            SpaNavDestination.CHECKOUT -> {
                CheckoutQrScreen(
                    invoice = currentInvoice,
                    storeProfile = storeProfile,
                    isStoreDefaultMode = !checkoutFromAppointment && unpaidAppointments.isEmpty(),
                    unpaidAppointments = unpaidAppointments,
                    onSelectAppointmentInvoice = { appt ->
                        repository.createInvoiceFromAppointment(appt)
                        checkoutFromAppointment = true
                    },
                    onConfirmPayment = {
                        repository.markInvoiceAsPaid()
                        selectedDashboardDate = todayDateStr
                    },
                    onConfirmStorePayment = { amount, memo ->
                        repository.recordStoreQrPayment(amount, memo)
                        selectedDashboardDate = todayDateStr
                    },
                    onResetInvoice = {
                        repository.resetInvoice()
                    },
                    onUpdateBank = { bank, accNo, holder, memo ->
                        repository.updateBank(bank, accNo, holder, memo)
                    },
                    onBackClick = { currentDestination = previousDestination },
                    modifier = screenModifier
                )
            }
            SpaNavDestination.DASHBOARD -> {
                val dynamicMetrics = remember(selectedDashboardDate, appointments, transactions, isRealUsageMode) {
                    repository.getMetricsForDate(selectedDashboardDate)
                }
                RevenueDashboardScreen(
                    metrics = dynamicMetrics,
                    selectedDate = selectedDashboardDate,
                    transactions = transactions,
                    onDateSelected = { newDate ->
                        selectedDashboardDate = newDate
                    },
                    onDeleteTransaction = { txId ->
                        repository.deleteTransaction(txId)
                    },
                    modifier = screenModifier
                )
            }
            SpaNavDestination.SETTINGS -> {
                StoreSettingsScreen(
                    storeProfile = storeProfile,
                    catalogPackages = catalogPackages,
                    technicians = technicians,
                    isRealUsageMode = isRealUsageMode,
                    clientCount = clients.size,
                    appointmentCount = appointments.size,
                    transactionCount = transactions.size,
                    onStartRealUsage = {
                        repository.startRealUsage()
                    },
                    onExportBackup = {
                        coroutineScope.launch {
                            val result = repository.exportBackup(context)
                            result.onSuccess { file ->
                                Toast.makeText(context, "Xuất file sao lưu thành công: ${file.name}", Toast.LENGTH_SHORT).show()
                                repository.shareBackup(context, file)
                            }.onFailure { e ->
                                Toast.makeText(context, "Lỗi xuất file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onImportBackup = { jsonContent ->
                        coroutineScope.launch {
                            val result = repository.importBackup(context, jsonContent)
                            result.onSuccess { stats ->
                                Toast.makeText(
                                    context,
                                    "Khôi phục thành công: ${stats.clientCount} khách hàng, ${stats.appointmentCount} lịch hẹn, ${stats.transactionCount} giao dịch!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }.onFailure { e ->
                                Toast.makeText(context, "Lỗi khôi phục: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    onUpdateStoreProfile = { updated ->
                        repository.updateStoreProfile(updated)
                    },
                    onAddCatalogPackage = { pkg ->
                        repository.addCatalogPackage(pkg)
                    },
                    onUpdateCatalogPackage = { pkg ->
                        repository.updateCatalogPackage(pkg)
                    },
                    onDeleteCatalogPackage = { pkgId ->
                        repository.deleteCatalogPackage(pkgId)
                    },
                    onAddTechnician = { tech ->
                        repository.addTechnician(tech)
                    },
                    onUpdateTechnician = { tech ->
                        repository.updateTechnician(tech)
                    },
                    onDeleteTechnician = { techId ->
                        repository.deleteTechnician(techId)
                    },
                    modifier = screenModifier
                )
            }
        }
    }

    // Khóa màn hình bắt buộc nhập key kích hoạt bản quyền khi đã hết hạn dùng thử 15 ngày
    if (licenseStatus.isExpired) {
        TrialExpiredLockDialog(
            licenseManager = licenseManager,
            onActivatedSuccess = {
                licenseStatus = licenseManager.getLicenseStatus()
            }
        )
    }
}
