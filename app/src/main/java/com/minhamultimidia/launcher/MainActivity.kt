package com.minhamultimidia.launcher

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

private data class Car(val name: String, val image: Int)
private val cars = listOf(
    Car("Golf", R.drawable.car_golf),
    Car("Argo", R.drawable.car_argo),
    Car("Polo", R.drawable.car_polo),
    Car("Jetta", R.drawable.car_jetta),
    Car("Tiggo", R.drawable.car_tiggo),
    Car("Palio", R.drawable.car_palio)
)
private val paintColors = listOf(
    Color.White, Color(0xFFBFC7D1), Color(0xFF252A31), Color(0xFFB21F2D),
    Color(0xFF174E9B), Color(0xFF1D7049), Color(0xFFE0B52D), Color(0xFF77716B)
)
private data class ThemeBg(
    val title: String, val a: Color, val b: Color, val c: Color,
    val imageRes: Int? = null
)
private val backgrounds = listOf(
    ThemeBg("Simbionte", Color(0xFF05070B), Color(0xFF111827), Color(0xFF3478FF)),
    ThemeBg("Simbionte Carnificina", Color(0xFF080608), Color(0xFF281014), Color(0xFFFF263F)),
    ThemeBg("Garagem Âmbar", Color(0xFF090807), Color(0xFF302014), Color(0xFFFF8A24)),
    ThemeBg("Costa Neon", Color(0xFF09091B), Color(0xFF43216D), Color(0xFFFF4FCB)),
    ThemeBg("NFS Underground Garage", Color(0xFF17100A), Color(0xFF5A3B20), Color(0xFFFFB45C), R.drawable.bg_nfs_garage)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN or
            android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { MultimediaApp() } }
    }
}

