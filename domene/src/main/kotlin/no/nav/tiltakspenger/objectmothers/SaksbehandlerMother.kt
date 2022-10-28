package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

fun saksbehandler() = Saksbehandler(
    navIdent = "Z12345",
    brukernavn = "Sak Behandler",
    epost = "Sak.Behandler@nav.no",
    roller = listOf(Rolle.SAKSBEHANDLER)
)

fun saksbehandlerMedKode6() = Saksbehandler(
    navIdent = "Z12345",
    brukernavn = "Sak Behandler",
    epost = "Sak.Behandler@nav.no",
    roller = listOf(Rolle.SAKSBEHANDLER, Rolle.STRENGT_FORTROLIG_ADRESSE)
)
