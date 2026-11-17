package stepup.app222shred.corpapp.integration

/**
 * Точка доступа к [IntegrationRepository] для гейта (без Koin, чтобы пакет gate
 * оставался независимым). Значение выставляется в Application.onCreate до любого UI.
 */
object Integrations {
    @Volatile
    var repository: IntegrationRepository? = null

    /** JSON интеграций для тела запроса; пустой объект, если сборщик ещё не создан. */
    fun payloadJson(): String = repository?.payloadJson() ?: "{}"
}
