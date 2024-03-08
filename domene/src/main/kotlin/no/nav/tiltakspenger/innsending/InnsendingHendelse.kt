package no.nav.tiltakspenger.innsending

interface IInnsendingHendelse :
    KontekstLogable,
    IAktivitetslogg {
    val aktivitetslogg: Aktivitetslogg
    fun journalpostId(): String
    fun toLogString(): String
}

abstract class InnsendingHendelse protected constructor(
    override val aktivitetslogg: Aktivitetslogg = Aktivitetslogg(),
) : IAktivitetslogg by aktivitetslogg, IInnsendingHendelse {

    init {
        aktivitetslogg.addKontekst(this)
    }

    override fun opprettKontekst(): Kontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            Kontekst(it, mapOf("journalpostId" to journalpostId()))
        }
    }

    override fun toLogString() = aktivitetslogg.toString()
}
