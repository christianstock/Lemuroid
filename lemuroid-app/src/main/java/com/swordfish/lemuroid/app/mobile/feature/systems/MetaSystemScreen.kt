package com.swordfish.lemuroid.app.mobile.feature.systems

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.swordfish.lemuroid.app.mobile.feature.home.HomeViewModel
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidEmptyView
import com.swordfish.lemuroid.app.mobile.shared.compose.ui.LemuroidSystemCard
import com.swordfish.lemuroid.app.shared.systems.MetaSystemInfo

@Composable
fun MetaSystemsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: MetaSystemsViewModel,
    homeViewModel: HomeViewModel,
) {
    val metaSystems = viewModel.availableMetaSystems.collectAsState(emptyList())
    MetaSystemsScreen(
        modifier = modifier,
        metaSystems = metaSystems.value,
        onSystemClicked = { 
            // Update the home screen focus system
            homeViewModel.setSelectedSystem(it.metaSystem.name)
            // Navigate back home
            navController.popBackStack()
        },
    )
}

@Composable
private fun MetaSystemsScreen(
    modifier: Modifier = Modifier,
    metaSystems: List<MetaSystemInfo>,
    onSystemClicked: (MetaSystemInfo) -> Unit,
) {
    if (metaSystems.isEmpty()) {
        LemuroidEmptyView()
        return
    }

    LazyVerticalGrid(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        columns = GridCells.Adaptive(144.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(metaSystems.size, key = { metaSystems[it].metaSystem }) { index ->
            val system = metaSystems[index]
            LemuroidSystemCard(
                modifier = Modifier.animateItem(),
                system = system,
                onClick = { onSystemClicked(system) },
            )
        }
    }
}
