package no.nav.tiltakspenger.vedtak.clients.meldekort

import no.nav.tiltakspenger.meldekort.domene.Meldekort
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.MeldekortDagStatusMotFrontendDTO
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.MeldekortstatusDTO
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.toMeldekortstatusDTO
import no.nav.tiltakspenger.vedtak.routes.meldekort.dto.toStatusDTO
import java.time.LocalDate

data class MeldekortDagTilUtfyllingDTO(
    val dag: LocalDate,
    val status: MeldekortDagStatusMotFrontendDTO,
)

data class MeldekortTilUtfyllingDTO(
    val id: String,
    val fraOgMed: LocalDate,
    val tilOgMed: LocalDate,
    val status: MeldekortstatusDTO,
    val meldekortDager: List<MeldekortDagTilUtfyllingDTO>,
)

fun Meldekort.tilUtfyllingDTO(): MeldekortTilUtfyllingDTO {
    return MeldekortTilUtfyllingDTO(
        id = this.id.toString(),
        fraOgMed = this.fraOgMed,
        tilOgMed = this.tilOgMed,
        status = this.toMeldekortstatusDTO(),
        meldekortDager = this.meldeperiode.dager.map {
            MeldekortDagTilUtfyllingDTO(
                dag = it.dato,
                status = it.toStatusDTO()
            )
        }
    )
}
