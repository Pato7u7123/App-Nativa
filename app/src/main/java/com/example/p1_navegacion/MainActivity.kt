package com.example.p1_navegacion

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

// ========== CORRECCIÓN: IMPORTS PARA CORRUTINAS ==========
import kotlinx.coroutines.delay
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
// ========================================================

// ==============================================================================
// ENTRY POINT
// ==============================================================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Navegacion() }
    }
}

// ==============================================================================
// MODELOS DE DATOS GLOBALES
// ==============================================================================

data class AppIcono(
    val nombre: String,
    val icono: ImageVector?,
    val iconoRes: Int? = null,
    val color: Color,
    val ruta: String
)

// ---------- MoveCity ----------

enum class OcupacionRuta { VACIA, MEDIA, LLENA }

data class ParadaRuta(
    val nombre: String,
    val minutos: Int,
    val esFinal: Boolean = false
)

data class LineaTransporte(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val estado: String,
    val color: Color,
    val colorFondo: Color,
    val icono: ImageVector?,
    val iconoRes: Int? = null,
    val ocupacion: OcupacionRuta,
    val proximaLlegada: Int,
    val distancia: String,
    val precio: String,
    val paradas: List<ParadaRuta> = emptyList(),
    val reportes: Int = 0,
    val favorita: Boolean = false
)

val LINEAS_INICIALES = listOf(
    LineaTransporte(
        id = 1,
        nombre = "Línea MoveCity Express",
        tipo = "Servicio Preferente",
        estado = "Normal",
        color = Color(0xFFEA5B0C),
        colorFondo = Color(0xFFFFF1EB),
        icono = Icons.Default.DirectionsBus,
        iconoRes = R.drawable.logo,
        ocupacion = OcupacionRuta.MEDIA,
        proximaLlegada = 3,
        distancia = "150 m",
        precio = "$9.00",
        reportes = 14,
        paradas = listOf(
            ParadaRuta("CAPU Terminal", 0),
            ParadaRuta("Mercado Hidalgo", 4),
            ParadaRuta("Centro Histórico", 8),
            ParadaRuta("Av. Juárez", 12),
            ParadaRuta("Tecnológico", 17),
            ParadaRuta("San Manuel (Final)", 22, esFinal = true)
        )
    ),
    LineaTransporte(
        id = 2,
        nombre = "Línea A – Diagonal",
        tipo = "Tren Urbano",
        estado = "Retraso 10 min",
        color = Color(0xFF1E88E5),
        colorFondo = Color(0xFFE8F3FD),
        icono = Icons.Default.DirectionsSubway,
        ocupacion = OcupacionRuta.LLENA,
        proximaLlegada = 14,
        distancia = "320 m",
        precio = "$12.00",
        reportes = 31,
        paradas = listOf(
            ParadaRuta("Estación Norte", 0),
            ParadaRuta("Plaza Dorada", 5),
            ParadaRuta("Diagonal Defensores", 9),
            ParadaRuta("Forum Puebla", 13),
            ParadaRuta("Angelópolis (Final)", 18, esFinal = true)
        )
    ),
    LineaTransporte(
        id = 3,
        nombre = "Línea 3 – Valsequillo",
        tipo = "Autobús",
        estado = "Normal",
        color = Color(0xFF43A047),
        colorFondo = Color(0xFFEBF5EB),
        icono = Icons.Default.DirectionsBus,
        ocupacion = OcupacionRuta.VACIA,
        proximaLlegada = 7,
        distancia = "80 m",
        precio = "$8.00",
        reportes = 7,
        paradas = listOf(
            ParadaRuta("Zócalo", 0),
            ParadaRuta("5 de Mayo", 3),
            ParadaRuta("Blvd. Valsequillo", 8),
            ParadaRuta("Lago Valsequillo", 13),
            ParadaRuta("Africam Safari (Final)", 20, esFinal = true)
        )
    ),
    LineaTransporte(
        id = 4,
        nombre = "Línea 2 – Periférico",
        tipo = "Autobús Rápido",
        estado = "Normal",
        color = Color(0xFF8E24AA),
        colorFondo = Color(0xFFF3E5F5),
        icono = Icons.Default.DirectionsBus,
        ocupacion = OcupacionRuta.MEDIA,
        proximaLlegada = 9,
        distancia = "500 m",
        precio = "$10.00",
        reportes = 19,
        paradas = listOf(
            ParadaRuta("Terminal Oriente", 0),
            ParadaRuta("Blvd. Norte", 6),
            ParadaRuta("Periférico Echegaray", 11),
            ParadaRuta("UDLAP", 16),
            ParadaRuta("San Andrés Cholula (Final)", 22, esFinal = true)
        )
    )
)

val ALERTAS_MOVECITY = listOf(
    Triple("⚠️ Línea A – Diagonal",
        "Retraso de 10 min por obras en Blvd. Héroes del 5 de Mayo. Se recomienda tomar la Línea 3 como alternativa.",
        Color(0xFFFFF3E0)),
    Triple("ℹ️ MoveCity Express",
        "Servicio operando con normalidad. Próxima unidad con espacio disponible en 3 minutos.",
        Color(0xFFE8F5E9)),
    Triple("🟢 Línea 3 – Valsequillo",
        "Unidad vacía disponible. Óptima para evitar saturación. Última actualización: hace 1 min.",
        Color(0xFFE8F5E9)),
    Triple("🔴 Línea 2 – Periférico",
        "Alta demanda en hora pico. Se espera normalización a las 20:00 hrs.",
        Color(0xFFFFEBEE))
)

// ==============================================================================
// NAVEGACIÓN PRINCIPAL
// ==============================================================================

