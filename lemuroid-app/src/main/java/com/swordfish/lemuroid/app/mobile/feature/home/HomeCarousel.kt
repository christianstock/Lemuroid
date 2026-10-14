package com.swordfish.lemuroid.app.mobile.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.swordfish.lemuroid.R
import com.swordfish.lemuroid.app.mobile.feature.home.cartridge.CartridgeItem
import com.swordfish.lemuroid.app.mobile.shared.GameArtBlurBackground
import com.swordfish.lemuroid.app.mobile.shared.extractAccentColor
import com.swordfish.lemuroid.lib.library.db.entity.Game
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

val PressStart2PFontFamily = FontFamily(Font(R.font.press_start_2p))

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCarousel(
    systemLibraries: List<HomeViewModel.SystemLibrary>,
    selectedSystemId: String?,
    systemScrollPositions: Map<String, Int>,
    refreshCount: Int,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    onSystemSelected: (String) -> Unit,
    onSystemScroll: (String, Int) -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (systemLibraries.isEmpty()) return

    val systemsCount = systemLibraries.size
    val baseIndex = HomeViewModel.BASE_PAGE_INDEX

    val initialSystemIndex = if (systemsCount > 1) baseIndex - (baseIndex % systemsCount) else 0
    val currentSystemInternalIndex = systemLibraries
        .indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
        .coerceAtLeast(0)

    val systemPagerState = rememberPagerState(
        initialPage = initialSystemIndex + currentSystemInternalIndex
    ) {
        if (systemsCount > 1) Int.MAX_VALUE else 1
    }

    LaunchedEffect(systemPagerState.currentPage) {
        if (systemLibraries.isNotEmpty()) {
            val currentSystemId = systemLibraries[systemPagerState.currentPage % systemsCount].systemId
            if (!currentSystemId.equals(selectedSystemId, ignoreCase = true)) {
                onSystemSelected(currentSystemId)
            }
        }
    }

    LaunchedEffect(selectedSystemId) {
        if (selectedSystemId != null && systemsCount > 1) {
            val targetIndex = systemLibraries.indexOfFirst { it.systemId.equals(selectedSystemId, ignoreCase = true) }
            if (targetIndex != -1) {
                val currentModulo = systemPagerState.currentPage % systemsCount
                val difference = targetIndex - currentModulo
                val targetPage = systemPagerState.currentPage + difference
                if (systemPagerState.currentPage != targetPage) {
                    systemPagerState.animateScrollToPage(targetPage)
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = systemPagerState,
            key = { page -> systemLibraries[page % systemsCount].systemId },
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = systemsCount > 1
        ) { systemPage ->
            val library = systemLibraries[systemPage % systemsCount]
            val scrollPage = systemScrollPositions[library.systemId] ?: baseIndex

            ConsoleStage(
                library = library,
                refreshCount = refreshCount,
                scrollPage = scrollPage,
                onScroll = { newIndex -> onSystemScroll(library.systemId, newIndex) },
                onGameClick = onGameClick,
                onShowContextMenu = onShowContextMenu,
                onNavigateToList = onNavigateToList,
                onOpenSettings = onOpenSettings
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConsoleStage(
    library: HomeViewModel.SystemLibrary,
    refreshCount: Int,
    scrollPage: Int,
    onScroll: (Int) -> Unit,
    onGameClick: (Game) -> Unit,
    onShowContextMenu: (Game) -> Unit,
    onNavigateToList: (Game) -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val games = library.games

    val initialPage = if (games.size <= 1) 0 else scrollPage
    val gamePagerState = rememberPagerState(initialPage = initialPage) {
        if (games.size <= 1) 1 else Int.MAX_VALUE
    }
    val context = LocalContext.current

    LaunchedEffect(scrollPage) {
        if (games.size > 1 && gamePagerState.currentPage != scrollPage) {
            gamePagerState.scrollToPage(scrollPage)
        }
    }

    LaunchedEffect(gamePagerState.currentPage, gamePagerState.isScrollInProgress) {
        if (!gamePagerState.isScrollInProgress && games.isNotEmpty()) {
            onScroll(gamePagerState.currentPage)
        }
    }

    val currentGame = if (games.isNotEmpty()) {
        games[gamePagerState.currentPage % games.size]
    } else null

    val fallbackAccentColor = MaterialTheme.colorScheme.primary
    var accentColor by remember { mutableStateOf(fallbackAccentColor) }

    LaunchedEffect(currentGame?.id, currentGame?.coverFrontUrl) {
        accentColor = extractAccentColor(
            context = context,
            coverUrl = currentGame?.coverFrontUrl,
            fallbackColor = fallbackAccentColor
        )
    }

    val animatedAccentColor by animateColorAsState(
        targetValue = accentColor,
        animationSpec = tween(durationMillis = 500),
        label = "AccentColorCrossfade"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "CartridgeFloatTransition")
    val idleFloatDp by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dyFloat"
    )

    val hintAlphaAnim = remember { Animatable(0f) }
    LaunchedEffect(library.systemId) {
        hintAlphaAnim.snapTo(0f)
        hintAlphaAnim.animateTo(0.85f, animationSpec = tween(600))
        delay(3500.milliseconds)
        hintAlphaAnim.animateTo(0f, animationSpec = tween(800))
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        GameArtBlurBackground(
            coverFrontUrl = currentGame?.coverFrontUrl,
            modifier = Modifier.fillMaxSize()
        )

        // Top Header Info
        if (currentGame != null) {
            GameTitle(
                game = currentGame,
                accentColor = animatedAccentColor,
                pagerState = gamePagerState,
                itemCount = games.size,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)
                    .fillMaxWidth()
            )
        }

        // Middle Games Carousel
        if (games.isNotEmpty()) {
            val cartridgeWidth = 300.dp
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val dynamicPadding = ((screenWidth - cartridgeWidth) / 2).coerceAtLeast(0.dp)

            HorizontalPager(
                state = gamePagerState,
                key = { page -> games[page % games.size].id },
                pageSize = PageSize.Fixed(cartridgeWidth),
                pageSpacing = (-60).dp,
                contentPadding = PaddingValues(horizontal = dynamicPadding),
                userScrollEnabled = games.size > 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 160.dp, top = 140.dp)
            ) { page ->
                val game = games[page % games.size]
                val isFocused = gamePagerState.currentPage == page

                CartridgeItem(
                    game = game,
                    page = page,
                    totalGames = games.size,
                    pagerState = gamePagerState,
                    isFocused = isFocused,
                    idleFloatDp = idleFloatDp,
                    refreshCount = refreshCount,
                    accentColor = animatedAccentColor,
                    hintAlpha = hintAlphaAnim.value,
                    onGameClick = onGameClick,
                    onShowContextMenu = onShowContextMenu,
                    onNavigateToList = onNavigateToList
                )
            }
        }

        // Bottom Console Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.BottomCenter
        ) {
            SystemForegroundView(
                systemId = library.systemId,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
