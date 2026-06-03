package com.leonardo.formulario

import android.os.Bundle
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.leonardo.formulario.databinding.ActivityReporteBinding

class Reporte : AppCompatActivity() {

    // Binding para acceder a los componentes de la interfaz de reporte
    private lateinit var binding: ActivityReporteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Desactivamos el modo oscuro para que el reporte sea legible con el diseño morado
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        // Usamos el modo Edge-to-Edge como en la pantalla principal
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Iniciamos el binding
        binding = ActivityReporteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ajustamos el padding para evitar que el texto se encime con la
        // barra de navegación o la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Recuperamos los datos enviados desde el formulario
        // Usamos .orEmpty() para que no fallen si llega algún nulo
        val nombre = intent.getStringExtra(NOMBRE).orEmpty()
        val apellido = intent.getStringExtra(APELLIDO).orEmpty()
        val fechaNacimiento = intent.getStringExtra(FECHA_NACIMIENTO).orEmpty()
        val genero = intent.getStringExtra(GENERO).orEmpty()
        val telefono = intent.getStringExtra(TELEFONO).orEmpty()
        val correo = intent.getStringExtra(CORREO).orEmpty()
        val intereses = intent.getStringExtra(INTERESES).orEmpty()
        val biografia = intent.getStringExtra(BIOGRAFIA).orEmpty()
        val uriFotoString = intent.getStringExtra("FOTO_PERFIL")

        // Mostramos la foto de perfil si se seleccionó una, si no, se queda la predeterminada
        uriFotoString?.let {
            // Convertimos la cadena de texto en un objeto Uri para cargar la foto seleccionada
            binding.ivFotoReporte.setImageURI(Uri.parse(it))
        }

        // Asignamos los valores a los TextViews correspondientes
        // Unimos nombre y apellido con un espacio de
        // no ruptura (\u00A0) para evitar saltos de línea prematuros
        binding.tvNombreValor.text = "$nombre\u00A0$apellido"
        binding.tvFechaNacimientoValor.text = fechaNacimiento
        binding.tvGeneroValor.text = genero
        binding.tvTelefonoValor.text = telefono
        binding.tvCorreoValor.text = correo
        binding.tvInteresesValor.text = intereses
        binding.tvBiografiaValor.text = biografia
    }

    // Aquí definimos las llaves para identificar correctamente cada dato al pasar entre pantallas
    companion object {
        const val NOMBRE = "nombre"
        const val APELLIDO = "apellido"
        const val FECHA_NACIMIENTO = "fecha_nacimiento"
        const val GENERO = "genero"
        const val TELEFONO = "telefono"
        const val CORREO = "correo"
        const val INTERESES = "intereses"
        const val BIOGRAFIA = "biografia"
    }
}