package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.BackgroundLight
import com.example.ui.theme.BorderLight
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySurface
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueHover
import com.example.ui.theme.PrimaryBlueLight
import com.example.ui.theme.StockInGreen
import com.example.ui.theme.StockOutRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppNavDestination
import com.example.ui.viewmodel.InventoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val currentDestination by viewModel.currentDestination.collectAsState()
    val summary by viewModel.dashboardSummary.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSupabaseDialog by remember { mutableStateOf(false) }
    val supabaseSyncStatus by viewModel.supabaseSyncStatus.collectAsState()

    // Live clock state
    var currentDateTimeString by remember {
        mutableStateOf(SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentDateTimeString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            delay(30000L)
        }
    }

    // Snackbar listeners
    LaunchedEffect(viewModel) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }
    LaunchedEffect(viewModel) {
        viewModel.errorEvent.collectLatest { err ->
            snackbarHostState.showSnackbar("Error: $err")
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavyDark,
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                SidebarMenuContent(
                    currentDestination = currentDestination,
                    currentDateTime = currentDateTimeString,
                    lowStockCount = summary.lowStockCount,
                    supabaseStatus = supabaseSyncStatus,
                    onNavigate = { dest ->
                        viewModel.navigateTo(dest)
                        scope.launch { drawerState.close() }
                    },
                    onOpenSupabase = {
                        showSupabaseDialog = true
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.metinex_logo),
                                contentDescription = "Logo",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "METINEX ALUMINIUM",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = currentDestination.label,
                                    fontSize = 11.sp,
                                    color = PrimaryBlueLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Open Sidebar Menu",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Supabase Cloud sync icon
                        val cloudTint = when (val st = supabaseSyncStatus) {
                            is com.example.data.remote.supabase.SupabaseSyncStatus.Connected -> if (st.tablesReady) Color(0xFF10B981) else Color(0xFFF59E0B)
                            is com.example.data.remote.supabase.SupabaseSyncStatus.Syncing -> Color(0xFF38BDF8)
                            is com.example.data.remote.supabase.SupabaseSyncStatus.Error -> Color(0xFFEF4444)
                            else -> Color(0xFF94A3B8)
                        }
                        IconButton(onClick = { showSupabaseDialog = true }) {
                            Icon(
                                Icons.Default.CloudDone,
                                contentDescription = "Supabase Sync Status",
                                tint = cloudTint
                            )
                        }

                        // Notification icon
                        BadgedBox(
                            badge = {
                                if (summary.lowStockCount > 0) {
                                    Badge(containerColor = AlertAmber) {
                                        Text("${summary.lowStockCount}", fontSize = 10.sp)
                                    }
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            IconButton(onClick = { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = NavyDark
                    )
                )
            },
            bottomBar = {
                // Quick navigation bottom bar for mobile
                NavigationBar(
                    containerColor = NavySurface,
                    contentColor = Color.White
                ) {
                    NavigationBarItem(
                        selected = currentDestination == AppNavDestination.HOME,
                        onClick = { viewModel.navigateTo(AppNavDestination.HOME) },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Home", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = NavyLight,
                            unselectedTextColor = NavyLight
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppNavDestination.STOCK_IN,
                        onClick = { viewModel.navigateTo(AppNavDestination.STOCK_IN) },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                        label = { Text("Stock In", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = StockInGreen,
                            unselectedIconColor = NavyLight,
                            unselectedTextColor = NavyLight
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppNavDestination.STOCK_OUT,
                        onClick = { viewModel.navigateTo(AppNavDestination.STOCK_OUT) },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                        label = { Text("Stock Out", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = StockOutRed,
                            unselectedIconColor = NavyLight,
                            unselectedTextColor = NavyLight
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppNavDestination.LIVE_STOCK,
                        onClick = { viewModel.navigateTo(AppNavDestination.LIVE_STOCK) },
                        icon = { Icon(Icons.Default.Inventory2, contentDescription = null) },
                        label = { Text("Live Stock", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = NavyLight,
                            unselectedTextColor = NavyLight
                        )
                    )
                    NavigationBarItem(
                        selected = currentDestination == AppNavDestination.REPORTS,
                        onClick = { viewModel.navigateTo(AppNavDestination.REPORTS) },
                        icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                        label = { Text("Reports", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            indicatorColor = PrimaryBlue,
                            unselectedIconColor = NavyLight,
                            unselectedTextColor = NavyLight
                        )
                    )
                }
            },
            containerColor = BackgroundLight
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    AppNavDestination.HOME -> HomeScreen(viewModel = viewModel)
                    AppNavDestination.STOCK_IN -> StockInScreen(viewModel = viewModel)
                    AppNavDestination.STOCK_OUT -> StockOutScreen(viewModel = viewModel)
                    AppNavDestination.LIVE_STOCK -> LiveStockScreen(viewModel = viewModel)
                    AppNavDestination.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                    AppNavDestination.REPORTS -> ReportsHubScreen(viewModel = viewModel)
                    AppNavDestination.LOGS -> LogScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showSupabaseDialog) {
        SupabaseCloudDialog(
            viewModel = viewModel,
            onDismiss = { showSupabaseDialog = false }
        )
    }
}

@Composable
fun SidebarMenuContent(
    currentDestination: AppNavDestination,
    currentDateTime: String,
    lowStockCount: Int,
    supabaseStatus: com.example.data.remote.supabase.SupabaseSyncStatus,
    onNavigate: (AppNavDestination) -> Unit,
    onOpenSupabase: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
    ) {
        // Company Brand & Logo
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.metinex_logo),
                contentDescription = "Metinex Logo",
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = "METINEX",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "ALUMINIUM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF93C5FD),
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Date & Time Display
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = NavySurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentDateTime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = StockInGreen.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StockInGreen,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main 5 Navigation Items
        Text(
            text = "MAIN MENU",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NavyLight,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        SidebarNavItem(
            label = "1. Home",
            icon = Icons.Default.Home,
            isSelected = currentDestination == AppNavDestination.HOME,
            onClick = { onNavigate(AppNavDestination.HOME) }
        )

        SidebarNavItem(
            label = "2. Stock In (+)",
            icon = Icons.Default.SwapHoriz,
            isSelected = currentDestination == AppNavDestination.STOCK_IN,
            accentColor = StockInGreen,
            onClick = { onNavigate(AppNavDestination.STOCK_IN) }
        )

        SidebarNavItem(
            label = "3. Stock Out (-)",
            icon = Icons.Default.SwapHoriz,
            isSelected = currentDestination == AppNavDestination.STOCK_OUT,
            accentColor = StockOutRed,
            onClick = { onNavigate(AppNavDestination.STOCK_OUT) }
        )

        SidebarNavItem(
            label = "4. Live Stock",
            icon = Icons.Default.Inventory2,
            badge = if (lowStockCount > 0) "$lowStockCount Low" else null,
            badgeColor = AlertAmber,
            isSelected = currentDestination == AppNavDestination.LIVE_STOCK,
            onClick = { onNavigate(AppNavDestination.LIVE_STOCK) }
        )

        SidebarNavItem(
            label = "5. Products Catalog",
            icon = Icons.Default.Category,
            isSelected = currentDestination == AppNavDestination.PRODUCTS,
            onClick = { onNavigate(AppNavDestination.PRODUCTS) }
        )

        SidebarNavItem(
            label = "6. Reports & Excel",
            icon = Icons.Default.Assessment,
            isSelected = currentDestination == AppNavDestination.REPORTS,
            onClick = { onNavigate(AppNavDestination.REPORTS) }
        )

        SidebarNavItem(
            label = "7. Activity Log",
            icon = Icons.Default.History,
            isSelected = currentDestination == AppNavDestination.LOGS,
            onClick = { onNavigate(AppNavDestination.LOGS) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // User Profile & Supabase Status
        val cloudIconTint = when (supabaseStatus) {
            is com.example.data.remote.supabase.SupabaseSyncStatus.Connected -> if (supabaseStatus.tablesReady) Color(0xFF10B981) else Color(0xFFF59E0B)
            is com.example.data.remote.supabase.SupabaseSyncStatus.Syncing -> Color(0xFF38BDF8)
            is com.example.data.remote.supabase.SupabaseSyncStatus.Error -> Color(0xFFEF4444)
            else -> Color(0xFF94A3B8)
        }
        val statusText = when (supabaseStatus) {
            is com.example.data.remote.supabase.SupabaseSyncStatus.Connected -> if (supabaseStatus.tablesReady) "Supabase Active" else "Setup Tables"
            is com.example.data.remote.supabase.SupabaseSyncStatus.Syncing -> "Syncing..."
            is com.example.data.remote.supabase.SupabaseSyncStatus.Error -> "Sync Error"
            else -> "Supabase Cloud"
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSupabase() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = NavySurface)
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "User Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Admin User",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "bzzmtjoejpdfiqrzaqlg",
                        fontSize = 10.sp,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = cloudIconTint.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Supabase Status",
                            tint = cloudIconTint,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = statusText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = cloudIconTint
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color = PrimaryBlue,
    badge: String? = null,
    badgeColor: Color = PrimaryBlue,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) accentColor else Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                )
            }

            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor
                ) {
                    Text(
                        text = badge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
