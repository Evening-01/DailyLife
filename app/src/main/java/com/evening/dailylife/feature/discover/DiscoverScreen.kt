package com.evening.dailylife.feature.discover

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.evening.dailylife.R
import com.evening.dailylife.app.ui.theme.LocalExtendedColorScheme
import com.evening.dailylife.feature.discover.component.DiscoverAiSection
import com.evening.dailylife.feature.discover.component.DiscoverCommonToolsSection
import com.evening.dailylife.feature.discover.component.TypeProfileSection
import com.evening.dailylife.feature.discover.model.DiscoverTypeProfileUiState
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import java.text.DecimalFormat

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val typeProfileState by viewModel.typeProfileState.collectAsState()
    val headerContainerColor = LocalExtendedColorScheme.current.headerContainer
    val headerContentColor = LocalExtendedColorScheme.current.onHeaderContainer
    val numberFormatter = remember { DecimalFormat("#,##0.00") }
    var profileAnimationTrigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                profileAnimationTrigger++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(typeProfileState.typeProfile, typeProfileState.isLoading) {
        if (!typeProfileState.isLoading) {
            profileAnimationTrigger++
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.discover),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = headerContainerColor,
                    titleContentColor = headerContentColor,
                ),
            )
        },
    ) { innerPadding ->
        DiscoverContent(
            innerPadding = innerPadding,
            typeProfileState = typeProfileState,
            numberFormatter = numberFormatter,
            profileAnimationKey = profileAnimationTrigger,
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DiscoverContent(
    innerPadding: PaddingValues,
    typeProfileState: DiscoverTypeProfileUiState,
    numberFormatter: DecimalFormat,
    profileAnimationKey: Int,
) {
    val scrollState = rememberScrollState()
    val sectionSpacing = 8.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing),
    ) {
        RoundedColumn {
            val profileTitle = typeProfileState.month?.let { month ->
                stringResource(id = R.string.discover_type_profile_title_month, month)
            } ?: stringResource(id = R.string.chart_type_profile_title)
            ItemTitle(text = profileTitle)
            DiscoverTypeProfileContent(
                state = typeProfileState,
                numberFormatter = numberFormatter,
                animationKey = profileAnimationKey,
            )
        }
        DiscoverAiSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        DiscoverCommonToolsSection()
    }
}

@Composable
private fun DiscoverTypeProfileContent(
    state: DiscoverTypeProfileUiState,
    numberFormatter: DecimalFormat,
    animationKey: Int,
) {
    if (state.isLoading) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        )
    } else {
        TypeProfileSection(
            profile = state.typeProfile,
            numberFormatter = numberFormatter,
            animationKey = animationKey,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
        )
    }
}
