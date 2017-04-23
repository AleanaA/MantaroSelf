package net.kodehawa.mantaroself.modules.commands;

import net.kodehawa.mantaroself.modules.commands.base.Category;
import net.kodehawa.mantaroself.modules.commands.builders.SimpleCommandBuilder;

public class Commands {
	@Deprecated
	public static SimpleCommandBuilder newSimple(Category category) {
		return new SimpleCommandBuilder(category);
	}
}
