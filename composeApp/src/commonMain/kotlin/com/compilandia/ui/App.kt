package com.compilandia.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.compilandia.learning.Learner
import com.compilandia.platform.Storage
import com.compilandia.ui.fx.FxOverlay
import com.compilandia.ui.games.PlayScreen
import com.compilandia.ui.screens.BrainScreen
import com.compilandia.ui.screens.FeynmanScreen
import com.compilandia.ui.screens.FlashcardScreen
import com.compilandia.ui.screens.JournalScreen
import com.compilandia.ui.screens.LessonScreen
import com.compilandia.ui.screens.MapScreen
import com.compilandia.ui.screens.OnboardingScreen
import com.compilandia.ui.screens.RegionScreen
import com.compilandia.ui.screens.SettingsScreen
import kotlinx.coroutines.delay

@Composable
fun App(injected: AppState? = null) {
    val app = remember { injected ?: AppState(Learner.load(Storage.load())) }
    CompilandiaTheme {
        LaunchedEffect(Unit) {
            com.compilandia.ui.voice.VoiceBank.load()
            for (sfx in com.compilandia.platform.Sfx.entries) {
                try { com.compilandia.platform.Sound.load(sfx, com.compilandia.resources.Res.readBytes("files/${sfx.file}")) } catch (e: Exception) { }
            }
        }
        Box(Modifier.fillMaxSize().background(Palette.paper).safeDrawingPadding()) {
            val shake = shakeOffset()
            // Avançar entra pela direita, voltar pela esquerda. A chave garante estado novo a cada tela.
            AnimatedContent(
                targetState = app.stack.size to app.screen,
                modifier = Modifier.fillMaxSize().offset { shake },
                transitionSpec = {
                    val forward = targetState.first >= initialState.first
                    if (ThemeState.reduceMotion) {
                        EnterTransition.None togetherWith ExitTransition.None
                    } else {
                        (slideInHorizontally(tween(280)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(220))) togetherWith fadeOut(tween(140))
                    }
                },
                label = "tela",
            ) { (_, screen) ->
                when (screen) {
                    Screen.Onboarding -> OnboardingScreen(app)
                    Screen.Room -> com.compilandia.ui.room.RoomScreen(app)
                    Screen.Shop -> com.compilandia.ui.room.ShopScreen(app)
                    is Screen.Episode -> com.compilandia.ui.screens.EpisodeScreen(app, screen)
                    Screen.Map -> MapScreen(app)
                    is Screen.RegionView -> RegionScreen(app, screen.region)
                    is Screen.Lesson -> LessonScreen(app, screen)
                    is Screen.Feynman -> FeynmanScreen(app, screen.skill)
                    is Screen.Play -> PlayScreen(app, screen)
                    is Screen.Flashcards -> FlashcardScreen(app, screen.skills)
                    Screen.Brain -> BrainScreen(app)
                    Screen.Journal -> JournalScreen(app)
                    Screen.Settings -> SettingsScreen(app)
                    Screen.Lab -> com.compilandia.ui.lab.LabScreen(app)
                    is Screen.LabBison -> com.compilandia.ui.lab.LabBisonScreen(app, screen.spec)
                    is Screen.Exemplo -> com.compilandia.ui.lab.ExemploScreen(app, screen.skill)
                    Screen.Album -> com.compilandia.ui.album.AlbumScreen(app)
                    is Screen.CacaBug -> com.compilandia.ui.album.CacaBugScreen(app, screen.id)
                    Screen.Corrida -> com.compilandia.ui.corrida.CorridaScreen(app)
                    Screen.Desafio -> com.compilandia.ui.desafio.DesafioScreen(app)
                    Screen.Feed -> com.compilandia.ui.feed.FeedScreen(app)
                    is Screen.BossFight -> com.compilandia.ui.games.BossScreen(app, screen.region)
                    Screen.Guide -> com.compilandia.ui.screens.GuideScreen(app)
                    is Screen.GuideChapter -> com.compilandia.ui.screens.GuideChapterScreen(app, screen.number)
                }
            }
            JuiceOverlay()
            FxOverlay()
            val msg = app.toast
            LaunchedEffect(msg) { if (msg != null) { delay(2600); app.toast = null } }
            AnimatedVisibility(
                visible = msg != null,
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
            ) {
                Box(
                    Modifier.clip(RoundedCornerShape(16.dp)).background(Palette.codeBg)
                        .border(2.dp, Palette.yellow, RoundedCornerShape(16.dp)).padding(14.dp)
                ) { Text(msg ?: "", color = Palette.onAccent, style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}
