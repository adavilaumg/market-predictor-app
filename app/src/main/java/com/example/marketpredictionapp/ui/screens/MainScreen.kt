package com.example.marketpredictionapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marketpredictionapp.model.*
import com.example.marketpredictionapp.network.RetrofitClient
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

// ─── Colores ──────────────────────────────────────────────────
private val BgTop    = Color(0xFF0A0E1A)
private val BgBot    = Color(0xFF0F1629)
private val Accent   = Color(0xFF00D4FF)
private val Accent2  = Color(0xFFFF6B35)
private val Accent3  = Color(0xFF00FF88)
private val AccentY  = Color(0xFFFFCC00)
private val CardBg   = Color(0xFF0F1629)
private val Border   = Color(0xFF1E2D4A)
private val TextMain = Color(0xFFE2EAF8)
private val TextMuted= Color(0xFF4A6080)
private val ErrorClr = Color(0xFFFF6B6B)

// ─── Tabs ─────────────────────────────────────────────────────
private enum class Tab(val label: String, val icon: ImageVector) {
    WEATHER("Clima",       Icons.Default.WbSunny),
    MARKET ("Mercado",     Icons.Default.ShowChart),
    ANALYSIS("Análisis",  Icons.Default.Analytics),
    CORRELATIONS("Datos", Icons.Default.TableChart),
}

