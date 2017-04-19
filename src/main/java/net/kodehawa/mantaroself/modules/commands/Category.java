package net.kodehawa.mantaroself.modules.commands;

public enum Category {
	ACTION("Action"),
	IMAGE("Image"),
	FUN("Fun"),
	OWNER("Owner"),
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
