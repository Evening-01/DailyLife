package com.evening.dailylife.feature.discover

data class DiscoverUiState(
    val typeProfile: TypeProfile = TypeProfile(),
    val isLoading: Boolean = true,
    val year: Int? = null,
    val month: Int? = null
)
