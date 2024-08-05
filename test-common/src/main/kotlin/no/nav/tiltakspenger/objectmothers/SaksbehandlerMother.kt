package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Rolle
import no.nav.tiltakspenger.felles.Saksbehandler

interface SaksbehandlerMother {

    fun saksbehandler(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
        roller: List<Rolle> = listOf(Rolle.SAKSBEHANDLER),
    ) = Saksbehandler(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller,
    )

    fun saksbehandlerUtenTilgang(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = emptyList(),
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
    )

    fun beslutter(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = listOf(Rolle.BESLUTTER),
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
    )

    fun saksbehandlerMedAdmin(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = listOf(Rolle.ADMINISTRATOR),
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
    )

    fun saksbehandlerMedKode6(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = Saksbehandler(
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
        roller = listOf(Rolle.SAKSBEHANDLER, Rolle.STRENGT_FORTROLIG_ADRESSE),
    )

    fun saksbehandlerMedKode7(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = Saksbehandler(
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
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
