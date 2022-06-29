package no.nav.tiltakspenger.vedtak


interface SøknadVisitor {
    fun visitSøknad(søknad: Søknad?) {}
}

interface PersonVisitor {
    fun visitPerson(person: Person?) {}
}

interface SøkerVisitor : SøknadVisitor, PersonVisitor, AktivitetsloggVisitor {
    fun preVisitSøker(søker: Søker, ident: String) {}
    fun visitTilstand(tilstandType: Søker.Tilstand) {}

    //Skal såvidt jeg har skjønt ha dataene som er rett på Søker, men det er ingenting pt
    //fun visitSøker() {}
    fun visitSøkerAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitSøker(søker: Søker, ident: String) {}
}
