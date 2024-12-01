package pro.cloudnode.smp.bankaccounts.commands.result;

public abstract class CommandResult {
    public abstract void send();

    private static class DoNothing extends CommandResult {
        public void send() {
        }
    }

    public static final CommandResult DO_NOTHING = new DoNothing();
}
