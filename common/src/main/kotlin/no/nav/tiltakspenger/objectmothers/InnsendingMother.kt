@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.SøkerId
import no.nav.tiltakspenger.felles.TiltakId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.innsending.domene.Innsending
import no.nav.tiltakspenger.innsending.domene.Søker
import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyPersonopplysningHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySkjermingHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknad
import no.nav.tiltakspenger.objectmothers.ObjectMother.nySøknadMottattHendelse
import no.nav.tiltakspenger.objectmothers.ObjectMother.nyTiltakHendelse
import no.nav.tiltakspenger.saksbehandling.domene.behandling.Søknad
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDager
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.AntallDagerSaksopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak.Tiltak
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.Personopplysninger
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerBarnMedIdent
import no.nav.tiltakspenger.saksbehandling.domene.personopplysninger.PersonopplysningerSøker
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming
import no.nav.tiltakspenger.saksbehandling.domene.skjerming.Skjerming.SkjermingPerson
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

interface InnsendingMother {
    fun innsendingRegistrert(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.mars(2022),
    ): Innsending {
        return Innsending(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
        )
    }

    fun nySøker(
        søkerId: SøkerId = SøkerId.random(),
        ident: String = Random().nextInt().toString(),
        personopplysninger: PersonopplysningerSøker = personopplysningKjedeligFyr(ident = ident),
    ): Søker {
        return Søker.fromDb(
            søkerId = søkerId,
            ident = ident,
            personopplysninger = personopplysninger,
        )
    }

    fun innsendingMedSøknad(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.mars(2022),
        søknad: Søknad = nySøknad(
            periode = Periode(fom, tom),
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
            journalpostId = journalpostId,
        ),
    ): Innsending {
        val innsending = innsendingRegistrert(journalpostId, ident, fom, tom)
        val hendelse = nySøknadMottattHendelse(
            journalpostId = journalpostId,
            søknad = søknad,
        )
        innsending.håndter(hendelse)
        return innsending
    }

    fun innsendingMedPersonopplysninger(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.mars(2022),
        søknad: Søknad = nySøknad(
            periode = Periode(fom, tom),
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
            journalpostId = journalpostId,
        ),
        personopplysninger: List<Personopplysninger> = listOf(
            personopplysningKjedeligFyr(
                ident = ident,
                strengtFortroligUtland = false,
            ),
        ),
    ): Innsending {
        val innsending = innsendingMedSøknad(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
            søknad = søknad,
        )
        innsending.håndter(
            nyPersonopplysningHendelse(
                journalpostId = journalpostId,
                personopplysninger = personopplysninger,
            ),
        )
        return innsending
    }

    fun innsendingMedSkjerming(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.mars(2022),
        søknad: Søknad = nySøknad(
            periode = Periode(fom, tom),
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
            journalpostId = journalpostId,
        ),
        personopplysninger: List<Personopplysninger> = listOf(
            personopplysningKjedeligFyr(
                ident = ident,
                strengtFortroligUtland = false,
            ),
        ),
        skjerming: Skjerming = skjermingFalse(ident = ident),
    ): Innsending {
        val innsending = innsendingMedPersonopplysninger(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
            søknad = søknad,
            personopplysninger = personopplysninger,
        )
        innsending.håndter(
            nySkjermingHendelse(
                journalpostId = journalpostId,
                skjerming = skjerming,
            ),
        )
        return innsending
    }

    fun innsendingMedTiltak(
        journalpostId: String = Random().nextInt().toString(),
        ident: String = Random().nextInt().toString(),
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.mars(2022),
        søknad: Søknad = nySøknad(
            periode = Periode(fom, tom),
            personopplysninger = Søknad.Personopplysninger(
                ident = ident,
                fornavn = "Fornavn",
                etternavn = "Etternavn",
            ),
        ),
        personopplysninger: List<Personopplysninger> = listOf(
            personopplysningKjedeligFyr(
                ident = ident,
                strengtFortroligUtland = false,
            ),
        ),
        skjerming: Skjerming = skjermingFalse(ident = ident),
    ): Innsending {
        val innsending = innsendingMedSkjerming(
            journalpostId = journalpostId,
            ident = ident,
            fom = fom,
            tom = tom,
            søknad = søknad,
            personopplysninger = personopplysninger,
            skjerming = skjerming,
        )
        innsending.håndter(
            nyTiltakHendelse(
                journalpostId = journalpostId,
                tiltak = emptyList(),
                tidsstempelTiltakInnhentet = LocalDateTime.now(),
            ),
        )
        return innsending
    }

    fun personopplysningKjedeligFyr(
        ident: String = Random().nextInt().toString(),
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
        ident: String = Random().nextInt().toString(),
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
        ident: String = Random().nextInt().toString(),
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

    fun skjermingFalse(
        ident: String = Random().nextInt().toString(),
        barn: List<SkjermingPerson> = emptyList(),
    ): Skjerming {
        return Skjerming(
            søker = SkjermingPerson(
                ident = ident,
                skjerming = false,
            ),
            barn = barn,
            innhentet = 1.januarDateTime(2022),
        )
    }

    fun skjermingTrue(
        ident: String = Random().nextInt().toString(),
        barn: List<SkjermingPerson> = emptyList(),
    ): Skjerming {
        return Skjerming(
            søker = SkjermingPerson(
                ident = ident,
                skjerming = true,
            ),
            barn = barn,
            innhentet = 1.januarDateTime(2022),
        )
    }

    fun tiltak(
        eksternId: String = "123",
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
