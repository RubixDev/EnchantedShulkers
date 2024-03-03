package de.rubixdev.enchantedshulkers.config

import net.minecraft.text.Text

class InvalidOptionValueException(val text: Text) : Exception(text.string)
