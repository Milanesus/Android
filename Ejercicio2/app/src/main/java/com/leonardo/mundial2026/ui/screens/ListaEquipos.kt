package com.leonardo.mundial2026.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.leonardo.mundial2026.R
import com.leonardo.mundial2026.data.model.TeamContainer
import com.leonardo.mundial2026.ui.viewmodel.FootballViewModel
import com.leonardo.mundial2026.ui.viewmodel.TeamsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaEquipos(
    viewModel: FootballViewModel,
    onTeamClick: (Int, String) -> Unit
) {
    // Obtenemos los estados desde el ViewModel
    val state by viewModel.teamsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Carga inicial de los equipos
    LaunchedEffect(Unit) {
        viewModel.fetchTeams()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_teams), fontWeight = FontWeight.ExtraBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Buscador de países
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.search_placeholder)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.back))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                )
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val currentState = state) {
                    is TeamsUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is TeamsUiState.Error -> {
                        // Si algo sale mal o no hay internet, mostramos esta vista
                        ErrorView(
                            message = if (currentState.isNoInternet) stringResource(R.string.error_no_internet) else stringResource(R.string.error_loading_teams),
                            onRetry = { viewModel.fetchTeams() }
                        )
                    }
                    is TeamsUiState.Empty -> {
                        Text(stringResource(R.string.error_no_teams), modifier = Modifier.align(Alignment.Center))
                    }
                    is TeamsUiState.Success -> {
                        // Filtramos la lista según lo que el usuario escriba en el buscador
                        val filteredTeams = remember(searchQuery, currentState.teams) {
                            if (searchQuery.isEmpty()) currentState.teams
                            else currentState.teams.filter {
                                it.team.name.contains(searchQuery, ignoreCase = true) ||
                                (it.team.country?.contains(searchQuery, ignoreCase = true) ?: false)
                            }
                        }

                        if (filteredTeams.isEmpty()) {
                            Text(
                                text = stringResource(R.string.no_results),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = filteredTeams,
                                    key = { it.team.id }, // Usar key mejora el rendimiento del scroll
                                    contentType = { "team_card" }
                                ) { teamContainer ->
                                    TeamItem(
                                        teamContainer = teamContainer,
                                        modifier = Modifier.animateItem(), // Animación suave al filtrar
                                        onClick = {
                                            onTeamClick(teamContainer.team.id, teamContainer.team.name)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
fun TeamItem(
    teamContainer: TeamContainer, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del logo del equipo
            AsyncImage(
                model = teamContainer.team.logo,
                contentDescription = null,
                modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                // Nombre de la selección
                Text(
                    text = teamContainer.team.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val teamName = teamContainer.team.name
                    val countryName = teamContainer.team.country
                    
                    // Lógica para no mostrar el nombre del país si ya es igual al del equipo
                    val isDuplicate = countryName != null && (
                        countryName.equals(teamName, ignoreCase = true) || 
                        countryName.replace("-", " ").equals(teamName.replace("-", " "), ignoreCase = true)
                    )

                    if (!isDuplicate && countryName != null) {
                        Text(text = countryName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.tertiary)
                        if (teamContainer.team.code != null) Text(" | ", style = MaterialTheme.typography.bodySmall)
                    }
                    
                    // Código FIFA (en azul)
                    teamContainer.team.code?.let {
                        Text(
                            text = stringResource(R.string.fifa_code, it), 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Año de la federación
                teamContainer.team.founded?.let {
                    Text(text = stringResource(R.string.founded_at, it), style = MaterialTheme.typography.labelSmall)
                }
                // Información del estadio y ciudad
                teamContainer.venue?.name?.let { venueName ->
                    val stadiumText = if (!teamContainer.venue.city.isNullOrBlank()) {
                        stringResource(R.string.stadium, venueName, teamContainer.venue.city)
                    } else venueName
                    Text(text = stadiumText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
