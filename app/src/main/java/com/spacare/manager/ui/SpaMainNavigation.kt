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
import com.spacare.manager.feature.crm.ClientProfileScreen
import com.spacare.manager.feature.dashboard.RevenueDashboardScreen

enum class SpaNavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CALENDAR("Lịch Hẹn", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    CRM("Khách Hàng", Icons.Filled.People, Icons.Outlined.People),
    CHECKOUT("Thu Ngân", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner),
    DASHBOARD("Báo Cáo", Icons.Filled.BarChart, Icons.Outlined.BarChart)
}

@Composable
fun SpaMainApp(
    repository: SpaRepository = remember { SpaRepository() }
) {
    var currentDestination by remember { mutableStateOf(SpaNavDestination.CRM) }

    val currentClient by repository.currentClient.collectAsState(initial = null)
    val appointments by repository.appointments.collectAsState(initial = emptyList())
    val currentInvoice by repository.currentInvoice.collectAsState(initial = null)

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
                        onClick = { currentDestination = destination },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelSmall
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
        when (currentDestination) {
            SpaNavDestination.CALENDAR -> {
                CalendarSchedulerScreen(
                    appointments = appointments,
                    onAppointmentClick = {
                        // Xem chi tiết khách hàng của lịch hẹn này
                        currentDestination = SpaNavDestination.CRM
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            SpaNavDestination.CRM -> {
                currentClient?.let { client ->
                    ClientProfileScreen(
                        client = client,
                        onBackClick = { currentDestination = SpaNavDestination.CALENDAR },
                        onBookAppointmentClick = { currentDestination = SpaNavDestination.CALENDAR },
                        onCheckoutClick = { currentDestination = SpaNavDestination.CHECKOUT },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            SpaNavDestination.CHECKOUT -> {
                currentInvoice?.let { invoice ->
                    CheckoutQrScreen(
                        invoice = invoice,
                        onSimulatePaymentSuccess = {
                            repository.simulatePaymentSuccess()
                        },
                        onResetPayment = {
                            repository.resetPayment()
                        },
                        onBackClick = { currentDestination = SpaNavDestination.CRM },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            SpaNavDestination.DASHBOARD -> {
                RevenueDashboardScreen(
                    metrics = repository.dashboardMetrics,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