// ─── Screen ───────────────────────────────────────────────────
@Composable
fun MainScreen(token: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableStateOf(Tab.WEATHER) }
    val analytics = remember { Firebase.analytics }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgTop, BgBot)))
    ) {
        Column(Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CardBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CLIMA & MERCADOS", color = Accent,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                IconButton(onClick = onLogout) {
                    Icon(Icons.Default.Logout, contentDescription = "Salir", tint = TextMuted)
                }
            }

            // ── Tab bar ───────────────────────────────────────
            /*Row(
                Modifier.fillMaxWidth().background(CardBg),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Tab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(tab.icon, contentDescription = tab.label,
                            tint = if (selected) Accent else TextMuted,
                            modifier = Modifier.size(20.dp))
                        Text(tab.label, fontSize = 9.sp,
                            color = if (selected) Accent else TextMuted)
                        if (selected) Spacer(Modifier.height(2.dp).fillMaxWidth()
                            .background(Accent))
                    }
                    // tap para cambiar tab + log analytics
                    androidx.compose.foundation.clickable(
                        onClick = {
                            selectedTab = tab
                            analytics.logEvent("tab_view") {
                                param("tab_name", tab.name)
                            }
                        }
                    )
                }
            }

            Divider(color = Border, thickness = 1.dp)*/

            Row(
                Modifier.fillMaxWidth().background(CardBg),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Tab.entries.forEach { tab ->
                    val selected = selectedTab == tab
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectedTab = tab
                                analytics.logEvent("tab_view") {
                                    param("tab_name", tab.name)
                                }
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) Accent else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(tab.label, fontSize = 9.sp,
                            color = if (selected) Accent else TextMuted)
                        if (selected) {
                            Spacer(
                                Modifier
                                    .padding(top = 2.dp)
                                    .height(2.dp)
                                    .fillMaxWidth()
                                    .background(Accent)
                            )
                        }
                    }
                }
            }

            // ── Contenido ─────────────────────────────────────
            Box(Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    Tab.WEATHER      -> WeatherTab(token, analytics)
                    Tab.MARKET       -> MarketTab(token, analytics)
                    Tab.ANALYSIS     -> AnalysisTab(token, analytics)
                    Tab.CORRELATIONS -> CorrelationsTab(token)
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  TAB: CLIMA
// ════════════════════════════════════════════════════════════
@Composable
fun WeatherTab(token: String, analytics: FirebaseAnalytics) {
    var city    by remember { mutableStateOf("Guatemala City") }
    var country by remember { mutableStateOf("GT") }
    var data    by remember { mutableStateOf<WeatherResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf("") }
    val scope   = rememberCoroutineScope()

    fun fetch() {
        loading = true; error = ""
        scope.launch {
            try {
                val r = RetrofitClient.instance.getCurrentWeather(
                    RetrofitClient.bearerToken(token), city, country)
                if (r.isSuccessful) {
                    data = r.body()
                    analytics.logEvent("weather_fetch") {
                        param("city", city)
                        param("temperature", data?.temperature ?: 0.0)
                    }
                } else error = "Error ${r.code()}"
            } catch (e: Exception) { error = "Sin conexión" }
            finally { loading = false }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard(title = "Clima actual", accent = Accent) {
                DarkTextField("Ciudad", city) { city = it }
                Spacer(Modifier.height(8.dp))
                DarkTextField("País (código)", country) { country = it }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Consultar", Accent, loading) { fetch() }
                if (error.isNotEmpty()) ErrorText(error)
            }
        }
        if (data != null && !loading) {
            item {
                SectionCard(title = "Resultado", accent = Accent3) {
                    data?.let { w ->
                        StatGrid(listOf(
                            Triple("Temperatura", "${w.temperature}°C",  Accent),
                            Triple("Sensación",   "${w.feelsLike}°C",    Accent),
                            Triple("Humedad",     "${w.humidity}%",       Accent3),
                            Triple("Viento",      "${w.windSpeed} m/s",  Accent2),
                            Triple("Condición",   w.description,          AccentY),
                            Triple("Ciudad",      "${w.city}, ${w.country}", TextMuted),
                        ))
                    }
                }
            }
        }
        if (loading) item { CenteredLoader() }
    }
}

// ════════════════════════════════════════════════════════════
//  TAB: MERCADO
// ════════════════════════════════════════════════════════════
@Composable
fun MarketTab(token: String, analytics: FirebaseAnalytics) {
    var symbols by remember { mutableStateOf("AAPL,AMZN") }
    var limit   by remember { mutableStateOf("10") }
    var data    by remember { mutableStateOf<List<MarketRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf("") }
    val scope   = rememberCoroutineScope()

    fun fetch() {
        loading = true; error = ""
        scope.launch {
            try {
                val r = RetrofitClient.instance.getEodPrices(
                    RetrofitClient.bearerToken(token), symbols, limit.toIntOrNull() ?: 10)
                if (r.isSuccessful) {
                    data = r.body()?.data ?: emptyList()
                    analytics.logEvent("market_fetch") {
                        param("symbols", symbols)
                        param("count", data.size.toLong())
                    }
                } else error = "Error ${r.code()}"
            } catch (e: Exception) { error = "Sin conexión" }
            finally { loading = false }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard(title = "Precios EOD", accent = Accent2) {
                DarkTextField("Símbolos (separados por coma)", symbols) { symbols = it }
                Spacer(Modifier.height(8.dp))
                DarkTextField("Registros", limit) { limit = it }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Buscar", Accent2, loading) { fetch() }
                if (error.isNotEmpty()) ErrorText(error)
            }
        }
        if (loading) item { CenteredLoader() }
        items(data) { record ->
            MarketCard(record)
        }
    }
}

@Composable
fun MarketCard(record: MarketRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(record.symbol, color = Accent2, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(record.date.take(10), color = TextMuted, fontSize = 11.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$${String.format("%.2f", record.closePrice)}", color = TextMain,
                    fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("H: $${String.format("%.2f", record.highPrice)}  L: $${String.format("%.2f", record.lowPrice)}",
                    color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  TAB: ANÁLISIS
// ════════════════════════════════════════════════════════════
@Composable
fun AnalysisTab(token: String, analytics: FirebaseAnalytics) {
    var city    by remember { mutableStateOf("Guatemala City") }
    var symbols by remember { mutableStateOf("AAPL,AMZN") }
    var result  by remember { mutableStateOf<AnalysisResponse?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error   by remember { mutableStateOf("") }
    val scope   = rememberCoroutineScope()

    fun run() {
        loading = true; error = ""; result = null
        scope.launch {
            try {
                val syms = symbols.split(",").map { it.trim().uppercase() }
                val r = RetrofitClient.instance.runAnalysis(
                    RetrofitClient.bearerToken(token),
                    AnalysisRequest(city, syms)
                )
                if (r.isSuccessful) {
                    result = r.body()
                    analytics.logEvent("analysis_run") {
                        param("city", city)
                        param("symbols", symbols)
                        param("temperature", result?.avgTemperature ?: 0.0)
                    }
                } else error = "Error ${r.code()}"
            } catch (e: Exception) { error = "Sin conexión" }
            finally { loading = false }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard(title = "Análisis clima + mercado", accent = AccentY) {
                Text("Consulta clima y precios, guarda correlaciones en MongoDB.",
                    color = TextMuted, fontSize = 12.sp)
                Spacer(Modifier.height(10.dp))
                DarkTextField("Ciudad", city) { city = it }
                Spacer(Modifier.height(8.dp))
                DarkTextField("Símbolos", symbols) { symbols = it }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Ejecutar análisis", AccentY, loading) { run() }
                if (error.isNotEmpty()) ErrorText(error)
            }
        }
        if (loading) item { CenteredLoader() }
        result?.let { res ->
            item {
                SectionCard(title = "Resultado general", accent = Accent3) {
                    StatGrid(listOf(
                        Triple("Ciudad",      res.city,                        TextMain),
                        Triple("Temperatura", "${res.avgTemperature}°C",       Accent),
                        Triple("Registros",   "${res.weatherRecords}",         Accent3),
                    ))
                    Spacer(Modifier.height(8.dp))
                    Text(res.message, color = AccentY, fontSize = 12.sp)
                }
            }
            items(res.symbols) { stat ->
                SectionCard(title = stat.symbol, accent = Accent2) {
                    StatGrid(listOf(
                        Triple("Promedio", "$${String.format("%.2f", stat.avgPrice)}", Accent2),
                        Triple("Mínimo",   "$${String.format("%.2f", stat.minPrice)}", Accent3),
                        Triple("Máximo",   "$${String.format("%.2f", stat.maxPrice)}", ErrorClr),
                        Triple("Puntos",   "${stat.dataPoints}",                       TextMuted),
                    ))
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  TAB: CORRELACIONES
// ════════════════════════════════════════════════════════════
@Composable
fun CorrelationsTab(token: String) {
    var cityFilter   by remember { mutableStateOf("") }
    var symbolFilter by remember { mutableStateOf("") }
    var data         by remember { mutableStateOf<List<CorrelationRecord>>(emptyList()) }
    var loading      by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf("") }
    val scope        = rememberCoroutineScope()

    fun fetch() {
        loading = true; error = ""
        scope.launch {
            try {
                val r = RetrofitClient.instance.getCorrelations(
                    RetrofitClient.bearerToken(token),
                    cityFilter.ifBlank { null },
                    symbolFilter.ifBlank { null }
                )
                if (r.isSuccessful) data = r.body() ?: emptyList()
                else error = "Error ${r.code()}"
            } catch (e: Exception) { error = "Sin conexión" }
            finally { loading = false }
        }
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SectionCard(title = "Correlaciones guardadas", accent = Accent3) {
                DarkTextField("Filtrar por ciudad", cityFilter)   { cityFilter = it }
                Spacer(Modifier.height(8.dp))
                DarkTextField("Filtrar por símbolo", symbolFilter) { symbolFilter = it }
                Spacer(Modifier.height(12.dp))
                PrimaryButton("Cargar", Accent3, loading) { fetch() }
                if (error.isNotEmpty()) ErrorText(error)
                if (data.isEmpty() && !loading && error.isEmpty())
                    Text("Presiona Cargar para ver las correlaciones.",
                        color = TextMuted, fontSize = 12.sp)
            }
        }
        if (loading) item { CenteredLoader() }
        items(data) { c ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border)
            ) {
                Row(
                    Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(c.symbol, color = Accent3, fontWeight = FontWeight.Bold)
                        Text(c.city, color = TextMuted, fontSize = 11.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${c.temperature}°C", color = Accent, fontSize = 13.sp)
                        Text("$${String.format("%.2f", c.closePrice)}", color = Accent2, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ════════════════════════════════════════════════════════════
//  COMPONENTES REUTILIZABLES
// ════════════════════════════════════════════════════════════

@Composable
fun SectionCard(title: String, accent: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title.uppercase(), color = accent,
                fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun DarkTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label, color = TextMuted, fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Accent,
            unfocusedBorderColor = Border,
            focusedTextColor     = TextMain,
            unfocusedTextColor   = TextMain,
            cursorColor          = Accent,
        )
    )
}

@Composable
fun PrimaryButton(label: String, color: Color, loading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(46.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        if (loading) CircularProgressIndicator(
            color = BgTop, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        else Text(label, color = BgTop, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun StatGrid(stats: List<Triple<String, String, Color>>) {
    val cols = 2
    stats.chunked(cols).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { (label, value, color) ->
                Box(
                    Modifier
                        .weight(1f)
                        .background(Color(0xFF0A0E1A), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(label.uppercase(), color = TextMuted, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            // Rellena si la fila tiene menos de `cols` elementos
            repeat(cols - row.size) { Spacer(Modifier.weight(1f)) }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun ErrorText(msg: String) {
    Text(msg, color = ErrorClr, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
}

@Composable
fun CenteredLoader() {
    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Accent, strokeWidth = 2.dp)
    }
}