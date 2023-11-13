package no.nav.tiltakspenger.objectmothers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.felles.SøknadId
import no.nav.tiltakspenger.felles.januar
import no.nav.tiltakspenger.felles.januarDateTime
import no.nav.tiltakspenger.felles.juni
import no.nav.tiltakspenger.felles.mars
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Søknad.Personopplysninger
import no.nav.tiltakspenger.vedtak.SøknadsTiltak
import no.nav.tiltakspenger.vedtak.Vedlegg
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Random

interface SøknadMother {
    fun arenaTiltak(
        arenaId: String = "arenaId",
        arrangoernavn: String? = "arrangørnavn",
        tiltakskode: String = "JOBBK",
        opprinneligStartdato: LocalDate = 1.januar(2022),
        opprinneligSluttdato: LocalDate? = 31.januar(2022),
        startdato: LocalDate = 1.januar(2022),
        sluttdato: LocalDate? = 31.januar(2022),
    ): SøknadsTiltak.ArenaTiltak {
        return SøknadsTiltak.ArenaTiltak(
            arenaId = arenaId,
            arrangoernavn = arrangoernavn,
            tiltakskode = tiltakskode,
            opprinneligStartdato = opprinneligStartdato,
            opprinneligSluttdato = opprinneligSluttdato,
            startdato = startdato,
            sluttdato = sluttdato,
        )
    }

    fun barnetilleggMedIdent(
        oppholderSegIEØS: Søknad.JaNeiSpm = Søknad.JaNeiSpm.Ja,
        fornavn: String = "Fornavn Barn",
        mellomnavn: String? = "Mellomnavn Barn",
        etternavn: String = "Etternavn Barn",
        fødselsdato: LocalDate = 14.juni(2012),
        søktBarnetillegg: Boolean = true,
    ): Barnetillegg {
        return Barnetillegg.FraPdl(
            oppholderSegIEØS = oppholderSegIEØS,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fødselsdato = fødselsdato,
        )
    }

    fun barnetilleggUtenIdent(
        oppholderSegIEØS: Søknad.JaNeiSpm = Søknad.JaNeiSpm.Ja,
        fornavn: String = "Fornavn Barn",
        mellomnavn: String? = "Mellomnavn Barn",
        etternavn: String = "Etternavn Barn",
        fødselsdato: LocalDate = 14.juni(2012),
    ): Barnetillegg {
        return Barnetillegg.Manuell(
            oppholderSegIEØS = oppholderSegIEØS,
            fornavn = fornavn,
            mellomnavn = mellomnavn,
            etternavn = etternavn,
            fødselsdato = fødselsdato,
        )
    }

    fun nySøknadMedTiltak(
        periode: Periode = Periode(1.januar(2023), 31.mars(2023)),
        versjon: String = "1",
        id: SøknadId = Søknad.randomId(),
        søknadId: String = "søknadId",
        journalpostId: String = "journalpostId",
        dokumentInfoId: String = "dokumentInfoId",
        filnavn: String = "filnavn",
        personopplysninger: Personopplysninger = personSøknad(),
        kvp: Søknad.PeriodeSpm = periodeNei(),
        intro: Søknad.PeriodeSpm = periodeNei(),
        institusjon: Søknad.PeriodeSpm = periodeNei(),
        opprettet: LocalDateTime = 1.januarDateTime(2022),
        barnetillegg: List<Barnetillegg> = listOf(),
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
        tiltak: SøknadsTiltak = arenaTiltak(startdato = periode.fra, sluttdato = periode.til),
        trygdOgPensjon: Søknad.PeriodeSpm = periodeNei(),
        vedlegg: List<Vedlegg> = emptyList(),
        etterlønn: Søknad.JaNeiSpm = nei(),
        gjenlevendepensjon: Søknad.PeriodeSpm = periodeNei(),
        alderspensjon: Søknad.FraOgMedDatoSpm = fraOgMedDatoNei(),
        sykepenger: Søknad.PeriodeSpm = periodeNei(),
        supplerendeStønadAlder: Søknad.PeriodeSpm = periodeNei(),
        supplerendeStønadFlyktning: Søknad.PeriodeSpm = periodeNei(),
        jobbsjansen: Søknad.PeriodeSpm = periodeNei(),
        lønnetArbeid: Søknad.JaNeiSpm = nei(),
    ): Søknad {
        return Søknad(
            versjon = versjon,
            id = id,
            søknadId = søknadId,
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = filnavn,
            personopplysninger = personopplysninger,
            tiltak = tiltak,
            barnetillegg = barnetillegg,
            opprettet = opprettet,
            tidsstempelHosOss = tidsstempelHosOss,
            vedlegg = vedlegg,
            kvp = kvp,
            intro = intro,
            institusjon = institusjon,
            etterlønn = etterlønn,
            gjenlevendepensjon = gjenlevendepensjon,
            alderspensjon = alderspensjon,
            sykepenger = sykepenger,
            supplerendeStønadAlder = supplerendeStønadAlder,
            supplerendeStønadFlyktning = supplerendeStønadFlyktning,
            jobbsjansen = jobbsjansen,
            trygdOgPensjon = trygdOgPensjon,
            lønnetArbeid = lønnetArbeid,
        )
    }

