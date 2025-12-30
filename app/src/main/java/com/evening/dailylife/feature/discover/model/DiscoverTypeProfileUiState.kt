package com.evening.dailylife.feature.discover.model

import com.evening.dailylife.core.model.TypeProfile

data class DiscoverTypeProfileUiState(
    val typeProfile: TypeProfile = TypeProfile(),
    val isLoading: Boolean = true,
    val year: Int? = null,
    val month: Int? = null,
)
