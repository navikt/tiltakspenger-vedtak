package no.nav.tiltakspenger.domene

import java.time.LocalDate

interface Vedtak {
    fun tilArenaVedtak()
}

class Innvilgelse : Vedtak {
    // kan vi legge vedtak på barn også her, eller må man ha et vedtak per barn pga jus
    override fun tilArenaVedtak() {
        TODO("Not yet implemented")
    }
}

class Avslag : Vedtak {
    override fun tilArenaVedtak() {
        // 
        TODO("Not yet implemented")
    }
}

class Stans(val tidligereInnvilgetVedtak: Vedtak) : Vedtak {
    override fun tilArenaVedtak() {
        TODO("Not yet implemented")
    }
}

class Avbrutt : Vedtak {
    override fun tilArenaVedtak() {
        TODO("Not yet implemented")
    }
}

data class ArenaVedtak(
    val rettighetsType: String = "Tiltakspenger (basisytelse f",
    val vedtakstype: String,
    val status: String,
    val aktivitetsfase: String = "Under gjennomføring av tiltak",
    val utfall: Boolean,
    val regdato: LocalDate,
    val saksbehandler: String,
    val beslutter: String,
    val dagsats: Int,
    val vedtaksdato: LocalDate,
    val gjelderFra: LocalDate,
    val gjelderTil: LocalDate,
    val opprinneligTilDato: LocalDate,
    val knyttetTilTilak: String,
    val valgtSats: String = "BYHOY",
    val antallDagerMedutbetaling: Int
)
