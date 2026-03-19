package com.example.levantamiento

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.io.File
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var txtCoordenadas: EditText
    private lateinit var txtDireccion: EditText
    private lateinit var spTipoLampara: Spinner
    private lateinit var spTipoPoste: Spinner
    private lateinit var txtEquipoInstalado: EditText
    private lateinit var txtTipoBanco: EditText
    private lateinit var txtCapacidad: EditText

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            obtenerUbicacionActual()
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtCoordenadas = findViewById(R.id.txtCoordenadas)
        txtDireccion = findViewById(R.id.txtDireccion)
        spTipoLampara = findViewById(R.id.spTipoLampara)
        spTipoPoste = findViewById(R.id.spTipoPoste)
        txtEquipoInstalado = findViewById(R.id.txtEquipoInstalado)
        txtTipoBanco = findViewById(R.id.txtTipoBanco)
        txtCapacidad = findViewById(R.id.txtCapacidad)

        val btnUbicacion: Button = findViewById(R.id.btnUbicacion)
        val btnGuardar: Button = findViewById(R.id.btnGuardar)

        configurarSpinners()

        btnUbicacion.setOnClickListener {
            verificarPermisosYObtenerUbicacion()
        }

        btnGuardar.setOnClickListener {
            guardarDatos()
        }
    }

    private fun configurarSpinners() {
        val tiposLampara = listOf("LED 100W", "LED 150W", "SODIO 150W", "SODIO 250W", "SODIO 400W")
        val tiposPoste = listOf("ALTA", "BAJA")

        spTipoLampara.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposLampara)
        spTipoPoste.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tiposPoste)
    }

    private fun verificarPermisosYObtenerUbicacion() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            obtenerUbicacionActual()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    private fun obtenerUbicacionActual() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted && !coarseGranted) {
            Toast.makeText(this, "Sin permisos para acceder a la ubicación", Toast.LENGTH_SHORT).show()
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    txtCoordenadas.setText("$lat, $lon")
                    txtDireccion.setText(obtenerDireccion(lat, lon))
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al obtener ubicación", Toast.LENGTH_SHORT).show()
            }
    }

    private fun obtenerDireccion(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) ?: "Dirección no disponible"
            } else {
                "Dirección no disponible"
            }
        } catch (_: Exception) {
            "Dirección no disponible"
        }
    }

    private fun guardarDatos() {
        val coordenadas = txtCoordenadas.text.toString().trim()
        val direccion = txtDireccion.text.toString().trim()
        val tipoLampara = spTipoLampara.selectedItem.toString()
        val tipoPoste = spTipoPoste.selectedItem.toString()
        val equipoInstalado = txtEquipoInstalado.text.toString().trim()
        val tipoBanco = txtTipoBanco.text.toString().trim()
        val capacidad = txtCapacidad.text.toString().trim()

        if (coordenadas.isEmpty() || direccion.isEmpty()) {
            Toast.makeText(this, "Completa al menos coordenadas y dirección", Toast.LENGTH_SHORT).show()
            return
        }

        val registro = """
            Coordenadas: $coordenadas
            Dirección: $direccion
            Tipo de lámpara: $tipoLampara
            Tipo de poste: $tipoPoste
            Equipo instalado: $equipoInstalado
            Tipo de banco: $tipoBanco
            Capacidad: $capacidad
            --------------------------------------
        """.trimIndent() + "\n"

        val archivo = File(filesDir, "datos.txt")
        archivo.appendText(registro)

        Toast.makeText(this, "Datos guardados en ${archivo.name}", Toast.LENGTH_SHORT).show()
        limpiarFormulario()
    }

    private fun limpiarFormulario() {
        txtCoordenadas.text.clear()
        txtDireccion.text.clear()
        txtEquipoInstalado.text.clear()
        txtTipoBanco.text.clear()
        txtCapacidad.text.clear()
        spTipoLampara.setSelection(0)
        spTipoPoste.setSelection(0)
    }
}
