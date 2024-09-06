package no.nav.tiltakspenger.vedtak.routes.meldekort.dto

import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort.IkkeUtfyltMeldekort
import no.nav.tiltakspenger.meldekort.domene.Meldekort.UtfyltMeldekort
import no.nav.tiltakspenger.vedtak.routes.dto.PeriodeDTO
import no.nav.tiltakspenger.vedtak.routes.dto.toDTO

data class MeldekortDTO(
    val id: String,
    val periode: PeriodeDTO,
    val meldekortDager: List<MeldekortDagDTO>,
    val tiltakstype: TiltakstypeSomGirRettDTO,
    val saksbehandler: String?,
    val beslutter: String?,
    val status: MeldekortstatusDTO,
    // TODO post-mvp Kew: Må få på antall dager per uke når vi trenger det.
//    val antallDagerPerUke: Int,
)

fun Meldekort.toDTO(): MeldekortDTO =
    MeldekortDTO(
        id = id.toString(),
        periode = periode.toDTO(),
        saksbehandler = saksbehandler,
        beslutter = beslutter,
        tiltakstype = tiltakstype.toDTO(),
        status = if (this is UtfyltMeldekort && beslutter.isNullOrBlank()) {
            MeldekortstatusDTO.KLAR_TIL_BESLUTNING
        } else if (this is IkkeUtfyltMeldekort) {
            MeldekortstatusDTO.KLAR_TIL_UTFYLLING
        } else {
            MeldekortstatusDTO.GODKJENT
        },
        meldekortDager = meldekortperiode.toDTO(),
    )
