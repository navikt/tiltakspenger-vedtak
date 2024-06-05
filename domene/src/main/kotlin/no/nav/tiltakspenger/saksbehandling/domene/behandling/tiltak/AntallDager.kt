package no.nav.tiltakspenger.saksbehandling.domene.behandling.tiltak

import no.nav.tiltakspenger.libs.periodisering.Periode
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

    fun leggTilAntallDagerFraSaksbehandler(tiltaksperiode: Periode, nyAntallDager: PeriodeMedVerdi<AntallDager>): AntallDagerSaksopplysninger {
        val eksisterendePerioderMergetSammen = nyAntallDager.periode.mergeInnIPerioder(antallDagerSaksopplysningerFraSBH.map { it.periode })
        val allePerioder = tiltaksperiode.kompletter(
            eksisterendePerioderMergetSammen,
        )

        val nyeOpplysningerFraSBH = allePerioder.map { periode ->
            if (periode == nyAntallDager.periode) {
                PeriodeMedVerdi(
                    periode = periode,
                    verdi = nyAntallDager.verdi,
                )
            } else {
                val eksisterendeSaksopplysningFraSBH = antallDagerSaksopplysningerFraSBH.find { it.periode.overlapperMed(periode) }
                val eksisterendeSaksopplysningFraRegister = antallDagerSaksopplysningerFraRegister.find { it.periode.overlapperMed(periode) }
                PeriodeMedVerdi(
                    periode = periode,
                    verdi = AntallDager(
                        antallDager = eksisterendeSaksopplysningFraSBH?.verdi?.antallDager ?: eksisterendeSaksopplysningFraRegister?.verdi!!.antallDager,
                        kilde = nyAntallDager.verdi.kilde,
                        saksbehandlerIdent = nyAntallDager.verdi.saksbehandlerIdent,
                    ),
                )
            }
        }
        return this.copy(
            antallDagerSaksopplysningerFraSBH = nyeOpplysningerFraSBH,
        )
    }

    fun tilbakestillAntallDagerFraSaksbehandler(): AntallDagerSaksopplysninger {
        return this.copy(
            antallDagerSaksopplysningerFraSBH = emptyList(),
        )
    }
}
