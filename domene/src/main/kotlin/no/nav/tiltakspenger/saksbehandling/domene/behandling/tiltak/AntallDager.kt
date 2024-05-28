package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class AntallDager(
    val antallDager: Int,
    val kilde: Kilde,
)

data class AntallDagerSaksopplysninger(
    // todo: Vi trenger informasjon om hvilken saksbehandler som har endret antall dager.
    val antallDagerSaksopplysningerFraSBH: List<PeriodeMedVerdi<AntallDager>> = emptyList(),
    val antallDagerSaksopplysningerFraRegister: List<PeriodeMedVerdi<AntallDager>>,
    val avklartAntallDager: List<PeriodeMedVerdi<AntallDager>> = emptyList(),
) {
    companion object {
        fun initAntallDagerSaksopplysning(
            antallDager: List<PeriodeMedVerdi<AntallDager>>,
            avklarteAntallDager: List<PeriodeMedVerdi<AntallDager>>,
        ): AntallDagerSaksopplysninger {
            return AntallDagerSaksopplysninger(
                antallDagerSaksopplysningerFraSBH = antallDager.filter { it.verdi.kilde == Kilde.SAKSB },
                antallDagerSaksopplysningerFraRegister = antallDager.filter { it.verdi.kilde != Kilde.SAKSB },
                avklartAntallDager = avklarteAntallDager,
            )
        }
    }
    fun avklar(): AntallDagerSaksopplysninger {
        val avklart = antallDagerSaksopplysningerFraSBH.ifEmpty { antallDagerSaksopplysningerFraRegister }
        return this.copy(
            avklartAntallDager = avklart,
        )
    }
}
