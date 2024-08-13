package no.nav.tiltakspenger.vedtak.clients.person

import io.kotest.matchers.shouldBe
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnUtenIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PersonMapperTest {
    @Test
    fun `serialisering av barn med manglende ident`() {
        val tidspunkt = LocalDateTime.now()
        val fnr = Fnr.random()
        mapPersonopplysninger(
            json = pdlResponseManglendeIdentPåBarn,
            innhentet = tidspunkt,
            fnr = fnr,
        ).also {
            it shouldBe listOf(
                PersonopplysningerBarnUtenIdent(
                    fødselsdato = LocalDate.of(2016, 5, 23),
                    fornavn = "Geometrisk",
                    mellomnavn = "Sprudlende",
                    etternavn = "Jakt",
                    tidsstempelHosOss = tidspunkt,
                ),
                PersonopplysningerSøker(
                    fnr = fnr,
                    fødselsdato = LocalDate.of(1984, 7, 4),
                    fornavn = "Lykkelig",
                    mellomnavn = null,
                    etternavn = "Eksamen",
                    fortrolig = false,
                    strengtFortrolig = false,
                    tidsstempelHosOss = tidspunkt,
                    bydel = null,
                    kommune = "5444",
                    skjermet = null,
                    strengtFortroligUtland = false,
                ),
            )
        }
    }
}
