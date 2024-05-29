package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.libs.periodisering.PeriodeMedVerdi
import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class AntallDager(
    val antallDager: Int,
    val kilde: Kilde,
    val saksbehandlerIdent: String?,
) {
    init {
        if (kilde == Kilde.SAKSB) {
            require(saksbehandlerIdent != null) {
                "Må ha saksbehandler-ident når opplysningen kommer fra saksbehandler"
            }
        } else {
            require(saksbehandlerIdent == null) {
                "En opplysning som kommer fra et register kan ikke ha en saksbehandler-ident"
            }
        }
    }
}

data class AntallDagerSaksopplysninger(
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
