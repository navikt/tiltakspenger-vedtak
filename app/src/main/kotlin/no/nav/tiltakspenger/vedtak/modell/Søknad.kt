package no.nav.tiltakspenger.vedtak.modell

import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("LongParameterList")
class SøknadDTO(
    val id: String,
    val fornavn: String?,
    val etternavn: String?,
    val ident: String,
    val deltarKvp: Boolean,
    val deltarIntroduksjonsprogrammet: Boolean?,
    val oppholdInstitusjon: Boolean?,
    val typeInstitusjon: String?,
    val tiltaksArrangoer: String?,
    val tiltaksType: String?,
    val opprettet: LocalDateTime?,
    val brukerRegistrertStartDato: LocalDate?,
    val brukerRegistrertSluttDato: LocalDate?,
    val systemRegistrertStartDato: LocalDate?,
    val systemRegistrertSluttDato: LocalDate?,
    val barnetillegg: List<BarnetilleggDTO>,
) {
    companion object {
        fun fromDto(søknadDTO: SøknadDTO, innhentet: LocalDateTime): Søknad {
            return Søknad(
                id = søknadDTO.id,
                fornavn = søknadDTO.fornavn,
                etternavn = søknadDTO.etternavn,
                ident = søknadDTO.ident,
                deltarKvp = søknadDTO.deltarKvp,
                deltarIntroduksjonsprogrammet = søknadDTO.deltarIntroduksjonsprogrammet,
                oppholdInstitusjon = søknadDTO.oppholdInstitusjon,
                typeInstitusjon = søknadDTO.typeInstitusjon,
                tiltaksArrangoer = søknadDTO.tiltaksArrangoer,
                tiltaksType = søknadDTO.tiltaksType,
                opprettet = søknadDTO.opprettet,
                brukerRegistrertStartDato = søknadDTO.brukerRegistrertStartDato,
                brukerRegistrertSluttDato = søknadDTO.brukerRegistrertSluttDato,
                systemRegistrertStartDato = søknadDTO.systemRegistrertStartDato,
                systemRegistrertSluttDato = søknadDTO.systemRegistrertSluttDato,
                barnetillegg = søknadDTO.barnetillegg.map { BarnetilleggDTO.fromDTO(it) },
                innhentet = innhentet,
            )
        }
    }
}

class BarnetilleggDTO(
    val fornavn: String?,
    val etternavn: String?,
    val alder: Int,
    val ident: String,
    val bosted: String
) {
    companion object {
        fun fromDTO(barnetilleggDTO: BarnetilleggDTO): Barnetillegg {
            return Barnetillegg(
                fornavn = barnetilleggDTO.fornavn,
                etternavn = barnetilleggDTO.etternavn,
                alder = barnetilleggDTO.alder,
                ident = barnetilleggDTO.ident,
                bosted = barnetilleggDTO.bosted,
            )
        }
    }
}
