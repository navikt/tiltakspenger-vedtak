@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.tiltak.TiltakstypeSomGirRett
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.TiltakDeltakerstatus.Deltar
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde.Komet
import java.time.LocalDate
import java.time.LocalDateTime

interface PersonMother {
    /** Felles default fødselsdato for testdatatypene */
    fun fødselsdato(): LocalDate = 1.januar(2001)

    fun personopplysningKjedeligFyr(
        fnr: Fnr = Fnr.random(),
        fødselsdato: LocalDate = fødselsdato(),
        fornavn: String = "Fornavn",
        mellomnavn: String? = null,
        etternavn: String = "Etternavn",
        fortrolig: Boolean = false,
        strengtFortrolig: Boolean = false,
        strengtFortroligUtland: Boolean = false,
        kommune: String? = null,
        bydel: String? = null,
    ): PersonopplysningerSøker =
        PersonopplysningerSøker(
            fnr = fnr,
            fødselsdato = fødselsdato,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fortrolig = fortrolig,
            strengtFortrolig = strengtFortrolig,
            strengtFortroligUtland = strengtFortroligUtland,
            kommune = kommune,
            bydel = bydel,
        )

    fun personopplysningMaxFyr(
        fnr: Fnr = Fnr.random(),
        fødselsdato: LocalDate = fødselsdato(),
        fornavn: String = "Kjell",
        mellomnavn: String? = "T.",
        etternavn: String = "Ring",
        fortrolig: Boolean = false,
        strengtFortrolig: Boolean = true,
        strengtFortroligUtland: Boolean = false,
        kommune: String? = "Oslo",
        bydel: String? = "3440",
    ): PersonopplysningerSøker =
        PersonopplysningerSøker(
            fnr = fnr,
            fødselsdato = fødselsdato,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fortrolig = fortrolig,
            strengtFortrolig = strengtFortrolig,
            strengtFortroligUtland = strengtFortroligUtland,
            kommune = kommune,
            bydel = bydel,
        )

    fun tiltak(
        eksternId: String = "arenaId",
        tiltakstype: TiltakstypeSomGirRett = TiltakstypeSomGirRett.GRUPPE_AMO,
        gjennomføring: Tiltak.Gjennomføring =
            Tiltak.Gjennomføring(
                id = "123",
                arrangørnavn = "arrangør",
                typeNavn = "Arbeidsmarkedsopplæring (AMO)",
                typeKode = tiltakstype,
                rettPåTiltakspenger = true,
            ),
        deltakelseFom: LocalDate = 1.januar(2022),
        deltakelseTom: LocalDate = 31.januar(2022),
        kilde: Tiltakskilde = Komet,
        deltakelseProsent: Float? = 100F,
        status: TiltakDeltakerstatus = Deltar,
        antallDagerPerUke: Float? = 5F,
        registrertDato: LocalDateTime = 1.januarDateTime(2022),
        innhentet: LocalDateTime = 1.januarDateTime(2022),
    ): Tiltak =
        Tiltak(
            id = TiltakId.random(),
            eksternId = eksternId,
            gjennomføring = gjennomføring,
            deltakelsesperiode = Periode(deltakelseFom, deltakelseTom),
            deltakelseStatus = status,
            deltakelseProsent = deltakelseProsent,
            kilde = kilde,
            registrertDato = registrertDato,
            antallDagerPerUke = antallDagerPerUke,
            innhentetTidspunkt = innhentet,
        )
}
