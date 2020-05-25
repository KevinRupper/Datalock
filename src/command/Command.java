package command;

public abstract class Command {

    public enum CommandType {
        encrypt,
        decrypt,
        rsa,
        share,
        help,
        unknown
    }

    public CommandType type;
}
