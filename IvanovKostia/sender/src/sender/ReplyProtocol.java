package sender;

import com.sun.istack.internal.Nullable;

public interface ReplyProtocol<RequestType extends RequestMessage<ReplyType>, ReplyType extends ResponseMessage> {
    @Nullable ReplyType makeResponse(RequestType type);

}
