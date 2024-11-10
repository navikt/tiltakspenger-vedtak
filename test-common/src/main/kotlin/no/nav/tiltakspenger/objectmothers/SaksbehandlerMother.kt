package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.libs.common.Saksbehandler
import no.nav.tiltakspenger.libs.common.Saksbehandlerrolle
import no.nav.tiltakspenger.libs.common.Saksbehandlerroller

interface SaksbehandlerMother {
    fun saksbehandler(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
        roller: Saksbehandlerroller = Saksbehandlerroller(listOf(Saksbehandlerrolle.SAKSBEHANDLER)),
    ) = Saksbehandler(
        navIdent = navIdent,
        brukernavn = brukernavn,
        epost = epost,
        roller = roller,
    )

    fun saksbehandlerUtenTilgang(
        navIdent: String = "U12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = Saksbehandlerroller(emptyList()),
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
    )

    fun beslutter(
        navIdent: String = "B12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = Saksbehandlerroller(listOf(Saksbehandlerrolle.BESLUTTER)),
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
        roller = Saksbehandlerroller(listOf(Saksbehandlerrolle.SAKSBEHANDLER, Saksbehandlerrolle.STRENGT_FORTROLIG_ADRESSE)),
    )

    fun saksbehandlerMedKode7(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = Saksbehandler(
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
        roller = Saksbehandlerroller(listOf(Saksbehandlerrolle.SAKSBEHANDLER, Saksbehandlerrolle.FORTROLIG_ADRESSE)),
    )

    fun saksbehandler123(): Saksbehandler =
        Saksbehandler(
            navIdent = "123",
            brukernavn = "Test Testesen",
            epost = "Test.Testesen@nav.no",
            roller = Saksbehandlerroller(listOf(Saksbehandlerrolle.SAKSBEHANDLER)),
        )
}
