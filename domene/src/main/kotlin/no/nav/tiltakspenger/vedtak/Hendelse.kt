package no.nav.tiltakspenger.vedtak

abstract class Hendelse protected constructor(
    internal val aktivitetslogg: Aktivitetslogg = Aktivitetslogg()
) : IAktivitetslogg by aktivitetslogg, KontekstLogable {

    abstract fun journalpostId(): String

    init {
        aktivitetslogg.addKontekst(this)
    }

    override fun opprettKontekst(): Kontekst {
        return this.javaClass.canonicalName.split('.').last().let {
            Kontekst(it, mapOf("journalpostId" to journalpostId()))
        }
    }

    fun toLogString() = aktivitetslogg.toString()
}
