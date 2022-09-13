@file:Suppress("LongParameterList")

package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.vedtak.ArenaTiltak
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.BrukerregistrertTiltak
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.TrygdOgPensjon
import no.nav.tiltakspenger.vedtak.rivers.ArenaTiltakDTO.Companion.mapArenatiltak
import no.nav.tiltakspenger.vedtak.rivers.BarnetilleggDTO.Companion.mapBarnetillegg
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
    val barnetillegg: List<BarnetilleggDTO>,
    val arenaTiltak: ArenaTiltakDTO?,
    val brukerregistrertTiltak: BrukerregistrertTiltakDTO?,
    val trygdOgPensjon: List<TrygdOgPensjonDTO>?,
    val fritekst: String?,
) {
    companion object {
        internal fun mapSøknad(søknadDTO: SøknadDTO, innhentet: LocalDateTime): Søknad {
            return Søknad(
                søknadId = søknadDTO.søknadId,
                journalpostId = søknadDTO.journalpostId,
                dokumentInfoId = søknadDTO.dokumentInfoId,
                fornavn = søknadDTO.fornavn,
                etternavn = søknadDTO.etternavn,
                ident = søknadDTO.ident,
                deltarKvp = søknadDTO.deltarKvp,
                deltarIntroduksjonsprogrammet = søknadDTO.deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = søknadDTO.oppholdInstitusjon,
                typeInstitusjon = søknadDTO.typeInstitusjon,
                opprettet = søknadDTO.opprettet,
                barnetillegg = søknadDTO.barnetillegg.map { mapBarnetillegg(it) },
                innhentet = innhentet,
                arenaTiltak = mapArenatiltak(søknadDTO.arenaTiltak),
                brukerregistrertTiltak = mapBrukerregistrertTiltak(søknadDTO.brukerregistrertTiltak),
                trygdOgPensjon = søknadDTO.trygdOgPensjon?.map { mapTrygdOgPensjon(it) },
                fritekst = søknadDTO.fritekst
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
        internal fun mapBrukerregistrertTiltak(brukerregistrertTiltak: BrukerregistrertTiltakDTO?): BrukerregistrertTiltak? {
            TODO("Not yet implemented")
        }

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
        internal fun mapArenatiltak(arenaTiltak: ArenaTiltakDTO?): ArenaTiltak? {
            TODO("Not yet implemented")
        }
    }
}

class TrygdOgPensjonDTO(
    val utbetaler: String,
    val prosent: Int? = null,
    val fom: LocalDate,
    val tom: LocalDate? = null
) {
    companion object {
        internal fun mapTrygdOgPensjon(trygdOgPensjon: TrygdOgPensjonDTO): TrygdOgPensjon {
            TODO("Not yet implemented")
        }
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
        internal fun mapBarnetillegg(barnetilleggDTO: BarnetilleggDTO): Barnetillegg {
            return Barnetillegg(
                fornavn = barnetilleggDTO.fornavn,
                etternavn = barnetilleggDTO.etternavn,
                alder = barnetilleggDTO.alder,
                ident = barnetilleggDTO.ident,
                land = barnetilleggDTO.land,
            )
        }
    }
}
