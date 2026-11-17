package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactFilter
import com.example.data.model.ContactWithSummary
import com.example.data.model.MainTab
import com.example.ui.components.DarkHeroCard
import com.example.ui.components.KuraAvatar
import com.example.ui.components.KuraFormatters
import com.example.ui.theme.CoralDark
import com.example.ui.theme.CoralSoft
import com.example.ui.theme.ErrorContainer
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LedgerCanvas
import com.example.ui.theme.OnErrorContainer
import com.example.ui.theme.OnPrimaryWhite
import com.example.ui.theme.OnSecondaryContainer
import com.example.ui.theme.OnSecondaryFixedVariant
import com.example.ui.theme.OnSurface
import com.example.ui.theme.OnSurfaceVariant
import com.example.ui.theme.OnTertiaryContainer
import com.example.ui.theme.OnTertiaryFixedVariant
import com.example.ui.theme.Outline
import com.example.ui.theme.OutlineVariant
import com.example.ui.theme.PrimaryBlack
import com.example.ui.theme.Saffron
import com.example.ui.theme.SecondaryContainer
import com.example.ui.theme.SecondaryFixed
import com.example.ui.theme.SurfaceContainer
import com.example.ui.theme.SurfaceContainerLow
import com.example.ui.theme.TertiaryFixed
import com.example.ui.theme.TertiaryFixedDim
import com.example.ui.theme.WarmAmberBg
import com.example.ui.viewmodel.KuraLedgerData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun MainLedgerScreen(
    data: KuraLedgerData,
    selectedTab: MainTab,
    filter: ContactFilter,
    searchQuery: String,
    onSelectTab: (MainTab) -> Unit,
    onSelectFilter: (ContactFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenContact: (String) -> Unit,
    onAddContact: () -> Unit,
    onOpenImportContacts: () -> Unit,
    onEditProfile: () -> Unit,
    onTogglePin: () -> Unit,
    onBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onOpenRecentlyDeleted: () -> Unit,
    onOpenRegion: () -> Unit,
    onLoadSampleData: () -> Unit,
    onEraseAllData: () -> Unit
) {
    val currency = data.profile?.currency ?: "GH₵"

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(LedgerCanvas)) {
        val useRail = maxWidth >= 720.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(
                    containerColor = Color.White,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    MainTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { onSelectTab(tab) },
                            icon = {
                                Icon(
                                    imageVector = tabIcon(tab, selected),
                                    contentDescription = tabLabel(tab)
                                )
                            },
                            label = { Text(tabLabel(tab)) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = SecondaryContainer,
                                selectedIconColor = PrimaryBlack,
                                selectedTextColor = PrimaryBlack,
                                unselectedIconColor = Outline,
                                unselectedTextColor = Outline
                            )
                        )
                    }
                }
            }

            Scaffold(
                containerColor = LedgerCanvas,
                contentWindowInsets = WindowInsets.safeDrawing,
                floatingActionButton = {
                    if (selectedTab == MainTab.CUSTOMERS) {
                        ExtendedFloatingActionButton(
                            onClick = onAddContact,
                            containerColor = SecondaryContainer,
                            contentColor = OnSecondaryContainer,
                            shape = CircleShape,
                            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                            text = {
                                Text(
                                    "Add contact",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            },
                            modifier = Modifier.testTag("add_contact_fab")
                        )
                    }
                },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = Color.White.copy(alpha = 0.96f),
                            tonalElevation = 4.dp,
                            modifier = Modifier.navigationBarsPadding()
                        ) {
                            MainTab.entries.forEach { tab ->
                                val selected = selectedTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onSelectTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = tabIcon(tab, selected),
                                            contentDescription = tabLabel(tab)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tabLabel(tab),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
                                            )
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        indicatorColor = SecondaryContainer,
                                        selectedIconColor = PrimaryBlack,
                                        selectedTextColor = PrimaryBlack,
                                        unselectedIconColor = Outline,
                                        unselectedTextColor = Outline
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (selectedTab) {
                        MainTab.CUSTOMERS -> CustomersTabContent(
                            data = data,
                            currency = currency,
                            filter = filter,
                            searchQuery = searchQuery,
                            onSelectFilter = onSelectFilter,
                            onSearchChange = onSearchChange,
                            onOpenContact = onOpenContact,
                            onAddContact = onAddContact,
                            onOpenImportContacts = onOpenImportContacts,
                            onEditProfile = onEditProfile,
                            onGoSettings = { onSelectTab(MainTab.SETTINGS) }
                        )
                        MainTab.SUMMARY -> SummaryTabContent(
                            data = data,
                            currency = currency,
                            onOpenContact = onOpenContact
                        )
                        MainTab.SETTINGS -> SettingsTabContent(
                            data = data,
                            currency = currency,
                            onEditProfile = onEditProfile,
                            onTogglePin = onTogglePin,
                            onOpenImportContacts = onOpenImportContacts,
                            onBackup = onBackup,
                            onRestoreBackup = onRestoreBackup,
                            onOpenRecentlyDeleted = onOpenRecentlyDeleted,
                            onOpenRegion = onOpenRegion,
                            onLoadSampleData = onLoadSampleData,
                            onEraseAllData = onEraseAllData
                        )
                    }
                }
            }
        }
    }
}

