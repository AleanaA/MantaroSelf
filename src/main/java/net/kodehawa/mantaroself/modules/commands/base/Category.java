package net.kodehawa.mantaroself.modules.commands.base;

public enum Category {
	ACTION("Action"),
	IMAGE("Image"),
	FUN("Fun"),
	SELF("Self"),
	INFO("Info"),
	UTILS("Utility"),
	MISC("Misc");

	private final String s;

	Category(String s) {
		this.s = s;
	}

	@Override
	public String toString() {
		return s;
	}
}
