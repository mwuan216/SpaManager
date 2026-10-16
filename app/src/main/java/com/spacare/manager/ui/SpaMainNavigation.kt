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
    repository: SpaRepository = remember { SpaRepository() }
) {
    var currentDestination by remember { mutableStateOf(SpaNavDestination.CALENDAR) }
    var checkoutFromAppointment by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val licenseManager = remember { LicenseManager.getInstance(context) }
    var licenseStatus by remember { mutableStateOf(licenseManager.getLicenseStatus()) }

    LaunchedEffect(currentDestination) {
        licenseStatus = licenseManager.getLicenseStatus()
    }

    val clients by repository.clients.collectAsState(initial = emptyList())
    val selectedClientId by repository.selectedClientId.collectAsState(initial = null)
    val appointments by repository.appointments.collectAsState(initial = emptyList())
    val currentInvoice by repository.currentInvoice.collectAsState(initial = null)
    val storeProfile by repository.storeProfile.collectAsState(initial = com.spacare.manager.domain.model.StoreProfile())
    val catalogPackages by repository.catalogPackages.collectAsState(initial = emptyList())
    val technicians by repository.technicians.collectAsState(initial = emptyList())
    val transactions by repository.transactions.collectAsState(initial = emptyList())

    var selectedDashboardDate by remember { mutableStateOf("16/10/2026") }

    val selectedClient = remember(clients, selectedClientId) {
        clients.find { it.id == selectedClientId }
    }

    val unpaidAppointments = remember(appointments) {
        appointments.filter {
            it.status != com.spacare.manager.domain.model.AppointmentStatus.COMPLETED &&
            it.status != com.spacare.manager.domain.model.AppointmentStatus.CANCELLED
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
                            if (destination == SpaNavDestination.CRM) {
                                // Luôn mở màn danh sách khách hàng mới khi ấn vào tab Khách Hàng
                                repository.selectClient(null)
                            }
                            if (destination == SpaNavDestination.CHECKOUT) {
                                checkoutFromAppointment = false
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
                    onAppointmentClick = { appt ->
                        val matchedClient = clients.find { it.fullName == appt.clientName || it.phone == appt.clientPhone }
                        if (matchedClient != null) {
                            repository.selectClient(matchedClient.id)
                            currentDestination = SpaNavDestination.CRM
                        }
                    },
                    onAddAppointment = { newAppt ->
                        repository.addAppointment(newAppt)
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
                            currentDestination = SpaNavDestination.CALENDAR
                        },
                        onCheckoutClick = {
                            checkoutFromAppointment = true
                            currentDestination = SpaNavDestination.CHECKOUT
                        },
                        onUpdateClient = { updatedClient ->
                            repository.updateClient(updatedClient)
                        },
                        onDeleteClient = { clientId ->
                            repository.deleteClient(clientId)
                        },
                        onCheckInSession = { packageId ->
                            repository.checkInSession(selectedClient.id, packageId)
                        },
                        onAddTreatmentPackage = { pkg ->
                            repository.addTreatmentPackage(selectedClient.id, pkg)
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
                currentInvoice?.let { invoice ->
                    CheckoutQrScreen(
                        invoice = invoice,
                        storeProfile = storeProfile,
                        isStoreDefaultMode = !checkoutFromAppointment,
                        unpaidAppointments = unpaidAppointments,
                        onSelectAppointmentInvoice = { appt ->
                            repository.createInvoiceFromAppointment(appt)
                            checkoutFromAppointment = true
                        },
                        onConfirmPayment = {
                            repository.markInvoiceAsPaid()
                        },
                        onResetInvoice = {
                            repository.resetInvoice()
                        },
                        onUpdateBank = { bank, accNo, holder, memo ->
                            repository.updateBank(bank, accNo, holder, memo)
                        },
                        onBackClick = { currentDestination = SpaNavDestination.CRM },
                        modifier = screenModifier
                    )
                }
            }
            SpaNavDestination.DASHBOARD -> {
                val dynamicMetrics = remember(selectedDashboardDate, appointments) {
                    repository.getMetricsForDate(selectedDashboardDate)
                }
                RevenueDashboardScreen(
                    metrics = dynamicMetrics,
                    selectedDate = selectedDashboardDate,
                    transactions = transactions,
                    onDateSelected = { newDate ->
                        selectedDashboardDate = newDate
                    },
                    modifier = screenModifier
                )
            }
            SpaNavDestination.SETTINGS -> {
                StoreSettingsScreen(
                    storeProfile = storeProfile,
                    catalogPackages = catalogPackages,
                    technicians = technicians,
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

    // Khóa màn hình bắt buộc nhập key kích hoạt bản quyền khi đã hết hạn dùng thử 3 ngày
    if (licenseStatus.isExpired) {
        TrialExpiredLockDialog(
            licenseManager = licenseManager,
            onActivatedSuccess = {
                licenseStatus = licenseManager.getLicenseStatus()
            }
        )
    }
}
