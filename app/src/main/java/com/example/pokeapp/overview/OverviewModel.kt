package com.example.pokeapp.overview

sealed class OverviewState {
    object LoadingState : OverviewState()
    object ResultsState : OverviewState()
    object NoResultsState : OverviewState()
    data class ErrorState(val error: String) : OverviewState()
}