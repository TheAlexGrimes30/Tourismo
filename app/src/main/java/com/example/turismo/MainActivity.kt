package com.example.turismo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                .background(Color.LightGray)
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
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Text("🏠", color = if (selectedTab == 0) Color.White else Color.Gray) },
            label = { Text("Home", color = if (selectedTab == 0) Color.White else Color.Gray) },
            colors = NavigationBarItemDefaults.colors()
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Text("⛅", color = if (selectedTab == 1) Color.White else Color.Gray) },
            label = { Text("Weather", color = if (selectedTab == 1) Color.White else Color.Gray) },
            colors = NavigationBarItemDefaults.colors()
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Text("📜", color = if (selectedTab == 2) Color.White else Color.Gray) },
            label = { Text("History", color = if (selectedTab == 2) Color.White else Color.Gray) },
            colors = NavigationBarItemDefaults.colors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val latitude = remember { mutableStateOf(0.0) }
    val longitude = remember { mutableStateOf(0.0) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val coroutineScope = rememberCoroutineScope()

    val generateRandomCoordinates: () -> Unit = {
        latitude.value = Random.nextDouble(-90.0, 90.0)
        longitude.value = Random.nextDouble(-180.0, 180.0)
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = bottomSheetState,
        ) {
            AndroidView(
                factory = { ctx ->
                    LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_dialog, null).apply {
                        val latitudeTextView = findViewById<TextView>(R.id.latitude_value)
                        val longitudeTextView = findViewById<TextView>(R.id.longitude_value)

                        generateRandomCoordinates()

                        latitudeTextView.text = latitude.value.toString()
                        longitudeTextView.text = longitude.value.toString()

                        val addButton = findViewById<Button>(R.id.btn_add_location)
                        addButton.setOnClickListener {
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .clickable {
                if (!showBottomSheet) {
                    showBottomSheet = true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Здесь будет карта (нажми, чтобы добавить место)",
            modifier = Modifier.padding(16.dp)
        )
    }
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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    TurismoTheme {
        MainScreen()
    }
}
