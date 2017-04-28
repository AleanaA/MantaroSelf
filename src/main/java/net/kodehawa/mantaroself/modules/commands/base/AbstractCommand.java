package net.kodehawa.mantaroself.modules.commands.base;

/**
 * Abstract implementation of {@link Command}. This implementation delegates the {@link Category} to the constructor.
 */
public abstract class AbstractCommand implements AssistedCommand {
	private final Category category;

	/**
	 * Creates a {@link AbstractCommand} with a specific {@link Category}
	 *
	 * @param category the {@link Command}'s {@link Category} (<code>null</code> for hidden)
	 */
	public AbstractCommand(Category category) {
		this.category = category;
	}

	@Override
	public Category category() {
		return category;
	}
}