@Composable
fun Navegacion() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "PantallaBloqueo") {
        composable("PantallaBloqueo")       { PantallaBloqueo(navController) }
        composable("PantallaInicioCelular") { PantallaInicioCelular(navController) }
        composable("PantallaCamara")        { PantallaCamara(navController) }
        composable("PantallaCalculadora")   { PantallaCalculadora(navController) }
        composable("PantallaTelefono")      { PantallaTelefono(navController) }
        composable("PantallaGaleria")       { PantallaGaleria(navController) }
        composable("PantallaNotas")         { PantallaNotas(navController) }
        composable("PantallaClima")         { PantallaClima(navController) }
        composable("PantallaMoveCity")      { PantallaMoveCity(navController) }
        composable("PantallaStore")         { PantallaGenerica(navController, "App Store") }
        composable("PantallaPlayStore")     { PantallaGenerica(navController, "Google Play Store") }
        composable("PantallaGoogle")        { PantallaGenerica(navController, "Google Search") }
        composable("PantallaInstagram")     { PantallaGenerica(navController, "Instagram") }
        composable("PantallaAjustes")       { PantallaGenerica(navController, "Ajustes del Sistema") }
        composable("PantallaPixelskyline")  { PantallaGenerica(navController, "Pixelskyline") }
    }
}

// ==============================================================================
// PANTALLA GENÉRICA (SCAFFOLD DE RESPALDO)
// ==============================================================================

@Composable
fun PantallaGenerica(navController: NavController, nombre: String) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1C1C1E)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(nombre, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
        }
    }
}

// ==============================================================================
// 1. PANTALLA DE BLOQUEO
// ==============================================================================

@Composable
fun PantallaBloqueo(navController: NavController) {
    val currentDate = remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            currentDate.value = Date()
        }
    }
    val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = timeFormat.format(currentDate.value),
            fontSize = 86.sp, fontWeight = FontWeight.Thin,
            color = Color.White, textAlign = TextAlign.Center, letterSpacing = 4.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = dateFormat.format(currentDate.value).replaceFirstChar { it.uppercase() },
            fontSize = 18.sp, color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(160.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable { navController.navigate("PantallaInicioCelular") }
        ) {
            Column(
                modifier = Modifier.size(85.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Fingerprint, contentDescription = "Huella",
                    modifier = Modifier.size(48.dp), tint = Color.White.copy(alpha = 0.9f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Touch ID o Face ID", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.7f))
            Text("Presiona para desbloquear", fontSize = 13.sp, color = Color.White.copy(alpha = 0.45f))
        }
    }
}

// ==============================================================================
// 2. PANTALLA INICIO (ESCRITORIO)
// ==============================================================================

@Composable
fun PantallaInicioCelular(navController: NavController) {
    var paginaActual by remember { mutableStateOf(0) }
    val currentTime = remember { mutableStateOf(Date()) }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        while (true) { kotlinx.coroutines.delay(1000); currentTime.value = Date() }
    }
    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())

    val pagina1 = listOf(
        AppIcono("Store",     Icons.Default.ShoppingBag, null, Color(0xFF007AFF), "PantallaStore"),
        AppIcono("Galería",   Icons.Default.Collections, null, Color(0xFF34C759), "PantallaGaleria"),
        AppIcono("Play Store",Icons.Default.PlayArrow,   null, Color(0xFF4285F4), "PantallaPlayStore"),
        AppIcono("Google",    Icons.Default.Search,      null, Color(0xFFEA4335), "PantallaGoogle"),
        AppIcono("Facebook",  Icons.Default.People,      null, Color(0xFF1877F2), "https://www.facebook.com")
    )
    val pagina2 = listOf(
        AppIcono("Cámara",      Icons.Default.CameraAlt,  null, Color(0xFFFF3B30), "PantallaCamara"),
        AppIcono("Teléfono",    Icons.Default.Call,        null, Color(0xFF34C759), "PantallaTelefono"),
        AppIcono("Calculadora", Icons.Default.Calculate,   null, Color(0xFF5856D6), "PantallaCalculadora"),
        AppIcono("WhatsApp",    Icons.Default.Forum,       null, Color(0xFF25D366), "https://wa.me"),
        AppIcono("Instagram",   Icons.Default.PhotoCamera, null, Color(0xFFE4405F), "PantallaInstagram")
    )
    val pagina3 = listOf(
        AppIcono("YouTube",   Icons.Default.PlayCircle, null,            Color(0xFFFF0000), "https://www.youtube.com"),
        AppIcono("MOVE CITY", null,                     R.drawable.logo, Color(0xFFFFFFFF), "PantallaMoveCity"),
        AppIcono("Clima",     Icons.Default.WbSunny,    null,            Color(0xFF2F80ED), "PantallaClima"),
        AppIcono("Notas",     Icons.Default.NoteAlt,    null,            Color(0xFFF2C94C), "PantallaNotas"),
        AppIcono("Ajustes",   Icons.Default.Settings,   null,            Color(0xFF8E8E93), "PantallaAjustes")
    )
    val paginas = listOf(pagina1, pagina2, pagina3)

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 54.dp, start = 24.dp, end = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(timeFormat.format(currentTime.value), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Row {
                Icon(Icons.Default.Notifications, null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("37%", fontSize = 13.sp, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("24°C", fontSize = 52.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(start = 24.dp))
        Text("Ciudad de México", fontSize = 24.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.padding(start = 24.dp))
        Text("Parcialmente nublado durante la tarde.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(start = 24.dp, top = 4.dp))
        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(paginas[paginaActual].chunked(3)) { rowApps ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    rowApps.forEach { app ->
                        Column(
                            modifier = Modifier.weight(1f).clickable {
                                if (app.ruta.startsWith("http")) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(app.ruta))
                                    context.startActivity(intent)
                                } else { navController.navigate(app.ruta) }
                            },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Column(
                                modifier = Modifier.size(65.dp).clip(RoundedCornerShape(16.dp)).background(app.color),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                if (app.iconoRes != null) {
                                    Image(painter = painterResource(id = app.iconoRes), contentDescription = app.nombre,
                                        modifier = Modifier.size(65.dp), contentScale = ContentScale.Crop)
                                } else if (app.icono != null) {
                                    Icon(app.icono, contentDescription = app.nombre, modifier = Modifier.size(32.dp), tint = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(app.nombre, fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center, maxLines = 1)
                        }
                    }
                    repeat(3 - rowApps.size) { Spacer(modifier = Modifier.weight(1f)) }
                }
            }
        }

        // Paginador
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            paginas.indices.forEach { index ->
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp)
                        .size(if (paginaActual == index) 24.dp else 20.dp)
                        .clip(CircleShape)
                        .background(if (paginaActual == index) Color.White else Color.White.copy(alpha = 0.35f))
                        .clickable { paginaActual = index },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (paginaActual == index) Spacer(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF1A1A1A)))
                }
            }
        }

        // Dock
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            listOf(
                AppIcono("Teléfono", Icons.Default.Call,       null, Color(0xFF34C759), "PantallaTelefono"),
                AppIcono("Galería",  Icons.Default.Collections, null, Color(0xFF5856D6), "PantallaGaleria"),
                AppIcono("Cámara",   Icons.Default.CameraAlt,  null, Color(0xFFFF3B30), "PantallaCamara"),
                AppIcono("Notas",    Icons.Default.NoteAlt,    null, Color(0xFFF2C94C), "PantallaNotas"),
                AppIcono("Ajustes",  Icons.Default.Settings,   null, Color(0xFF8E8E93), "PantallaAjustes")
            ).forEach { app ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { navController.navigate(app.ruta) }) {
                    Column(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(12.dp)).background(app.color),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        if (app.icono != null) Icon(app.icono, contentDescription = app.nombre, modifier = Modifier.size(26.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(app.nombre, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }
        }
    }
}

