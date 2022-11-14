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

interface SøkerVisitor : SøknadVisitor, PersonopplysningerVisitor, IAktivitetsloggVisitor, SkjermingVisitor {
    fun preVisitSøker(innsending: Innsending, ident: String) {}
    fun visitTilstand(tilstandType: Innsending.Tilstand) {}

    //Skal såvidt jeg har skjønt ha dataene som er rett på Søker, men det er ingenting pt
    //fun visitSøker() {}
    fun visitSøkerAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitSøker(innsending: Innsending, ident: String) {}
}
