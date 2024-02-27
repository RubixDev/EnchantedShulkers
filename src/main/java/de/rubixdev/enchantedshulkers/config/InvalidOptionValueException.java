package de.rubixdev.enchantedshulkers.config;

import net.minecraft.text.Text;

public class InvalidOptionValueException extends Exception {
    private final Text text;

    public InvalidOptionValueException(Text message) {
        super(message.getString());
        this.text = message;
    }

    public Text getText() {
        return text;
    }
}
