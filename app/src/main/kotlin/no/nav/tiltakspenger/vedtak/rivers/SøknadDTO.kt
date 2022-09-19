@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakDTO.Companion.mapArenatiltak
import no.nav.tiltakspenger.vedtak.rivers.BarnetilleggMedIdentDTO.Companion.mapBarnetilleggMedIdent
import no.nav.tiltakspenger.vedtak.rivers.BrukerregistrertTiltakDTO.Companion.mapBrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.rivers.TrygdOgPensjonDTO.Companion.mapTrygdOgPensjon
import java.time.LocalDate
import java.time.LocalDateTime

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
    val barnetillegg: List<BarnetilleggMedIdentDTO>,
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
                barnetillegg = dto.barnetillegg.map { mapBarnetilleggMedIdent(it) },
                tidsstempelHosOss = innhentet,
                tiltak = mapArenatiltak(dto.arenaTiltak) ?: mapBrukerregistrertTiltak(dto.brukerregistrertTiltak)!!,
                trygdOgPensjon = dto.trygdOgPensjon?.map { mapTrygdOgPensjon(it) } ?: emptyList(),
                fritekst = dto.fritekst
            )
        }
    }
}

class BrukerregistrertTiltakDTO(
    val tiltakstype: String?,
    val arrangoernavn: String?,
    val beskrivelse: String?,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val adresse: String? = null,
    val postnummer: String? = null,
    val antallDager: Int
) {
    companion object {
        internal fun mapBrukerregistrertTiltak(dto: BrukerregistrertTiltakDTO?): Tiltak.BrukerregistrertTiltak? =
            if (dto == null) null
            else Tiltak.BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.mapTiltaksType(dto.tiltakstype!!), // TODO:test
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
        internal fun mapArenatiltak(dto: ArenaTiltakDTO?): Tiltak.ArenaTiltak? = if (dto == null) null
        else Tiltak.ArenaTiltak(
            arenaId = dto.arenaId,
            arrangoer = dto.arrangoer,
            harSluttdatoFraArena = dto.harSluttdatoFraArena,
            tiltakskode = Tiltaksaktivitet.Tiltak.valueOf(dto.tiltakskode!!.uppercase()),  // TODO test this
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

class BarnetilleggMedIdentDTO(val alder: Int, val land: String, val ident: String) {
    companion object {
        internal fun mapBarnetilleggMedIdent(dto: BarnetilleggMedIdentDTO): Barnetillegg.MedIdent {
            return Barnetillegg.MedIdent(
                alder = dto.alder,
                land = dto.land,
                ident = dto.ident
            )
        }
    }
}

class BarnetilleggUtenIdentDTO(val alder: Int, val land: String, val fødselsdato: LocalDate) {
    companion object {
        internal fun mapBarnetilleggMedIdent(dto: BarnetilleggUtenIdentDTO): Barnetillegg.UtenIdent {
            return Barnetillegg.UtenIdent(
                alder = dto.alder,
                land = dto.land,
                fødselsdato = dto.fødselsdato
            )
        }
    }
}
