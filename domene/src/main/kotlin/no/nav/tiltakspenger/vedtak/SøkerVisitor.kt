package no.nav.tiltakspenger.vedtak


interface SøknadVisitor {
    fun visitSøknad(søknad: Søknad?) {}
}

interface PersoninfoVisitor {
    fun visitPersoninfo(personinfo: Personinfo?) {}
}

interface SøkerVisitor : SøknadVisitor, PersoninfoVisitor, AktivitetsloggVisitor {
    fun preVisitSøker(søker: Søker, ident: String) {}
    fun visitTilstand(tilstandType: Søker.Tilstand) {}

    //Skal såvidt jeg har skjønt ha dataene som er rett på Søker, men det er ingenting pt
    //fun visitSøker() {}
    fun visitSøkerAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitSøker(søker: Søker, ident: String) {}
}
