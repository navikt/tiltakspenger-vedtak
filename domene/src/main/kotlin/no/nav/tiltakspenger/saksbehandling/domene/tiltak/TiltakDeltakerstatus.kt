package no.nav.tiltakspenger.saksbehandling.domene.tiltak

/**
 * Skal ikke serialiseres direkte.
 * Hvis vi ønsker denne i domenet på tvers av appene våre kan denne flyttes til libs.
 * Doc: https://confluence.adeo.no/pages/viewpage.action?pageId=573710206
 */
enum class TiltakDeltakerstatus(val rettTilÅSøke: Boolean) {
    VenterPåOppstart(true),

    /**
     * Brukes både ved løpende inntak og kurs.
     */
    Deltar(true),

    /**
     * Brukes ved løpende inntak.
     * Brukeren har deltatt på tiltaket (minst en dag), og så sluttet.
     */
    HarSluttet(true),
    Avbrutt(true),
    Fullført(true),

    /**
     * Brukes både ved løpende inntak og kurs.
     * Når brukeren har vært vurdert for tiltaket, men aldri startet og skal ikke delta.
     */
    IkkeAktuell(false),
    Feilregistrert(false),
    PåbegyntRegistrering(false),
    SøktInn(false),
    Venteliste(false),
    Vurderes(false),
}
