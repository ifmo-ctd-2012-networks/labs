package sender;

import com.sun.istack.internal.Nullable;
import sender.message.Message;

public interface ReplyProtocol<RequestType extends Message<ReplyType>, ReplyType extends Message> {
    @Nullable ReplyType makeResponse(RequestType type);

}
