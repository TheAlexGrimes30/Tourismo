package com.example.turismo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.core.content.ContextCompat
import com.example.turismo.ui.theme.TurismoTheme
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.image.ImageProvider
import android.Manifest
import android.graphics.PointF
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.IconStyle

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("04b285be-eee6-45c5-8a1f-acfb37111273")
        MapKitFactory.initialize(this)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true

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
    var selectedPoint by remember { mutableStateOf<Point?>(null) }
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    Box(modifier = Modifier.fillMaxSize()) {

        YandexMapComponent(
            modifier = Modifier.fillMaxSize(),
            onMapClick = { point ->
                selectedPoint = point
                showBottomSheet = true
            }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationBar(selectedTab) { selectedTab = it }
        }

        if (selectedTab == 1) {
            WeatherScreen()
        } else if (selectedTab == 2) {
            HistoryScreen(onReturnToHome = { selectedTab = 0 })
        }

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
    }
}

@Composable
fun YandexMapComponent(
    modifier: Modifier = Modifier,
    onMapClick: (Point) -> Unit = {},
    moveToUserLocation: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        if (!granted) {
            Toast.makeText(context, "Необходимо разрешение на доступ к местоположению", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted || !coarseGranted) {
            permissionLauncher.launch(locationPermissions)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            mapWindow.map.move(
                CameraPosition(Point(55.751574, 37.573856), 11f, 0f, 0f),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        }
    }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        factory = {
            mapView.apply {
                mapWindow.map.addInputListener(object : InputListener {
                    override fun onMapTap(map: com.yandex.mapkit.map.Map, point: Point) {
                        val placemark = map.mapObjects.addPlacemark(point)

                        placemark.setIcon(
                            ImageProvider.fromResource(context, R.drawable.add),
                            IconStyle().apply {
                                scale = 5.0f
                                anchor = PointF(0.5f, 1.0f)
                            }
                        )

                        onMapClick(point)
                    }

                    override fun onMapLongTap(map: com.yandex.mapkit.map.Map, point: Point) {}
                })

                val mapKit = MapKitFactory.getInstance()
                val userLocationLayer = mapKit.createUserLocationLayer(mapWindow).apply {
                    isVisible = true
                    isHeadingEnabled = true
                }

                if (moveToUserLocation) {
                    userLocationLayer.setObjectListener(object : UserLocationObjectListener {
                        override fun onObjectAdded(userLocationView: UserLocationView) {
                            userLocationView.arrow?.geometry?.let { userLocation ->
                                mapWindow.map.move(
                                    CameraPosition(userLocation, 15f, 0f, 0f),
                                    Animation(Animation.Type.SMOOTH, 1f),
                                    null
                                )
                            }
                            userLocationLayer.setObjectListener(null)
                        }

                        override fun onObjectRemoved(view: UserLocationView) {}
                        override fun onObjectUpdated(p0: UserLocationView, p1: ObjectEvent) {}
                    })
                }
            }
        },
        modifier = modifier.fillMaxSize()
    )
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
        },
        moveToUserLocation = true
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
    ) { result -> onReturnToHome() }

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
                label = { Text(label, color = if (selectedTab == index) Color.White else Color.Gray) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TurismoTheme {
        MainScreen()
    }
}