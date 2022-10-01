@file:Suppress("LongParameterList")
package no.nav.tiltakspenger.vedtak.testcommon

import no.nav.tiltakspenger.domene.januar
import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søker
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.meldinger.SøknadMottattHendelse
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

fun søkerRegistrert(
    ident: String = Random().nextInt().toString(),
) : Søker {
    return Søker(
        ident = ident,
    )
}

fun søkerMedSøknad(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknad(ident = ident),
): Søker {
    val søker = søkerRegistrert(ident)
    val hendelse = nySøknadMottattHendelse(
        ident = ident,
        søknad = søknad,
    )
    søker.håndter(hendelse)
    return søker
}

fun nySøknadMottattHendelse(
    ident: String = Random().nextInt().toString(),
    søknad: Søknad = nySøknad(),
    aktivitetslogg: Aktivitetslogg = Aktivitetslogg(forelder = null),
): SøknadMottattHendelse {
    return SøknadMottattHendelse(
        aktivitetslogg = aktivitetslogg,
        ident = ident,
        søknad = søknad,
    )
}
fun arenaTiltak(): Tiltak.ArenaTiltak {
    return Tiltak.ArenaTiltak(
        arenaId = "arenaId",
        arrangoernavn = null,
        harSluttdatoFraArena = false,
        tiltakskode = Tiltaksaktivitet.Tiltak.JOBBK,
        erIEndreStatus = false,
        opprinneligSluttdato = null,
        opprinneligStartdato = 1.januar(2022),
        sluttdato = null,
        startdato = 1.januar(2022),
    )
}

fun trygdOgPensjon(
    utbetaler: String = "Utbetaler",
    prosent: Int? = 0,
    fom: LocalDate? = 1.januar(2022),
    tom: LocalDate? = 31.januar(2022),
) : TrygdOgPensjon {
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
    fornavn: String? = "fornavn",
    etternavn: String? = "etternavn",
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
) : Søknad {
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
