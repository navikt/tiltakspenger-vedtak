package no.nav.tiltakspenger.vedtak


interface PersonVisitor {
    fun visitPerson(
        navn: String,
        aktørId: String,
        ident: String,
        norskTilknytning: Boolean,
        diskresjonskode: Boolean
    ) {
    }
}

interface ArenaSakVisitor {
    fun visitArenaSak(oppgaveId: String, fagsakId: String?) {}
}

interface InnsendingVisitor :
    PersonVisitor,
    ArenaSakVisitor,
    AktivitetsloggVisitor {
    fun preVisitInnsending(innsending: Innsending, journalpostId: String) {}
    fun visitTilstand(tilstandType: Innsending.Tilstand) {}
    fun visitInnsending(oppfyllerMinsteArbeidsinntekt: Boolean?, eksisterendeSaker: Boolean?) {}
    fun visitInnsendingAktivitetslogg(aktivitetslogg: Aktivitetslogg) {}
    fun postVisitInnsending(innsending: Innsending, journalpostId: String) {}
}
