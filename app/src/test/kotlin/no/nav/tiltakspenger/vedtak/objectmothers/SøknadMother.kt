package no.nav.tiltakspenger.vedtak.objectmothers

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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

fun nySøknad(
    id: UUID = UUID.randomUUID(),
    søknadId: String = "søknadId",
    journalpostId: String = "journalpostId",
    dokumentInfoId: String = "dokumentInfoId",
    fornavn: String? = "Fornavn",
    etternavn: String? = "Etternavn",
    ident: String = Random().nextInt().toString(),
    deltarKvp: Boolean = false,
    deltarIntroduksjonsprogrammet: Boolean? = false,
    oppholdInstitusjon: Boolean? = false,
    typeInstitusjon: String? = null,
    opprettet: LocalDateTime? = null,
    barnetillegg: List<Barnetillegg> = listOf(),
    tidsstempelHosOss: LocalDateTime = LocalDateTime.now(),
    tiltak: Tiltak = arenaTiltak(),
    trygdOgPensjon: List<TrygdOgPensjon> = emptyList(),
    fritekst: String? = "fritekst"
): Søknad {
    return Søknad(
        id = id,
        søknadId = søknadId,
        journalpostId = journalpostId,
        dokumentInfoId = dokumentInfoId,
        fornavn = fornavn,
        etternavn = etternavn,
        ident = ident,
        deltarKvp = deltarKvp,
        deltarIntroduksjonsprogrammet = deltarIntroduksjonsprogrammet,
        oppholdInstitusjon = oppholdInstitusjon,
        typeInstitusjon = typeInstitusjon,
        opprettet = opprettet,
        barnetillegg = barnetillegg,
        tidsstempelHosOss = tidsstempelHosOss,
        tiltak = tiltak,
        trygdOgPensjon = trygdOgPensjon,
        fritekst = fritekst,
    )
}