@Composable
private fun MultimediaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("multimidia", Context.MODE_PRIVATE) }
    var selectedCar by remember { mutableIntStateOf(prefs.getInt("car", 2).coerceIn(0, cars.lastIndex)) }
    var selectedBg by remember { mutableIntStateOf(prefs.getInt("bg", 0).coerceIn(0, backgrounds.lastIndex)) }
    var selectedColor by remember { mutableIntStateOf(prefs.getInt("paint", 0).coerceIn(0, paintColors.lastIndex)) }
    var headlights by remember { mutableStateOf(prefs.getBoolean("lights", false)) }
    var showSettings by remember { mutableStateOf(false) }
    var showGarage by remember { mutableStateOf(false) }
    var showWelcome by remember { mutableStateOf(true) }
    var customBg by remember { mutableStateOf(prefs.getString("custom_bg", null)) }
    var speed by remember { mutableIntStateOf(0) }
    var speedEnabled by remember { mutableStateOf(prefs.getBoolean("show_speed", true)) }
    var batteryEnabled by remember { mutableStateOf(prefs.getBoolean("show_battery", true)) }
    var clockEnabled by remember { mutableStateOf(prefs.getBoolean("show_clock", true)) }
    var shortcutsEnabled by remember { mutableStateOf(prefs.getBoolean("show_shortcuts", true)) }
    var carNameEnabled by remember { mutableStateOf(prefs.getBoolean("show_carname", true)) }
    var welcomeEnabled by remember { mutableStateOf(prefs.getBoolean("show_welcome", true)) }
    var headlightsEnabled by remember { mutableStateOf(prefs.getBoolean("show_lights", true)) }

    val bg = backgrounds[selectedBg]
    val bgBrush = Brush.linearGradient(listOf(bg.a, bg.b, bg.a))
    val customBitmap = remember(customBg) {
        try {
            customBg?.let { uri -> context.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }
        } catch (_: Exception) { null }
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) { }
            customBg = uri.toString()
            prefs.edit().putString("custom_bg", customBg).apply()
        }
    }
    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted -> if (granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true) startGps(context) { speed = it } }

    LaunchedEffect(Unit) {
        delay(2600)
        showWelcome = false
    }

    Box(Modifier.fillMaxSize().background(bgBrush)) {
        // subtle dashboard glow and decorative lines
        Box(Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(bg.c.copy(alpha = .22f), Color.Transparent), radius = 850f)
        ))
        if (bg.imageRes != null) {
            Image(
                painterResource(bg.imageRes),
                contentDescription = "Tema NFS Underground Garage",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .12f)))
        }
        if (customBitmap != null) {
            Image(customBitmap, contentDescription = "Plano de fundo personalizado",
                modifier = Modifier.fillMaxSize().alpha(.30f), contentScale = ContentScale.Crop)
        }
        if (showWelcome && welcomeEnabled) {
            val alpha by animateFloatAsState(if (showWelcome) 1f else 0f, tween(700), label = "welcome")
            Column(Modifier.fillMaxSize().alpha(alpha), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                Text("BEM-VINDO AO", color = Color.White.copy(.75f), fontSize = 20.sp, letterSpacing = 5.sp)
                Text(cars[selectedCar].name.uppercase() + "!", color = bg.c, fontSize = 54.sp,
                    fontWeight = FontWeight.Black, letterSpacing = 8.sp)
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(color = bg.c, trackColor = Color.White.copy(.12f), modifier = Modifier.width(180.dp))
            }
        } else if (showSettings) {
            SettingsScreen(
                bg = bg, selectedBg = selectedBg, onBg = { selectedBg = it; prefs.edit().putInt("bg", it).apply() },
                onCustom = { imagePicker.launch(arrayOf("image/*")) }, customBg = customBg,
                speedEnabled = speedEnabled, onSpeed = { speedEnabled = it; prefs.edit().putBoolean("show_speed", it).apply() },
                batteryEnabled = batteryEnabled, onBattery = { batteryEnabled = it; prefs.edit().putBoolean("show_battery", it).apply() },
                clockEnabled = clockEnabled, onClock = { clockEnabled = it; prefs.edit().putBoolean("show_clock", it).apply() },
                shortcutsEnabled = shortcutsEnabled, onShortcuts = { shortcutsEnabled = it; prefs.edit().putBoolean("show_shortcuts", it).apply() },
                carNameEnabled = carNameEnabled, onCarName = { carNameEnabled = it; prefs.edit().putBoolean("show_carname", it).apply() },
                welcomeEnabled = welcomeEnabled, onWelcome = { welcomeEnabled = it; prefs.edit().putBoolean("show_welcome", it).apply() },
                headlightsEnabled = headlightsEnabled, onHeadlights = { headlightsEnabled = it; prefs.edit().putBoolean("show_lights", it).apply() },
                onHomeSettings = {
                    try {
                        context.startActivity(android.content.Intent("android.settings.HOME_SETTINGS"))
                    } catch (_: Exception) {
                        context.startActivity(android.content.Intent(android.provider.Settings.ACTION_SETTINGS))
                    }
                },
                onBack = { showSettings = false }
            )
        } else {
            Row(Modifier.fillMaxSize().padding(22.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1.25f).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("MINHA MULTIMÍDIA", color = Color.White.copy(.6f), fontSize = 11.sp, letterSpacing = 3.sp)
                            if (carNameEnabled) Text(cars[selectedCar].name.uppercase(), color = Color.White,
                                fontSize = 25.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.weight(1f))
                        if (clockEnabled) ClockView()
                    }
                    Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                        if (headlights && headlightsEnabled) {
                            Box(Modifier.fillMaxWidth(.7f).height(120.dp).blur(45.dp)
                                .background(Color(0xFFFFE9A6).copy(.32f), CircleShape))
                        }
                        Image(painterResource(cars[selectedCar].image), contentDescription = cars[selectedCar].name,
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(.9f),
                            contentScale = ContentScale.Fit,
                            colorFilter = if (selectedColor == 0) null else ColorFilter.tint(paintColors[selectedColor], BlendMode.SrcAtop))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(onClick = { showGarage = !showGarage }, colors = ButtonDefaults.buttonColors(containerColor = bg.c)) {
                            Icon(Icons.Default.DirectionsCar, null); Spacer(Modifier.width(6.dp)); Text("GARAGEM")
                        }
                        Spacer(Modifier.width(10.dp))
                        if (headlightsEnabled) OutlinedButton(onClick = {
                            headlights = !headlights; prefs.edit().putBoolean("lights", headlights).apply()
                        }) {
                            Icon(if (headlights) Icons.Default.LightMode else Icons.Default.Highlight, null)
                            Spacer(Modifier.width(5.dp)); Text(if (headlights) "FAROL ON" else "FAROL OFF")
                        }
                    }
                }
                Spacer(Modifier.width(18.dp))
                Column(Modifier.width(220.dp).fillMaxHeight(), verticalArrangement = Arrangement.SpaceBetween) {
                    if (speedEnabled) MetricCard("VELOCIDADE", "$speed", "km/h", Icons.Default.Speed, bg.c)
                    if (batteryEnabled) MetricCard("BATERIA DO CARRO", "--", "OBD2 não conectado", Icons.Default.BatteryFull, bg.c)
                    if (shortcutsEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ATALHOS", color = Color.White.copy(.55f), fontSize = 11.sp, letterSpacing = 2.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SmallAction("MAPA", Icons.Default.Map, bg.c) { launchApp(context, "com.google.android.apps.maps") }
                                SmallAction("MÚSICA", Icons.Default.MusicNote, bg.c) { launchMusic(context) }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SmallAction("APPS", Icons.Default.Apps, bg.c) { showGarage = true }
                                SmallAction("AJUSTES", Icons.Default.Settings, bg.c) { showSettings = true }
                            }
                        }
                    }
                    TextButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Tune, null, tint = bg.c); Text(" PERSONALIZAR", color = Color.White)
                    }
                }
            }
            if (showGarage) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    GaragePanel(selectedCar, selectedColor, bg.c,
                        onCar = { selectedCar = it; prefs.edit().putInt("car", it).apply() },
                        onColor = { selectedColor = it; prefs.edit().putInt("paint", it).apply() },
                        onClose = { showGarage = false })
                }
            }
        }
    }
    // Start GPS once, after user grants access.
    LaunchedEffect(Unit) {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fine || coarse) startGps(context) { speed = it }
        else locationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }
}