// ==============================================================================
// 3. PANTALLA TELÉFONO
// ==============================================================================

@Composable
fun PantallaTelefono(navController: NavController) {
    var numero by remember { mutableStateOf("") }
    val context = LocalContext.current
    val teclado = listOf(
        listOf("1","2","3"), listOf("4","5","6"),
        listOf("7","8","9"), listOf("*","0","#")
    )
    Column(modifier = Modifier.fillMaxSize().background(Color.Black), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 44.dp, start = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(numero, fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Light, maxLines = 1, modifier = Modifier.padding(horizontal = 24.dp))
        Spacer(modifier = Modifier.weight(1f))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            teclado.forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    fila.forEach { digito ->
                        Column(
                            modifier = Modifier.size(75.dp).clip(CircleShape)
                                .background(Color(0xFF333333)).clickable { numero += digito },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                        ) { Text(digito, color = Color.White, fontSize = 30.sp) }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 40.dp)) {
                Spacer(modifier = Modifier.size(75.dp))
                Column(
                    modifier = Modifier.size(75.dp).clip(CircleShape).background(Color(0xFF34C759))
                        .clickable { if (numero.isNotEmpty()) Toast.makeText(context, "Llamando al $numero...", Toast.LENGTH_LONG).show() },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) { Icon(Icons.Default.Call, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
                Column(
                    modifier = Modifier.size(75.dp).clip(CircleShape)
                        .clickable { if (numero.isNotEmpty()) numero = numero.dropLast(1) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                ) { if (numero.isNotEmpty()) Icon(Icons.Default.Backspace, null, tint = Color.LightGray) }
            }
        }
    }
}

// ==============================================================================
// 4. PANTALLA GALERÍA
// ==============================================================================

@Composable
fun PantallaGaleria(navController: NavController) {
    val urls = listOf(
        "https://picsum.photos/id/10/400/400","https://picsum.photos/id/11/400/400",
        "https://picsum.photos/id/12/400/400","https://picsum.photos/id/13/400/400",
        "https://picsum.photos/id/14/400/400","https://picsum.photos/id/15/400/400",
        "https://picsum.photos/id/16/400/400","https://picsum.photos/id/17/400/400",
        "https://picsum.photos/id/18/400/400"
    )
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F14)).padding(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 44.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.08f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Biblioteca", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text("Fotos recientes", color = Color.Gray, fontSize = 14.sp)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(urls) { url ->
                AsyncImage(
                    model = url, contentDescription = "Imagen",
                    modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp))
                        .border(0.5.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

// ==============================================================================
// 5. PANTALLA NOTAS
// ==============================================================================

@Composable
fun PantallaNotas(navController: NavController) {
    val listaNotas = remember {
        mutableStateListOf(
            "Lista de Súper:\n• Leche\n• Huevos\n• Pan integral",
            "Recordatorio:\nLlamar al taller mecánico a las 4:00 PM.",
            "Proyectos:\nTerminar la integración de Moovit en MoveCity."
        )
    }
    var campoTexto by remember { mutableStateOf("") }
    val colores = listOf(Color(0xFFFFE082), Color(0xFFC5E1A5), Color(0xFF90CAF9), Color(0xFFF48FB1))

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121214)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 44.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.08f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Bloc de Notas", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = campoTexto, onValueChange = { campoTexto = it },
                placeholder = { Text("Añadir nota...", color = Color.Gray) },
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1F1F23), unfocusedContainerColor = Color(0xFF1F1F23),
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                ), maxLines = 3
            )
            Spacer(modifier = Modifier.width(10.dp))
            IconButton(
                onClick = { if (campoTexto.isNotBlank()) { listaNotas.add(campoTexto); campoTexto = "" } },
                modifier = Modifier.size(52.dp).background(Color(0xFFFFC107), RoundedCornerShape(14.dp))
            ) { Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.Black) }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(listaNotas.size) { index ->
                Column(
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colores[index % colores.size]).padding(14.dp)
                ) {
                    Text(listaNotas[index], color = Color(0xFF1C1C1E), fontSize = 14.sp,
                        fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 5)
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = Color(0xFFC62828),
                        modifier = Modifier.align(Alignment.End).size(22.dp).clickable { listaNotas.removeAt(index) })
                }
            }
        }
    }
}

// ==============================================================================
// 6. PANTALLA CLIMA
// ==============================================================================

