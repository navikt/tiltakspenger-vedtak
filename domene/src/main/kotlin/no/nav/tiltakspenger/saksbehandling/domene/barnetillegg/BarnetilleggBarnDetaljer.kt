package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import java.time.LocalDate

sealed interface BarnetilleggBarnDetaljer {
    val fornavn: String
    val mellomnavn: String?
    val etternavn: String
    val fødselsdato: LocalDate
    val saksbehandler: String
}

/*
TODO: Trenger vi egentlig å skille på dem på dette tidspunktet?
 */
data class BarnetilleggBarnPdl(
    override val fornavn: String,
    override val mellomnavn: String?,
    override val etternavn: String,
    override val fødselsdato: LocalDate,
    override val saksbehandler: String,
) : BarnetilleggBarnDetaljer

data class BarnetilleggBarnManuell(
    override val fornavn: String,
    override val mellomnavn: String?,
    override val etternavn: String,
    override val fødselsdato: LocalDate,
    override val saksbehandler: String,
) : BarnetilleggBarnDetaljer
