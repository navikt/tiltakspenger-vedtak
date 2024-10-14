package no.nav.tiltakspenger.saksbehandling.domene.behandling

sealed interface KanIkkeOppretteBehandling {
    data object StøtterIkkeBarnetillegg : KanIkkeOppretteBehandling
    data object FantIkkeTiltak : KanIkkeOppretteBehandling
    data class StøtterKunInnvilgelse(val underliggende: StøtterIkkeUtfall) : KanIkkeOppretteBehandling
}

sealed interface StøtterIkkeUtfall {
    data object DelvisKravfrist : StøtterIkkeUtfall
    data object DelvisAlder : StøtterIkkeUtfall
    data object DelvisKVP : StøtterIkkeUtfall
    data object DelvisIntroduksjonsprogram : StøtterIkkeUtfall
    data object DelvisLivsopphold : StøtterIkkeUtfall
    data object DelvisInstitusjon : StøtterIkkeUtfall
    data object DelvisTiltaksdeltagelse : StøtterIkkeUtfall
    data object DelvisInnvilgelse : StøtterIkkeUtfall
}