@Composable
fun PantallaClima(navController: NavController) {
    val horasClima = listOf("Ahora" to "24°","19:00" to "22°","20:00" to "21°","21:00" to "19°","22:00" to "18°","23:00" to "18°")
    Column(
        modifier = Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1F619E), Color(0xFF5A99D4))))
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 54.dp), horizontalArrangement = Arrangement.Start) {
            IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Ciudad de México", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text("Pronóstico de Hoy", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
        Spacer(modifier = Modifier.height(40.dp))
        Icon(Icons.Default.WbCloudy, null, tint = Color.White, modifier = Modifier.size(110.dp))
        Spacer(modifier = Modifier.height(20.dp))
        Text("24°C", color = Color.White, fontSize = 76.sp, fontWeight = FontWeight.Light)
        Text("Parcialmente Nublado", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(30.dp))
        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f)).padding(16.dp)) {
            Text("Pronóstico por hora", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                items(horasClima) { (hora, temp) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(hora, color = Color.White, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(Icons.Default.WbCloudy, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(temp, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==============================================================================
// 7. PANTALLA CÁMARA
// ==============================================================================

@Composable
fun PantallaCamara(navController: NavController) {
    val context = LocalContext.current
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { cameraPermissionGranted = it }

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
            PackageManager.PERMISSION_GRANTED -> cameraPermissionGranted = true
            else -> permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 44.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { navController.popBackStack() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("←", color = Color.White, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Cámara", fontSize = 20.sp, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        when {
            !cameraPermissionGranted -> Column(modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("📷", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Sin permiso de cámara", fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.2f))
                    .clickable { permissionLauncher.launch(Manifest.permission.CAMERA) }.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Conceder permiso", color = Color.White, fontSize = 16.sp)
                }
            }
            capturedImage != null -> Column(modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("📸", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("¡Foto capturada!", fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Column(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.2f))
                    .clickable { capturedImage = null }.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Tomar otra", color = Color.White, fontSize = 16.sp)
                }
            }
            else -> AndroidView(factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = androidx.camera.core.Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                        imageCapture = ImageCapture.Builder().build()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(ctx as LifecycleOwner, androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                    } catch (e: Exception) { e.printStackTrace() }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            }, modifier = Modifier.fillMaxWidth().weight(1f))
        }
        if (cameraPermissionGranted && capturedImage == null) {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.Center) {
                Column(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White).clickable {
                    imageCapture?.takePicture(ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            capturedImage = imageProxyToBitmap(image); image.close()
                            Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
                        }
                        override fun onError(exc: ImageCaptureException) {
                            Toast.makeText(context, "Error: ${exc.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.size(62.dp).clip(CircleShape).border(2.dp, Color.Black, CircleShape))
                }
            }
        }
    }
}

// ==============================================================================
// 8. PANTALLA CALCULADORA
// ==============================================================================

@Composable
fun PantallaCalculadora(navController: NavController) {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }
    var lastResult by remember { mutableStateOf("") }
    val buttons = listOf(
        listOf("AC","±","%","÷"),
        listOf("7","8","9","×"),
        listOf("4","5","6","-"),
        listOf("1","2","3","+"),
        listOf("0",".","√","=")
    )

    fun evaluateAdvanced(expr: String): String {
        if (expr.isEmpty()) return "0"
        if (expr == "Error") return "Error"
        return try {
            val clean = expr.replace("÷","/").replace("×","*").replace("√","sqrt")
            if (clean.contains("/0") && !clean.contains("0.")) return "Error"
            val r = evaluateArithmeticExpression(clean)
            when {
                r.isNaN() || r.isInfinite() -> "Error"
                r == r.toLong().toDouble() -> r.toLong().toString()
                else -> String.format("%.10f", r).trimEnd('0').trimEnd('.')
            }
        } catch (e: Exception) { "Error" }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Row(modifier = Modifier.fillMaxWidth().padding(top = 54.dp, start = 20.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { navController.popBackStack() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("←", color = Color.White, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text("Calculadora", fontSize = 20.sp, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalAlignment = Alignment.End) {
            if (expression.isNotEmpty() && expression != lastResult) {
                Text(evaluateAdvanced(expression), fontSize = 24.sp, color = Color.White.copy(alpha = 0.5f), maxLines = 1)
            } else Spacer(modifier = Modifier.height(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(if (expression.isEmpty()) "0" else expression, fontSize = 64.sp, fontWeight = FontWeight.Light,
                color = Color.White, maxLines = 2, letterSpacing = 2.sp, textAlign = TextAlign.End)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            buttons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { btn ->
                        val bColor = when {
                            btn in listOf("AC","±","%","√") -> Color(0xFF505050)
                            btn in listOf("÷","×","-","+","=") -> Color(0xFFFF9F0A)
                            else -> Color(0xFF2C2C2E)
                        }
                        val bWidth = if (btn == "0") 160.dp else 80.dp
                        Column(
                            modifier = Modifier.size(width = bWidth, height = 80.dp).clip(CircleShape)
                                .background(bColor).clickable {
                                    when (btn) {
                                        "AC" -> { expression = ""; result = "0"; lastResult = "" }
                                        "±"  -> {
                                            if (expression.isNotEmpty()) {
                                                val nums = expression.split(Regex("(?<=[+\\-×÷])|(?=[+\\-×÷])"))
                                                if (nums.isNotEmpty()) {
                                                    val last = nums.last()
                                                    if (last.isNotEmpty() && last !in listOf("+","-","×","÷")) {
                                                        val newLast = if (last.startsWith("-")) last.substring(1) else "-$last"
                                                        expression = expression.dropLast(last.length) + newLast
                                                    }
                                                }
                                            }
                                        }
                                        "=" -> {
                                            if (expression.isNotEmpty() && expression != lastResult) {
                                                val ev = evaluateAdvanced(expression)
                                                if (ev != "Error") { result = ev; lastResult = expression; expression = ev }
                                                else { result = "Error"; expression = "Error" }
                                            }
                                        }
                                        "%" -> {
                                            if (expression.isNotEmpty()) {
                                                val ev = evaluateAdvanced(expression.replace("%",""))
                                                if (ev != "Error") {
                                                    val pct = ev.toDouble() / 100
                                                    expression = if (pct == pct.toLong().toDouble()) pct.toLong().toString() else pct.toString()
                                                } else expression = "Error"
                                            }
                                        }
                                        "√" -> {
                                            if (expression.isNotEmpty()) {
                                                val ev = evaluateAdvanced(expression)
                                                if (ev != "Error") {
                                                    val num = ev.toDouble()
                                                    if (num < 0) expression = "Error"
                                                    else {
                                                        val sq = kotlin.math.sqrt(num)
                                                        expression = if (sq == sq.toLong().toDouble()) sq.toLong().toString() else sq.toString()
                                                    }
                                                } else expression = "Error"
                                            }
                                        }
                                        else -> {
                                            if (expression == "Error") { expression = ""; result = "0" }
                                            if (btn == ".") {
                                                val lastNum = expression.split(Regex("(?<=[+\\-×÷])|(?=[+\\-×÷])")).lastOrNull() ?: ""
                                                if (lastNum.contains(".")) return@clickable
                                            }
                                            if (btn in listOf("+","-","×","÷") && expression.isNotEmpty()) {
                                                if (expression.last() in listOf('+','-','×','÷')) {
                                                    expression = expression.dropLast(1) + btn; return@clickable
                                                }
                                            }
                                            expression += btn
                                        }
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                        ) {
                            Text(btn, fontSize = when(btn) { "±","√" -> 28.sp; "AC" -> 24.sp; else -> 32.sp },
                                color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (row.size < 4) repeat(4 - row.size) { Spacer(modifier = Modifier.width(80.dp)) }
                }
            }
        }
    }
}

// ==============================================================================
// 9. PANTALLA MOVECITY
// ==============================================================================

// ---------- Sub-componentes de MoveCity ----------

@Composable
fun OcupacionChip(ocupacion: OcupacionRuta) {
    val (texto, colorFondo, colorTexto) = when (ocupacion) {
        OcupacionRuta.VACIA -> Triple("Vacía",  Color(0xFFE8F5E9), Color(0xFF2E7D32))
        OcupacionRuta.MEDIA -> Triple("Media",  Color(0xFFFFF8E1), Color(0xFFF57F17))
        OcupacionRuta.LLENA -> Triple("Llena",  Color(0xFFFFEBEE), Color(0xFFC62828))
    }
    val dotColor = when (ocupacion) {
        OcupacionRuta.VACIA -> Color(0xFF43A047)
        OcupacionRuta.MEDIA -> Color(0xFFFFC107)
        OcupacionRuta.LLENA -> Color(0xFFE53935)
    }
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(colorFondo).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.size(7.dp).clip(CircleShape).background(dotColor))
        Spacer(modifier = Modifier.width(5.dp))
        Text(texto, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = colorTexto)
    }
}

@Composable
fun BarraOcupacionMC(ocupacion: OcupacionRuta) {
    val (pct, color, desc) = when (ocupacion) {
        OcupacionRuta.VACIA -> Triple(0.15f, Color(0xFF43A047), "Pocos pasajeros")
        OcupacionRuta.MEDIA -> Triple(0.55f, Color(0xFFFFC107), "Capacidad media")
        OcupacionRuta.LLENA -> Triple(0.95f, Color(0xFFE53935), "Espera el siguiente")
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Ocupación del vehículo", fontSize = 11.sp, color = Color.Gray)
            Text(desc, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFE0E0E0))) {
            Spacer(modifier = Modifier.fillMaxWidth(pct).fillMaxSize().clip(RoundedCornerShape(3.dp)).background(color))
        }
    }
}

// ---------- Modal reporte de ocupación ----------

@Composable
fun ModalReporteOcupacion(
    linea: LineaTransporte,
    onDismiss: () -> Unit,
    onEnviar: (Int, OcupacionRuta) -> Unit
) {
    var seleccion by remember { mutableStateOf<OcupacionRuta?>(null) }
    var comentario by remember { mutableStateOf("") }
    var enviado by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .background(Color.White).padding(20.dp)
        ) {
            if (enviado) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                    Text("✅", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("¡Gracias por reportar!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1C1C1E))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tu reporte ayuda a otros pasajeros de MoveCity.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Reportar ocupación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1C1C1E))
                        Text(linea.nombre, fontSize = 12.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text("¿Cómo va el camión ahora mismo?", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    OcupacionRuta.VACIA to "😊 Casi vacío — hay espacio de sobra",
                    OcupacionRuta.MEDIA to "😐 Capacidad media — algunos lugares",
                    OcupacionRuta.LLENA to "😰 Lleno — mejor esperar el siguiente"
                ).forEach { (nivel, texto) ->
                    val (bg, border) = when (nivel) {
                        OcupacionRuta.VACIA -> Color(0xFFE8F5E9) to Color(0xFF43A047)
                        OcupacionRuta.MEDIA -> Color(0xFFFFF8E1) to Color(0xFFFFC107)
                        OcupacionRuta.LLENA -> Color(0xFFFFEBEE) to Color(0xFFE53935)
                    }
                    val isSelected = seleccion == nivel
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) bg else Color.White)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) border else Color(0xFFE5E5EA), RoundedCornerShape(12.dp))
                            .clickable { seleccion = nivel }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(texto, fontSize = 13.sp, color = Color(0xFF1C1C1E), modifier = Modifier.weight(1f))
                        if (isSelected) Icon(Icons.Default.Check, null, tint = border, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = comentario, onValueChange = { if (it.length <= 120) comentario = it },
                    placeholder = { Text("Comentario adicional (opcional)...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    supportingText = { Text("${comentario.length}/120") }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        seleccion?.let { nivel ->
                            enviado = true
                            onEnviar(linea.id, nivel)
                        }
                    },
                    enabled = seleccion != null,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA5B0C))
                ) { Text("Enviar reporte", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }
        }
    }
}

// ---------- Tarjeta de línea expandible con contador regresivo ----------

@Composable
fun TarjetaLineaMovecity(
    linea: LineaTransporte,
    onReportar: (LineaTransporte) -> Unit,
    onToggleFavorito: (Int) -> Unit
) {
    var expandida by remember { mutableStateOf(false) }
    var segundos by remember { mutableStateOf(linea.proximaLlegada * 60) }

    LaunchedEffect(linea.proximaLlegada) {
        segundos = linea.proximaLlegada * 60
        while (segundos > 0) {
            kotlinx.coroutines.delay(1000)
            segundos--
        }
    }

    val minR = segundos / 60
    val secR = segundos % 60
    val estadoOk = linea.estado == "Normal"

    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color.White).border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp))
    ) {
        // Cabecera
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            // Ícono
            Column(
                modifier = Modifier.size(46.dp).clip(RoundedCornerShape(10.dp)).background(linea.colorFondo),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                if (linea.iconoRes != null) {
                    Image(painter = painterResource(id = linea.iconoRes!!), contentDescription = null,
                        modifier = Modifier.size(46.dp), contentScale = ContentScale.Crop)
                } else if (linea.icono != null) {
                    Icon(linea.icono!!, null, tint = linea.color, modifier = Modifier.size(26.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Info
            Column(modifier = Modifier.weight(1f).clickable { expandida = !expandida }) {
                Text(linea.nombre, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                Text(linea.tipo, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OcupacionChip(linea.ocupacion)
                    Text("📍 ${linea.distancia}", fontSize = 11.sp, color = Color.Gray)
                    Text("🎟 ${linea.precio}", fontSize = 11.sp, color = Color.Gray)
                }
            }
            // Tiempo + favorito
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (segundos > 0) "$minR:${secR.toString().padStart(2,'0')}" else "¡Ya!",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = linea.color
                )
                Text(if (segundos > 0) "en llegar" else "¡Aborda!", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!estadoOk) Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9500), modifier = Modifier.size(13.dp))
                    Text(linea.estado, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = if (estadoOk) Color(0xFF34C759) else Color(0xFFFF9500))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (linea.favorita) "⭐" else "☆",
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onToggleFavorito(linea.id) }
                )
            }
        }

        // Barra ocupación
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 10.dp)) {
            BarraOcupacionMC(linea.ocupacion)
            Spacer(modifier = Modifier.height(5.dp))
            Text("📊 ${linea.reportes} usuarios reportaron esta ocupación", fontSize = 11.sp, color = Color.Gray)
        }

        // Botón reportar
        Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                    .border(1.dp, Color(0xFFEA5B0C), RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF1EB)).clickable { onReportar(linea) }
                    .padding(vertical = 9.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📢 Reportar ocupación actual", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFEA5B0C))
            }
        }

        // Paradas expandibles
        if (expandida && linea.paradas.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFF8F8FC)).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)) {
                Text("RECORRIDO COMPLETO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF555555), letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                linea.paradas.forEachIndexed { idx, parada ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(modifier = Modifier.size(10.dp).clip(CircleShape)
                                .background(if (parada.esFinal || idx == 0) linea.color else Color(0xFFBDBDBD))
                                .border(2.dp, linea.color, CircleShape))
                            if (idx < linea.paradas.size - 1)
                                Spacer(modifier = Modifier.width(2.dp).height(20.dp).background(Color(0xFFBDBDBD)))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = parada.nombre + if (idx == 0) " 🔵" else if (parada.esFinal) " 🏁" else "",
                            fontSize = 13.sp,
                            fontWeight = if (parada.esFinal || idx == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (parada.esFinal || idx == 0) linea.color else Color(0xFF333333),
                            modifier = Modifier.weight(1f)
                        )
                        Text(if (parada.minutos == 0) "Ahora" else "+${parada.minutos} min", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Toggle
        Row(
            modifier = Modifier.fillMaxWidth().background(Color(0xFFF0F0F5))
                .clickable { expandida = !expandida }.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (expandida) "▲ Ocultar recorrido" else "▼ Ver recorrido completo",
                fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = linea.color
            )
        }
    }
}

