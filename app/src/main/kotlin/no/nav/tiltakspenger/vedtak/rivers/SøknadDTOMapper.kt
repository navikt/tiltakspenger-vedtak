package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.Vedlegg
import java.time.LocalDate
import java.time.LocalDateTime

object SøknadDTOMapper {

    fun mapSøknad(dto: SøknadDTO, innhentet: LocalDateTime): Søknad {
        return Søknad(
            søknadId = dto.søknadId,
            journalpostId = dto.journalpostId,
            dokumentInfoId = dto.dokumentInfoId,
            personopplysninger = Søknad.Personopplysninger(
                fornavn = dto.personopplysninger.fornavn,
                etternavn = dto.personopplysninger.etternavn,
                ident = dto.personopplysninger.ident,
            ),
            kvp = try {
                if (dto.kvalifiseringsprogram.deltar) {
                    Søknad.PeriodeSpm.Ja(
                        periode = Periode(
                            fra = dto.kvalifiseringsprogram.periode!!.fra,
                            til = dto.kvalifiseringsprogram.periode.til,
                        ),
                    )
                } else {
                    Søknad.PeriodeSpm.Nei
                }
            } catch (e: NullPointerException) {
                Søknad.PeriodeSpm.FeilaktigBesvart(
                    svartJa = dto.kvalifiseringsprogram.deltar,
                    fom = dto.kvalifiseringsprogram.periode?.fra,
                    tom = dto.kvalifiseringsprogram.periode?.til,
                )
            },
            intro = try {
                if (dto.introduksjonsprogram.deltar) {
                    Søknad.PeriodeSpm.Ja(
                        periode = Periode(
                            fra = dto.introduksjonsprogram.periode!!.fra,
                            til = dto.introduksjonsprogram.periode.til,
                        ),
                    )
                } else {
                    Søknad.PeriodeSpm.Nei
                }
            } catch (e: NullPointerException) {
                Søknad.PeriodeSpm.FeilaktigBesvart(
                    svartJa = dto.introduksjonsprogram.deltar,
                    fom = dto.introduksjonsprogram.periode?.fra,
                    tom = dto.introduksjonsprogram.periode?.til,
                )
            },
            institusjon = mapInstitusjon(dto.oppholdInstitusjon, dto.typeInstitusjon),
            innsendt = dto.opprettet,
            barnetillegg = dto.barnetillegg.map { mapBarnetillegg(it) },
            tidsstempelHosOss = innhentet,
            tiltak = mapArenatiltak(dto.arenaTiltak)
                ?: BrukerregistrertTiltakDTO.mapBrukerregistrertTiltak(dto.brukerregistrertTiltak),
            trygdOgPensjon = mapTrygdOgPensjon(dto.trygdOgPensjon),
            vedlegg = dto.vedlegg?.map { mapVedlegg(it) } ?: emptyList(),
            etterlønn = Søknad.JaNeiSpm.IkkeMedISøknaden,
            gjenlevendepensjon = Søknad.FraOgMedDatoSpm.IkkeMedISøknaden,
            alderspensjon = Søknad.FraOgMedDatoSpm.IkkeMedISøknaden,
            sykepenger = Søknad.PeriodeSpm.IkkeMedISøknaden,
            supplerendeStønadAlder = Søknad.PeriodeSpm.IkkeMedISøknaden,
            supplerendeStønadFlyktning = Søknad.PeriodeSpm.IkkeMedISøknaden,
            jobbsjansen = Søknad.PeriodeSpm.IkkeMedISøknaden,
        )
    }

    private fun mapTrygdOgPensjon(trygdOgPensjon: List<TrygdOgPensjonDTO>?): Søknad.FraOgMedDatoSpm {
        // if(dto.trygdOgPensjon.isNullOrEmpty()) { Søknad.FraOgMedDatoSpm.Nei } else { Søknad.FraOgMedDatoSpm.Ja(fra = ) }
        // TODO
        return Søknad.FraOgMedDatoSpm.Nei
    }

    private fun mapInstitusjon(oppholdInstitusjon: Boolean, typeInstitusjon: String?): Søknad.PeriodeSpm {
        // TODO
        return Søknad.PeriodeSpm.Nei
    }

    fun mapArenatiltak(dto: ArenaTiltakDTO?): Tiltak.ArenaTiltak? = if (dto == null) {
        null
    } else {
        Tiltak.ArenaTiltak(
            arenaId = dto.arenaId,
            arrangoernavn = dto.arrangoer,
            tiltakskode = Tiltaksaktivitet.Tiltak.valueOf(dto.tiltakskode.uppercase()), // TODO test this
            opprinneligSluttdato = dto.opprinneligSluttdato,
            opprinneligStartdato = dto.opprinneligStartdato,
            sluttdato = dto.sluttdato,
            startdato = dto.startdato,
        )
    }

    fun mapVedlegg(dto: VedleggDTO): Vedlegg {
        return Vedlegg(
            journalpostId = dto.journalpostId,
            dokumentInfoId = dto.dokumentInfoId,
            filnavn = dto.filnavn,
        )
    }

    internal fun mapBarnetillegg(dto: BarnetilleggDTO): Barnetillegg {
        return if (dto.ident != null) {
            Barnetillegg.FraPdl(
                oppholderSegIEØS = Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = dto.fornavn ?: "---",
                mellomnavn = dto.mellomnavn,
                etternavn = dto.etternavn ?: "---",
                fødselsdato = toFødselsdato(dto.ident),
            )
        } else {
            Barnetillegg.Manuell(
                oppholderSegIEØS = Søknad.JaNeiSpm.IkkeMedISøknaden,
                fornavn = dto.fornavn ?: "---",
                mellomnavn = dto.mellomnavn,
                etternavn = dto.etternavn ?: "---",
                fødselsdato = dto.fødselsdato!!,
            )
        }
    }

    private fun toFødselsdato(ident: String): LocalDate {
        return LocalDate.now() // TODO
    }
}
