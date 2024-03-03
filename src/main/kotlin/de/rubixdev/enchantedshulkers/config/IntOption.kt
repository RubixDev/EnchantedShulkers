package de.rubixdev.enchantedshulkers.config

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IntOption(
    val min: Int,
    val max: Int,
    val suggestions: Array<String>,
)
