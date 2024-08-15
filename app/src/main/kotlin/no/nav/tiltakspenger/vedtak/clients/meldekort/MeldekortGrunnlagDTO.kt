package no.nav.tiltakspenger.vedtak.clients.meldekort

import java.time.LocalDate

data class MeldekortGrunnlagDTO(
    val vedtakId: String,
    val sakId: String,
    val behandlingId: String,
    val status: StatusDTO,
    val vurderingsperiode: PeriodeDTO,
    val tiltak: List<TiltakDTO>,
    val personopplysninger: PersonopplysningerDTO,
    val utfallsperioder: List<UtfallsperiodeDTO>,
)

data class UtfallsperiodeDTO(
    val fom: LocalDate,
    val tom: LocalDate,
    // val antallBarn: Int, TODO jah: Fjerner denne inntil vi har implementert barnetillegg
    val utfall: UtfallForPeriodeDTO,
)

enum class UtfallForPeriodeDTO {
    GIR_RETT_TILTAKSPENGER,
    GIR_IKKE_RETT_TILTAKSPENGER,
}

data class PersonopplysningerDTO(
    val fornavn: String,
    val etternavn: String,
    val ident: String,
)

enum class StatusDTO {
    AKTIV,
    IKKE_AKTIV,
}

data class TiltakDTO(
    val periodeDTO: PeriodeDTO,
    val typeKode: String,
    val antDagerIUken: Float,
)

data class PeriodeDTO(
    val fra: LocalDate,
    val til: LocalDate,
)