// ---------- Pantalla principal MoveCity ----------

@Composable
fun PantallaMoveCity(navController: NavController) {
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }
    var lineas by remember { mutableStateOf(LINEAS_INICIALES) }
    var filtroOcc by remember { mutableStateOf("todos") }
    var ordenPor by remember { mutableStateOf("llegada") }
    var lineaModal by remember { mutableStateOf<LineaTransporte?>(null) }
    var reportesEnviados by remember { mutableStateOf(0) }
    val hora = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        while (true) {
            val d = Date()
            hora.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(d)
            kotlinx.coroutines.delay(10000)
        }
    }

    // Modal reporte
    lineaModal?.let { linea ->
        ModalReporteOcupacion(
            linea = linea,
            onDismiss = { lineaModal = null },
            onEnviar = { id, nuevaOcc ->
                lineas = lineas.map { if (it.id == id) it.copy(ocupacion = nuevaOcc, reportes = it.reportes + 1) else it }
                reportesEnviados++
                // ========== CORRECCIÓN: USO CORRECTO DE MainScope() ==========
                MainScope().launch {
                    delay(1600)
                    lineaModal = null
                }
                // ==============================================================
            }
        )
    }

    // Filtrar y ordenar
    val lineasFiltradas = lineas.filter { l ->
        val matchOcc = filtroOcc == "todos" ||
                (filtroOcc == "vacia" && l.ocupacion == OcupacionRuta.VACIA) ||
                (filtroOcc == "media" && l.ocupacion == OcupacionRuta.MEDIA) ||
                (filtroOcc == "llena" && l.ocupacion == OcupacionRuta.LLENA)
        val matchBusq = busqueda.isEmpty() ||
                l.nombre.contains(busqueda, ignoreCase = true) ||
                l.paradas.any { p -> p.nombre.contains(busqueda, ignoreCase = true) }
        matchOcc && matchBusq
    }.sortedWith(
        when (ordenPor) {
            "ocupacion" -> compareBy { when (it.ocupacion) { OcupacionRuta.VACIA -> 0; OcupacionRuta.MEDIA -> 1; OcupacionRuta.LLENA -> 2 } }
            "distancia" -> compareBy { it.distancia.replace(" m","").toIntOrNull() ?: 9999 }
            else         -> compareBy { it.proximaLlegada }
        }
    )
    val favCount = lineas.count { it.favorita }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F7))) {

        // ===== HEADER =====
        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFFEA5B0C)).padding(top = 44.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)) {
                    Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Image(painter = painterResource(id = R.drawable.logo), contentDescription = null,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("MoveCity", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Transporte público · Puebla", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(hora.value, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (reportesEnviados > 0)
                        Text("$reportesEnviados reporte${if (reportesEnviados>1) "s" else ""}", fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            // Buscador origen/destino
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White).padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF43A047)))
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(value = origen, onValueChange = { origen = it },
                        placeholder = { Text("Tu ubicación actual", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFFEA5B0C), modifier = Modifier.size(20.dp))
                }
                // Intercambiar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFE53935)))
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(value = destino, onValueChange = { destino = it },
                        placeholder = { Text("¿A dónde vas?", color = Color.Gray, fontSize = 14.sp) },
                        modifier = Modifier.weight(1f), singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }

        // ===== BARRA BÚSQUEDA PARADAS (solo tab 0) =====
        if (tab == 0) {
            Row(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp)).background(Color(0xFFF5F5F7)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField_Simple(
                        value = busqueda, onValueChange = { busqueda = it },
                        placeholder = "Buscar línea o parada...",
                        modifier = Modifier.weight(1f)
                    )
                    if (busqueda.isNotEmpty()) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp).clickable { busqueda = "" })
                    }
                }
            }
        }

        // ===== FILTROS (solo tab 0) =====
        if (tab == 0) {
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 14.dp, vertical = 6.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    item { Text("Ocupación:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold) }
                    items(listOf("todos" to "Todas", "vacia" to "🟢 Vacías", "media" to "🟡 Media", "llena" to "🔴 Llenas")) { (val_, lbl) ->
                        FilterChipMC(label = lbl, selected = filtroOcc == val_) { filtroOcc = val_ }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    item { Text("Ordenar:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold) }
                    items(listOf("llegada" to "⏱ Tiempo", "ocupacion" to "👥 Ocupación", "distancia" to "📍 Distancia")) { (val_, lbl) ->
                        FilterChipMC(label = lbl, selected = ordenPor == val_) { ordenPor = val_ }
                    }
                }
            }
        }

        // ===== TABS =====
        Row(modifier = Modifier.fillMaxWidth().background(Color.White).border(width = 1.dp, color = Color(0xFFE5E5EA))) {
            listOf("🚌 Líneas", "🗺 Mapa", "🔔 Alertas", "⭐ Favoritos").forEachIndexed { i, lbl ->
                Column(
                    modifier = Modifier.weight(1f).clickable { tab = i }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(lbl, fontSize = 11.sp, fontWeight = if (tab == i) FontWeight.Bold else FontWeight.Normal,
                        color = if (tab == i) Color(0xFFEA5B0C) else Color.Gray)
                    if (i == 3 && favCount > 0) {
                        Text("$favCount", fontSize = 9.sp, color = Color.White,
                            modifier = Modifier.clip(CircleShape).background(Color(0xFFEA5B0C)).padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Spacer(modifier = Modifier.height(2.dp).fillMaxWidth(if (tab == i) 0.6f else 0f)
                        .clip(RoundedCornerShape(1.dp)).background(Color(0xFFEA5B0C)))
                }
            }
        }

        // ===== CONTENIDO DE CADA TAB =====
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            when (tab) {

                // TAB 0: LÍNEAS
                0 -> {
                    item {
                        Text("${lineasFiltradas.size} de ${lineas.size} líneas${if (busqueda.isNotEmpty()) " para \"$busqueda\"" else " cerca de ti"}",
                            fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                    }
                    if (lineasFiltradas.isEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Sin resultados", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1C1C1E))
                                Text("Cambia los filtros o el término de búsqueda.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        items(lineasFiltradas) { linea ->
                            TarjetaLineaMovecity(
                                linea = linea,
                                onReportar = { lineaModal = it },
                                onToggleFavorito = { id -> lineas = lineas.map { if (it.id == id) it.copy(favorita = !it.favorita) else it } }
                            )
                        }
                    }
                    // Leyenda y botón reportar general
                    item {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFEA5B0C), RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF1EB)).padding(14.dp)
                        ) {
                            Text("📊 Reportar en una línea", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1C1C1E))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Usa el botón 'Reportar ocupación' en cada tarjeta para ayudar a otros usuarios a elegir mejor su transporte.", fontSize = 12.sp, color = Color.Gray, lineHeight = 18.sp)
                        }
                    }
                    item {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(12.dp)).background(Color.White).padding(14.dp)) {
                            Text("¿Qué significa cada color?", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1C1C1E))
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf(
                                Triple(Color(0xFF43A047), "Vacía", "Pocos pasajeros — viaja cómodo"),
                                Triple(Color(0xFFFFC107), "Media", "Capacidad moderada"),
                                Triple(Color(0xFFE53935), "Llena", "Espera la siguiente unidad")
                            ).forEach { (dot, titulo, desc) ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 5.dp)) {
                                    Spacer(modifier = Modifier.size(10.dp).clip(CircleShape).background(dot))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("$titulo — ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1C1E))
                                    Text(desc, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // TAB 1: MAPA SIMULADO
                1 -> {
                    item { Text("Toca una línea para ver su trayecto", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp)) }
                    item {
                        // Mapa estático estilo diagram
                        var rutaSelec by remember { mutableStateOf<Int?>(null) }
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .border(1.dp, Color(0xFFE5E5EA), RoundedCornerShape(14.dp)).background(Color(0xFFE8F4EA))) {
                            // Leyenda ↔ Rutas
                            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(Color.White).padding(12.dp)) {
                                Text("Líneas disponibles", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1C1C1E), modifier = Modifier.padding(bottom = 8.dp))
                                lineas.forEach { l ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable { rutaSelec = if (rutaSelec == l.id) null else l.id }
                                            .padding(vertical = 6.dp).background(if (rutaSelec == l.id) l.colorFondo else Color.Transparent)
                                            .clip(RoundedCornerShape(8.dp)).padding(horizontal = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Spacer(modifier = Modifier.width(20.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(l.color))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(l.nombre, fontSize = 13.sp, color = Color(0xFF1C1C1E), modifier = Modifier.weight(1f))
                                        OcupacionChip(l.ocupacion)
                                    }
                                    if (rutaSelec == l.id) {
                                        Column(modifier = Modifier.padding(start = 30.dp, top = 4.dp, bottom = 4.dp)) {
                                            l.paradas.forEach { p ->
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                                    Spacer(modifier = Modifier.size(8.dp).clip(CircleShape).background(if (p.esFinal) l.color else Color(0xFFBDBDBD)))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(p.nombre, fontSize = 12.sp, color = if (p.esFinal) l.color else Color(0xFF555555), fontWeight = if (p.esFinal) FontWeight.Bold else FontWeight.Normal)
                                                    Spacer(modifier = Modifier.weight(1f))
                                                    Text(if (p.minutos == 0) "Ahora" else "+${p.minutos} min", fontSize = 11.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: ALERTAS
                2 -> {
                    item { Text("${ALERTAS_MOVECITY.size} alertas activas en tu zona", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp)) }
                    items(ALERTAS_MOVECITY) { (titulo, msg, fondo) ->
                        Column(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                .background(fondo).border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp)).padding(14.dp)
                        ) {
                            Text(titulo, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(msg, fontSize = 13.sp, color = Color(0xFF555555), lineHeight = 18.sp)
                        }
                    }
                    item {
                        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEEF4FF)).border(1.dp, Color(0xFFBFCFEE), RoundedCornerShape(12.dp)).padding(14.dp)) {
                            Text("💡 Información colaborativa", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1A4080))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("MoveCity usa reportes de pasajeros para actualizar el nivel de ocupación en tiempo real. Cuantos más usuarios reportan, más precisa es la información para todos.",
                                fontSize = 12.sp, color = Color(0xFF2A4A80), lineHeight = 18.sp)
                        }
                    }
                }

                // TAB 3: FAVORITOS
                3 -> {
                    val favoritas = lineas.filter { it.favorita }
                    if (favoritas.isEmpty()) {
                        item {
                            Column(modifier = Modifier.fillMaxWidth().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⭐", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Sin rutas favoritas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1C1C1E))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Toca la estrella ☆ en cualquier línea para guardarla aquí y acceder rápido.", fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        item { Text("${favoritas.size} ruta${if (favoritas.size > 1) "s" else ""} guardada${if (favoritas.size > 1) "s" else ""}", fontSize = 12.sp, color = Color.Gray) }
                        items(favoritas) { linea ->
                            TarjetaLineaMovecity(
                                linea = linea,
                                onReportar = { lineaModal = it },
                                onToggleFavorito = { id -> lineas = lineas.map { if (it.id == id) it.copy(favorita = !it.favorita) else it } }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// ---------- Componentes utilitarios MoveCity ----------

@Composable
fun FilterChipMC(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) Color(0xFFEA5B0C) else Color(0xFFE5E5EA), RoundedCornerShape(20.dp))
            .background(if (selected) Color(0xFFFFF1EB) else Color.White)
            .clickable { onClick() }.padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color(0xFFEA5B0C) else Color.Gray)
    }
}

@Composable
fun BasicTextField_Simple(value: String, onValueChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    androidx.compose.foundation.text.BasicTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xFF1C1C1E)),
        decorationBox = { inner ->
            if (value.isEmpty()) Text(placeholder, fontSize = 13.sp, color = Color.Gray)
            inner()
        },
        singleLine = true
    )
}

// ==============================================================================
// LÓGICA ARITMÉTICA (Calculadora)
// ==============================================================================

fun evaluateArithmeticExpression(expression: String): Double {
    if (expression.isBlank()) return 0.0
    var processed = expression.replace("sqrt", "√")
    while (processed.contains("√")) {
        val sqrtIndex = processed.indexOf("√")
        var endIndex = sqrtIndex + 1
        for (i in sqrtIndex + 1 until processed.length) {
            if (processed[i].isDigit() || processed[i] == '.') endIndex = i + 1 else break
        }
        val sqrtExpr = processed.substring(sqrtIndex + 1, endIndex)
        val num = sqrtExpr.toDoubleOrNull() ?: 0.0
        val sqrtResult = if (num < 0) Double.NaN else kotlin.math.sqrt(num)
        processed = processed.replaceRange(sqrtIndex, endIndex, sqrtResult.toString())
    }
    return evaluateBasicExpression(processed)
}

fun evaluateBasicExpression(expression: String): Double {
    val tokens = mutableListOf<String>()
    var current = ""
    var i = 0
    while (i < expression.length) {
        val ch = expression[i]
        when {
            ch.isDigit() || ch == '.' -> { current += ch; i++ }
            ch == '-' && (i == 0 || expression[i-1] in listOf('+','-','*','/')) -> { current += ch; i++ }
            ch in listOf('+','-','*','/') -> { if (current.isNotEmpty()) { tokens.add(current); current = "" }; tokens.add(ch.toString()); i++ }
            else -> i++
        }
    }
    if (current.isNotEmpty()) tokens.add(current)
    val mid = mutableListOf<String>()
    var j = 0
    while (j < tokens.size) {
        when (tokens[j]) {
            // ========== CORRECCIÓN: removeLast() → removeAt(mid.lastIndex) ==========
            "*" -> { val l = mid.removeAt(mid.lastIndex).toDouble(); val r = tokens[++j].toDouble(); mid.add((l*r).toString()); j++ }
            "/" -> { val l = mid.removeAt(mid.lastIndex).toDouble(); val r = tokens[++j].toDouble(); if (r==0.0) return Double.NaN; mid.add((l/r).toString()); j++ }
            // ==========================================================================
            else -> { mid.add(tokens[j]); j++ }
        }
    }
    var result = mid[0].toDouble()
    var k = 1
    while (k < mid.size) {
        when (mid[k]) { "+" -> result += mid[++k].toDouble(); "-" -> result -= mid[++k].toDouble() }; k++
    }
    return result
}