package plugins

/** Base class for all rsmod skill/content plugins. */
abstract class Plugin {
    abstract fun register(ctx: PluginContext)
}
