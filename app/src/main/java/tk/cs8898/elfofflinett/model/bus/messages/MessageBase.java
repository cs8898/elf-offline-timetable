package tk.cs8898.elfofflinett.model.bus.messages;

public abstract class MessageBase {
    private final Class origin;
    public MessageBase(Class origin){
        this.origin = origin;
    }

    public Class getOrigin() {
        return origin;
    }
}
