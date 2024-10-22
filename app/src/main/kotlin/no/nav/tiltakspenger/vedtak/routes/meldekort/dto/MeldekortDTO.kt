package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.libs.periodisering.Periode
import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

data class MeldekortDTO(
    val id: String,
    val periode: PeriodeDTO,
    val meldekortDager: List<MeldekortDagDTO>,
    val tiltaksnavn: String,
    val saksbehandler: String?,
    val beslutter: String?,
    val status: MeldekortstatusDTO,
    val totalbeløpTilUtbetaling: Int?,
    val vedtaksPeriode: PeriodeDTO,
    // TODO post-mvp Kew: Må få på antall dager per uke når vi trenger det.
//    val antallDagerPerUke: Int,
)

fun Meldekort.toDTO(vedtaksPeriode: Periode): MeldekortDTO =
    MeldekortDTO(
        id = id.toString(),
        periode = periode.toDTO(),
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        tiltaksnavn = tiltaksnavn,
        status = status.toDTO(),
        meldekortDager = meldeperiode.toDTO(),
        totalbeløpTilUtbetaling = this.beløpTotal,
        vedtaksPeriode = vedtaksPeriode.toDTO(),
    )
