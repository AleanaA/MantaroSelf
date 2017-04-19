package net.kodehawa.mantaroself.modules.commands;

public abstract class SimpleCommandCompat implements SimpleCommand {
	private final Category category;

	public SimpleCommandCompat(Category category) {
		this.category = category;
	}

	@Override
	public Category category() {
		return category;
	}

	@Override
	public boolean hidden() {
		return false;
	}
}
