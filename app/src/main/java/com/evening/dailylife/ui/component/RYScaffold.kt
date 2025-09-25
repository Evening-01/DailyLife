package com.evening.dailylife.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.evening.dailylife.R
import com.evening.dailylife.util.debouncedPopBackStack
import com.moriafly.salt.ui.SaltTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RYScaffold(
    // 改为 @Composable lambda，允许传入复杂的标题布局，而不仅仅是字符串
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController?,
    // 是否显示导航图标（返回按钮），默认为 true
    showNavigationIcon: Boolean = true,
    // 允许自定义 TopAppBar 的颜色
    topAppBarColors: TopAppBarColors = TopAppBarDefaults.centerAlignedTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = SaltTheme.colors.text,
        navigationIconContentColor = SaltTheme.colors.text
    ),
    containerColor: Color = SaltTheme.colors.background,
    actions: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackBarHostState: SnackbarHostState? = null,
    // content 的 lambda 签名改变，会传出内边距，让调用者决定如何使用
    content: @Composable (PaddingValues) -> Unit,
) {
    // 判断是否能返回：只有当导航控制器存在且后退栈中有上一个页面时，才认为可以返回
    val canPop = navController?.previousBackStackEntry != null

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        topBar = {
            // 使用 CenterAlignedTopAppBar 来实现标题居中
            CenterAlignedTopAppBar(
                title = title,
                navigationIcon = {
                    // 只有当允许显示图标且确实可以返回时，才显示返回按钮
                    if (showNavigationIcon && canPop) {
                        IconButton(onClick = { navController.debouncedPopBackStack() }) {
                            Icon(
                                imageVector = Icons.Sharp.ArrowBackIosNew,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = { actions() },
                colors = topAppBarColors,
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        content = { innerPadding ->
            // 将 Scaffold 计算好的内边距（包含顶部栏高度等）传给 content
            // 外部的布局就可以使用这个 padding 来避免内容被遮挡
            content(innerPadding)
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        snackbarHost = {
            snackBarHostState?.let {
                SnackbarHost(hostState = it)
            }
        }
    )
}