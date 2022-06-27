package no.nav.tiltakspenger.vedtak

import no.nav.tiltakspenger.Søknad


interface SøknadVisitor {
    fun visitSøknad(søknad: Søknad?) {}
}

interface SøkerVisitor : SøknadVisitor, AktivitetsloggVisitor {
    fun preVisitSøker(søker: Søker, ident: String) {}
    fun visitTilstand(tilstandType: Søker.Tilstand) {}

    //fun visitSøker() {}
    fun visitSøkerAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitSøker(søker: Søker, ident: String) {}
}
