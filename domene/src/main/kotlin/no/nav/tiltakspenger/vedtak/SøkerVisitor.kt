package no.nav.tiltakspenger.vedtak


interface SøknadVisitor {
    fun visitSøknad(søknad: Søknad?) {}
}

interface PersonopplysningerVisitor {
    fun visitPersonopplysninger(personopplysninger: Personopplysninger?) {}
}

interface SkjermingVisitor {
    fun visitSkjerming(skjerming: Skjerming?) {}
}

interface SøkerVisitor : SøknadVisitor, PersonopplysningerVisitor, AktivitetsloggVisitor, SkjermingVisitor {
    fun preVisitSøker(søker: Søker, ident: String) {}
    fun visitTilstand(tilstandType: Søker.Tilstand) {}

    //Skal såvidt jeg har skjønt ha dataene som er rett på Søker, men det er ingenting pt
    //fun visitSøker() {}
    fun visitSøkerAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitSøker(søker: Søker, ident: String) {}
}
