package com.example.turismo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.launch
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("04b285be-eee6-45c5-8a1f-acfb37111273")
        MapKitFactory.initialize(this)

        setContent {
            TurismoTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var selectedTab by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (selectedTab) {
            0 -> MapScreen()
            1 -> WeatherScreen()
            2 -> HistoryScreen(onReturnToHome = { selectedTab = 0 })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
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
    }
}


@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Text("🏠", color = if (selectedTab == 0) Color.White else Color.Gray) },
            label = { Text("Home", color = if (selectedTab == 0) Color.White else Color.Gray) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.White,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Black
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Text("⛅", color = if (selectedTab == 1) Color.White else Color.Gray) },
            label = { Text("Weather", color = if (selectedTab == 1) Color.White else Color.Gray) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                unselectedIconColor = Color.Gray,
                selectedTextColor = Color.White,
                unselectedTextColor = Color.Gray,
                indicatorColor = Color.Black
            )
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Text("📜", color = if (selectedTab == 2) Color.White else Color.Gray) },
            label = { Text("History", color = if (selectedTab == 2) Color.White else Color.Gray) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val latitude = remember { mutableStateOf(0.0) }
    val longitude = remember { mutableStateOf(0.0) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val coroutineScope = rememberCoroutineScope()
    val mapView = remember { MapView(context) }

    DisposableEffect(Unit) {
        mapView.onStart()
        onDispose {
            mapView.onStop()
        }
    }

    DisposableEffect(Unit) {
        val map = mapView.map
        val mapObjects = map.mapObjects

        map.move(
            CameraPosition(Point(55.751244, 37.618423), 10.0f, 0.0f, 0.0f),
            Animation(Animation.Type.SMOOTH, 1f),
            null
        )

        val inputListener = object : com.yandex.mapkit.map.InputListener {
            override fun onMapTap(map: Map, point: Point) {
                mapObjects.clear()
                val placemark = mapObjects.addPlacemark(point)
                placemark.setText("Маркер")

                latitude.value = point.latitude
                longitude.value = point.longitude
                showBottomSheet = true
            }

            override fun onMapLongTap(map: Map, point: Point) {}
        }

        map.addInputListener(inputListener)

        onDispose {
            map.removeInputListener(inputListener)
            mapObjects.clear()
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState,
        ) {
            AndroidView(
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_dialog, null).apply {
                        findViewById<TextView>(R.id.latitude_value).text = latitude.value.toString()
                        findViewById<TextView>(R.id.longitude_value).text = longitude.value.toString()

                        findViewById<Button>(R.id.btn_add_location).setOnClickListener {
                            val intent = Intent(ctx, CreateActivity::class.java).apply {
                                putExtra("LATITUDE", latitude.value)
                                putExtra("LONGITUDE", longitude.value)
                            }
                            ctx.startActivity(intent)

                            coroutineScope.launch {
                                bottomSheetState.hide()
                                showBottomSheet = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                update = { view ->
                    view.findViewById<TextView>(R.id.latitude_value).text = latitude.value.toString()
                    view.findViewById<TextView>(R.id.longitude_value).text = longitude.value.toString()
                }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize()
        )
    }
}



@Composable
fun WeatherScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Прогноз погоды", modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun HistoryScreen(onReturnToHome: () -> Unit) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) {
        onReturnToHome()
    }

    LaunchedEffect(Unit) {
        launcher.launch(Intent(context, ListActivity::class.java))
    }

    Box(modifier = Modifier.fillMaxSize()) {}
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TurismoTheme {
        MainScreen()
    }
}
