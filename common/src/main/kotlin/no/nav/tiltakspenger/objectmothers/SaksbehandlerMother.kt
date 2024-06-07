package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

interface SaksbehandlerMother {
    fun saksbehandler() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = listOf(Rolle.SAKSBEHANDLER),
    )

    fun saksbehandlerUtenTilgang() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = emptyList(),
    )

    fun beslutter() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = listOf(Rolle.BESLUTTER),
    )

    fun saksbehandlerMedAdmin() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = listOf(Rolle.ADMINISTRATOR),
    )

    fun saksbehandlerMedKode6() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = listOf(Rolle.SAKSBEHANDLER, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    fun saksbehandlerMedKode7() = Saksbehandler(
        navIdent = "Z12345",
        brukernavn = "Sak Behandler",
        epost = "Sak.Behandler@nav.no",
        roller = listOf(Rolle.SAKSBEHANDLER, Rolle.FORTROLIG_ADRESSE),
    )

    fun saksbehandler123(): Saksbehandler =
        Saksbehandler(
            navIdent = "123",
            brukernavn = "Test Testesen",
            epost = "Test.Testesen@nav.no",
            roller = listOf(Rolle.SAKSBEHANDLER),
        )
}
