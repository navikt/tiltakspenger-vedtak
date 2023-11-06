package no.nav.tiltakspenger.vedtak.exception

enum class MessageCode(val value: Int) {
    BEHANDLING_ID_MANGLER(1001),
    BEHANDLING_FINNES_IKKE(1002),
    BUSINESS_LOGIC_ERROR_X(1003)
}
