package no.nav.tiltakspenger

import io.kotest.assertions.fail
import no.nav.tiltakspenger.libs.persistering.domene.SessionContext
import no.nav.tiltakspenger.libs.persistering.domene.SessionFactory
import no.nav.tiltakspenger.libs.persistering.domene.TransactionContext
import kotlin.concurrent.getOrSet

class TestSessionFactory : SessionFactory {
    companion object {
        // Gjør det enklere å verifisere i testene.
        val sessionContext =
            object : SessionContext {
                override fun isClosed() = false
            }
        val transactionContext =
            object : TransactionContext {
                override fun isClosed() = false
            }
    }

    override fun <T> withSessionContext(action: (SessionContext) -> T): T = SessionCounter().withCountSessions { action(sessionContext) }

    override fun <T> withSessionContext(
        sessionContext: SessionContext?,
        action: (SessionContext) -> T,
    ): T =
        SessionCounter().withCountSessions {
            action(sessionContext ?: TestSessionFactory.sessionContext)
        }

    override fun <T> withTransactionContext(action: (TransactionContext) -> T): T =
        SessionCounter().withCountSessions { action(transactionContext) }

    override fun <T> withTransactionContext(
        transactionContext: TransactionContext?,
        action: (TransactionContext) -> T,
    ): T =
        SessionCounter().withCountSessions {
            action(transactionContext ?: TestSessionFactory.transactionContext)
        }

    override fun <T> use(
        transactionContext: TransactionContext,
        action: (TransactionContext) -> T,
    ): T =
        SessionCounter().withCountSessions {
            action(transactionContext)
        }

    fun newSessionContext() = sessionContext

    fun newTransactionContext() = transactionContext

    // TODO jah: Denne er duplikat med den som ligger i database siden test-common ikke har en referanse til database-modulen.
    private class SessionCounter {
        private val activeSessionsPerThread: ThreadLocal<Int> = ThreadLocal()

        fun <T> withCountSessions(action: () -> T): T =
            activeSessionsPerThread.getOrSet { 0 }.inc().let {
                if (it > 1) {
                    fail("Database sessions were over the threshold while running test.")
                }
                activeSessionsPerThread.set(it)
                try {
                    action()
                } finally {
                    activeSessionsPerThread.set(activeSessionsPerThread.getOrSet { 1 }.dec())
                }
            }
    }
}
