package no.nav.tiltakspenger.felles

sealed interface KunneIkkeHenteNavnForNavIdent {
    data object FeilVedHentingAvOnBehalfOfToken : KunneIkkeHenteNavnForNavIdent

    data object KallTilMicrosoftGraphApiFeilet : KunneIkkeHenteNavnForNavIdent

    data object DeserialiseringAvResponsFeilet : KunneIkkeHenteNavnForNavIdent

    data object FantIkkeBrukerForNavIdent : KunneIkkeHenteNavnForNavIdent
}
