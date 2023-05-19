package no.nav.tiltakspenger.vedtak.rivers

import no.nav.tiltakspenger.felles.Periode
import no.nav.tiltakspenger.vedtak.Barnetillegg
import no.nav.tiltakspenger.vedtak.Søknad
import no.nav.tiltakspenger.vedtak.Tiltak
import no.nav.tiltakspenger.vedtak.Tiltaksaktivitet
import no.nav.tiltakspenger.vedtak.Vedlegg
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object SøknadDTOMapper {

    fun mapSøknad(dto: SøknadDTO, innhentet: LocalDateTime): Søknad {
        return Søknad(
            søknadId = dto.søknadId,
            journalpostId = dto.journalpostId,
            dokumentInfoId = dto.dokumentInfoId,
            personopplysninger = Søknad.Personopplysninger(
                fornavn = dto.fornavn!!,
                etternavn = dto.etternavn!!,
                ident = dto.ident,
            ),
            kvp =
            if (dto.deltarKvp) {
                Søknad.PeriodeSpm.Ja(
                    periode = Periode(
                        fra = LocalDate.MIN,
                        til = LocalDate.MAX,
                    ),
                )
            } else {
                Søknad.PeriodeSpm.Nei
            },
            intro = try {
                if (dto.deltarIntroduksjonsprogrammet == null) {
                    Søknad.PeriodeSpm.IkkeBesvart
                } else if (dto.deltarIntroduksjonsprogrammet) {
                    Søknad.PeriodeSpm.Ja(
                        periode = Periode(
                            fra = dto.introduksjonsprogrammetDetaljer!!.fom,
                            til = dto.introduksjonsprogrammetDetaljer.tom!!,
                        ),
                    )
                } else {
                    Søknad.PeriodeSpm.Nei
                }
            } catch (e: NullPointerException) {
                Søknad.PeriodeSpm.FeilaktigBesvart(
                    svartJa = dto.deltarIntroduksjonsprogrammet,
                    fom = dto.introduksjonsprogrammetDetaljer?.fom,
                    tom = dto.introduksjonsprogrammetDetaljer?.tom,
                )
            },
            institusjon = mapInstitusjon(dto.oppholdInstitusjon, dto.typeInstitusjon),
            opprettet = dto.opprettet,
            barnetillegg = dto.barnetillegg.map { mapBarnetillegg(it) },
            tidsstempelHosOss = innhentet,
            tiltak = mapArenatiltak(dto.arenaTiltak)
                ?: mapBrukerregistrertTiltak(dto.brukerregistrertTiltak),
            trygdOgPensjon = Søknad.FraOgMedDatoSpm.IkkeMedISøknaden,
            vedlegg = dto.vedlegg?.map { mapVedlegg(it) } ?: emptyList(),
            etterlønn = mapTrygdOgPensjon(dto.trygdOgPensjon),
            gjenlevendepensjon = Søknad.FraOgMedDatoSpm.IkkeMedISøknaden,
            alderspensjon = Søknad.FraOgMedDatoSpm.IkkeMedISøknaden,
            sykepenger = Søknad.PeriodeSpm.IkkeMedISøknaden,
            supplerendeStønadAlder = Søknad.PeriodeSpm.IkkeMedISøknaden,
            supplerendeStønadFlyktning = Søknad.PeriodeSpm.IkkeMedISøknaden,
            jobbsjansen = Søknad.PeriodeSpm.IkkeMedISøknaden,
        )
    }

    private fun mapTrygdOgPensjon(trygdOgPensjon: List<TrygdOgPensjonDTO>?): Søknad.JaNeiSpm {
        return if (trygdOgPensjon.isNullOrEmpty()) {
            Søknad.JaNeiSpm.Nei
        } else {
            Søknad.JaNeiSpm.Ja
        }
    }

    private fun mapInstitusjon(oppholdInstitusjon: Boolean, typeInstitusjon: String?): Søknad.PeriodeSpm {
        return if (!oppholdInstitusjon) {
            Søknad.PeriodeSpm.Nei
        } else {
            if (typeInstitusjon.equals("Barneverninstitusjon", ignoreCase = true)) {
                // TODO: Hvor henter vi datoene fra?
                Søknad.PeriodeSpm.Ja(periode = Periode(LocalDate.MIN, LocalDate.MAX))
            } else {
                Søknad.PeriodeSpm.IkkeBesvart // TODO Ok?
            }
        }
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

    fun mapBrukerregistrertTiltak(dto: BrukerregistrertTiltakDTO?): Tiltak.BrukerregistrertTiltak? =
        if (dto == null) {
            null
        } else {
            Tiltak.BrukerregistrertTiltak(
                tiltakskode = Tiltaksaktivitet.mapTiltaksType(dto.tiltakskode), // TODO:test
                arrangoernavn = dto.arrangoernavn,
                beskrivelse = dto.beskrivelse,
                startdato = dto.fom,
                sluttdato = dto.tom,
                adresse = dto.adresse,
                postnummer = dto.postnummer,
                antallDager = dto.antallDager,
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

    private fun toFødselsdato(ident: String): LocalDate =
        LocalDate.parse(ident.subSequence(0, 6), DateTimeFormatter.ofPattern("ddMMuu"))
}
