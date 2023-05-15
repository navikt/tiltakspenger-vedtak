@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.juni
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Søknad.Personopplysninger
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.TypeInstitusjon
import no.nav.tiltakspenger.vedtak.Vedlegg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

interface SøknadMother {
    fun arenaTiltak(
        arenaId: String = "arenaId",
        arrangoernavn: String? = "arrangørnavn",
        harSluttdatoFraArena: Boolean = false,
        tiltakskode: Tiltaksaktivitet.Tiltak = Tiltaksaktivitet.Tiltak.JOBBK,
        erIEndreStatus: Boolean = false,
        opprinneligStartdato: LocalDate = 1.januar(2022),
        opprinneligSluttdato: LocalDate? = 31.januar(2022),
        startdato: LocalDate = 1.januar(2022),
        sluttdato: LocalDate? = 31.januar(2022),
    ): Tiltak.ArenaTiltak {
        return Tiltak.ArenaTiltak(
            arenaId = arenaId,
            arrangoernavn = arrangoernavn,
            harSluttdatoFraArena = harSluttdatoFraArena,
            tiltakskode = tiltakskode,
            erIEndreStatus = erIEndreStatus,
            opprinneligStartdato = opprinneligStartdato,
            opprinneligSluttdato = opprinneligSluttdato,
            startdato = startdato,
            sluttdato = sluttdato,
        )
    }

    fun brukerTiltak(
        tiltakskode: Tiltaksaktivitet.Tiltak? = Tiltaksaktivitet.Tiltak.JOBBK,
        arrangoernavn: String? = "arrangørnavn",
        beskrivelse: String? = "beskrivelse",
        startdato: LocalDate = 1.januar(2022),
        sluttdato: LocalDate = 31.januar(2022),
        adresse: String? = "adresse",
        postnummer: String? = "1234",
        antallDager: Int = 30,
    ): Tiltak.BrukerregistrertTiltak {
        return Tiltak.BrukerregistrertTiltak(
            tiltakskode = tiltakskode,
            arrangoernavn = arrangoernavn,
            beskrivelse = beskrivelse,
            startdato = startdato,
            sluttdato = sluttdato,
            adresse = adresse,
            postnummer = postnummer,
            antallDager = antallDager,
        )
    }

    fun trygdOgPensjon(
        utbetaler: String = "Utbetaler",
        prosent: Int? = 0,
        fom: LocalDate? = 1.januar(2022),
        tom: LocalDate? = 31.januar(2022),
    ): TrygdOgPensjon {
        return TrygdOgPensjon(
            utbetaler = utbetaler,
            prosent = prosent,
            fom = fom,
            tom = tom,
        )
    }

    fun barnetilleggMedIdent(
        alder: Int = 10,
        oppholdsland: String = "NOR",
        fornavn: String? = "Fornavn Barn",
        mellomnavn: String? = "Mellomnavn Barn",
        etternavn: String? = "Etternavn Barn",
        ident: String = Random().nextInt().toString(),
        søktBarnetillegg: Boolean = true,
    ): Barnetillegg {
        return Barnetillegg.MedIdent(
            alder = alder,
            oppholdsland = oppholdsland,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            ident = ident,
            søktBarnetillegg = søktBarnetillegg,
        )
    }

    fun barnetilleggUtenIdent(
        alder: Int = 10,
        oppholdsland: String = "NOR",
        fornavn: String? = "Fornavn Barn",
        mellomnavn: String? = "Mellomnavn Barn",
        etternavn: String? = "Etternavn Barn",
        fødselsdato: LocalDate = 14.juni(2012),
        søktBarnetillegg: Boolean = true,
    ): Barnetillegg {
        return Barnetillegg.UtenIdent(
            alder = alder,
            oppholdsland = oppholdsland,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fødselsdato = fødselsdato,
            søktBarnetillegg = søktBarnetillegg,
        )
    }

