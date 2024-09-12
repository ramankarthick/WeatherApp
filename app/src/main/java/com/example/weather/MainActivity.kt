package com.example.weather

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.weather.model.WeatherViewModel
import com.example.weather.ui.theme.WeatherTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WeatherTheme {
                RequestPermission(requestPermissionLauncher, locationClient)
            }
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun RequestPermission(request: ActivityResultLauncher<String>, locationClient: FusedLocationProviderClient) {
    var location by remember { mutableStateOf<Location?>(null) }
    var permissionGranted by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        permissionGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            request.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            fetchLocation(context, locationClient) { newlocation ->
                location = newlocation
            }
        }
    }

    WeatherScreen(location)
}

private fun fetchLocation(context: Context, locationClient: FusedLocationProviderClient, onLocationFetched: (Location?) -> Unit, ) {
    if (ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        locationClient.lastLocation.addOnSuccessListener { location ->
            onLocationFetched(location)
        }
    }
}

@Composable
fun WeatherScreen(location: Location?) {
    val weatherViewModel: WeatherViewModel = viewModel(LocalContext.current as ComponentActivity)
    var cityName by rememberSaveable { mutableStateOf("") }
    var searchLocation by rememberSaveable { mutableStateOf("") }
    var currentTemperature by rememberSaveable { mutableStateOf("") }
    var weatherCondition by rememberSaveable { mutableStateOf("") }
    var weatherIcon by rememberSaveable { mutableStateOf("") }
    val cityWeather by weatherViewModel.cityWeather.observeAsState()

    location?.let {
        weatherViewModel.getWeatherUsingLatLon(location.latitude, location.longitude)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.LightGray
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            SearchBar(
                query = searchLocation,
                onQueryChange = { searchLocation = it },
                onSearch = {
                    if (searchLocation.isEmpty() && location != null) {
                        weatherViewModel.getWeatherUsingLatLon(location.latitude, location.longitude)
                    } else {
                        weatherViewModel.getWeather(searchLocation)
                    }
                }
            )

            cityWeather?.let {
                cityName = it.name
                weatherCondition = it.weather[0].description
                currentTemperature = it.main.temp.toInt().toString()
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = cityName,
                style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentTemperature,
                    style = MaterialTheme.typography.h3.copy(fontWeight = FontWeight.Bold),
                    color = Color.Blue
                )

                if (currentTemperature.isNotEmpty()) {
                    Image(
                        painterResource(
                            R.drawable.icons_fahrenheit_symbol
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row (verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = weatherCondition,
                    style = MaterialTheme.typography.h6,
                    color = Color.DarkGray
                )

                var iconUrl = "https://openweathermap.org/img/wn/$weatherIcon@2x.png"
                cityWeather?.let {
                    weatherIcon = it.weather[0].icon
                }
                Image(painter = rememberAsyncImagePainter(iconUrl),
                    contentDescription = "Weather Condition image",
                    modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    TextField(
        value = query,
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Default.Search,
                contentDescription = ""
            )
        },
        onValueChange = onQueryChange,
        label = { Text("Enter location") },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    )
}

@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun PreviewWeatherScreen() {
    WeatherTheme {
        WeatherScreen(null)
    }
}