@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.libs.common.Fnr
import no.nav.tiltakspenger.libs.common.random
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde
import no.nav.tiltakspenger.saksbehandling.domene.tiltak.Tiltakskilde.Komet
import java.time.LocalDate
import java.time.LocalDateTime

interface PersonMother {

    /** Felles default fødselsdato for testdatatypene */
    fun fødselsdato(): LocalDate = 1.januar(2001)

    fun nySøker(
        søkerId: SøkerId = SøkerId.random(),
        fnr: Fnr = Fnr.random(),
        personopplysninger: PersonopplysningerSøker = personopplysningKjedeligFyr(fnr = fnr),
    ): Søker {
        return Søker.fromDb(
            søkerId = søkerId,
            fnr = fnr,
            personopplysninger = personopplysninger,
        )
    }

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
        skjermet: Boolean? = false,
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
    ): PersonopplysningerSøker = PersonopplysningerSøker(
        fnr = fnr,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        strengtFortroligUtland = strengtFortroligUtland,
        skjermet = skjermet,
        kommune = kommune,
        bydel = bydel,
        tidsstempelHosOss = tidsstempelHosOss,
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
        skjermet: Boolean? = true,
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
    ): PersonopplysningerSøker = PersonopplysningerSøker(
        fnr = fnr,
        fødselsdato = fødselsdato,
        fornavn = fornavn,
        mellomnavn = mellomnavn,
        etternavn = etternavn,
        fortrolig = fortrolig,
        strengtFortrolig = strengtFortrolig,
        strengtFortroligUtland = strengtFortroligUtland,
        skjermet = skjermet,
        kommune = kommune,
        bydel = bydel,
        tidsstempelHosOss = tidsstempelHosOss,
    )

    fun barn(
        fnr: Fnr = Fnr.random(),
        fødselsdato: LocalDate = fødselsdato(),
        fornavn: String = "Fornavn",
        mellomnavn: String? = null,
        etternavn: String = "Etternavn",
        fortrolig: Boolean = false,
        strengtFortrolig: Boolean = false,
        strengtFortroligUtland: Boolean = false,
        skjermet: Boolean = false,
        oppholdsland: String? = null,
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
    ): PersonopplysningerBarnMedIdent {
        return PersonopplysningerBarnMedIdent(
            fnr = fnr,
            fødselsdato = fødselsdato,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fortrolig = fortrolig,
            strengtFortrolig = strengtFortrolig,
            strengtFortroligUtland = strengtFortroligUtland,
            skjermet = skjermet,
            oppholdsland = oppholdsland,
            tidsstempelHosOss = tidsstempelHosOss,
        )
    }

    fun tiltak(
        eksternId: String = "arenaId",
        gjennomføring: Tiltak.Gjennomføring = Tiltak.Gjennomføring(
            id = "123",
            arrangørnavn = "arrangør",
            typeNavn = "Arbeidsmarkedsopplæring (AMO)",
            typeKode = "AMO",
            rettPåTiltakspenger = true,
        ),
        deltakelseFom: LocalDate = 1.januar(2022),
        deltakelseTom: LocalDate = 31.januar(2022),
        kilde: Tiltakskilde = Komet,
        deltakelseProsent: Float? = 100F,
        deltakerStatus: String = "DELTAR",
        antallDagerPerUke: Float? = 1F,
        registrertDato: LocalDateTime = 1.januarDateTime(2022),
        innhentet: LocalDateTime = 1.januarDateTime(2022),
        antallDagerFraSaksbehandler: List<PeriodeMedVerdi<AntallDager>> = emptyList(),
    ): Tiltak {
        return Tiltak(
            id = TiltakId.random(),
            eksternId = eksternId,
            gjennomføring = gjennomføring,
            deltakelseFom = deltakelseFom,
            deltakelseTom = deltakelseTom,
            deltakelseStatus = deltakerStatus,
            deltakelseProsent = deltakelseProsent,
            kilde = kilde,
            registrertDato = registrertDato,
            innhentet = innhentet,
            antallDagerSaksopplysninger = AntallDagerSaksopplysninger(
                antallDagerSaksopplysningerFraSBH = emptyList(),
                antallDagerSaksopplysningerFraRegister = emptyList(),
                avklartAntallDager = emptyList(),
            ),
        )
    }
}
