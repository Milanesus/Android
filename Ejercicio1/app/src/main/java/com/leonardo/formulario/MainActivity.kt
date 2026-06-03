package com.leonardo.formulario

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.text.InputFilter
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.AdapterView
import android.widget.RadioButton
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.widget.ArrayAdapter
import com.leonardo.formulario.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // Binding para acceder a los componentes de la interfaz principal
    private lateinit var binding: ActivityMainBinding
    // Almacenamos la fecha de nacimiento
    private var calendarioNacimiento: Calendar? = null
    // Formato para mostrar la fecha
    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    // Almacenamos la URI de la foto de perfil
    private var uriFotoPerfil: Uri? = null
    // Definimos el lanzador para la selección de imágenes al elegir una foto de perfil
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriFotoPerfil = uri
            binding.ivFotoPerfil.setImageURI(uri)
        }
    }

    // Longitud del número de teléfono para cada código de país
    private val longitudesTelefono = mapOf(
        "+52" to 10, // México
        "+1"  to 10, // EE.UU. / Canadá
        "+55" to 11, // Brasil
        "+91" to 10, // India
        "+57" to 10, // Colombia
        "+34" to 9,  // España
        "+54" to 10, // Argentina
        "+58" to 10, // Venezuela
        "+51" to 9,  // Perú
        "+84" to 9   // Vietnam
    )
    // Iniciamos la interfaz
    override fun onCreate(savedInstanceState: Bundle?) {
        // Desactivamos el modo oscuro para que la app siempre use el diseño morado claro
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Usamos el modo Edge-to-Edge en la pantalla principal
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Ajustamos el padding para evitar que el texto se encime con la
        // barra de navegación o la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (imeInsets.bottom > 0) imeInsets.bottom else systemBars.bottom
            )
            insets
        }

        configurarSpinnerTelefono()
        // Configuramos el botón para mostrar el selector de fecha
        binding.etFechaNacimiento.setOnClickListener {
            mostrarSelectorFecha()
        }
        // Configuramos el botón para cambiar la foto de perfil
        binding.btnCambiarFoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        // Configuramos el botón para guardar perfil
        binding.btnGuardarPerfil.setOnClickListener {
            enviarFormulario()
        }
    }

    // Configuramos el spinner para seleccionar el código de país
    private fun configurarSpinnerTelefono() {
        val nombres = resources.getStringArray(R.array.nombres_pais)
        val claves = resources.getStringArray(R.array.claves_pais)
        // Combinamos los nombres y las claves en una lista para mostrarlo en la lista desplegable
        val listaCombinada = nombres.zip(claves) { nombre, clave -> "$nombre ($clave)" }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaCombinada)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spClavePais.adapter = adapter

        binding.spClavePais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            // Actualizamos la longitud del teléfono según la clave seleccionada
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val claveSeleccionada = claves[position]
                val longitudRequerida = longitudesTelefono[claveSeleccionada] ?: 10
                binding.etTelefono.filters = arrayOf(InputFilter.LengthFilter(longitudRequerida))
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    // Mostramos el selector de fecha
    private fun mostrarSelectorFecha() {
        val calendario = calendarioNacimiento ?: Calendar.getInstance()

        val dialogo = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendarioNacimiento = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                binding.etFechaNacimiento.setText(formatoFecha.format(calendarioNacimiento!!.time))
                binding.tilFechaNacimiento.error = null
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        )

        dialogo.datePicker.maxDate = System.currentTimeMillis()
        dialogo.show()
    }

    private fun enviarFormulario() {
        // Validamos el formulario
        if (!validarFormulario()) return
        // Obtenemos los datos del formulario
        // Se utiliza trim() para eliminar espacios en blanco al inicio y al final
        val nombre = binding.etNombre.text.toString().trim()
        val apellido = binding.etApellido.text.toString().trim()
        val fechaNacimiento = binding.etFechaNacimiento.text.toString().trim()
        val genero = obtenerGeneroSeleccionado().orEmpty()

        val claves = resources.getStringArray(R.array.claves_pais)
        val clavePais = claves[binding.spClavePais.selectedItemPosition]

        val telefono = binding.etTelefono.text.toString().trim()
        val correo = binding.etCorreo.text.toString().trim()
        val biografia = binding.etBiografia.text.toString().trim()

        val intereses = obtenerInteresesSeleccionados()
        val textoIntereses = if (intereses.isEmpty()) {
            getString(R.string.sin_intereses)
        } else {
            intereses.joinToString(", ")
        }

        // Si la biografía está vacía, mostramos un mensaje predeterminado
        val textoBiografia = biografia.ifEmpty { getString(R.string.biografia_vacia) }
        val telefonoCompleto = getString(R.string.formato_telefono, clavePais, telefono)
        // Se utiliza para pasar la información a la pantalla de Reporte
        val intent = Intent(this, Reporte::class.java).apply {
            putExtra(Reporte.NOMBRE, nombre)
            putExtra(Reporte.APELLIDO, apellido)
            putExtra(Reporte.FECHA_NACIMIENTO, fechaNacimiento)
            putExtra(Reporte.GENERO, genero)
            putExtra(Reporte.TELEFONO, telefonoCompleto)
            putExtra(Reporte.CORREO, correo)
            putExtra(Reporte.INTERESES, textoIntereses)
            putExtra(Reporte.BIOGRAFIA, textoBiografia)
            // Si hay una foto de perfil, la pasamos a la pantalla de Reporte
            uriFotoPerfil?.let { putExtra("FOTO_PERFIL", it.toString()) }
        }
        startActivity(intent)
    }

    // Validamos cada campo del formulario
    private fun validarFormulario(): Boolean {
        var esValido = true
        // Limpiamos los errores previos para volver a validar
        binding.tilNombre.error = null
        binding.tilApellido.error = null
        binding.tilFechaNacimiento.error = null
        binding.tilTelefono.error = null
        binding.tilCorreo.error = null
        binding.tvErrorGenero.visibility = View.GONE

        // Validación de campos obligatorios
        if (binding.etNombre.text.toString().trim().isEmpty()) {
            binding.tilNombre.error = getString(R.string.error_campo_obligatorio)
            esValido = false
        }

        if (binding.etApellido.text.toString().trim().isEmpty()) {
            binding.tilApellido.error = getString(R.string.error_campo_obligatorio)
            esValido = false
        }

        // Validación de edad mínima (13 años)
        if (calendarioNacimiento == null) {
            binding.tilFechaNacimiento.error = getString(R.string.error_fecha_obligatoria)
            esValido = false
        } else if (calcularEdad(calendarioNacimiento!!) < 13) {
            binding.tilFechaNacimiento.error = getString(R.string.error_edad_minima)
            esValido = false
        }

        if (binding.rgGenero.checkedRadioButtonId == -1) {
            binding.tvErrorGenero.text = getString(R.string.error_genero_obligatorio)
            binding.tvErrorGenero.visibility = View.VISIBLE
            esValido = false
        }

        // Validación de longitud del número de teléfono para cada país
        val claves = resources.getStringArray(R.array.claves_pais)
        val pos = binding.spClavePais.selectedItemPosition
        val clave = claves[pos]
        val nombrePais = resources.getStringArray(R.array.nombres_pais)[pos]
        val longEsperada = longitudesTelefono[clave] ?: 10

        if (binding.etTelefono.text.toString().trim().length != longEsperada) {
            binding.tilTelefono.error = getString(R.string.error_telefono_invalido, longEsperada, nombrePais)
            esValido = false
        }

        // Validación de formato válido para el correo
        val correo = binding.etCorreo.text.toString().trim()
        if (correo.isEmpty()) {
            binding.tilCorreo.error = getString(R.string.error_campo_obligatorio)
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            binding.tilCorreo.error = getString(R.string.error_correo_invalido)
            esValido = false
        }

        return esValido
    }

    // Obtenemos el género seleccionado
    private fun obtenerGeneroSeleccionado(): String? {
        val id = binding.rgGenero.checkedRadioButtonId
        return if (id != -1) findViewById<RadioButton>(id).text.toString() else null
    }

    // Obtenemos los intereses seleccionados
    private fun obtenerInteresesSeleccionados(): List<String> {
        val intereses = mutableListOf<String>()
        val checks = listOf(
            binding.cbMusica, binding.cbViajes, binding.cbDeportes,
            binding.cbTecnologia, binding.cbArte, binding.cbCine,
            binding.cbLectura, binding.cbCocina, binding.cbFotografia,
            binding.cbVideojuegos
        )
        checks.forEach { if (it.isChecked) intereses.add(it.text.toString()) }
        return intereses
    }

    // Calculamos la edad a partir de la fecha de nacimiento para validarla (13 años cumplidos)
    private fun calcularEdad(fecha: Calendar): Int {
        val hoy = Calendar.getInstance()
        var edad = hoy.get(Calendar.YEAR) - fecha.get(Calendar.YEAR)
        if (hoy.get(Calendar.DAY_OF_YEAR) < fecha.get(Calendar.DAY_OF_YEAR)) edad--
        return edad
    }
}