private fun tabLabel(tab: MainTab): String = when (tab) {
    MainTab.CUSTOMERS -> "Customers"
    MainTab.SUMMARY -> "Summary"
    MainTab.SETTINGS -> "Settings"
}

private fun tabIcon(tab: MainTab, selected: Boolean): ImageVector = when (tab) {
    MainTab.CUSTOMERS -> if (selected) Icons.Filled.Group else Icons.Outlined.Group
    MainTab.SUMMARY -> if (selected) Icons.Filled.BarChart else Icons.Outlined.BarChart
    MainTab.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
}

@Composable
private fun CustomersTabContent(
    data: KuraLedgerData,
    currency: String,
    filter: ContactFilter,
    searchQuery: String,
    onSelectFilter: (ContactFilter) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenContact: (String) -> Unit,
    onAddContact: () -> Unit,
    onOpenImportContacts: () -> Unit,
    onEditProfile: () -> Unit,
    onGoSettings: () -> Unit
) {
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(15_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val filteredContacts = remember(data.activeContacts, filter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        val qDigits = q.filter(Char::isDigit)
        data.activeContacts.filter { item ->
            val matchFilter = when (filter) {
                ContactFilter.ALL -> true
                ContactFilter.OWE_ME -> item.balanceCents > 0L
                ContactFilter.I_OWE -> item.balanceCents < 0L
            }
            val matchQuery = if (q.isEmpty()) true else {
                item.contact.name.lowercase().contains(q) ||
                    item.contact.phone.lowercase().contains(q) ||
                    item.contact.loc.lowercase().contains(q) ||
                    (qDigits.isNotEmpty() && item.contact.phone.filter(Char::isDigit).contains(qDigits))
            }
            matchFilter && matchQuery
        }
    }

    val totals = data.totals
    val totalVolume = totals.owedToYouCents + totals.youOweCents
    val recvPct = if (totalVolume > 0L) ((totals.owedToYouCents.toDouble() / totalVolume) * 100).roundToInt() else 0
    val payPct = if (totalVolume > 0L) 100 - recvPct else 0

    val allCount = data.activeContacts.size
    val oweMeCount = data.activeContacts.count { it.balanceCents > 0L }
    val iOweCount = data.activeContacts.count { it.balanceCents < 0L }

    LazyColumn(
        state = listState,
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val userName = data.profile?.name?.ifBlank { "Friend" } ?: "Friend"
            val bizName = data.profile?.biz.orEmpty()
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = KuraFormatters.formatHeaderClock(nowMillis),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "${KuraFormatters.greetingForHour()}, $userName",
                        style = MaterialTheme.typography.headlineLarge,
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (bizName.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                Icons.Default.Storefront,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = bizName,
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Surface(
                            onClick = onEditProfile,
                            shape = CircleShape,
                            color = Color.White,
                            border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "+ Add business name",
                                style = MaterialTheme.typography.labelMedium,
                                color = Outline,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .border(2.dp, Saffron.copy(alpha = 0.55f), CircleShape)
                        .background(SecondaryFixed)
                        .clickable(onClick = onGoSettings),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = KuraFormatters.initials(userName),
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSecondaryContainer
                    )
                }
            }
        }

        item {
            DarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Net balance",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        val badgeLabel = when {
                            totals.netBalanceCents < 0L -> "Deficit"
                            totals.netBalanceCents > 0L -> "Surplus"
                            else -> "Even"
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (totals.netBalanceCents < 0L) ErrorRed.copy(alpha = 0.3f) else Saffron
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (totals.netBalanceCents < 0L) CoralDark else PrimaryBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = KuraFormatters.formatSignedMoney(totals.netBalanceCents, currency),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        if (recvPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(recvPct.toFloat())
                                    .fillMaxHeight()
                                    .background(TertiaryFixedDim)
                            )
                        }
                        if (payPct > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(payPct.toFloat())
                                    .fillMaxHeight()
                                    .background(CoralSoft)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$recvPct% receivables",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "$payPct% payables",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroSubTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ArrowDownward,
                            iconTint = TertiaryFixedDim,
                            label = "Owed to you",
                            amount = KuraFormatters.formatMoney(totals.owedToYouCents, currency),
                            countText = "${totals.owedCount} ${if (totals.owedCount == 1) "contact" else "contacts"}",
                            onClick = {
                                onSelectFilter(ContactFilter.OWE_ME)
                                onSearchChange("")
                                scope.launch { listState.animateScrollToItem(3) }
                            }
                        )
                        HeroSubTile(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ArrowUpward,
                            iconTint = CoralSoft,
                            label = "You owe",
                            amount = KuraFormatters.formatMoney(totals.youOweCents, currency),
                            countText = "${totals.oweCount} ${if (totals.oweCount == 1) "contact" else "contacts"}",
                            onClick = {
                                onSelectFilter(ContactFilter.I_OWE)
                                onSearchChange("")
                                scope.launch { listState.animateScrollToItem(3) }
                            }
                        )
                    }
                }
            }
        }

        if (data.overdueCustomers.isNotEmpty()) {
            item {
                val count = data.overdueCustomers.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(WarmAmberBg)
                        .clickable {
                            onSelectFilter(ContactFilter.OWE_ME)
                            onSearchChange("")
                            scope.launch { listState.animateScrollToItem(3) }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Saffron),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryBlack, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "$count ${if (count == 1) "customer owes" else "customers owe"} you with no activity in over a week",
                            style = MaterialTheme.typography.labelLarge,
                            color = OnSurface
                        )
                        Text(
                            text = "${KuraFormatters.formatMoney(data.overdueTotalCents, currency)} waiting to be collected",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your people", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                    Text(
                        text = "$allCount ${if (allCount == 1) "contact" else "contacts"}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search name, phone or location", color = OnSurfaceVariant.copy(alpha = 0.65f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = SecondaryContainer,
                        unfocusedBorderColor = OutlineVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contacts_search_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChipItem("All", allCount, filter == ContactFilter.ALL) {
                        onSelectFilter(ContactFilter.ALL)
                    }
                    FilterChipItem("Owe me", oweMeCount, filter == ContactFilter.OWE_ME) {
                        onSelectFilter(ContactFilter.OWE_ME)
                    }
                    FilterChipItem("I owe", iOweCount, filter == ContactFilter.I_OWE) {
                        onSelectFilter(ContactFilter.I_OWE)
                    }
                }
            }
        }

        if (filteredContacts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Outline, modifier = Modifier.size(30.dp))
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (data.activeContacts.isNotEmpty()) "No one found" else "Your ledger is empty",
                        style = MaterialTheme.typography.headlineSmall,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (data.activeContacts.isNotEmpty()) {
                            "Try a different name or number, or clear the filter."
                        } else {
                            "Add your first customer or import from your phonebook to start tracking who owes you."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                    if (data.activeContacts.isEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddContact,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SecondaryContainer,
                                contentColor = OnSecondaryContainer
                            ),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Add your first customer", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOpenImportContacts,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SurfaceContainerLow,
                                contentColor = OnSurface
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Import from phonebook", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        } else {
            items(filteredContacts, key = { it.contact.id }) { item ->
                ContactRowCard(
                    item = item,
                    currency = currency,
                    nowMillis = nowMillis,
                    onClick = { onOpenContact(item.contact.id) }
                )
            }
        }
    }
}