@Composable
private fun ClockView() {
    var now by remember { mutableStateOf(java.time.LocalDateTime.now()) }
    LaunchedEffect(Unit) { while (true) { now = java.time.LocalDateTime.now(); delay(1000) } }
    Column(horizontalAlignment = Alignment.End) {
        Text(now.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")), color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Light)
        Text(now.format(java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM")), color = Color.White.copy(.55f), fontSize = 11.sp)
    }
}

@Composable
private fun MetricCard(title: String, value: String, unit: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.07f))
        .border(1.dp, Color.White.copy(.12f), RoundedCornerShape(18.dp)).padding(15.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp)); Text(title, color = Color.White.copy(.65f), fontSize = 10.sp, letterSpacing = 1.sp)
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Color.White, fontSize = 35.sp, fontWeight = FontWeight.Light)
            Spacer(Modifier.width(5.dp)); Text(unit, color = Color.White.copy(.55f), fontSize = 11.sp, modifier = Modifier.padding(bottom = 7.dp))
        }
    }
}

@Composable
private fun RowScope.SmallAction(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Column(Modifier.weight(1f).clip(RoundedCornerShape(13.dp)).background(Color.White.copy(.07f))
        .clickable { onClick() }.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = accent); Spacer(Modifier.height(4.dp))
        Text(label, color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
    }
}

