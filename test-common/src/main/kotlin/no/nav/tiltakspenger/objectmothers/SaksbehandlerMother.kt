package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Saksbehandler
import no.nav.tiltakspenger.libs.common.Rolle
import no.nav.tiltakspenger.libs.common.Roller

interface SaksbehandlerMother {
    fun saksbehandler(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
        roller: Roller = Roller(listOf(Rolle.SAKSBEHANDLER)),
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
        roller = Roller(emptyList()),
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
    )

    fun beslutter(
        navIdent: String = "B12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = saksbehandler(
        roller = Roller(listOf(Rolle.BESLUTTER)),
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
        roller = Roller(listOf(Rolle.SAKSBEHANDLER, Rolle.STRENGT_FORTROLIG_ADRESSE)),
    )

    fun saksbehandlerMedKode7(
        navIdent: String = "Z12345",
        brukernavn: String = "Sak Behandler",
        epost: String = "Sak.Behandler@nav.no",
    ) = Saksbehandler(
        navIdent = navIdent,
        epost = epost,
        brukernavn = brukernavn,
        roller = Roller(listOf(Rolle.SAKSBEHANDLER, Rolle.FORTROLIG_ADRESSE)),
    )

    fun saksbehandler123(): Saksbehandler =
        Saksbehandler(
            navIdent = "123",
            brukernavn = "Test Testesen",
            epost = "Test.Testesen@nav.no",
            roller = Roller(listOf(Rolle.SAKSBEHANDLER)),
        )
}
