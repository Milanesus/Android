package com.leonardo.mundial2026

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.Coil
import coil.ImageLoader
import com.leonardo.mundial2026.data.network.RetrofitClient
import com.leonardo.mundial2026.ui.screens.ListaEquipos
import com.leonardo.mundial2026.ui.screens.ListaJugadores
import com.leonardo.mundial2026.ui.theme.Mundial2026Theme
import com.leonardo.mundial2026.ui.viewmodel.FootballViewModel

class MainActivity : ComponentActivity() {
    
    private val TAG = "Mundial2026_Main"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configuración para que las imágenes carguen en Android 7
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(RetrofitClient.getUnsafeOkHttpClient())
            .build()
        Coil.setImageLoader(imageLoader)
        
        try {
            // Obtenemos el ViewModel para manejar los datos de los equipos
            val viewModel: FootballViewModel by viewModels()
            
            enableEdgeToEdge()
            
            setContent {
                Mundial2026Theme(dynamicColor = false) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Configuramos la navegación entre pantallas
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController, 
                            startDestination = "teamList",
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Pantalla 1: La lista de todos los países
                            composable("teamList") {
                                ListaEquipos(viewModel = viewModel) { teamId, teamName ->
                                    navController.navigate("playerList/$teamId/$teamName")
                                }
                            }
                            // Pantalla 2: Los jugadores del equipo seleccionado
                            composable(
                                route = "playerList/{teamId}/{teamName}",
                                arguments = listOf(
                                    navArgument("teamId") { type = NavType.IntType },
                                    navArgument("teamName") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val teamId = backStackEntry.arguments?.getInt("teamId") ?: 0
                                val teamName = backStackEntry.arguments?.getString("teamName") ?: ""
                                ListaJugadores(
                                    viewModel = viewModel,
                                    teamId = teamId,
                                    teamName = teamName,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en onCreate: ${e.message}", e)
        }
    }
}
