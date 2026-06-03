package com.leonardo.mundial2026.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leonardo.mundial2026.R
import com.leonardo.mundial2026.data.model.Player
import com.leonardo.mundial2026.ui.viewmodel.FootballViewModel
import com.leonardo.mundial2026.ui.viewmodel.PlayersUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaJugadores(
    viewModel: FootballViewModel,
    teamId: Int,
    teamName: String,
    onBack: () -> Unit
) {
    val state by viewModel.playersState.collectAsState()

    // Cada vez que entramos a esta pantalla con un ID de equipo diferente, cargamos sus jugadores
    LaunchedEffect(teamId) {
        viewModel.fetchPlayers(teamId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_players, teamName), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val currentState = state) {
                is PlayersUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PlayersUiState.Empty -> {
                    // Si el equipo no tiene jugadores registrados en la API
                    Text(
                        text = stringResource(R.string.error_no_players),
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                is PlayersUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentState.isNoInternet) 
                                stringResource(R.string.error_no_internet) 
                            else 
                                stringResource(R.string.error_loading_players),
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.fetchPlayers(teamId) }, modifier = Modifier.padding(top = 8.dp)) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                is PlayersUiState.Success -> {
                    // Mostramos la lista de jugadores
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentState.players) { player ->
                            PlayerItem(player)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerItem(player: Player) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto del jugador con forma circular
            AsyncImage(
                model = player.photo,
                contentDescription = stringResource(R.string.player_photo_desc, player.name),
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Traducción de las posiciones de los jugadores
                val translatedPosition = when (player.position?.lowercase()) {
                    "goalkeeper" -> stringResource(R.string.pos_goalkeeper)
                    "defender" -> stringResource(R.string.pos_defender)
                    "midfielder" -> stringResource(R.string.pos_midfielder)
                    "attacker" -> stringResource(R.string.pos_attacker)
                    else -> player.position ?: ""
                }
                val numberText = player.number?.toString() ?: stringResource(R.string.not_available)
                // Mostramos posición (en rojo) y número
                Text(
                    text = stringResource(
                        R.string.position_number,
                        translatedPosition,
                        numberText
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
                // Edad (en azul)
                player.age?.let {
                    Text(
                        text = stringResource(R.string.player_age, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
