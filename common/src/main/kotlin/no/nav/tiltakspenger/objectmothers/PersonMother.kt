@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.stønadsdager.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.søker.Søker
import no.nav.tiltakspenger.saksbehandling.domene.vilkår.tiltakdeltagelse.Tiltak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

interface PersonMother {

    companion object {
        private val random = Random()
    }

    fun nySøker(
        søkerId: SøkerId = SøkerId.random(),
        ident: String = random.nextInt().toString(),
        personopplysninger: PersonopplysningerSøker = personopplysningKjedeligFyr(ident = ident),
    ): Søker {
        return Søker.fromDb(
            søkerId = søkerId,
            ident = ident,
            personopplysninger = personopplysninger,
        )
    }

    fun personopplysningKjedeligFyr(
        ident: String = random.nextInt().toString(),
        fødselsdato: LocalDate = 1.januar(2001),
        fornavn: String = "Fornavn",
        mellomnavn: String? = null,
        etternavn: String = "Etternavn",
        fortrolig: Boolean = false,
        strengtFortrolig: Boolean = false,
        strengtFortroligUtland: Boolean = false,
        kommune: String? = null,
        bydel: String? = null,
        skjermet: Boolean? = null,
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
    ): PersonopplysningerSøker = PersonopplysningerSøker(
        ident = ident,
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
        ident: String = random.nextInt().toString(),
        fødselsdato: LocalDate = 1.januar(2001),
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
        ident = ident,
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
        ident: String = random.nextInt().toString(),
        fødselsdato: LocalDate = 1.januar(2001),
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
            ident = ident,
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
        kilde: String = "Komet",
        deltakelseProsent: Float? = 100F,
        deltakerStatus: Tiltak.DeltakerStatus = Tiltak.DeltakerStatus("DELTAR", true),
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
