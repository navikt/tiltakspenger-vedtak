@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.tiltakspenger.vedtak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakDTO.Companion.mapArenatiltak
import no.nav.tiltakspenger.vedtak.rivers.BarnetilleggDTO.Companion.mapBarnetillegg
import no.nav.tiltakspenger.vedtak.rivers.BrukerregistrertTiltakDTO.Companion.mapBrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.rivers.TrygdOgPensjonDTO.Companion.mapTrygdOgPensjon

class SøknadDTO(
    val søknadId: String,
    val journalpostId: String,
    val dokumentInfoId: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    val opprettet: LocalDateTime?,
    val barnetillegg: List<BarnetilleggDTO>,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerregistrertTiltak: BrukerregistrertTiltakDTO?,
    val trygdOgPensjon: List<TrygdOgPensjonDTO>?,
    val fritekst: String?,
) {
    companion object {
        internal fun mapSøknad(dto: SøknadDTO, innhentet: LocalDateTime): Søknad {
            return Søknad(
                søknadId = dto.søknadId,
                journalpostId = dto.journalpostId,
                dokumentInfoId = dto.dokumentInfoId,
                fornavn = dto.fornavn,
                etternavn = dto.etternavn,
                ident = dto.ident,
                deltarKvp = dto.deltarKvp,
                deltarIntroduksjonsprogrammet = dto.deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = dto.oppholdInstitusjon,
                typeInstitusjon = dto.typeInstitusjon,
                opprettet = dto.opprettet,
                barnetillegg = dto.barnetillegg.map { mapBarnetillegg(it) },
                innhentet = innhentet,
                arenaTiltak = mapArenatiltak(dto.arenaTiltak),
                brukerregistrertTiltak = mapBrukerregistrertTiltak(dto.brukerregistrertTiltak),
                trygdOgPensjon = dto.trygdOgPensjon?.map { mapTrygdOgPensjon(it) },
                fritekst = dto.fritekst
            )
        }
    }
}

class BrukerregistrertTiltakDTO(
    val tiltakskode: String?,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int
) {
    companion object {
        internal fun mapBrukerregistrertTiltak(dto: BrukerregistrertTiltakDTO?): BrukerregistrertTiltak? =
            if (dto == null) null
            else BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.mapTiltaksType(dto.tiltakskode!!), // TODO:test
                arrangoernavn = dto.arrangoernavn,
                beskrivelse = dto.beskrivelse,
                fom = dto.fom,
                tom = dto.tom,
                adresse = dto.adresse,
                postnummer = dto.postnummer,
                antallDager = dto.antallDager
            )

    }
}

class ArenaTiltakDTO(
    val arenaId: String? = null,
    val arrangoer: String? = null,
    val harSluttdatoFraArena: Boolean? = null,
    val tiltakskode: String? = null,
    val erIEndreStatus: Boolean? = null,
    val opprinneligSluttdato: LocalDate? = null,
    val opprinneligStartdato: LocalDate? = null,
    val sluttdato: LocalDate? = null,
    val startdato: LocalDate? = null
) {
    companion object {
        internal fun mapArenatiltak(dto: ArenaTiltakDTO?): ArenaTiltak? = if (dto == null) null
        else ArenaTiltak(
            arenaId = dto.arenaId,
            arrangoer = dto.arrangoer,
            harSluttdatoFraArena = dto.harSluttdatoFraArena,
            tiltakskode = Tiltaksaktivitet.Tiltaksnavn.valueOf(dto.tiltakskode!!),  // TODO test this
            erIEndreStatus = dto.erIEndreStatus,
            opprinneligSluttdato = dto.opprinneligSluttdato,
            opprinneligStartdato = dto.opprinneligStartdato,
            sluttdato = dto.sluttdato,
            startdato = dto.startdato
        )
    }
}

class TrygdOgPensjonDTO(
    val utbetaler: String, val prosent: Int? = null, val fom: LocalDate, val tom: LocalDate? = null
) {
    companion object {
        internal fun mapTrygdOgPensjon(dto: TrygdOgPensjonDTO): TrygdOgPensjon = TrygdOgPensjon(
            utbetaler = dto.utbetaler, prosent = dto.prosent, fom = dto.fom, tom = dto.tom
        )
    }
}

class BarnetilleggDTO(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val land: String,
) {
    companion object {
        internal fun mapBarnetillegg(dto: BarnetilleggDTO): Barnetillegg {
            return Barnetillegg(
                fornavn = dto.fornavn,
                etternavn = dto.etternavn,
                alder = dto.alder,
                ident = dto.ident,
                land = dto.land,
            )
        }
    }
}