@Composable
private fun GaragePanel(car: Int, color: Int, accent: Color, onCar: (Int) -> Unit, onColor: (Int) -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
        .background(Color(0xF20A101A)).border(1.dp, Color.White.copy(.15f)).padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SUA GARAGEM", color = Color.White, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.weight(1f)); IconButton(onClick = onClose) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(cars.indices.toList()) { i ->
                Column(Modifier.width(100.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (car == i) accent.copy(.2f) else Color.White.copy(.05f))
                    .border(1.dp, if (car == i) accent else Color.White.copy(.1f), RoundedCornerShape(12.dp))
                    .clickable { onCar(i) }.padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painterResource(cars[i].image), null, Modifier.height(48.dp).fillMaxWidth(), contentScale = ContentScale.Fit)
                    Text(cars[i].name, color = Color.White, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(13.dp))
        Text("COR DA PINTURA", color = Color.White.copy(.65f), fontSize = 10.sp, letterSpacing = 2.sp)
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            paintColors.forEachIndexed { i, c ->
                Box(Modifier.size(28.dp).clip(CircleShape).background(c)
                    .border(if (color == i) 3.dp else 1.dp, if (color == i) accent else Color.White.copy(.5f), CircleShape)
                    .clickable { onColor(i) })
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    bg: ThemeBg, selectedBg: Int, onBg: (Int) -> Unit, onCustom: () -> Unit, customBg: String?,
    speedEnabled: Boolean, onSpeed: (Boolean) -> Unit, batteryEnabled: Boolean, onBattery: (Boolean) -> Unit,
    clockEnabled: Boolean, onClock: (Boolean) -> Unit, shortcutsEnabled: Boolean, onShortcuts: (Boolean) -> Unit,
    carNameEnabled: Boolean, onCarName: (Boolean) -> Unit, welcomeEnabled: Boolean, onWelcome: (Boolean) -> Unit,
    headlightsEnabled: Boolean, onHeadlights: (Boolean) -> Unit,
    onHomeSettings: () -> Unit, onBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("CONFIGURAÇÕES DO PAINEL", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.weight(1f)); TextButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null); Text(" VOLTAR") }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            Column(Modifier.weight(1f)) {
                Text("PLANOS DE FUNDO", color = Color.White.copy(.7f), fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(10.dp))
                backgrounds.forEachIndexed { i, theme ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (i == selectedBg) theme.c.copy(.13f) else Color.White.copy(.035f))
                        .border(if (i == selectedBg) 1.dp else .5.dp,
                            if (i == selectedBg) theme.c else Color.White.copy(.12f), RoundedCornerShape(12.dp))
                        .clickable { onBg(i) }.padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(46.dp).height(30.dp).clip(RoundedCornerShape(7.dp))
                            .background(Brush.linearGradient(listOf(theme.a, theme.b, theme.c))))
                        Spacer(Modifier.width(11.dp))
                        Column {
                            Text(theme.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Tema esportivo", color = Color.White.copy(.48f), fontSize = 10.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        if (i == selectedBg) Icon(Icons.Default.CheckCircle, null, tint = theme.c)
                    }
                }
                OutlinedButton(onClick = onCustom, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Image, null); Text("  ESCOLHER / INSTALAR IMAGEM")
                }
                if (customBg != null) Text("Imagem personalizada salva", color = Color.LightGray, fontSize = 10.sp)
            }
            Column(Modifier.weight(1f)) {
                Text("O QUE APARECE NO PAINEL", color = Color.White.copy(.7f), fontSize = 11.sp, letterSpacing = 2.sp)
                Spacer(Modifier.height(8.dp))
                SettingToggle("Velocidade GPS", speedEnabled, onSpeed)
                SettingToggle("Bateria do veículo (OBD2)", batteryEnabled, onBattery)
                SettingToggle("Relógio e data", clockEnabled, onClock)
                SettingToggle("Atalhos", shortcutsEnabled, onShortcuts)
                SettingToggle("Nome do carro", carNameEnabled, onCarName)
                SettingToggle("Animação de boas-vindas", welcomeEnabled, onWelcome)
                SettingToggle("Controle visual dos faróis", headlightsEnabled, onHeadlights)
                Spacer(Modifier.height(14.dp))
                OutlinedButton(onClick = onHomeSettings, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Home, null)
                    Spacer(Modifier.width(8.dp))
                    Text("ESCOLHER TELA INICIAL DO ANDROID")
                }
                Text(
                    "Use esta opção para voltar ao launcher original ou definir a Minha Multimídia como tela inicial. A escolha é feita pelo Android; o aplicativo não altera o firmware.",
                    color = Color.White.copy(.55f), fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

private fun startGps(context: Context, onSpeed: (Int) -> Unit) {
    try {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                onSpeed((location.speed * 3.6f).toInt().coerceAtLeast(0))
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) { onSpeed(0) }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val provider = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            lm.requestLocationUpdates(provider, 1000L, 1f, listener)
        }
    } catch (_: Exception) { onSpeed(0) }
}

private fun launchApp(context: Context, packageName: String) {
    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (intent != null) context.startActivity(intent)
}
private fun launchMusic(context: Context) {
    val candidates = listOf("com.google.android.apps.youtube.music", "com.spotify.music", "com.android.music")
    candidates.firstNotNullOfOrNull { context.packageManager.getLaunchIntentForPackage(it) }?.let { context.startActivity(it) }
}
