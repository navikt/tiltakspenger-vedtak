package no.nav.tiltakspenger.saksbehandling.domene.barnetillegg

import no.nav.tiltakspenger.saksbehandling.domene.saksopplysning.Kilde

data class JaNeiSaksopplysning(
    val kilde: Kilde,
    val detaljer: String,
    val saksbehandler: String,
    val verdi: JaNei,
) : Saksopplysning
