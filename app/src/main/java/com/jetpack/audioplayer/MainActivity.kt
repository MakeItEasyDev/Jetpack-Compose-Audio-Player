package com.jetpack.audioplayer

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jetpack.audioplayer.ui.theme.AudioPlayerTheme
import com.jetpack.audioplayer.viewmodel.MediaPlayerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AudioPlayerTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val mediaPlayer: MediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)
                    AudioPlayer(mediaPlayer)
                }
            }
        }
    }
}

@Composable
fun AudioPlayer(mediaPlayer: MediaPlayer) {
    val viewModel: MediaPlayerViewModel = viewModel()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        viewModel.getFirstColor(),
                        viewModel.getSecondColor()
                    )
                )
            )
            .padding(horizontal = 10.dp)
    ) {
        TopAppBar()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(id = R.drawable.music),
                contentDescription = "Image Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .sizeIn(maxWidth = 500.dp, maxHeight = 500.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .weight(10f)
            )
            Spacer(modifier = Modifier.height(30.dp))
            SongDescription("Audio Name", "Singer Name")
            Spacer(modifier = Modifier.height(35.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(10f)
            ) {
                PlayerSlider(mediaPlayer)
                Spacer(modifier = Modifier.height(40.dp))
                PlayerButtons(modifier = Modifier.padding(vertical = 8.dp), mediaPlayer)
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun TopAppBar() {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back Icon",
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.PlaylistAdd,
                contentDescription = "Add list",
                tint = Color.White
            )
        }
        IconButton(onClick = { /*TODO*/ }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Icon",
                tint = Color.White
            )
        }
    }
}

@Composable
fun SongDescription(
    title: String,
    name: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.h5,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = Color.White,
        fontWeight = FontWeight.Bold
    )

    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = name,
            style = MaterialTheme.typography.body2,
            maxLines = 1,
            color = Color.White
        )
    }
}

@Composable
fun PlayerSlider(mediaPlayer: MediaPlayer) {
    val viewModel: MediaPlayerViewModel = viewModel()
    val currentMinutes = viewModel.currentMinutes.observeAsState()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Slider(
            value = currentMinutes.value!!.toFloat(),
            onValueChange = {},
            valueRange = 0f..mediaPlayer.duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${currentMinutes.value!!} s",
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${mediaPlayer.duration} s",
                color = Color.White
            )
        }
    }
}

@Composable
fun PlayerButtons(
    modifier: Modifier = Modifier,
    mediaPlayer: MediaPlayer,
    playerButtonSize: Dp = 72.dp,
    sideButtonSize: Dp = 42.dp
) {
    val viewModel : MediaPlayerViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val audioFinish = viewModel.audioFinish.observeAsState()
    val audioFlag = remember { mutableStateOf(true) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val buttonModifier = Modifier
            .size(sideButtonSize)
            .semantics { role = Role.Button }

        Image(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = "Skip Icon",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = buttonModifier
        )
        Image(
            imageVector = Icons.Filled.Replay10,
            contentDescription = "Reply 10 Sec Icon",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = buttonModifier
        )
        Image(
            imageVector = if (audioFinish.value == false) {
                if (audioFlag.value) {
                    Icons.Filled.PlayCircleFilled
                } else {
                    Icons.Filled.PauseCircleFilled
                }
            } else {
                Icons.Filled.PlayCircleFilled
            },
            contentDescription = "Play / Pause Icon",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = Modifier
                .size(playerButtonSize)
                .semantics { role = Role.Button }
                .clickable {
                    if (audioFlag.value) {
                        mediaPlayer.start()
                        scope.launch {
                            delay(500)
                            viewModel.getMediaDuration(mediaPlayer = mediaPlayer)
                        }
                        audioFlag.value = false
                    } else {
                        audioFlag.value = true
                        mediaPlayer.pause()
                    }
                }
        )

        Image(
            imageVector = Icons.Filled.Forward10,
            contentDescription = "Forward Icon",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = buttonModifier
        )

        Image(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "Next Icon",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(Color.White),
            modifier = buttonModifier
        )
    }
}


















