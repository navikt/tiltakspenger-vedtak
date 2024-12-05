package no.nav.tiltakspenger.meldekort.domene

import no.nav.tiltakspenger.saksbehandling.domene.sak.Sak

fun Sak.sisteGodkjenteMeldekort(): Meldekort? {
    return meldeperioder.godkjenteMeldekort.lastOrNull()
}