    fun nySøknadMedArenaTiltak(
        id: SøknadId = Søknad.randomId(),
        søknadId: String = "søknadId",
        journalpostId: String = "journalpostId",
        dokumentInfoId: String = "dokumentInfoId",
        personopplysninger: Personopplysninger = personSøknad(),
        kvp: Søknad.Kvp = kvpNei(),
        intro: Søknad.Intro = introNei(),
        oppholdInstitusjon: Boolean? = false,
        typeInstitusjon: TypeInstitusjon? = null,
        opprettet: LocalDateTime? = 1.januarDateTime(2022),
        barnetillegg: List<Barnetillegg> = listOf(),
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
        tiltak: Tiltak = arenaTiltak(),
        trygdOgPensjon: List<TrygdOgPensjon> = emptyList(),
        fritekst: String? = "fritekst",
        vedlegg: List<Vedlegg> = emptyList(),
    ): Søknad {
        return Søknad(
            id = id,
            søknadId = søknadId,
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            personopplysninger = personopplysninger,
            kvp = kvp,
            intro = intro,
            oppholdInstitusjon = oppholdInstitusjon,
            typeInstitusjon = typeInstitusjon,
            opprettet = opprettet,
            barnetillegg = barnetillegg,
            tidsstempelHosOss = tidsstempelHosOss,
            tiltak = tiltak,
            trygdOgPensjon = trygdOgPensjon,
            fritekst = fritekst,
            vedlegg = vedlegg,
        )
    }

    fun nySøknadMedBrukerTiltak(
        id: SøknadId = Søknad.randomId(),
        søknadId: String = Random().nextInt().toString(),
        journalpostId: String = "journalpostId",
        dokumentInfoId: String = "dokumentInfoId",
        personopplysninger: Personopplysninger = personSøknad(),
        kvp: Søknad.Kvp = kvpNei(),
        intro: Søknad.Intro = introNei(),
        oppholdInstitusjon: Boolean? = false,
        typeInstitusjon: TypeInstitusjon? = null,
        opprettet: LocalDateTime? = 1.januarDateTime(2022),
        barnetillegg: List<Barnetillegg> = listOf(),
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
        tiltak: Tiltak = brukerTiltak(),
        trygdOgPensjon: List<TrygdOgPensjon> = emptyList(),
        fritekst: String? = "fritekst",
        vedlegg: List<Vedlegg> = emptyList(),
    ): Søknad {
        return Søknad(
            id = id,
            søknadId = søknadId,
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            personopplysninger = personopplysninger,
            kvp = kvp,
            intro = intro,
            oppholdInstitusjon = oppholdInstitusjon,
            typeInstitusjon = typeInstitusjon,
            opprettet = opprettet,
            barnetillegg = barnetillegg,
            tidsstempelHosOss = tidsstempelHosOss,
            tiltak = tiltak,
            trygdOgPensjon = trygdOgPensjon,
            fritekst = fritekst,
            vedlegg = vedlegg,
        )
    }

    fun personSøknad(
        fornavn: String = "Fornavn",
        etternavn: String = "Etternavn",
        ident: String = Random().nextInt().toString(),
    ) = Personopplysninger(
        fornavn = fornavn,
        etternavn = etternavn,
        ident = ident,
    )

    fun kvpNei() = Søknad.Kvp(
        deltar = false,
        periode = null,
    )

    fun kvpJa(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 31.januar(2022),
    ) = Søknad.Kvp(
        deltar = false,
        periode = Periode(
            fra = fra,
            til = til,
        ),
    )

    fun introNei() = Søknad.Intro(
        deltar = false,
        periode = null,
    )

    fun introJa(
        fra: LocalDate = 1.januar(2022),
        til: LocalDate = 31.januar(2022),
    ) = Søknad.Intro(
        deltar = false,
        periode = Periode(
            fra = fra,
            til = til,
        ),
    )
}