    fun nySøknadMedBrukerTiltak(
        id: SøknadId = Søknad.randomId(),
        søknadId: String = Random().nextInt().toString(),
        journalpostId: String = "journalpostId",
        dokumentInfoId: String = "dokumentInfoId",
        filnavn: String = "filnavn",
        personopplysninger: Personopplysninger = personSøknad(),
        kvp: Søknad.PeriodeSpm = periodeNei(),
        intro: Søknad.PeriodeSpm = periodeNei(),
        oppholdInstitusjon: Søknad.PeriodeSpm = periodeNei(),
        opprettet: LocalDateTime = 1.januarDateTime(2022),
        barnetillegg: List<Barnetillegg> = listOf(),
        tidsstempelHosOss: LocalDateTime = 1.januarDateTime(2022),
        tiltak: SøknadsTiltak = brukerTiltak(),
        trygdOgPensjon: Søknad.PeriodeSpm = periodeNei(),
        fritekst: String? = "fritekst",
        vedlegg: List<Vedlegg> = emptyList(),
        etterlønn: Søknad.JaNeiSpm = nei(),
        gjenlevendepensjon: Søknad.PeriodeSpm = periodeNei(),
        alderspensjon: Søknad.FraOgMedDatoSpm = fraOgMedDatoNei(),
        sykepenger: Søknad.PeriodeSpm = periodeNei(),
        supplerendeStønadAlder: Søknad.PeriodeSpm = periodeNei(),
        supplerendeStønadFlyktning: Søknad.PeriodeSpm = periodeNei(),
        jobbsjansen: Søknad.PeriodeSpm = periodeNei(),
        lønnetArbeid: Søknad.JaNeiSpm = nei(),
    ): Søknad {
        return Søknad(
            versjon = "1",
            id = id,
            søknadId = søknadId,
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            filnavn = filnavn,
            personopplysninger = personopplysninger,
            tiltak = tiltak,
            barnetillegg = barnetillegg,
            opprettet = opprettet,
            tidsstempelHosOss = tidsstempelHosOss,
            vedlegg = vedlegg,
            kvp = kvp,
            intro = intro,
            institusjon = oppholdInstitusjon,
            etterlønn = etterlønn,
            gjenlevendepensjon = gjenlevendepensjon,
            alderspensjon = alderspensjon,
            sykepenger = sykepenger,
            supplerendeStønadAlder = supplerendeStønadAlder,
            supplerendeStønadFlyktning = supplerendeStønadFlyktning,
            jobbsjansen = jobbsjansen,
            trygdOgPensjon = trygdOgPensjon,
            lønnetArbeid = lønnetArbeid,
        )
    }

    fun brukerTiltak(
        tiltakskode: String? = "JOBBK",
        arrangoernavn: String? = "arrangørnavn",
        beskrivelse: String? = "beskrivelse",
        startdato: LocalDate = 1.januar(2022),
        sluttdato: LocalDate = 31.januar(2022),
        adresse: String? = "adresse",
        postnummer: String? = "1234",
        antallDager: Int = 30,
    ): SøknadsTiltak.BrukerregistrertTiltak {
        return SøknadsTiltak.BrukerregistrertTiltak(
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

    fun personSøknad(
        fornavn: String = "Fornavn",
        etternavn: String = "Etternavn",
        ident: String = Random().nextInt().toString(),
    ) = Personopplysninger(
        fornavn = fornavn,
        etternavn = etternavn,
        ident = ident,
    )

    fun nei() = Søknad.JaNeiSpm.Nei
    fun fraOgMedDatoNei() = Søknad.FraOgMedDatoSpm.Nei
    fun periodeNei() = Søknad.PeriodeSpm.Nei
    fun ja() = Søknad.JaNeiSpm.Ja
    fun fraOgMedDatoJa(
        fom: LocalDate = 1.januar(2022),
    ) = Søknad.FraOgMedDatoSpm.Ja(
        fra = fom,
    )

    fun periodeJa(
        fom: LocalDate = 1.januar(2022),
        tom: LocalDate = 31.januar(2022),
    ) = Søknad.PeriodeSpm.Ja(
        periode = Periode(
            fra = fom,
            til = tom,
        ),
    )
}
