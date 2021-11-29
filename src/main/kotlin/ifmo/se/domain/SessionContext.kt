package ifmo.se.domain

import kotlin.coroutines.CoroutineContext

class SessionContext(
) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key
    var login:String? = null

    companion object Key:CoroutineContext.Key<SessionContext>
}