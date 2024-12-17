package no.nav.tiltakspenger.vedtak.routes.sak

import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak

data class SakDTO(
    val saksnummer: String,
    val sakId: String,
    val fnr: String,
    val behandlingsoversikt: List<SaksoversiktDTO>,
    val meldekortoversikt: List<MeldekortoversiktDTO>,
)

fun Sak.toDTO() =
    SakDTO(
        saksnummer = saksnummer.verdi,
        sakId = id.toString(),
        fnr = fnr.verdi,
        behandlingsoversikt = behandlinger.toSaksoversiktDTO(),
        meldekortoversikt = meldeperioder.verdi.toMeldekortoversiktDTO(),
    )