@Composable
private fun HeroSubTile(
    modifier: Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    amount: String,
    countText: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(amount, style = MaterialTheme.typography.headlineMedium, color = Color.White, maxLines = 1)
        Text(countText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) PrimaryBlack else Color.White,
        border = if (selected) null else BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.6f)),
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) OnPrimaryWhite else OnSurfaceVariant
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = (if (selected) OnPrimaryWhite else OnSurfaceVariant).copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun ContactRowCard(
    item: ContactWithSummary,
    currency: String,
    nowMillis: Long,
    onClick: () -> Unit
) {
    val c = item.contact
    val bal = item.balanceCents
    val daysSince = if (item.lastActivityMs > 0L) (nowMillis - item.lastActivityMs).toDouble() / 86_400_000.0 else 0.0
    val dotColor = when {
        !c.isSupplier && bal > 0L && daysSince > 14.0 -> ErrorRed
        !c.isSupplier && bal > 0L && daysSince > 7.0 -> SecondaryContainer
        else -> null
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contact_card_${c.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KuraAvatar(name = c.name, size = 48.dp, overdueDotColor = dotColor)

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = c.name,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (c.isSupplier) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = "Supplier",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                val locSuffix = if (c.loc.isNotBlank()) " · ${c.loc}" else ""
                Text(
                    text = KuraFormatters.relativeTimeAgo(item.lastActivityMs) + locSuffix,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = KuraFormatters.formatMoney(bal, currency),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        bal > 0L -> OnTertiaryContainer
                        bal < 0L -> ErrorRed
                        else -> Outline
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            when {
                                bal > 0L -> TertiaryFixed
                                bal < 0L -> ErrorContainer
                                else -> SurfaceContainer
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when {
                            bal > 0L -> "owes you"
                            bal < 0L -> "you owe"
                            else -> "settled"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            bal > 0L -> OnTertiaryFixedVariant
                            bal < 0L -> OnErrorContainer
                            else -> OnSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryTabContent(
    data: KuraLedgerData,
    currency: String,
    onOpenContact: (String) -> Unit
) {
    val totals = data.totals
    val weekMax = maxOf(data.last7DaysGivenCents, data.last7DaysCollectedCents, 1L)
    val topMax = data.topDebtors.firstOrNull()?.balanceCents?.coerceAtLeast(1L) ?: 1L

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Summary", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Text("Where your money stands today", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            }
        }

        item {
            DarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Net balance", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = KuraFormatters.formatSignedMoney(totals.netBalanceCents, currency),
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                        Column {
                            Text("Owed to you", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                KuraFormatters.formatMoney(totals.owedToYouCents, currency),
                                style = MaterialTheme.typography.headlineSmall,
                                color = TertiaryFixedDim
                            )
                        }
                        Column {
                            Text("You owe", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                            Text(
                                KuraFormatters.formatMoney(totals.youOweCents, currency),
                                style = MaterialTheme.typography.headlineSmall,
                                color = CoralSoft
                            )
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Last 7 days", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                        Text("Customers", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                    SummaryMetricBar(
                        label = "Credit given",
                        amountText = KuraFormatters.formatMoney(data.last7DaysGivenCents, currency),
                        fraction = data.last7DaysGivenCents.toFloat() / weekMax.toFloat(),
                        barColor = Saffron,
                        icon = Icons.Default.NorthEast,
                        iconBg = SecondaryFixed,
                        iconTint = OnSecondaryFixedVariant
                    )
                    SummaryMetricBar(
                        label = "Payments collected",
                        amountText = KuraFormatters.formatMoney(data.last7DaysCollectedCents, currency),
                        fraction = data.last7DaysCollectedCents.toFloat() / weekMax.toFloat(),
                        barColor = TertiaryFixedDim,
                        icon = Icons.Default.SouthWest,
                        iconBg = TertiaryFixed,
                        iconTint = OnTertiaryFixedVariant
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Who owes you most", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                    if (data.topDebtors.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = OnTertiaryContainer)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No one owes you right now.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                    } else {
                        data.topDebtors.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenContact(item.contact.id) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                KuraAvatar(name = item.contact.name, size = 40.dp, fontSize = 14.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = item.contact.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = OnSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                                        )
                                        Text(
                                            text = KuraFormatters.formatMoney(item.balanceCents, currency),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = OnTertiaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(CircleShape)
                                            .background(SurfaceContainer)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((item.balanceCents.toFloat() / topMax.toFloat()).coerceIn(0.05f, 1f))
                                                .fillMaxHeight()
                                                .clip(CircleShape)
                                                .background(OnTertiaryContainer)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricBar(
    label: String,
    amountText: String,
    fraction: Float,
    barColor: Color,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
                Text(label, style = MaterialTheme.typography.labelLarge, color = OnSurface)
            }
            Text(amountText, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(SurfaceContainer)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction.coerceIn(0.03f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun SettingsTabContent(
    data: KuraLedgerData,
    currency: String,
    onEditProfile: () -> Unit,
    onTogglePin: () -> Unit,
    onOpenImportContacts: () -> Unit,
    onBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onOpenRecentlyDeleted: () -> Unit,
    onOpenRegion: () -> Unit,
    onLoadSampleData: () -> Unit,
    onEraseAllData: () -> Unit
) {
    val profile = data.profile
    val userName = profile?.name?.ifBlank { "Add your name" } ?: "Add your name"
    val bizName = profile?.biz?.ifBlank { "Business name not set" } ?: "Business name not set"
    val cc = profile?.countryCode ?: "233"

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 560.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
        }

        item {
            DarkHeroCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Saffron),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = KuraFormatters.initials(userName),
                            style = MaterialTheme.typography.headlineMedium,
                            color = PrimaryBlack
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(userName, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(bizName, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.6f))

                    val metaItems = listOfNotNull(
                        profile?.phone?.takeIf { it.isNotBlank() }?.let { Icons.Default.Call to it },
                        profile?.loc?.takeIf { it.isNotBlank() }?.let { Icons.Default.LocationOn to it }
                    )
                    if (metaItems.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            metaItems.forEach { (icon, txt) ->
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                    Text(txt, style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.85f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onEditProfile,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.16f),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit profile", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            SettingsGroup(title = "Security") {
                SettingsRowItem(
                    icon = Icons.Default.Pin,
                    title = "Passcode lock",
                    subtitle = if (data.pinLockState.hasPin) "On. Tap to remove" else "Off. Tap to set a passcode",
                    trailing = {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (data.pinLockState.hasPin) TertiaryFixed else SurfaceContainer)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (data.pinLockState.hasPin) "On" else "Off",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (data.pinLockState.hasPin) OnTertiaryFixedVariant else OnSurfaceVariant
                            )
                        }
                    },
                    onClick = onTogglePin,
                    testTag = "settings_pin_lock_row"
                )
            }
        }

        item {
            SettingsGroup(title = "Your data") {
                SettingsRowItem(
                    icon = Icons.Default.Contacts,
                    title = "Import contacts",
                    subtitle = "Add people directly from your phonebook",
                    onClick = onOpenImportContacts,
                    testTag = "settings_import_contacts_row"
                )
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                SettingsRowItem(
                    icon = Icons.Default.Download,
                    title = "Back up data",
                    subtitle = "Copy or share all your records as text",
                    onClick = onBackup
                )
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                SettingsRowItem(
                    icon = Icons.Default.SettingsBackupRestore,
                    title = "Restore from backup",
                    subtitle = "Paste a backup to restore it",
                    onClick = onRestoreBackup
                )
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                val delCount = data.deletedContacts.size
                SettingsRowItem(
                    icon = Icons.Default.RestoreFromTrash,
                    title = "Recently deleted",
                    subtitle = "$delCount ${if (delCount == 1) "contact" else "contacts"}",
                    onClick = onOpenRecentlyDeleted
                )
            }
        }

        item {
            SettingsGroup(title = "Preferences") {
                SettingsRowItem(
                    icon = Icons.Default.Payments,
                    title = "Currency and country",
                    subtitle = "$currency · +$cc",
                    onClick = onOpenRegion
                )
            }
        }

        item {
            SettingsGroup(title = "Data & reset") {
                SettingsRowItem(
                    icon = Icons.Default.RestartAlt,
                    title = "Load sample data",
                    subtitle = "Replace your records with demo contacts",
                    isDestructive = true,
                    onClick = onLoadSampleData,
                    testTag = "settings_load_sample_row"
                )
                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                SettingsRowItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Erase everything",
                    subtitle = "Delete all contacts and records",
                    isDestructive = true,
                    onClick = onEraseAllData
                )
            }
        }

        item {
            Text(
                text = "Kura. stores your ledger safely on this device with passcode lock.",
                style = MaterialTheme.typography.bodySmall,
                color = Outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    testTag: String = "",
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDestructive) ErrorContainer else SurfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) OnErrorContainer else OnSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isDestructive) ErrorRed else OnSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Outline)
        }
    }
}
