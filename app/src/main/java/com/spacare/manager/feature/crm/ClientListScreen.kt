package com.spacare.manager.feature.crm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwapplication.spacare.manager.R
import com.spacare.manager.core.designsystem.theme.*
import com.spacare.manager.domain.model.Client
import com.spacare.manager.domain.model.VipTier
import com.spacare.manager.feature.crm.components.ClientFormDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListScreen(
    clients: List<Client>,
    onSelectClient: (Client) -> Unit,
    onAddClient: (Client) -> Unit,
    onUpdateClient: (Client) -> Unit,
    onDeleteClient: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTierFilter by remember { mutableStateOf<VipTier?>(null) }

    var showAddDialog by remember { mutableStateOf(false) }
    var clientToEdit by remember { mutableStateOf<Client?>(null) }
    var clientToDelete by remember { mutableStateOf<Client?>(null) }

    // Lọc danh sách khách hàng
    val filteredClients = remember(clients, searchQuery, selectedTierFilter) {
        clients.filter { client ->
            val matchQuery = searchQuery.isBlank() ||
                    client.fullName.contains(searchQuery, ignoreCase = true) ||
                    client.phone.contains(searchQuery)
            val matchTier = selectedTierFilter == null || client.vipTier == selectedTierFilter
            matchQuery && matchTier
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Column {
                            Text(
                                text = "Danh Sách Khách Hàng",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${filteredClients.size} / ${clients.size} khách hàng",
                                style = MaterialTheme.typography.bodySmall,
                                color = CharcoalTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Thêm khách hàng", tint = SageGreenPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WarmBeigeBackground,
                    titleContentColor = CharcoalTextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SageGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm khách hàng")
            }
        },
        containerColor = WarmBeigeBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Tìm theo tên hoặc số điện thoại...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalTextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Xóa tìm kiếm")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = WarmBeigeSurface,
                    unfocusedContainerColor = WarmBeigeSurface
                ),
                singleLine = true
            )

            // Dải lọc phân loại hội viên (2 mục: Khách hàng (tất cả) và Thân thiết)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                item {
                    val allCount = clients.size
                    FilterChip(
                        selected = selectedTierFilter == null,
                        onClick = { selectedTierFilter = null },
                        label = { Text("Khách hàng (Tất cả: $allCount)", fontWeight = if (selectedTierFilter == null) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenContainer,
                            selectedLabelColor = SageGreenOnContainer
                        )
                    )
                }
                item {
                    val vipCount = clients.count { it.vipTier == VipTier.VIP }
                    FilterChip(
                        selected = selectedTierFilter == VipTier.VIP,
                        onClick = {
                            selectedTierFilter = if (selectedTierFilter == VipTier.VIP) null else VipTier.VIP
                        },
                        label = { Text("Thân thiết ($vipCount)", fontWeight = if (selectedTierFilter == VipTier.VIP) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SpaGoldContainer,
                            selectedLabelColor = SpaGoldPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Danh sách khách hàng
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredClients, key = { it.id }) { client ->
                    ClientSummaryCard(
                        client = client,
                        onClick = { onSelectClient(client) },
                        onEdit = { clientToEdit = client },
                        onDelete = { clientToDelete = client }
                    )
                }
            }
        }
    }

    // Dialog Thêm khách hàng
    if (showAddDialog) {
        ClientFormDialog(
            initialClient = null,
            onDismissRequest = { showAddDialog = false },
            onSaveClient = { newClient ->
                onAddClient(newClient)
                showAddDialog = false
            }
        )
    }

    // Dialog Sửa khách hàng
    clientToEdit?.let { client ->
        ClientFormDialog(
            initialClient = client,
            onDismissRequest = { clientToEdit = null },
            onSaveClient = { updatedClient ->
                onUpdateClient(updatedClient)
                clientToEdit = null
            }
        )
    }

    // Dialog Xác nhận Xóa khách hàng
    clientToDelete?.let { client ->
        AlertDialog(
            onDismissRequest = { clientToDelete = null },
            title = { Text("Xác nhận xóa khách hàng") },
            text = { Text("Bạn có chắc chắn muốn xóa hồ sơ của khách hàng \"${client.fullName}\" không? Toàn bộ lịch sử điều trị và gói dịch vụ sẽ bị xóa.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient(client.id)
                        clientToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AllergyAlertRed)
                ) {
                    Text("Xóa vĩnh viễn")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { clientToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ClientSummaryCard(
    client: Client,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium, // 16.dp
        colors = CardDefaults.cardColors(containerColor = WarmBeigeSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar chữ viết tắt
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SageGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = client.fullName.split(" ").takeLast(2).map { it.first() }.joinToString(""),
                        style = MaterialTheme.typography.titleMedium,
                        color = SageGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = client.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CharcoalTextPrimary
                        )

                        // VIP / Phân loại Badge
                        Surface(
                            color = if (client.vipTier == VipTier.VIP) SpaGoldContainer else WarmBeigeSurfaceVariant,
                            shape = CircleShape
                        ) {
                            Text(
                                text = client.vipTier.title,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (client.vipTier == VipTier.VIP) SpaGoldPrimary else CharcoalTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${client.phone} • ${client.skinType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CharcoalTextSecondary
                    )
                }

                // Nút Sửa & Xóa
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Sửa",
                        tint = CharcoalTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = AllergyAlertRed,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Gói liệu trình tóm tắt
            if (client.treatmentPackages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = WarmBeigeBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = SageGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${client.treatmentPackages.size} gói liệu trình",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SageGreenPrimary
                        )
                    }

                    val totalRemaining = client.treatmentPackages.sumOf { it.remainingSessions }
                    Text(
                        text = "Còn $totalRemaining buổi khả dụng",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalTextSecondary
                    )
                }
            }
        }
    }
}

