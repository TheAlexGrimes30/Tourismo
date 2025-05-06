package com.example.turismo

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.turismo.ui.theme.TurismoTheme
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.MapWindow
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("04b285be-eee6-45c5-8a1f-acfb37111273")
        MapKitFactory.initialize(this)

        enableEdgeToEdge()

        setContent {
            TurismoTheme {
                MainScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        if (::mapView.isInitialized) {
            mapView.onStart()
        }
    }

    override fun onStop() {
        if (::mapView.isInitialized) {
            mapView.onStop()
        }
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .height(32.dp)
                .fillMaxWidth()
                .background(Color.DarkGray)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.LightGray)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (searchQuery.text.isEmpty()) {
                        Text(
                            text = "Search",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Start
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(6f)
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> WeatherScreen()
                2 -> HistoryScreen(onReturnToHome = { selectedTab = 0 })
            }
        }

        BottomNavigationBar(selectedTab) { selectedTab = it }
    }
}

@Composable
fun YandexMapComponent(
    modifier: Modifier = Modifier,
    onMapClick: (Point) -> Unit = {}
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }

    AndroidView(
        factory = { ctx ->
            MapView(ctx).apply {
                mapView = this

                val mapWindow: MapWindow = this.mapWindow

                mapWindow.map.move(
                    CameraPosition(Point(55.751574, 37.573856), 11.0f, 0.0f, 0.0f),
                    Animation(Animation.Type.SMOOTH, 1f),
                    null
                )

                mapWindow.map.addInputListener(object : InputListener {
                    override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                        onMapClick(point)
                    }

                    override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {
                    }
                })
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->  }
    )

    DisposableEffect(Unit) {
        onDispose {
            mapView?.onStop()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var selectedPoint by remember { mutableStateOf<Point?>(null) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val coroutineScope = rememberCoroutineScope()

    if (showBottomSheet && selectedPoint != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Новая точка", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Широта: ${"%.6f".format(selectedPoint?.latitude)}")
                Text("Долгота: ${"%.6f".format(selectedPoint?.longitude)}")
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        selectedPoint?.let { point ->
                            val intent = Intent(context, CreateActivity::class.java).apply {
                                putExtra("LATITUDE", point.latitude)
                                putExtra("LONGITUDE", point.longitude)
                            }
                            context.startActivity(intent)
                            coroutineScope.launch {
                                bottomSheetState.hide()
                                showBottomSheet = false
                            }
                        }
                    }
                ) {
                    Text("Добавить место")
                }
            }
        }
    }

    YandexMapComponent(
        modifier = Modifier.fillMaxSize(),
        onMapClick = { point ->
            selectedPoint = point
            showBottomSheet = true
        }
    )
}

@Composable
fun WeatherScreen() {
    Box(modifier = Modifier.fillMaxSize().background(Color.Cyan)) {
        Text(text = "Прогноз погоды", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HistoryScreen(onReturnToHome: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onReturnToHome()
    }

    LaunchedEffect(Unit) {
        val intent = Intent(context, ListActivity::class.java)
        launcher.launch(intent)
    }

    Box(modifier = Modifier.fillMaxSize()) {}
}

@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        listOf(
            "🏠" to "Home",
            "⛅" to "Weather",
            "📜" to "History"
        ).forEachIndexed { index, (icon, label) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Text(icon, color = if (selectedTab == index) Color.White else Color.Gray) },
                label = { Text(label, color = if (selectedTab == index) Color.White else Color.Gray) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Black
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TurismoTheme {
        MainScreen()
    }
}
