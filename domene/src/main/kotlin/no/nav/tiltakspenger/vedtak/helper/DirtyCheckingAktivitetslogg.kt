package no.nav.tiltakspenger.vedtak.helper

import no.nav.tiltakspenger.vedtak.Aktivitetslogg
import no.nav.tiltakspenger.vedtak.IAktivitetslogg
import java.util.concurrent.atomic.AtomicBoolean

class DirtyCheckingAktivitetslogg(
    private val wrappedAktivitetslogg: IAktivitetslogg,
    private val isDirty: AtomicBoolean
) : IAktivitetslogg by wrappedAktivitetslogg {

    override fun info(melding: String) {
        wrappedAktivitetslogg.info(melding)
        isDirty.set(true)
    }

    override fun warn(melding: String) {
        wrappedAktivitetslogg.info(melding)
        isDirty.set(true)
    }

    override fun behov(type: Aktivitetslogg.Aktivitet.Behov.Behovtype, melding: String, detaljer: Map<String, Any>) {
        wrappedAktivitetslogg.info(melding)
        isDirty.set(true)
    }

    override fun error(melding: String) {
        wrappedAktivitetslogg.info(melding)
        isDirty.set(true)
    }

    override fun severe(melding: String) {
        wrappedAktivitetslogg.info(melding)
        isDirty.set(true)
    }

    override fun add(aktivitet: Aktivitetslogg.Aktivitet) {
        wrappedAktivitetslogg.add(aktivitet)
        isDirty.set(true)
    }
}
